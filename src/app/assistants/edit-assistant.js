
function EditAssistant(params){
    if (params.profile){
        this.profile = params.profile;
        this.originalName = this.profile.name;
    }
}

EditAssistant.prototype.setup = function(){
    
    this.controller.listen('btn', Mojo.Event.tap, this.buttonEvent.bind(this));
    
    if (!this.profile)
        this.profile = {};
    if (!this.profile.routes)
        this.profile.routes = [];
    if (!this.profile.routes[0])
        this.profile.routes[0] = {};
    
    this.controller.setupWidget(
        "name",
        this.urlAttributes = {
            modelProperty: "name",
            limitResize: true,
            textReplacement: false,
            enterSubmits: false
        },
		this.profile
    );    
    this.controller.setupWidget(
        "host",
        this.urlAttributes = {
            modelProperty: "host",
            limitResize: true,
            textReplacement: false,
            enterSubmits: false
        },
		this.profile
    );    
    this.controller.setupWidget(
        "user",
        this.urlAttributes = {
            modelProperty: "user",
            limitResize: true,
            textReplacement: false,
            enterSubmits: false
        },
		this.profile
    );    
    this.controller.setupWidget(
        "password",
        this.urlAttributes = {
            modelProperty: "password",
            limitResize: true,
            textReplacement: false,
            enterSubmits: false
        },
		this.profile
    );    
    this.controller.setupWidget(
        "network",
        this.urlAttributes = {
            modelProperty: "network",
            limitResize: true,
            textReplacement: false,
            enterSubmits: false
        },
		this.profile.routes[0]
    );    
    this.controller.setupWidget(
        "gateway",
        this.urlAttributes = {
            modelProperty: "gateway",
            limitResize: true,
            textReplacement: false,
            enterSubmits: false
        },
		this.profile.routes[0]
    );
    
    
    this.controller.setupWidget(
        "type",
        this.attributes = {
            modelProperty: 'type',
            label: $L('VPN Type'),            
            choices: [
                {label: "PPTP", value: "PPTP"},
                {label: "OpenVPN", value: "OpenVPN"}
            ]},
        this.profile
    );
    
}

EditAssistant.prototype.buttonEvent = function(event){
    if (!this.profile)
        this.profile = {};
    
    VpnManager.getInstance().editProfile(this.originalName, this.profile, function(){
            Mojo.Log.error("pop Scene...");        
            Mojo.Controller.stageController.popScene();
        },
        function(event){
            Mojo.Log.error("SQL failed "+Object.toJSON(event)); 
            $('msg').innerHTML = Object.toJSON(event);
        }
        );
    
}
