package org.seethrough.glass



import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
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

	def retrievedUser
	def retrievedParams
	
	def storedUser	

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
	
	/*
	 * Not yet implemented
	@Test void testControllerPassesAllJSONRequestFieldsToTheMessageHandler() {
		sendJsonToController(REPLY_JSON)
		
		assert retrievedParams.request == REPLY_JSON
	}
	*/
}
