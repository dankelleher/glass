package org.seethrough.glass

import java.io.IOException
import java.io.InputStream

import org.springframework.beans.factory.InitializingBean

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson.JacksonFactory
import com.google.api.client.util.ByteStreams
import com.google.api.services.mirror.Mirror
import com.google.api.services.mirror.Mirror.Locations
import com.google.api.services.mirror.model.Attachment
import com.google.api.services.mirror.model.Contact
import com.google.api.services.mirror.model.ContactsListResponse
import com.google.api.services.mirror.model.Location;
import com.google.api.services.mirror.model.Subscription
import com.google.api.services.mirror.model.SubscriptionsListResponse
import com.google.api.services.mirror.model.TimelineItem
import com.google.api.services.mirror.model.TimelineListResponse

class MirrorService implements InitializingBean {

	public static String APP_NAME = "Default Glass App Name"	// set from config
	
	private static String TEST_ROOT_URL
	private static String TEST_SERVICE_PATH 
	
	def authorisationService
	def grailsApplication
	
	void afterPropertiesSet() {
		def config = grailsApplication.mergedConfig.grails.plugin.glass

		setConfig(config)
	}

	private void setConfig(config) {
		APP_NAME = config.appname
		
		TEST_ROOT_URL = config.mirror?.rooturl
		TEST_SERVICE_PATH = config.mirror?.servicepath
	}

	private def execute(executable) {
		def result
		try {
			log.debug "Executing: $executable"
			result = executable.execute()
		} catch (GoogleJsonResponseException e) {
			e.printStackTrace()
			log.error e.details
		} catch (Exception e) {	// exception occurring with demo glass app... TODO remove for live
			e.printStackTrace()
			log.error e.message
			// swallow it so we don't roll-back the transaction
		}

		return result
	}
	
	private Mirror getMirror(User user) {
		Credential credential = authorisationService.getCredential(user.id)
		createTestBuilder(credential).build()
	 }

    private Mirror getMirror(Credential credential) {
       createTestBuilder(credential).build()
    }

	private Mirror.Builder createBuilder(Credential credential) {
		HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport()
		new Mirror.Builder(transport, new JacksonFactory(), credential)
			.setApplicationName(APP_NAME)
	}

	private Mirror.Builder createTestBuilder(Credential credential) {
		Mirror.Builder builder = createBuilder(credential)
		
		if (TEST_ROOT_URL) {
			builder.setRootUrl(TEST_ROOT_URL)
					.setServicePath(TEST_SERVICE_PATH)
		}
		
		return builder
	}

    Contact insertContact(User user, Contact contact) throws IOException {
        Mirror.Contacts contacts = getMirror(user).contacts()
		execute(contacts.insert(contact))
    }

    void deleteContact(User user, String contactId) throws IOException {
        Mirror.Contacts contacts = getMirror(user).contacts()
        execute(contacts.delete(contactId))
    }

    ContactsListResponse listContacts(User user) throws IOException {
        Mirror.Contacts contacts = getMirror(user).contacts()
        return execute(contacts.list())
    }

    Contact getContact(User user, String id) throws IOException {
        try {
            Mirror.Contacts contacts = getMirror(user).contacts()
            return execute(contacts.get(id))
        } catch (GoogleJsonResponseException e) {
            log.warn("Could not find contact with ID " + id)
            return null
        }
    }


    TimelineListResponse listItems(User user, long count)
    throws IOException {
        Mirror.Timeline timelineItems = getMirror(user).timeline()
        Mirror.Timeline.List list = timelineItems.list()
        list.setMaxResults(count)
        return execute(list)
    }


    /**
     * Subscribes to notifications on the user's timeline.
     */
    Subscription insertSubscription(User user, String callbackUrlStr, String collectionStr) throws IOException {
        log.info("Attempting to subscribe verify_token " + user.id + " with callback " + callbackUrlStr)

        // Rewrite "appspot.com" to "Appspot.com" as a workaround for
        // http://b/6909300.
        callbackUrlStr = callbackUrlStr.replace("appspot.com", "Appspot.com")

        Subscription subscription = new Subscription()
        subscription.with {
            collection = collectionStr
            callbackUrl = callbackUrlStr
            userToken = user.id
			verifyToken = "SomeRandomToken"	// TODO Create a random token and store with user
        }

        return execute(getMirror(user).subscriptions().insert(subscription))
    }

    /**
     * Subscribes to notifications on the user's timeline.
     */
    void deleteSubscription(User user, String id) throws IOException {
        execute(getMirror(user).subscriptions().delete(id))
    }

    List<Subscription> listSubscriptions(User user) throws IOException {
        Mirror.Subscriptions subscriptions = getMirror(user).subscriptions()
        return execute(subscriptions.list()).items
    }

    /**
     * Inserts a simple timeline item.
     *
     * @param user the user object
     * @param item the item to insert
     */
    TimelineItem insertTimelineItem(User user, TimelineItem item) throws IOException {
		log.debug "Inserting card " + item
        return execute(getMirror(user).timeline().insert(item))
    }

    /**
     * Inserts an item with an attachment provided as a byte array.
     *
     * @param user the user object
     * @param item the item to insert
     * @param attachmentContentType the MIME type of the attachment (or null if
     *        none)
     * @param attachmentData data for the attachment (or null if none)
     */
    void insertTimelineItem(User user, TimelineItem item,
                                          String attachmentContentType, byte[] attachmentData) throws IOException {
        Mirror.Timeline timeline = getMirror(user).timeline()
        execute(timeline.insert(item, new ByteArrayContent(attachmentContentType, attachmentData)))

    }

    /**
     * Inserts an item with an attachment provided as an input stream.
     *
     * @param user the user object
     * @param item the item to insert
     * @param attachmentContentType the MIME type of the attachment (or null if
     *        none)
     * @param attachmentInputStream input stream for the attachment (or null if
     *        none)
     */
    void insertTimelineItem(User user, TimelineItem item,
                                          String attachmentContentType, InputStream attachmentInputStream) throws IOException {
        insertTimelineItem(user, item, attachmentContentType,
                ByteStreams.toByteArray(attachmentInputStream))
    }

	TimelineItem getTimelineItem(User user, String timelineItemId) {
		Mirror mirror = getMirror(user)
        TimelineItem item = execute(mirror.timeline().get(timelineItemId))
		return item
	}
	
	String getTimelineText(User user, String timelineItemId) {
		TimelineItem item = getTimelineItem(user, timelineItemId)
		return item.text
	}

    InputStream getAttachmentInputStream(User user, String timelineItemId,
                                                       String attachmentId) throws IOException {
        Mirror mirror = getMirror(user)
        Mirror.Timeline.Attachments attachments = mirror.timeline().attachments()
        Attachment attachmentMetadata = attachments.get(timelineItemId, attachmentId).execute()
        HttpResponse resp =
            mirror.getRequestFactory()
                    .buildGetRequest(new GenericUrl(attachmentMetadata.getContentUrl())).execute()
        return resp.getContent()
    }

    String getAttachmentContentType(User user, String timelineItemId,
                                                  String attachmentId) throws IOException {
        Mirror.Timeline.Attachments attachments = getMirror(user).timeline().attachments()
        Attachment attachmentMetadata = attachments.get(timelineItemId, attachmentId).execute()
        return attachmentMetadata.getContentType()
    }

	List<String> getAttachmentIds(User user, String timelineItemId) {
		TimelineItem item = execute(getMirror(user).timeline().get(timelineItemId))
		return item.attachments*.id
	}
	
	Location getLocation(User user) {
		return execute(getMirror(user).locations().get("latest"))
	}
}
