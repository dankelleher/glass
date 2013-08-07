package org.seethrough.glass

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class NotifyService {

	static transactional = false

	def mirrorService

	def getMessage(user, timelineItemId) {
		log.debug "Retrieving timeline item $timelineItemId for user $user.id"

		return mirrorService.getTimelineText(user, timelineItemId)
	}
	
	def getAttachedImages(user, timelineItemId) {
		List<String> attachmentIds = mirrorService.getAttachmentIds(user, timelineItemId)
		
		return attachmentIds.collect {
			toImage(retrieveAttachment(user, timelineItemId, it))
		}
	}
	
	def getLocation(user) {
		return mirrorService.getLocation(user)
	}
	
	private InputStream retrieveAttachment(User user, String timelineItemId, String attachmentId) {
		return mirrorService.getAttachmentInputStream(user, timelineItemId, attachmentId)
	}
	
	private BufferedImage toImage(InputStream stream) {
		return ImageIO.read(stream)
	}
}
