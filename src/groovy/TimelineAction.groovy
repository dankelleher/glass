enum TimelineAction {
	REPLY, // Initiate a reply to the timeline item using the voice recording UI. The creator attribute must be set in the timeline item for this menu to be available.
	REPLY_ALL, // Same behavior as REPLY. The original timeline item's recipients will be added to the reply item.
	DELETE, // Delete the timeline item.
	SHARE, // Share the timeline item with the available contacts.
	READ_ALOUD, // Read the timeline item's speakableText aloud; if this field is not set, read the text field; if none of those fields are set, this menu item is ignored.
	VOICE_CALL, // Initiate a phone call using the timeline item's creator.phone_number attribute as recipient.
	NAVIGATE, // Navigate to the timeline item's location.
	TOGGLE_PINNED // Toggle the isPinned state of the timeline item.
}
