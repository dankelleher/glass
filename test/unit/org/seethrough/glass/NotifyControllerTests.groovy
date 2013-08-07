package org.seethrough.glass

import grails.test.mixin.*
import org.junit.*
import java.awt.image.BufferedImage

@TestFor(NotifyController)
@Mock([User])
class NotifyControllerTests {

	static final String REPLY_JSON = """\
{
  "collection": "timeline",
  "itemId": "3hidvm0xez6r8_dacdb3103b8b604_h8rpllg",
  "operation": "INSERT",
  "userToken": "harold_penguin",
  "verifyToken": "random_hash_to_verify_referer",
  "userActions": [
    {
      "type": "REPLY"
    }
  ]
}
"""

	static final String CUSTOM_ACTION_JSON = """\
{
  "collection": "timeline",
  "itemId": "3hidvm0xez6r8_dacdb3103b8b604_h8rpllg",
  "operation": "UPDATE",
  "userToken": "harold_penguin",
  "userActions": [
    {
      "type": "CUSTOM",
      "payload": "MY_CUSTOM_ACTION"
    }
  ]
}
"""
	static final String SHARE_JSON = """\
{
  "collection": "timeline",
  "itemId": "3hidvm0xez6r8_dacdb3103b8b604_h8rpllg",
  "operation": "INSERT",
  "userToken": "harold_penguin",
  "verifyToken": "random_hash_to_verify_referer",
  "userActions": [
    {
      "type": "SHARE"
    }
  ]
}
"""

	static final String LOCATION_JSON = """\
{
  "collection": "locations",
  "itemId": "latest",
  "operation": "UPDATE",
  "userToken": "harold_penguin",
  "verifyToken": "random_hash_to_verify_referer"
}
"""

	def retrievedUser
	def retrievedParams

	def storedUser

	def dummyLocation = [latitude: 0, longitude: 0]

	@Before
	void setUp() {
		storedUser = new User()
		storedUser.id = "harold_penguin"
		storedUser.save()

		controller.messageHandlerService = [ reply : { params -> retrievedUser = params.user; retrievedParams = params}]
		controller.notifyService = [ getMessage: { userId, timelineId -> "Sample message" }]
	}

	@Test
	void testRetrieveUserFromRequestJSON() {
		sendJsonToController(REPLY_JSON)

		assert retrievedUser == storedUser
	}

	@Test
	void testIgnoreMessageWhenUserInRequestJSONNotFound() {
		def jsonWithUnknownUser = REPLY_JSON.replaceAll "harold_penguin", "jimmy_tortoise"

		sendJsonToController(jsonWithUnknownUser)

		assert retrievedUser == null
	}

	private sendJsonToController(String json) {
		controller.request.contentType = "text/json"
		controller.request.content = json
		controller.index()
	}

	@Test void testControllerPassesAllJSONRequestFieldsToTheMessageHandler() {
		sendJsonToController(REPLY_JSON)

		["collection", "itemId", "operation", "userToken", "verifyToken"].each {
			assert it in retrievedParams.request
		}

		assert retrievedParams.request.userActions.type == ["REPLY"] // list here because userActions is a list
	}

	@Test void testCustomActionTriggersAssociatedActionInMessageHandlerService() {
		def called = false

		controller.messageHandlerService.my_custom_action = { params -> called = true}

		sendJsonToController(CUSTOM_ACTION_JSON)

		assert called
	}

	@Test void testActionsWithSpacesAreRoutedToMethodsWithUnderscores() {
		def customActionJsonWithSpaces = CUSTOM_ACTION_JSON.replaceAll "MY_CUSTOM_ACTION", "MY CUSTOM ACTION"

		def called = false

		controller.messageHandlerService.my_custom_action = { params -> called = true}

		sendJsonToController(customActionJsonWithSpaces)

		assert called

	}

	@Test void testControllerAddsAttachmentImagesToMessageHandlerParametersIfTheyExist() {
		controller.messageHandlerService.share = { params -> retrievedParams = params }
		controller.notifyService.getAttachedImages = { user, timelineItemId ->
			assert timelineItemId == "3hidvm0xez6r8_dacdb3103b8b604_h8rpllg"
			return [new BufferedImage(1, 1, 1)]
		}

		sendJsonToController(SHARE_JSON)

		assert retrievedParams.attachments.size() == 1
	}

	@Test void testLocationNotificationsTriggerLocationRetrievalFromNotifyServer() {
		def called = false

		controller.notifyService.getLocation = { user ->
			called = true
			return dummyLocation
		}
		controller.messageHandlerService.location = {}

		sendJsonToController(LOCATION_JSON)

		assert called
	}

	@Test void testLocationNotificationsAreRoutedToLocationMethod() {
		controller.notifyService.getLocation = { user -> [latitude : 50, longitude : 100] }
		controller.messageHandlerService.location = { params -> retrievedParams = params }

		sendJsonToController(LOCATION_JSON)

		assert retrievedParams.user == storedUser
		assert retrievedParams.latitude == 50
		assert retrievedParams.longitude == 100
	}
}
