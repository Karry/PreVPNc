
function EditAssistant(params){
    if (params.profile)
        this.profile = params.profile;
}

EditAssistant.prototype.setup = function(){
    
    this.controller.listen('btn', Mojo.Event.tap, this.buttonEvent.bind(this));
    
    if (this.profile){

        this.controller.get("name").value = this.profile.name;
        this.controller.get("host").value = this.profile.host;
        this.controller.get("user").value = this.profile.user;
        this.controller.get("pass").value = this.profile.password;
        
        if (this.profile.routes && this.profile.routes.length > 0){
            this.controller.get("network").value = this.profile.routes[0].network;
            this.controller.get("gateway").value = this.profile.routes[0].gateway;
        }
    }
}

EditAssistant.prototype.buttonEvent = function(event){
    if (!this.profile)
        this.profile = {};
    
    this.profile.name = this.controller.get("name").value;
	this.profile.host = this.controller.get("host").value;
	this.profile.user = this.controller.get("user").value;
	this.profile.password = this.controller.get("pass").value;
    
    this.profile.routes = [];
    this.profile.routes[0] = {};
	this.profile.routes[0].network = this.controller.get("network").value;
	this.profile.routes[0].gateway = this.controller.get("gateway").value;   
    
    this.profile.type = "pptp";
    VpnManager.getInstance().editProfile(this.profile, function(){
            Mojo.Log.error("pop Scene...");        
            Mojo.Controller.stageController.popScene();
        },
        function(event){
            Mojo.Log.error("SQL failed "+Object.toJSON(event)); 
            $('msg').innerHTML = Object.toJSON(event);
        }
        );
    
}
