package org.seethrough.glass

import org.junit.Before;
import org.junit.Test;

import com.google.api.services.mirror.model.Subscription

import static org.junit.Assert.*

class AuthorisationServiceLocationUnitTests {

	// we are not using grails @TestFor here to avoid running spring afterPropertiesSet, which searches for, and fails to find, grailsApplication.mergedConfig
	def service = new AuthorisationService()
	def config = [oauth: []]
	def dummyUser = new User()

	@Before
	void setUp() {
		makeConfigStructure(service, ["grailsApplication", "mergedConfig", "grails", "plugin", "glass"], config)
	}

	// set up the config tree to mimic reading in the config file
	void makeConfigStructure(root, structure, target) {
		def level = structure.pop()

		if (structure.isEmpty()) {
			root[level] = target
		} else {
			makeConfigStructure(root, structure, [(level) : target])
		}
	}

	@Test
	void testLocationMissingReturnsScopeWithoutLocationURL() {
		service.readConfig()
		assert !service.glassScope.contains(AuthorisationService.LOCATION_SCOPE)
	}

	@Test
	void testLocationOnReturnsScopeIncludingLocationURL() {
		readConfigWithLocationValue(true)

		assert service.glassScope.contains(AuthorisationService.LOCATION_SCOPE)
	}

	@Test
	void testLocationOffReturnsScopeWithoutLocationURL() {
		readConfigWithLocationValue(false)

		assert !service.grailsApplication.mergedConfig.grails.plugin.glass.locationOn

		assert !service.glassScope.contains(AuthorisationService.LOCATION_SCOPE)
	}

	@Test
	void testAddLocationAndTimelineSubscriptionIfLocationOnIsTrue() {
		def expectedSubscriptionTypes = ["timeline", "locations"]

		addStubsForSubscriptions(expectedSubscriptionTypes)

		readConfigWithLocationValue(true)

		service.addSubscription(dummyUser)

		assert expectedSubscriptionTypes.isEmpty()
	}
	
	@Test
	void testDoNotAddLocationAndTimelineSubscriptionIfLocationOnIsFalse() {
		def expectedSubscriptionTypes = ["timeline"]

		addStubsForSubscriptions(expectedSubscriptionTypes)

		readConfigWithLocationValue(false)

		service.addSubscription(dummyUser)

		assert expectedSubscriptionTypes.isEmpty()
	}

	private readConfigWithLocationValue(locationOn) {
		config.locationOn = locationOn
		service.readConfig()
	}

	private addStubsForSubscriptions(List expectedSubscriptionTypes) {
		service.setMirrorServiceForTests([
			listSubscriptions : { user -> []},
			insertSubscription : { user, callbackLink, type ->
				assert user == dummyUser
				assert expectedSubscriptionTypes.remove(type)
			}
		])
		service.grailsLinkGenerator = [ link : { params -> ""}]
	}
}
