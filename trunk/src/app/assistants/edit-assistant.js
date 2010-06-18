
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
    
    
    // OpenVPN specific values
    this.controller.setupWidget(
        "openvpn_topology",
        this.attributes = {
            modelProperty: 'openvpn_topology',
            label: $L('Topology'),              
            choices: [
                {label: "subnet", value: "subnet"},
                {label: "p2p", value: "p2p"},
                {label: "net30", value: "net30"},
            ]},
        this.profile

    );
    this.controller.setupWidget(
        "openvpn_protocol",
        this.attributes = {
            modelProperty: 'openvpn_protocol',
            label: $L('Protocol'),  
            choices: [
                {label: "TCP", value: "tcp"},
                {label: "UDP", value: "udp"},
            ]},
        this.profile
   );
    this.controller.setupWidget(
        "openvpn_cipher",
        this.attributes = {
            modelProperty: 'openvpn_cipher',
            label: $L('Cipher'),  
            choices: [
                {label: "none", value: "none"},
                {label: "DES-CFB", value: "DES-CFB"},
                {label: "DES-CBC", value: "DES-CBC"},
                {label: "RC2-CBC", value: "RC2-CBC"},
                {label: "RC2-CFB", value: "RC2-CFB"},
                {label: "RC2-OFB", value: "RC2-OFB"},
                {label: "DES-EDE-CBC", value: "DES-EDE-CBC"},
                {label: "DES-EDE3-CBC", value: "DES-EDE3-CBC"},
                {label: "DES-OFB", value: "DES-OFB"},
                {label: "DES-EDE-CFB", value: "DES-EDE-CFB"},
                {label: "DES-EDE3-CFB", value: "DES-EDE3-CFB"},
                {label: "DES-EDE-OFB", value: "DES-EDE-OFB"},
                {label: "DES-EDE3-OFB", value: "DES-EDE3-OFB"},
                {label: "DESX-CBC", value: "DESX-CBC"},
                {label: "BF-CBC", value: "BF-CBC"},
                {label: "BF-CFB", value: "BF-CFB"},
                {label: "BF-OFB", value: "BF-OFB"},
                {label: "RC2-40-CBC", value: "RC2-40-CBC"},
                {label: "CAST5-CBC", value: "CAST5-CBC"},
                {label: "CAST5-CFB", value: "CAST5-CFB"},
                {label: "CAST5-OFB", value: "CAST5-OFB"},
                {label: "RC2-64-CBC", value: "RC2-64-CBC"},
                {label: "AES-128-CBC", value: "AES-128-CBC"},
                {label: "AES-128-OFB", value: "AES-128-OFB"},
                {label: "AES-128-CFB", value: "AES-128-CFB"},
                {label: "AES-192-CBC", value: "AES-192-CBC"},
                {label: "AES-192-OFB", value: "AES-192-OFB"},
                {label: "AES-192-CFB", value: "AES-192-CFB"},
                {label: "AES-256-CBC", value: "AES-256-CBC"},
                {label: "AES-256-OFB", value: "AES-256-OFB"},
                {label: "AES-256-CFB", value: "AES-256-CFB"},
                {label: "AES-128-CFB1", value: "AES-128-CFB1"},
                {label: "AES-192-CFB1", value: "AES-192-CFB1"},
                {label: "AES-256-CFB1", value: "AES-256-CFB1"},
                {label: "AES-128-CFB8", value: "AES-128-CFB8"},
                {label: "AES-192-CFB8", value: "AES-192-CFB8"},
                {label: "AES-256-CFB8", value: "AES-256-CFB8"},
                {label: "DES-CFB1", value: "DES-CFB1"},
                {label: "DES-CFB8", value: "DES-CFB8"},
                {label: "CAMELLIA-128-CBC", value: "CAMELLIA-128-CBC"},
                {label: "CAMELLIA-192-CBC", value: "CAMELLIA-192-CBC"},
                {label: "CAMELLIA-256-CBC", value: "CAMELLIA-256-CBC"},
                {label: "CAMELLIA-128-CFB", value: "CAMELLIA-128-CFB"},
                {label: "CAMELLIA-192-CFB", value: "CAMELLIA-192-CFB"},
                {label: "CAMELLIA-256-CFB", value: "CAMELLIA-256-CFB"},
                {label: "CAMELLIA-128-CFB1", value: "CAMELLIA-128-CFB1"},
                {label: "CAMELLIA-192-CFB1", value: "CAMELLIA-192-CFB1"},
                {label: "CAMELLIA-256-CFB1", value: "CAMELLIA-256-CFB1"},
                {label: "CAMELLIA-128-CFB8", value: "CAMELLIA-128-CFB8"},
                {label: "CAMELLIA-192-CFB8", value: "CAMELLIA-192-CFB8"},
                {label: "CAMELLIA-256-CFB8", value: "CAMELLIA-256-CFB8"},
                {label: "CAMELLIA-128-OFB", value: "CAMELLIA-128-OFB"},
                {label: "CAMELLIA-192-OFB", value: "CAMELLIA-192-OFB"},
                {label: "CAMELLIA-256-OFB", value: "CAMELLIA-256-OFB"},
                {label: "SEED-CBC", value: "SEED-CBC"},
                {label: "SEED-OFB", value: "SEED-OFB"},
                {label: "SEED-CFB", value: "SEED-CFB"},
            ]},
        this.profile
    );
    
    this.specificConfig = [];
    this.specificConfig["OpenVPN"] = { elementId: 'specificOpenVPN'};
    
    this.refreshType();
    this.controller.listen('type', Mojo.Event.propertyChange , this.refreshType.bind(this));
}


EditAssistant.prototype.refreshType = function(){
    
    for(var key in this.specificConfig){
    
        //elem = document.getElementById( 'specificOpenVPN' );
        elem = document.getElementById( this.specificConfig[key].elementId );
        //Mojo.Log.error("id: "+this.specificConfig[key].elementId+" value: "+elem);
        if ((!elem) || (!elem.style))
            continue        
        
        if (key == this.profile.type ){
            elem.style.display = "block";
        }else{
            elem.style.display = "none";            
        }
    }
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
