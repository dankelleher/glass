/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License") you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seethrough.glass

import org.apache.commons.logging.LogFactory

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson.JacksonFactory
import com.google.api.services.mirror.Mirror
import com.google.api.services.mirror.model.Attachment
import com.google.api.services.mirror.model.Contact
import com.google.api.services.mirror.model.ContactsListResponse
import com.google.api.services.mirror.model.Subscription
import com.google.api.services.mirror.model.SubscriptionsListResponse
import com.google.api.services.mirror.model.TimelineItem
import com.google.api.services.mirror.model.TimelineListResponse
import com.google.common.io.ByteStreams

/**
 * A facade for easier access to basic API operations
 *
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 *
 * Groovified by Daniel Kelleher
 */
class MirrorClient {
    private static final LOG = LogFactory.getLog(this)

	public static String APP_NAME = "Default Glass App Name"	// set from config
	public static String IMAGE_URL = ""

	static setConfig(config) {
		APP_NAME = config.appname
		IMAGE_URL = config.imageurl
	}

	static execute(executable) {
		def result
		try {
			result = executable.execute()
		} catch (GoogleJsonResponseException e) {
			LOG.error e.details
		} catch (Exception e) {	// exception occurring with demo glass app... TODO remove for live
			LOG.error e.message
			// swallow it so we don't roll-back the transaction
		}

		return result
	}

    static Mirror getMirror(Credential credential) {
       createTestBuilder(credential).build()
    }

	static Mirror.Builder createBuilder(Credential credential) {
		HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport()
		new Mirror.Builder(transport, new JacksonFactory(), credential)
			.setApplicationName(APP_NAME)
	}

	static Mirror.Builder createTestBuilder(Credential credential) {
		createBuilder(credential)
			.setRootUrl("https://seethroughtest.appspot.com/")
			.setServicePath("_ah/api/mirror/v1/")
	}

    static Contact insertContact(Credential credential, Contact contact) throws IOException {
        Mirror.Contacts contacts = getMirror(credential).contacts()
		execute(contacts.insert(contact))
    }

    static void deleteContact(Credential credential, String contactId) throws IOException {
        Mirror.Contacts contacts = getMirror(credential).contacts()
        execute(contacts.delete(contactId))
    }

    static ContactsListResponse listContacts(Credential credential) throws IOException {
        Mirror.Contacts contacts = getMirror(credential).contacts()
        return execute(contacts.list())
    }

    static Contact getContact(Credential credential, String id) throws IOException {
        try {
            Mirror.Contacts contacts = getMirror(credential).contacts()
            return execute(contacts.get(id))
        } catch (GoogleJsonResponseException e) {
            LOG.warn("Could not find contact with ID " + id)
            return null
        }
    }


    static TimelineListResponse listItems(Credential credential, long count)
    throws IOException {
        Mirror.Timeline timelineItems = getMirror(credential).timeline()
        Mirror.Timeline.List list = timelineItems.list()
        list.setMaxResults(count)
        return execute(list)
    }


    /**
     * Subscribes to notifications on the user's timeline.
     */
    static Subscription insertSubscription(Credential credential, String callbackUrlStr,
                                                  String userId, String collectionStr) throws IOException {
        LOG.info("Attempting to subscribe verify_token " + userId + " with callback " + callbackUrlStr)

        // Rewrite "appspot.com" to "Appspot.com" as a workaround for
        // http://b/6909300.
        callbackUrlStr = callbackUrlStr.replace("appspot.com", "Appspot.com")

        Subscription subscription = new Subscription()
        subscription.with {
            // Alternatively, subscribe to "locations"
            collection = collectionStr
            callbackUrl = callbackUrlStr
            userToken = userId
			verifyToken = "SomeRandomToken"	// TODO Create a random token and store with user
        }

        return execute(getMirror(credential).subscriptions().insert(subscription))
    }

    /**
     * Subscribes to notifications on the user's timeline.
     */
    static void deleteSubscription(Credential credential, String id) throws IOException {
        execute(getMirror(credential).subscriptions().delete(id))
    }

    static SubscriptionsListResponse listSubscriptions(Credential credential)
    throws IOException {
        Mirror.Subscriptions subscriptions = getMirror(credential).subscriptions()
        return execute(subscriptions.list())
    }

    /**
     * Inserts a simple timeline item.
     *
     * @param credential the user's credential
     * @param item the item to insert
     */
    static TimelineItem insertTimelineItem(Credential credential, TimelineItem item)
    throws IOException {
        return execute(getMirror(credential).timeline().insert(item))
    }

    /**
     * Inserts an item with an attachment provided as a byte array.
     *
     * @param credential the user's credential
     * @param item the item to insert
     * @param attachmentContentType the MIME type of the attachment (or null if
     *        none)
     * @param attachmentData data for the attachment (or null if none)
     */
    static void insertTimelineItem(Credential credential, TimelineItem item,
                                          String attachmentContentType, byte[] attachmentData) throws IOException {
        Mirror.Timeline timeline = getMirror(credential).timeline()
        execute(timeline.insert(item, new ByteArrayContent(attachmentContentType, attachmentData)))

    }

    /**
     * Inserts an item with an attachment provided as an input stream.
     *
     * @param credential the user's credential
     * @param item the item to insert
     * @param attachmentContentType the MIME type of the attachment (or null if
     *        none)
     * @param attachmentInputStream input stream for the attachment (or null if
     *        none)
     */
    static void insertTimelineItem(Credential credential, TimelineItem item,
                                          String attachmentContentType, InputStream attachmentInputStream) throws IOException {
        insertTimelineItem(credential, item, attachmentContentType,
                ByteStreams.toByteArray(attachmentInputStream))
    }

	static String getTimelineItem(Credential credential, String timelineItemId) {
		Mirror mirrorService = getMirror(credential)
        TimelineItem item = mirrorService.timeline().get(timelineItemId)
		return item.text
	}

    static InputStream getAttachmentInputStream(Credential credential, String timelineItemId,
                                                       String attachmentId) throws IOException {
        Mirror mirrorService = getMirror(credential)
        Mirror.Timeline.Attachments attachments = mirrorService.timeline().attachments()
        Attachment attachmentMetadata = attachments.get(timelineItemId, attachmentId).execute()
        HttpResponse resp =
            mirrorService.getRequestFactory()
                    .buildGetRequest(new GenericUrl(attachmentMetadata.getContentUrl())).execute()
        return resp.getContent()
    }

    static String getAttachmentContentType(Credential credential, String timelineItemId,
                                                  String attachmentId) throws IOException {
        Mirror.Timeline.Attachments attachments = getMirror(credential).timeline().attachments()
        Attachment attachmentMetadata = attachments.get(timelineItemId, attachmentId).execute()
        return attachmentMetadata.getContentType()
    }
}
