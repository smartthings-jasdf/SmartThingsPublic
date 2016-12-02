/*
*  HTTP Switch
*  Category: Device Handler
* 
*  Source: https://community.smartthings.com/t/beta-release-uri-switch-device-handler-for-controlling-items-via-http-calls/37842
* 
*  Credit: tguerena and surge919
*/

import groovy.json.JsonSlurper

metadata {
	definition (name: "HTTP Switch", namespace: "sc", author: "SC") {
        capability "Switch"
		attribute "triggerswitch", "string"
		command "DeviceTrigger"
	}

	preferences {
    }


	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
    	standardTile("DeviceTrigger", "device.triggerswitch", width: 2, height: 2, canChangeIcon: true) {
			state "triggeroff", label: 'Off', action: "on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
			state "triggeron", label: 'On', action: "off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "off"
		}
		main "DeviceTrigger"
			details (["DeviceTrigger"])
	}
}

def on(evt) {
	log.debug "---ON COMMAND--- ${evt}"
    sendEvent(name: "triggerswitch", value: "triggeron", isStateChange: true)
   	sendEvent(name: "switch", value: "on")
}

def off(evt) {
	log.debug "---OFF COMMAND--- ${evt}"
    sendEvent(name: "triggerswitch", value: "triggeroff", isStateChange: true)
    sendEvent(name: "switch", value: "off")
}

def parse(evt) {
	log.debug("${evt}")
}
