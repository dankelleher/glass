package org.seethrough.glass

import com.google.api.client.auth.oauth2.Credential

class NotifyService {

	static transactional = false

	def mirrorService

	def getMessage(user, timelineId) {
		log.debug "Retrieving timeline item $timelineId for user $userId"

		return mirrorService.getTimelineText(user, timelineId)
	}
}
