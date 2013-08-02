package org.seethrough.glass

import grails.converters.JSON

class NotifyController {

	def notifyService
	def messageHandlerService

	def index() {
		def messageJson = request.JSON
		
		render "OK" // put at start to avoid resending message
		response.flushBuffer()
		
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
		
		def timelineItemId = "" + messageJson.itemId

		def msgParams = [user: user, request: messageJson]
		
		if (action == "reply") {
			msgParams << [ text : notifyService.getMessage(user, timelineItemId)]
			
			log.error "Received reply: " + msgParams.text
		}

		messageHandlerService?."$action"(msgParams)
	}
	
	private getAction(messageJson) {
		// TODO in future allow multiple actions here rather than just taking the first one
		def type = messageJson.userActions[0]?.type?.toLowerCase()
		
		if (type == "custom") {
			def action = messageJson.userActions[0].payload.toLowerCase()
			action = action.replaceAll("\\s", "_")
			
			return action
		} else return type
	}
}
