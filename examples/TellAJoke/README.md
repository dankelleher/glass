TellAJoke Example Project
=========

TellAJoke is a simple Glass project that posts a random Chuck Norris joke of questionable comic value to the user's timeline on the user's request. It demonstrates the basics of posting to the timeline and responding to notifications. The project has no domain objects, views or controllers of its own. In fact, other than configuration files, it comprises just three services, described below.

## Setting up and running

To set up the project:

  - Register it with Google as described [here](https://developers.google.com/glass/quickstart/java)
  - Update Config.groovy with the client id and secret provided by Google
  - Deploy the app (to Google App Engine or otherwise)
  
## Code Walkthrough

### WelcomeService

Creates a custom welcome card for new users, with a single action "Hit me" which triggers a joke to be retrieved

### MessageHandlerService

Responds to the "Hit Me" action from the welcome card, and sends a joke to the user's timeline

### JokeService

Connects to the RESTful joke api at http://api.icndb.com/jokes/ and retrieves the joke text