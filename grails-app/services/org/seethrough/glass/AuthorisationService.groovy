package org.seethrough.glass

import java.io.IOException

import com.google.api.client.json.JsonFactory
import com.google.api.client.http.HttpTransport
import com.google.api.client.auth.oauth2.CredentialStore
import com.google.api.client.json.jackson.JacksonFactory
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.http.GenericUrl

import com.google.api.services.mirror.model.Contact
import com.google.api.services.mirror.model.MenuItem
import com.google.api.services.mirror.model.NotificationConfig
import com.google.api.services.mirror.model.Subscription
import com.google.api.services.mirror.model.TimelineItem
import com.sun.imageio.plugins.common.ImageUtil

import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.springframework.beans.factory.InitializingBean

import static org.seethrough.glass.MirrorClient.*

class AuthorisationService implements InitializingBean {
    public static final String GLASS_SCOPE = "https://www.googleapis.com/auth/glass.timeline https://www.googleapis.com/auth/glass.location https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email"

	def grailsApplication
	
    LinkGenerator grailsLinkGenerator
    JsonFactory jsonFactory
    HttpTransport httpTransport
    CredentialStore credentialStore
	
    String clientId
    String clientSecret

    public AuthorisationService() {
        jsonFactory = new JacksonFactory()
        httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        credentialStore = new GORMCredentialStore()
    }
	
	void afterPropertiesSet() {
		readConfig()
	}
	
	void readConfig() {
		def config = grailsApplication.mergedConfig.grails.plugin.glass

        clientId = config.oauth.clientid
        clientSecret = config.oauth.clientsecret
		
		MirrorClient.setConfig(config)
	}

    /**
     * Creates and returns a new {@link com.google.api.client.auth.oauth2.AuthorizationCodeFlow} for this app.
     */
    public AuthorizationCodeFlow newAuthorizationCodeFlow() throws IOException {
        return new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                jsonFactory,
                clientId, clientSecret,
                Collections.singleton(GLASS_SCOPE))
            .setAccessType("offline")
            .setCredentialStore(credentialStore)
            .build()
    }

    public Credential getCredential(String userId) throws IOException {
        if (userId == null) return null

        return newAuthorizationCodeFlow().loadCredential(userId)
    }

    public Credential getCredential(User user) {
        return getCredential(user.id)
    }
    
    public String buildGoogleOauthLink() {
        def link = grailsLinkGenerator.link(action: 'callback', absolute: true)

        AuthorizationCodeFlow flow = newAuthorizationCodeFlow()
        GenericUrl url = flow.newAuthorizationUrl().setRedirectUri(link)

        url.set("approval_prompt", "force")

        return url.build()
    }

    public User oauthUser(accessCode) {
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
	
	public void bootstrapNewUser(Credential credential, User user) throws IOException {
		// Create contact
		Contact appContact = new Contact()
		appContact.id = APP_NAME
		appContact.displayName = APP_NAME
		appContact.imageUrls = [IMAGE_URL]

		Contact insertedContact = insertContact(credential, appContact)
		
		// add a subscription callback link for replies or actions on timeline items
		def callbackLink = "http://seethrough.dyndns.org:8080/MicroProj/notify"//grailsLinkGenerator.link(controller: 'notify', absolute: true)
		Subscription subscription = insertSubscription(credential, callbackLink, user.id, "timeline")

		// Send welcome timeline item
		TimelineItem timelineItem = new TimelineItem()
		timelineItem.text = "Welcome to $APP_NAME"
		timelineItem.notification =  new NotificationConfig().setLevel("DEFAULT")
		timelineItem.setMenuItems([new MenuItem().setAction("REPLY")]);
		TimelineItem insertedItem = insertTimelineItem(credential, timelineItem)
	}
}
