package org.seethrough.glass

import com.google.api.client.auth.oauth2.Credential

class NotifyService {

	static transactional = false

	def authorisationService
	def mirrorService

	def getMessage(userId, timelineId) {
		log.error "Retrieving timeline item $timelineId for user $userId"

		Credential credential = authorisationService.getCredential(userId)
		return mirrorService.getTimelineItem(credential, timelineId)
	}
}
