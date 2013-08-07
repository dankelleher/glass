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
		def msgParams = [user: user, request: messageJson]

		def action
		
		if (isTimelineNotification(messageJson)) {
			action = prepareTimelineNotification(messageJson, msgParams, user)
		} else if (isLocationNotification(messageJson)) {
			action = prepareLocationNotification(msgParams, user)
		}

		messageHandlerService?."$action"(msgParams)
	}
	
	private boolean isTimelineNotification(json) {
		return json.collection == "timeline"
	}
	
	private boolean isLocationNotification(json) {
		return json.collection == "locations"
	}
	
	private String prepareLocationNotification(msgParams, user) {
		def location = notifyService.getLocation(user)
		msgParams.latitude = location.latitude
		msgParams.longitude = location.longitude
		return "location"
	}

	private String prepareTimelineNotification(messageJson, msgParams, user) {
		def action = getAction(messageJson)
		def timelineItemId = "" + messageJson.itemId
		
		if (action == "reply") {
			msgParams.text == notifyService.getMessage(user, timelineItemId)

			log.info "Received reply: " + msgParams.text
		}

		if (action == "share") {
			msgParams.attachments = notifyService.getAttachedImages(user, timelineItemId)
		}
		return action
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
