Grails Glass Plugin
=========

The Grails Glass Plugin allows you to quickly develop Grails apps for the upcoming Google Glass platform, via the [Mirror API](https://developers.google.com/glass/). 

The plugin takes care of the following common tasks:

  - OAuth2 user authentication with Google
  - Posting and adding contacts to a user's Glass timeline
  - Subscribing to notifications from Glass, triggered by user actions.

## Getting Started

Register your application with Google as described [here](https://developers.google.com/glass/overview).

Install the plugin and add the following config to your project's Config.groovy:

    grails {
        plugin {
		    glass {
			    username = <username of your app's google account (for email notifications - optional)>
			    password = <password of your app's google account (for email notifications - optional)>
			    appname = <name of your app>
			    imageurl = <URL to an image representing your app>
			    home {
    				controller = <controller and...>
				    action = <action to direct to after user has been authorised by Google OAuth2>
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

## Plugin Details

The plugin adds the following objects to the Grails project:

  - `AuthController`: By default, the URLMappings for the plugin map the root URL '/' to this controller, which will check if there is a user in the session. If not it will redirect to the Google login screen
  - `User` domain object: Stores the Google username (email) of a user against the OAuth2 access credentials provided by Google.
  - `AuthorisationService`: Wrapper for Google's OAuth2 libraries. Call authorisationService.getCredential(User) when posting to the Timeline
  - `NotifyService`: Listens to subscriptions on the Glass timeline. Passes notifications on to a service named `MessageHandlerService`, if one exists.

## Posting to the Timeline

The plugin uses the [Google Mirror API](https://developers.google.com/glass/about) to post to the timeline. Example code would be:

    import com.google.api.services.mirror.model.TimelineItem

    import com.google.api.client.auth.oauth2.Credential
    import org.seethrough.glass.User
    import org.seethrough.glass.MirrorClient

    class SomeServiceOrController
    
        def authorisationService
    
        void publish(User user, String text) {
            TimelineItem timelineItem = new TimelineItem()
            timelineItem.setText(text)

            Credential credential = authorisationService.getCredential(user)

            MirrorClient.insertTimelineItem(credential, timelineItem)
        }
    }

*Note, this plugin, and the Mirror API are both works-in-progress and are subject to change.*

## Subscribing to notifications

The plugin automatically adds a subscription to the user's timeline when they log in. To perform actions based on this subscription, create a service called MessageHandlerService. Each method inside this service should correspond to an action on a timeline card. E.g. a method called `share` will be called on receipt of Share actions.

*Note, only the Reply action is implemented at present. Other actions, including custom actions will be supported in the near future.*

## Testing, Bug Reports and Contributions

This project is in its early stages and needs significant testing. To help out, or report bugs please contact dankelleher@yahoo.com
