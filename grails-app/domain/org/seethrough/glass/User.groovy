package org.seethrough.glass

class User {

	String id
	String username

	static hasOne = [credential: UserCredential]

	static constraints = {
		username unique: true, nullable: true
		credential nullable: true
	}

	static mapping = {
		id generator: 'assigned', name: "id", type: 'string'
	}
}
