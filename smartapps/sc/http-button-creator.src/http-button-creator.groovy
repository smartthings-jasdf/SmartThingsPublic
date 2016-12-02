/**
 *  HTTP Button Creator
 *  Category: Smart App
 *  Copyright 2016 Soon Chye
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Credit: Eric Roberts (baldeagle072) - Virtual switch creator
 *  Credit: tguerena and surge919 - URI Switch
 *  
 *  Fix addChildDevice problem - https://community.smartthings.com/t/use-addchilddevice-with-manual-ip-entry/4594/23
 */
definition(
    name: "HTTP Button Creator",
    namespace: "sc",
    author: "SC",
    description: "Creates HTTP button on the fly!",
    category: "Convenience",
    iconUrl: "https://github.com/chancsc/icon/raw/master/standard-tile%401x.png",
    iconX2Url: "https://github.com/chancsc/icon/raw/master/standard-tile@2x.png",
    iconX3Url: "https://github.com/chancsc/icon/raw/master/standard-tile@3x.png")


preferences {
	section("Select Sensor") {
       input("sensorDevice","capability.contactSensor", title: "pick a contact sensor", required: true, multiple: false)
    }
	section("Create HTTP Button") {
		input "switchLabel", "text", title: "Button Label", required: true
	}
    section("on this hub...") {
        input "theHub", "hub", multiple: false, required: true
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
    
    subscribe(sensorDevice, "contact", contactHandler)
    state.currentState = "sigh"
    
    log.debug "Installed current state; ${state.currentState}"
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
    def deviceId = app.id
    log.debug(deviceId)
    def existing = getChildDevice(deviceId)
    if (!existing) {
        def childDevice = addChildDevice("sc", "HTTP Switch", deviceId, theHub.id, [label: switchLabel, completedSetup: true])
    }
    
     getChildDevices().each {
         subscribeToCommand(it, "on", onCommand)
         subscribeToCommand(it, "off", onCommand)
 	 }
}

def uninstalled() {
    removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def contactHandler(evt) {
   def childDevice =  getChildDevice(app.id)
  
  if("open" == evt.value) {
    log.debug "Contact is in ${evt.value} state" 
   	sendEvent(childDevice,[name:"switch", value:"on", data: [type:"manual"]])
  }
  
  if("closed" == evt.value) {
    log.debug "Contact is in ${evt.value} state"
    sendEvent(childDevice,[name:"switch", value:"off", data: [type:"manual"]])
  }
}

def onCommand(evt) {
	def currentValue = "${sensorDevice.currentValue('contact').value}"
    def isclosed = currentValue == 'closed'
	log.debug "Calling on ${evt.value} ${currentValue} "
    
    if (evt.data == null) { //real command, not being set by the door itself
    	def childDevice =  getChildDevice(app.id)
        log.debug "Current: ${currentValue} ${isclosed}, Val: ${evt.value == 'on'}"
        def needToToggle = (currentValue == 'closed' && evt.value == 'on') || (currentValue == 'open' && evt.value == 'off')
        
        if (needToToggle)
   			sendHubCommand(runCmd())
        else 
            log.debug 'DO NOT NEED TO TOGGLE'
    }
}

def runCmd() {
	def host = '192.168.1.80'
	def LocalDevicePort = '80'
	def path = '/toggle_door'
	def body = "" 

	def headers = [:] 
	headers.put("HOST", "$host:$LocalDevicePort")

	def method = "GET"
	try {
		def hubAction = new physicalgraph.device.HubAction(
			method: method,
			path: path,
			body: body,
			headers: headers
			)
		log.debug hubAction
		return hubAction
	}
	catch (Exception e) {
		log.debug "Hit Exception $e on $hubAction"
	}
}