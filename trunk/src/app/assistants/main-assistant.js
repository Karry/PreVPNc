
function MainAssistant(){
	/* this is the creator function for your scene assistant object. It will be passed all the 
	   additional parameters (after the scene name) that were passed to pushScene. The reference
	   to the scene controller (this.controller) has not be established yet, so any initialization
	   that needs the scene controller should be done in the setup function below. */
}

MainAssistant.prototype.setup = function(){
	
    // Store references to reduce the use of controller.get()
    this.profileList = this.controller.get('profileList');
	
    // Set up a few models so we can test setting the widget model:
    this.currentModel = {listTitle:$L('VPN profiles'), items:[]};
	
    // Set up the attributes for the list widget:
    this.profileAtts = {
            itemTemplate:'main/listitem',
            listTemplate:'main/listcontainer',
            showAddItem:true,
			addItemLabel:$L("Add..."),			
            swipeToDelete:false,
            reorderable:false,
            emptyTemplate:'main/emptylist'
    };

    this.controller.setupWidget('profileList', this.profileAtts , this.currentModel);
	
    this.controller.listen(this.profileList, Mojo.Event.listAdd, this.listAddHandler.bind(this));
    this.controller.listen(this.profileList, Mojo.Event.listTap, this.handleTrackTap.bind(this));
	
	this.addProfile({name:"test", state:"???", type:"pptp"});
	VpnManager.getInstance().loadProfiles( this.addProfile.bind(this) , this.tableErrorHandler.bind(this));

	/*
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
	
	this.log = "";
    this.controller.listen('btn', Mojo.Event.tap, this.buttonEvent.bind(this));
	*/
}

MainAssistant.prototype.tableErrorHandler = function(transaction, error){
    $('trackHeadermsg').update('Error: ' + error.message + ' (Code ' + error.code + ')');
    return true;
}

MainAssistant.prototype.addProfile = function(item){
	this.currentModel.items.push(item);
    //this.profileList.mojo.noticeAddedItems(this.currentModel.items.length, [item]);	
    //this.profileList.mojo.revealItem(350, true);
}


MainAssistant.prototype.reveal = function(){
    this.profileList.mojo.revealItem(350, true);
}

MainAssistant.prototype.listAddHandler = function(event){
    Mojo.Controller.stageController.pushScene("edit",{});	
}

MainAssistant.prototype.listDeleteHandler = function(event){
	
	this.currentModel.items.splice(this.currentModel.items.indexOf(event.item), 1);
    Mojo.log("EditablelistAssistant deleting '"+event.item.data+"'.");
	VpnManager.getInstance().deleteProfile( event.item,
			function(){
				 if (event.type == "mojo-list-tap") // event from context menu, not from "slide" delete
					 this.controller.modelChanged(this.currentModel);
			},
			this.tableErrorHandler.bind(this));
}

MainAssistant.prototype.handleTrackTap = function(event){

	var profilePopupModel = [];
	var i = 0;
	if (event.item.state == "CONNECTED"){
		profilePopupModel[i++] = {label: $L('Disconnect'), command: 'disconnect'};
	}else{
		profilePopupModel[i++] = {label: $L('Connect'), command: 'connect'};
	}
	profilePopupModel[i++] = {label: $L('Edit'), command: 'edit'};
	if (event.item.state != "INACTIVE"){
		profilePopupModel[i++] = {label: $L('Show log'), command: 'showLog'};
	}
	profilePopupModel[i++] = {label: $L('Delete'), command: 'delete'};

    this.controller.popupSubmenu({
        onChoose: function(response){
            if (response == 'connect') {
				this.connect( event.item );
			} else if (response == "edit"){
				Mojo.Controller.stageController.pushScene("edit",{profile: event.item});			
            } else if (response == 'delete') {
                this.controller.showAlertDialog({
                    onChoose: function(value) {
                        if (value == 'yes') {
                            this.listDeleteHandler(event);
                        }
                    }.bind(this),
                    title: $L("Delete?"),
                    message: $L("Are you sure you want to delete profile #{profilename}?")
                            .interpolate({profilename:"\"" + event.item.name +"\""}),
                    choices:[
                        {label:$L('Yes'), value:"yes", type:'affirmative'},
                        {label:$L('No'), value:"no", type:'negative'}
                    ]
                });
            }
        },
        placeNear: event.originalEvent.target,
        items: profilePopupModel
    });
};

MainAssistant.prototype.connect = function(item){
	
	/*
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
	*/
	
    this.controller.serviceRequest('luna://cz.karry.vpnc',
		                               {
		                                  method: 'connectVpn',	
		                                  parameters:
										  {
											type: item.type,
											host: item.host,
											name: item.name,
											user: item.user,
											pass: item.pass
		                                  },
		                                  onSuccess: this.connectedHandler.bind(this),
		                                  onFailure: this.connectingFailedHandler.bind(this)
	                                   }
									   );

}

MainAssistant.prototype.connectedHandler = function(obj){
	//this.log = this.log+"   \n"+Object.toJSON(obj);
    //$('msg').innerHTML = this.log; 
    $('msg').innerHTML = "connected, result: "+Object.toJSON(obj);

	if (obj.state == "CONNECTED" && obj.stateChanged){
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

}

