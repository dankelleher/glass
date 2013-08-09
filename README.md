Grails Glass Plugin
=========

The Grails Glass Plugin allows you to quickly develop Grails apps for the upcoming Google Glass platform, via the [Mirror API](https://developers.google.com/glass/). 

The plugin takes care of the following common tasks:

  - OAuth2 user authentication with Google
  - Posting and adding contacts to a user's Glass timeline
  - Subscribing to notifications from Glass, triggered by user actions.

## Getting Started

Register your application with Google as described [here](https://developers.google.com/glass/quickstart/java).

Install the plugin and add the following config to your project's Config.groovy:

    grails {
        plugin {
		    glass {
			    username = <username of your app's google account (for email notifications - optional)>
			    password = <password of your app's google account (for email notifications - optional)>
			    appname = <name of your app>
			    imageurl = <URL to an image representing your app>
			    locationOn = <set to true if you wish to subscribe to location notifications - false by default. See below>
			    home {
    				controller = <controller and...>
    				action = <action to direct to after user has been authorised by Google OAuth2 (optional. Will default to a simple "connected" message>
			    }
			    oauth {
    				clientid = <client ID provided by Google>
    				clientsecret = <client secret provided by Google>
			    }
		    }
	    }
    }
    
In addition, add the following line to UrlMappings.groovy (replacing any existing mapping for '/')

    "/"(controller:"auth")
    
You now have a working Glass application. Start up the grails project using run-app, and open a browser to the project root. You will be redirected to Google to login and provide authorisation for the app to gain access to your Glass timeline. Once you have provided authorisation, you will be redirected to whichever controller and action is set in the config.

## Examples

An example application, TellAJoke, can be found in the [examples](examples/) directory. This is a very simple application, that posts a random Chuck Norris joke of questionable comic value to the user's timeline on the user's request. It demonstrates the basics of posting to the timeline and responding to notifications. For more information see the [readme](examples/TellAJoke/README.md).

## Plugin Details

The plugin adds the following objects to the Grails project:

  - `AuthController`: By default, the URLMappings for the plugin map the root URL '/' to this controller, which will check if there is a user in the session. If not it will redirect to the Google login screen
  - `User` domain object: Stores the Google username (email) of a user against the OAuth2 access credentials provided by Google.
  - `AuthorisationService`: Wrapper for Google's OAuth2 libraries.
  - `NotifyController`: Listens to subscriptions on the Glass timeline. Passes notifications on to a service named `MessageHandlerService`, if one exists.
  - `MirrorService`: Wraps all client-server interactions with the Mirror API itself. Handles adding and removing TimelineItems, Contacts, Subscriptions and Locations

## Posting to the Timeline

The plugin uses the [Google Mirror API](https://developers.google.com/glass/about) to post to the timeline. Example code would be:

    import com.google.api.services.mirror.model.TimelineItem

    import org.seethrough.glass.User

    class SomeServiceOrController
    
        def mirrorService
    
        void publish(User user, String text) {
            TimelineItem timelineItem = new TimelineItem()
            timelineItem.text = text

            mirrorService.insertTimelineItem(user, timelineItem)
        }
    }

*Note, this plugin, and the Mirror API are both works-in-progress and are subject to change.*

## Adding actions to a timeline item

Actions, or [Menu Items](https://developers.google.com/glass/v1/reference/timeline#menuItems) can be added to a timeline item using the following method (added to the TimelineItem object by the plugin).

    timelineItem.addAction(ACTION_NAME)
 
The action name is an instance of the `TimelineAction` enum.

To add custom actions (i.e. actions not built-in to Glass and not included in TimelineAction), use:

    timelineItem.addCustomAction(ACTION_NAME)
   
where ACTION_NAME is any string.

The icon used for the custom action should be stored under `static/images/actions/<converted action name>.png` where `converted action name` is the action name in lower case with spaces removed (e.g "Say Hello" becomes "sayhello").

## Subscribing to notifications

The plugin automatically adds a subscription to the user's timeline when they log in. When the user performs an action on a timeline card, the plugin will be notified. To perform actions based on these notifictions, create a service called MessageHandlerService. Each method inside this service should correspond to an action on a timeline card. E.g. a method called `share` will be called on receipt of Share actions.

See, for example the following code:

    class MessageHandlerService {

        def reply(params) {
        	// handles the REPLY built-in action. 
        }
	
	def order(params) {
		// handles the ORDER custom item.
	}
    }
    
The `params` passed to the hander method include the following:

  - user: The User object representing the user that carried out the action.
  - request: The JSON passed by Google for this notification
  
In addition:

  - the `reply` action includes a `text` parameter, containing the user's reply.
  - the `share` action includes an `attachments` parameter, containing a list of any images shared by the action (as a `java.awt.image.BufferedImage`).
  - the `location` action includes `longitude` and `latitude` parameters (see below).

The method name is the lower-case form of the action name. Any spaces in the action name are removed.

## Location Notifications

If locationOn is set to true in the config, the app will receive regular location notifications from Glass. To receive location notifications, add a `location(params)` method to the `MessageHandlerService`. Params will contain `longitude` and `latitude` fields.

## Welcome Cards

When a user logs in, by default, the plugin will send a card with the text "Welcome to APPNAME" to the timeline, with a single menu item: Reply. To customise this welcome message, create a service called `WelcomeService`, with a method:

    TimelineItem makeWelcomeCard()    

## Testing, Bug Reports and Contributions

This project is in its early stages and needs significant testing. To help out, or report bugs please contact dankelleher@yahoo.com
