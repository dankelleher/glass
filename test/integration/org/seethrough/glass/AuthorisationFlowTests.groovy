package org.seethrough.glass

import static org.junit.Assert.*

import org.junit.Test

class AuthorisationFlowTests {

    @Test
    void testRedirectsToGoogleOAuthSite() {
		def authController = new AuthController()
		authController.index()

		assert authController.response.redirectedUrl.startsWith("https://accounts.google.com/o/oauth2/auth")
    }
}
