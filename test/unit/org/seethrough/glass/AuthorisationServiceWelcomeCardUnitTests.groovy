package org.seethrough.glass

import org.junit.Before;
import org.junit.Test;

import com.google.api.services.mirror.model.Subscription
import com.google.api.services.mirror.model.TimelineItem

import static org.junit.Assert.*

class AuthorisationServiceWelcomeCardUnitTests {

	def appName = "Dummy App Name"
	def customWelcomeText = "Custom Welcome Card"
	
	// we are not using grails @TestFor here to avoid running spring afterPropertiesSet, which searches for, and fails to find, grailsApplication.mergedConfig
	def service
	
	def dummyUser
	
	def mockMirrorService
	
	// example welcome service that generates a custom welcome card
	class CustomWelcomeService {
		TimelineItem makeWelcomeCard() {
			TimelineItem card = new TimelineItem()
			card.text = customWelcomeText
			return card
		}
	}
	
	// this example welcome service does not respond to the makeWelcomeCard method, and is therefore not used
	class EmptyWelcomeService {}
	
	// this example welcome service responds to the method but has the wrong return type.
	class InvalidWelcomeService {
		def makeWelcomeCard() {
			return 1
		}
	}
	
	@Before
    void setUp() {
		TimelineItem.metaClass.mixin TimelineCategory
		
		service = new AuthorisationService()
		
		// stub out link generator to create dummy link
        service.grailsLinkGenerator = [link : { params ->  "http://some.host.name/app" }]
		
		// stub out mirror service
		mockMirrorService = [
			APP_NAME : appName,
			insertContact : { user, appContact -> },
			insertSubscription : { user, link, type -> },
			listSubscriptions : { user -> [] }
			] 
		service.storedMirrorService = mockMirrorService
		
		dummyUser = new User()	
    }

	@Test
    void testDefaultWelcomeCardSentWhenNoServicePresent() {
		expectDefaultWelcomeCard()
		
		service.bootstrapNewUser(dummyUser)
	}

	private expectWelcomeCard(expectedCardText) {
		mockMirrorService << [insertTimelineItem : { passedInUser, card ->
				assert passedInUser == dummyUser
				assert card.text == expectedCardText
			}]
	}
	
	@Test
	void testDefaultWelcomeCardSentWhenServicePresentButNotMethod() {
		service.welcomeService = new EmptyWelcomeService()
		expectDefaultWelcomeCard()
		
		service.bootstrapNewUser(dummyUser)
	}
	
	@Test
	void testThrowExceptionWhenWelcomeCardMethodHasWrongReturnType() {
		service.welcomeService = new InvalidWelcomeService()
		
		try {
			service.bootstrapNewUser(dummyUser)
			fail("Expected glass plugin runtime exception ")
		} catch (GlassPluginRuntimeException ex) {
			// expected
		}
	}

	private expectDefaultWelcomeCard() {
		expectWelcomeCard("Welcome to $appName")
	}
	
	@Test
	void testCustomWelcomeCardSentWhenServiceAndMethodPresent() {
		service.welcomeService = new CustomWelcomeService()
		expectWelcomeCard(customWelcomeText)
				
		service.bootstrapNewUser(dummyUser)
	}
}
