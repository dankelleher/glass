package org.seethrough.glass

import com.google.api.client.auth.oauth2.Credential

class NotifyService {
	
	def authorisationService

    def getMessage(userId, timelineId) {
		Log.error "Retrieving timeline item $timelineId for user $userId"
		
		Credential credential = authorisationService.getCredential(userId)
		return MirrorClient.getTimelineItem(credential, timelineId)
    }
}
