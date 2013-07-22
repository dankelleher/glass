package org.seethrough.glass

import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.MenuValue;

class TimelineCategory {
	
	static def LINK_GENERATOR
	
	def addCustomAction(action) {
		def actionMethod = action.toLowerCase().replaceAll(/\s/,"")
		def iconUrl = LINK_GENERATOR.resource(dir: 'images', file: "actions/${actionMethod}.png", absolute: true)
		def menuValues = [
			new MenuValue()
				.setIconUrl(iconUrl)
				.setDisplayName(action)]
		def item = new MenuItem().setValues(menuValues).setId(actionMethod).setAction("CUSTOM")
  
		addMenuItem(item)
	}

	private addMenuItem(MenuItem item) {
		if (menuItems == null) setMenuItems([])

		this.menuItems << item
	}
	
	def addAction(TimelineAction action) {
		addMenuItem(new MenuItem().setAction(action.toString()))
	}
}
