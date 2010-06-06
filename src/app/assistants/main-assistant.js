function MainAssistant(){
	/* this is the creator function for your scene assistant object. It will be passed all the 
	   additional parameters (after the scene name) that were passed to pushScene. The reference
	   to the scene controller (this.controller) has not be established yet, so any initialization
	   that needs the scene controller should be done in the setup function below. */
}

MainAssistant.prototype.setup = function(){
    this.nameCookie = new Mojo.Model.Cookie( 'name' );
    if (this.nameCookie.get() != undefined)
        this.controller.get("name").value = this.nameCookie.get();
	
    this.hostCookie = new Mojo.Model.Cookie( 'host' );
    if (this.hostCookie.get() != undefined)
        this.controller.get("host").value = this.hostCookie.get();
	
    this.userCookie = new Mojo.Model.Cookie( 'user' );
    if (this.userCookie.get() != undefined)
        this.controller.get("user").value = this.userCookie.get();
	
    this.passCookie = new Mojo.Model.Cookie( 'pass' );
    if (this.passCookie.get() != undefined)
        this.controller.get("pass").value = this.passCookie.get();
	
    this.networkCookie = new Mojo.Model.Cookie( 'network' );
    if (this.networkCookie.get() != undefined)
        this.controller.get("network").value = this.networkCookie.get();
	
    this.gatewayCookie = new Mojo.Model.Cookie( 'gateway' );
    if (this.gatewayCookie.get() != undefined)
        this.controller.get("gateway").value = this.gatewayCookie.get();
	
	
    this.controller.listen('btn', Mojo.Event.tap, this.buttonEvent.bind(this));    
}

MainAssistant.prototype.buttonEvent = function(event){
	
	var name = this.controller.get("name").value;
	this.nameCookie.put( name );
	var host = this.controller.get("host").value;
	this.hostCookie.put( host );
	var user = this.controller.get("user").value;
	this.userCookie.put( user );
	var pass = this.controller.get("pass").value;
	this.passCookie.put( pass );
	var network = this.controller.get("network").value;
	this.networkCookie.put( network );
	var gateway = this.controller.get("gateway").value;
	this.gatewayCookie.put( gateway );
	
    this.controller.serviceRequest('luna://cz.karry.vpnc',
		                               {
		                                  method: 'connectVpn',	
		                                  parameters:
										  {
											type: "pptp",
											host: host,
											name: name,
											user: user,
											pass: pass
		                                  },
		                                  onSuccess: this.connectedHandler.bind(this),
		                                  onFailure: this.connectingFailedHandler.bind(this)
	                                   }
									   );

}

MainAssistant.prototype.connectedHandler = function(obj){
    $('msg').innerHTML = "connected, result: "+Object.toJSON(obj);

	var network = this.controller.get("network").value;
	var gateway = this.controller.get("gateway").value;
    this.controller.serviceRequest('luna://cz.karry.vpnc',
		                               {
		                                  method: 'addRoute',	
		                                  parameters:
										  {
											network: network,
											gateway: gateway
		                                  },
		                                  onSuccess: this.routeSetHandler.bind(this),
		                                  onFailure: this.routeSetHandler.bind(this)
	                                   }
									   );
}

MainAssistant.prototype.connectingFailedHandler = function(event){
    $('msg').innerHTML = "connection failed, result: "+Object.toJSON(event);
}
MainAssistant.prototype.routeSetHandler = function(event){
    $('msg2').innerHTML = Object.toJSON(event);
}

MainAssistant.prototype.cleanup = function(event){
	/* this function should do any cleanup needed before the scene is destroyed as 
	   a result of being popped off the scene stack */
	this.setScreenTimeout(2);
}

