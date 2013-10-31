package org.seethrough.glass

import org.junit.Before;
import org.junit.Test;

import com.google.api.services.mirror.model.Subscription
import com.google.api.services.mirror.model.TimelineItem

import static org.junit.Assert.*

class AuthorisationServiceInsertContactUnitTests {

	def appName = "Dummy App Name"
	
	// we are not using grails @TestFor here to avoid running spring afterPropertiesSet, which searches for, and fails to find, grailsApplication.mergedConfig
	def service
	
	def dummyUser
	def storedContact
	
	def mockMirrorService
	
	@Before
    void setUp() {
		TimelineItem.metaClass.mixin TimelineCategory
		
		service = new AuthorisationService()
		
		// stub out link generator to create dummy link
        service.grailsLinkGenerator = [link : { params ->  "http://some.host.name/app" }]
		
		// stub out mirror service
		mockMirrorService = [
			APP_NAME : appName,
			insertContact : { user, appContact -> storedContact = appContact},
			insertSubscription : { user, link, type -> },
			listSubscriptions : { user -> [] },
			insertTimelineItem :  { user, card -> }
			] 
		service.storedMirrorService = mockMirrorService
		
		dummyUser = new User()	
    }

	@Test
    void testImageURLIsContactImage() {
		def expectedURL = "http://some.host.name/app/images/contact.png"
		
		service.grailsLinkGenerator.resource = { params ->
			assert params.dir == "images"
			assert params.file == "contact.png"
			return expectedURL
		}
		
		service.bootstrapNewUser(dummyUser)
		
		assert storedContact.imageUrls == [expectedURL]
	}
}
