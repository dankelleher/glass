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
			notify(user, messageJson)
		} else {
			log.error "Unknown user $userId in JSON request: $messageJson"
		}
	}

	private void notify(user, messageJson) {
		def action = getAction(messageJson)
		
		def timelineItemId = messageJson.itemId

		def params = [user: user, request: messageJson]
		
		if (action == "reply") {
			params << [ text : notifyService.getMessage(user.id, timelineItemId)]
		}

		log.error "Received reply: " + params.text

		messageHandlerService?."$action"(params)
	}
	
	private getAction(messageJson) {
		// TODO in future allow multiple actions here rather than just taking the first one
		def type = messageJson.userActions[0]?.type?.toLowerCase()
		
		if (type == "custom") {
			return messageJson.userActions[0].payload.toLowerCase()
		} else return type
	}
}
