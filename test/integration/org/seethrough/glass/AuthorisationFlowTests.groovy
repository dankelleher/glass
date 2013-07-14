package org.seethrough.glass

import static org.junit.Assert.*

import org.apache.commons.logging.LogFactory;
import org.junit.*

class AuthorisationFlowTests {
    @Before
    void setUp() {
        // Setup logic here
    }

    @After
    void tearDown() {
        // Tear down logic here
    }

    @Test
    void testRedirectsToGoogleOAuthSite() {
		def authController = new AuthController()
        authController.index()
		
		assert authController.response.redirectedUrl.startsWith("https://accounts.google.com/o/oauth2/auth")
    }
}
