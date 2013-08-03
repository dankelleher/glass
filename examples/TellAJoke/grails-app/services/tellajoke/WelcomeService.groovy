package tellajoke

import com.google.api.services.mirror.model.TimelineItem
import com.google.api.services.mirror.model.NotificationConfig

class WelcomeService {

	// Create a custom welcome card for new users, with a single action "Hit me" which triggers a joke to be retrieved
	TimelineItem makeWelcomeCard() {
		TimelineItem welcomeCard = new TimelineItem()
		welcomeCard.text = "Tell A Joke"
		welcomeCard.notification =  new NotificationConfig().setLevel("DEFAULT")
		welcomeCard.addCustomAction("Hit me")
		
		return welcomeCard
	}
}
