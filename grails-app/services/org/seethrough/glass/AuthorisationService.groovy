package org.seethrough.glass

import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.springframework.beans.factory.InitializingBean

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.auth.oauth2.CredentialStore
import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson.JacksonFactory
import com.google.api.services.mirror.model.Contact
import com.google.api.services.mirror.model.MenuItem
import com.google.api.services.mirror.model.NotificationConfig
import com.google.api.services.mirror.model.Subscription
import com.google.api.services.mirror.model.TimelineItem

class AuthorisationService implements InitializingBean {
    public static final String GLASS_SCOPE = "https://www.googleapis.com/auth/glass.timeline https://www.googleapis.com/auth/glass.location https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email"

	def grailsApplication
	
	protected def storedMirrorService

    LinkGenerator grailsLinkGenerator
    JsonFactory jsonFactory
    HttpTransport httpTransport
    CredentialStore credentialStore

    String clientId
    String clientSecret

    AuthorisationService() {
        jsonFactory = new JacksonFactory()
        httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        credentialStore = new GORMCredentialStore()
    }
	
	
	// avoid circular reference: retrieve this only when needed
	def getMirrorService() {
		if (!storedMirrorService) {
			storedMirrorService = grailsApplication.mainContext.mirrorService
		}
		
		return storedMirrorService
	}

	void afterPropertiesSet() {
		readConfig()
	}

	void readConfig() {
		def config = grailsApplication.mergedConfig.grails.plugin.glass

        clientId = config.oauth.clientid
        clientSecret = config.oauth.clientsecret
	}

    /**
     * Creates and returns a new {@link com.google.api.client.auth.oauth2.AuthorizationCodeFlow} for this app.
     */
    AuthorizationCodeFlow newAuthorizationCodeFlow() throws IOException {
        return new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                jsonFactory,
                clientId, clientSecret,
                Collections.singleton(GLASS_SCOPE))
            .setAccessType("offline")
            .setCredentialStore(credentialStore)
            .build()
    }

    Credential getCredential(String userId) throws IOException {
        if (userId == null) return null

        return newAuthorizationCodeFlow().loadCredential(userId)
    }

    Credential getCredential(User user) {
        return getCredential(user.id)
    }

    String buildGoogleOauthLink() {
        def link = grailsLinkGenerator.link(action: 'callback', absolute: true)

        AuthorizationCodeFlow flow = newAuthorizationCodeFlow()
        GenericUrl url = flow.newAuthorizationUrl().setRedirectUri(link)

        url.set("approval_prompt", "force")

        return url.build()
    }

    User oauthUser(accessCode) {
        def link = grailsLinkGenerator.link(action: 'callback', absolute: true)
        AuthorizationCodeFlow flow = newAuthorizationCodeFlow()
        TokenResponse tokenResponse =
            flow.newTokenRequest(accessCode).setRedirectUri(link).execute()

        // Extract the Google User ID from the ID token in the auth response
        String userId = ((GoogleTokenResponse) tokenResponse).parseIdToken().getPayload().getUserId()

		// save the user (so far without a username/email address) so we have somewhere to store the credentials

		// I would like to just do this but it seems there is a bug
		// http://jira.grails.org/browse/GRAILS-8422
		// Should be fixed in Grails 2.2 RC3 but still there in 2.2.2 as far as I can see
		//User user = User.findOrSaveWhere(id: userId)

		// temp workaround until bug above is fixed
		User user = User.get(userId)

		if (!user) {
			user = new User()
			user.id = userId
			user = user.merge(failOnError:true)
		}

		// store the credentials (using the GORM credential store)
		flow.createAndStoreCredential(tokenResponse, userId)

		// retrieve the stored credentials and get the username (email address) for the user
		Credential credential = getCredential(userId)
		String username = UserInfoClient.getUsername(credential)

		// store the username in the user
        user.username = username
		return user.merge(failOnError:true)
    }

	void bootstrapNewUser(User user) throws IOException {
		// Create contact
		Contact appContact = new Contact()
		appContact.id = mirrorService.APP_NAME
		appContact.displayName = mirrorService.APP_NAME
		appContact.imageUrls = [mirrorService.IMAGE_URL]

		Contact insertedContact = mirrorService.insertContact(user, appContact)

		// add a subscription callback link for replies or actions on timeline items
		def callbackLink = grailsLinkGenerator.link(controller: 'notify', absolute: true)
		Subscription subscription = mirrorService.insertSubscription(user, callbackLink, user.id, "timeline")

		// Send welcome timeline item
		TimelineItem timelineItem = new TimelineItem()
		timelineItem.text = "Welcome to ${mirrorService.APP_NAME}"
		timelineItem.notification =  new NotificationConfig().setLevel("DEFAULT")
		timelineItem.setMenuItems([new MenuItem().setAction("REPLY")])
		TimelineItem insertedItem = mirrorService.insertTimelineItem(user, timelineItem)
	}
}
