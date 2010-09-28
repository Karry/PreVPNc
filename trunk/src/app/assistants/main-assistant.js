
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

	VpnManager.getInstance().loadProfiles( this.controller, this.addProfile.bind(this) , this.tableErrorHandler.bind(this));
	VpnManager.getInstance().addListener(this.update.bind(this));
	
	// I don't know why listener sometimes stops working. So We refresh state periodically
	// this isnt probably needed this time... 
	// this.timer = setTimeout(this.updateAll.bind(this), 2000);
}

MainAssistant.prototype.updateAll = function(){
	for (i = 0; i< this.currentModel.items.length; i++){
		VpnManager.getInstance().refreshProfileInfo(
													this.controller,
													this.currentModel.items[i],
													this.update.bind(this),
													this.tableErrorHandler.bind(this)
													);
	}
	this.timer = setTimeout(this.updateAll.bind(this), 2000);
}

MainAssistant.prototype.addProfile = function(item){
	Mojo.Log.error("add profile to model... "+Object.toJSON(item)); 	
	this.currentModel.items.push(item);
	this.controller.modelChanged(this.currentModel);
    //this.controller.get('profileList').mojo.noticeAddedItems(this.currentModel.items.length, [item]);	
    //this.profileList.mojo.revealItem(350, true);
}

MainAssistant.prototype.update = function(edited, deleted){
	if (edited && this.currentModel.items.indexOf(edited) < 0){
		this.currentModel.items.push(edited);
		this.controller.modelChanged(this.currentModel);		
	}
	this.controller.modelChanged(this.currentModel);
}


MainAssistant.prototype.reveal = function(){
    this.profileList.mojo.revealItem(350, true);
}

MainAssistant.prototype.listAddHandler = function(event){
    Mojo.Controller.stageController.pushScene("edit",{});	
}

MainAssistant.prototype.listDeleteHandler = function(event){
	this.currentModel.items.splice(this.currentModel.items.indexOf(event.item), 1);
    Mojo.log("EditablelistAssistant deleting '"+event.item.name+"'.");
	VpnManager.getInstance().deleteProfile( event.item,
			function(){},
			this.tableErrorHandler.bind(this));
}

MainAssistant.prototype.handleTrackTap = function(event){

	var profilePopupModel = [];
	var i = 0;
	if (event.item.state == "CONNECTED" || event.item.state == "CONNECTING" ){
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
				VpnManager.getInstance().connect( this.controller, event.item,
												function(event){
													$('msg').innerHTML = Object.toJSON(event);
												});
			} else if (response == "disconnect"){
				VpnManager.getInstance().disconnect( this.controller, event.item,
												function(event){
													$('msg').innerHTML = Object.toJSON(event);
												});				
			} else if (response == "edit"){
				Mojo.Controller.stageController.pushScene("edit",{profile: event.item});
			} else if (response == 'showLog'){
				VpnManager.getInstance().refreshProfileInfo(this.controller, event.item,
															function(profile){
																Mojo.Controller.stageController.pushScene("log",{profile: profile});
															},
															function(event){
																$('msg').innerHTML = Object.toJSON(event);
															});
            } else if (response == 'delete') {
                this.controller.showAlertDialog({
                    onChoose: function(value) {
                        if (value == 'yes') {
                            this.listDeleteHandler(event);
                        }
                    }.bind(this),
                    title: $L("Delete?"),
                    message: $L("Are you sure you want to delete profile #{profilename}?")
                            .interpolate({profilename:"\"" + event.item.display_name +"\""}),
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


MainAssistant.prototype.tableErrorHandler = function(transaction, error){
    $('msg').innerHTML = Object.toJSON(event);
    //$('msg').update('Error: ' + error.message + ' (Code ' + error.code + ')');
    return true;
}

MainAssistant.prototype.connectingFailedHandler = function(event){
    $('msg').innerHTML = Object.toJSON(event);
}
MainAssistant.prototype.routeSetHandler = function(event){
    $('msg2').innerHTML = Object.toJSON(event);
}

MainAssistant.prototype.cleanup = function(event){
	/* this function should do any cleanup needed before the scene is destroyed as 
	   a result of being popped off the scene stack */
	clearTimeout( this.timer);

}

