package org.seethrough.glass

class AuthController {
	def authorisationService

	def redirectParams

	def getRedirectParams() {
		if (!redirectParams) redirectParams = grailsApplication.mergedConfig.grails.plugin.glass.home

		return redirectParams
	}

	def index() {
		if (session.user) {
			redirectToHome()	// already logged in
		} else {
			def urlStr = authorisationService.buildGoogleOauthLink()
			redirect(uri: urlStr)
		}
	}

	def callback() {
		def user = authorisationService.oauthUser(params.code)
		session.user = user

		authorisationService.bootstrapNewUser(authorisationService.getCredential(user), user)

		redirectToHome()
	}

	def redirectToHome() {
		def params = getRedirectParams()

		redirect(params)
	}

	def logout() {
		session.removeAttribute('user')
	}
}
