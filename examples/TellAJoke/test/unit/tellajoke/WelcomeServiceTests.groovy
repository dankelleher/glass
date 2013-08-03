package tellajoke

import grails.test.mixin.*
import org.junit.*

import org.seethrough.glass.TimelineCategory
import com.google.api.services.mirror.model.TimelineItem

@TestFor(WelcomeService)
class WelcomeServiceTests {

	@Before
	void setUp() {
		TimelineItem.metaClass.mixin TimelineCategory
		TimelineCategory.LINK_GENERATOR = [ resource : {}]
	}	
	
	@Test
    void testCustomCard() {
        def card = service.makeWelcomeCard()
		
		assert card.text == "Tell A Joke"
		assert card.menuItems[0].id == "hitme"
    }
}
