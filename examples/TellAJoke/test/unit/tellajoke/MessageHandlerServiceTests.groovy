package tellajoke

import org.seethrough.glass.*

import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(MessageHandlerService)
class MessageHandlerServiceTests {

	static final String JOKE = "Wenn ist das Nunstück git und Slotermeyer? Ja! Beiherhund das Oder die Flipperwaldt gersput!"
	
	def dummyUser
	
	def mirrorServiceControl
	
	@Before
	void setUp() {
		mirrorServiceControl = mockFor(MirrorService)
		service.mirrorService = mirrorServiceControl.createMock()
		
		service.jokeService = [getJoke : { JOKE }]	// stub out joke service
		
		dummyUser = new User()
	}
	
	@Test
    void testReplyTakesResultFromJokeServiceAndPutsItIntoACard() {
		mirrorServiceControl.demand.insertTimelineItem { user, item ->
			assert user == dummyUser
			assert item.text == JOKE
		}
		
		service.reply([user: dummyUser])
		
		mirrorServiceControl.verify()
    }
}
