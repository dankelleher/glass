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
	def storedUser	

	@Before
	void setUp() {
		storedUser = new User()
		storedUser.id = "harold_penguin"
		storedUser.save()
		
		controller.messageHandlerService = [ reply : { params -> retrievedUser = params.user}]
		controller.notifyService = [ getMessage: { userId, timelineId -> "Sample message" }]
	}
	
	@Test
    void testRetrieveUserFromRequestJSON() {
       controller.request.contentType = "text/json"
	   controller.request.content = REPLY_JSON
	   controller.index()
	   
	   assert retrievedUser == storedUser 
    }
	
	@Test
	void testIgnoreMessageWhenUserInRequestJSONNotFound() {
		def jsonWithUnknownUser = REPLY_JSON.replaceAll "harold_penguin", "jimmy_tortoise"
		
		controller.request.contentType = "text/json"
		controller.request.content = jsonWithUnknownUser
		controller.index()
		
		assert retrievedUser == null
	}
}
