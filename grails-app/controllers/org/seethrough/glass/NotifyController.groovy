package org.seethrough.glass

import grails.converters.JSON

class NotifyController {

	def notifyService
	def messageHandlerService

	def index() {
		def messageJson = request.JSON
		
		render "OK" // put at start to avoid resending message
		
		def userId = messageJson.userToken
		def user = User.get(userId)
		
		if (user) {		
			notify(messageJson, user)
		} else {
			log.error "Unknown user $userId in JSON request: $messageJson"
		}
	}

	private void notify(user, messageJson) {
		def timelineItemId = messageJson.itemId

		def text = notifyService.getMessage(user.id, timelineItemId)

		log.error "Received reply: " + text

		messageHandlerService?.reply([user: user, text: text])
	}
}
