
function EditAssistant(params){
    if (params.profile){
        this.profile = params.profile;
    }
}

EditAssistant.prototype.setup = function(){
    
    //this.controller.listen('btn', Mojo.Event.tap, this.buttonEvent.bind(this));
    
    if (!this.profile)
        this.profile = {type: "PPTP", name: "VPN"+this.formatDate(new Date()), display_name:"preVPNc",
			configuration: { // default values for selectors
				pptp_mppe: "require-mppe\nrequire-mppe-128",
				pptp_mppe_stateful: "nomppe-stateful",
				openvpn_topology: "p2p",
				openvpn_protocol: "tcp",
				openvpn_cipher: "DES-CFB",
				cisco_userpasstype: "Xauth password",
				cisco_grouppasstype: "IPSec secret"
			}
			};
    if (!this.profile.routes)
        this.profile.routes = [];
    if (!this.profile.routes[0])
        this.profile.routes[0] = {network:"192.168.0.0/24", gateway:"192.168.0.1"};
    
    this.controller.setupWidget(
        "name",
        this.urlAttributes = {
            modelProperty: "display_name",
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
		this.profile.configuration
    );
    this.controller.setupWidget(
        "type",
        this.attributes = {
            modelProperty: 'type',
            label: $L('VPN Type'),            
            choices: [
                {label: "PPTP", value: "PPTP"},
                {label: "OpenVPN", value: "OpenVPN"},
                {label: "Cisco", value: "Cisco"}
            ]},
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
    
    
    // PPTP specific values
    this.controller.setupWidget(
        "pptp_user",
        this.urlAttributes = {
            modelProperty: "pptp_user",
            limitResize: true,
            textReplacement: false,
            enterSubmits: false
        },
		this.profile.configuration
    );    
    this.controller.setupWidget(
        "pptp_password",
        this.urlAttributes = {
            modelProperty: "pptp_password",
            limitResize: true,
            textReplacement: false,
            enterSubmits: false
        },
		this.profile.configuration
    );
    this.controller.setupWidget(
        "pptp_mppe",
        this.attributes = {
            modelProperty: 'pptp_mppe',
            label: $L('MPPE'),  
            choices: [
                {label: $L("Require 128 bit"), value: "require-mppe\nrequire-mppe-128"},
                {label: $L("Require"), value: "require-mppe"},
                {label: $L("Don't use"), value: "nomppe"},
            ]},
        this.profile.configuration
   );    
    this.controller.setupWidget(
        "pptp_mppe_stateful",
        this.attributes = {
            modelProperty: 'pptp_mppe_stateful',
            label: $L('MPPE Stateful'),  
            choices: [
                {label: $L("Permit"), value: "nomppe-stateful"},
                {label: $L("Allow"), value: "# allow mppe stateful"},
            ]},
        this.profile.configuration
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
        this.profile.configuration

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
        this.profile.configuration
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
        this.profile.configuration
    );
	
	// Cisco specific values
	/*
	cisco_userid
	cisco_userpasstype
	cisco_userpass
	cisco_groupid
	cisco_grouppasstype
	cisco_grouppass
	*/
    this.controller.setupWidget(
        "cisco_userpasstype",
        this.attributes = {
            modelProperty: 'cisco_userpasstype',
            label: $L('Pass. Type'),  
            choices: [
                {label: "Text", value: "Xauth password"},
                {label: "Obfuscated", value: "Xauth obfuscated password"}
            ]},
        this.profile.configuration
   );	
    this.controller.setupWidget(
        "cisco_grouppasstype",
        this.attributes = {
            modelProperty: 'cisco_grouppasstype',
            label: $L('Secret Type'),  
            choices: [
                {label: "None", value: "# no IPSec secret"},
                {label: "Text", value: "IPSec secret"},
                {label: "Obfuscated", value: "IPSec obfuscated secret"}
            ]},
        this.profile.configuration
   );
    this.controller.setupWidget(
        "cisco_userid",
        this.urlAttributes = {
            modelProperty: "cisco_userid",
            limitResize: true,
            textReplacement: false,
            enterSubmits: false
        },
		this.profile.configuration
    );	
    this.controller.setupWidget(
        "cisco_userpass",
        this.urlAttributes = {
            modelProperty: "cisco_userpass",
            limitResize: true,
            textReplacement: false,
            enterSubmits: false
        },
		this.profile.configuration
    );	
    this.controller.setupWidget(
        "cisco_groupid",
        this.urlAttributes = {
            modelProperty: "cisco_groupid",
            limitResize: true,
            textReplacement: false,
            enterSubmits: false
        },
		this.profile.configuration
    );	
    this.controller.setupWidget(
        "cisco_grouppass",
        this.urlAttributes = {
            modelProperty: "cisco_grouppass",
            limitResize: true,
            textReplacement: false,
            enterSubmits: false
        },
		this.profile.configuration
    );
    this.controller.setupWidget(
        "cisco_domain",
        this.urlAttributes = {
            modelProperty: "cisco_domain",
            limitResize: true,
            textReplacement: false,
            enterSubmits: false
        },
		this.profile.configuration
    );
	
	
    
    this.specificConfig = [];
    this.specificConfig["PPTP"] = { elementId: 'specificPPTP'};
    this.specificConfig["OpenVPN"] = { elementId: 'specificOpenVPN'};
    this.specificConfig["Cisco"] = { elementId: 'specificCisco'};
    
    this.refreshType();
    this.controller.listen('type', Mojo.Event.propertyChange , this.refreshType.bind(this));
    
    
    $('OpenVPNkeyNote').innerHTML 	=  $L("Your key, certificate and authority certificate put to "
                                          + "<b>.vpn/openvpn_#{name}.key</b>, <b>.vpn/openvpn_#{name}.crt</b> and <b>.vpn/openvpn_#{name}.ca.crt</b>.<br />"
                                          + "For more information read <a href=\"http://code.google.com/p/prevpnc/wiki/Howto\">project wiki</a>.").interpolate({name: this.profile.name })
}


EditAssistant.prototype.formatDate = function(dateobj){
	strRes = "NA";
	secs = dateobj.getSeconds(); if (secs > 9) strSecs = String(secs); else strSecs = "0" + String(secs);
	mins = dateobj.getMinutes(); if (mins > 9) strMins = String(mins); else strMins = "0" + String(mins);
	hrs  = dateobj.getHours(); if (hrs > 9) strHrs = String(hrs); else strHrs = "0" + String(hrs);
	day  = dateobj.getDate(); if (day > 9) strDays = String(day); else strDays = "0" + String(day);
	mnth = dateobj.getMonth() + 1; if (mnth > 9) strMnth = String(mnth); else strMnth = "0" + String(mnth);
	yr   = dateobj.getFullYear(); strYr = String(yr);

    strRes = strYr + strMnth + strDays + "" + strHrs + strMins + strSecs;
	return strRes
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

EditAssistant.prototype.cleanup = function(event){
    this.buttonEvent(null);
}

EditAssistant.prototype.buttonEvent = function(buttonEvent){
    if (!this.profile)
        this.profile = {};
    
    VpnManager.getInstance().editProfile(this.profile.name, this.profile, function(){
            if (buttonEvent && buttonEvent != null){
                Mojo.Log.error("pop Scene...");        
                Mojo.Controller.stageController.popScene();
            }
        },
        function(event){
            Mojo.Log.error("SQL failed "+Object.toJSON(event)); 
            $('msg').innerHTML = Object.toJSON(event);
        }
        );
    
}
