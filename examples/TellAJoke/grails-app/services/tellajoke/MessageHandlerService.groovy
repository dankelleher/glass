package tellajoke

import com.google.api.services.mirror.model.TimelineItem

class MessageHandlerService {
	def jokeService
	def mirrorService

	// Responds to the "Hit Me" action from the welcome card, and sends a joke to the user's timeline
    def hitme(params) {
		def timelineItem = new TimelineItem()
		timelineItem.text = jokeService.getJoke()
		
		mirrorService.insertTimelineItem(params.user, timelineItem)
    }
}
