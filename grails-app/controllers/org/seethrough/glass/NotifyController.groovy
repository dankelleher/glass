package org.seethrough.glass

import grails.converters.JSON

class NotifyController {

	def notifyService
	def messageHandlerService
	
    def index() {
		def message = request.JSON
		def userId = message.userToken
		def timelineItemId = message.itemId
		
		def text = notifyService.getMessage(userId, timelineItemId)
		
		Log.error "Received reply: " + text

		messageHandlerService?.reply(text)
				
		render "OK" // TODO put at start to avoid resending message
	}
}
