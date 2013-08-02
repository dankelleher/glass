package org.seethrough.glass

import com.google.api.services.mirror.model.Subscription

import static org.junit.Assert.*

class AuthorisationServiceUserSubscriptionUnitTests {

	// we are not using grails @TestFor here to avoid running spring afterPropertiesSet, which searches for, and fails to find, grailsApplication.mergedConfig
	def service
	
	static final String EXPECTED_CALLBACK = "http://some.host.name/app"
	
	def user
	def subscriptions = []
	
    void setUp() {
		service = new AuthorisationService()
        service.setMirrorServiceForTests([listSubscriptions : { user -> subscriptions}])
		service.grailsLinkGenerator = [link : { params -> EXPECTED_CALLBACK }]
		
		user = new User()	
    }

    void testIsUserSubscribedReturnsFalseIfUserHasNoSubscriptions() {
        assert ! service.isUserSubscribed(user)
    }
	
	void testIsUserSubscribedReturnsFalseIfSubscriptionsDoNotMatchExpectedCallback() {
		subscriptions = [
			new Subscription(callbackUrl : "http://incorrect.host.name/app"),
			new Subscription(callbackUrl : "http://some.other.incorrect.host.name/app")]
		
		assert ! service.isUserSubscribed(user)
	}
	
	void testIsUserSubscribedReturnsTrueIfASubscriptionMatchesExpectedCallback() {
		subscriptions = [
			new Subscription(callbackUrl : "http://incorrect.host.name/app"),
			new Subscription(callbackUrl : EXPECTED_CALLBACK)]
		
		assert service.isUserSubscribed(user)
	}
}
