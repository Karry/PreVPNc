

function VpnManager(){
    this.listeners = [];
    this.dbConnect();
}

VpnManager.instance = null;

VpnManager.getInstance = function(){
    if (!this.instance)
        this.instance = new VpnManager();
    return this.instance;
};

VpnManager.prototype.dbConnect = function(){
    Mojo.Log.error("listeners length "+this.listeners.length);
    
    // Open DB to store tracks
    this.db = openDatabase('ext:VpnDb', '', 'Sample Data Store', 65536);
    
    // create tables, if these don't exist
    var strSQL =  "CREATE TABLE IF NOT EXISTS `vpn_profile` ("
                + "`name` VARCHAR(255) NOT NULL ,"
                + "`type` VARCHAR(255) NOT NULL DEFAULT 'pptp',"
                + "`host` VARCHAR(255) NOT NULL ,"
                + "`user` VARCHAR(255) NOT NULL ,"
                + "`password` VARCHAR(255) NOT NULL,"
                + "  PRIMARY KEY (`name`)"
                + ");";
    this.executeSQL(strSQL,
                    function(event){
                        Mojo.Log.error("table vpn_profile created"); 
                    },
                    function(event){
                        Mojo.Log.error("error when creating table vpn_profile"); 
                    });
    var strSQL =  "CREATE TABLE IF NOT EXISTS `vpn_route` ("
                + "`profile_name` VARCHAR(255) NOT NULL ,"
                + "`network` VARCHAR(255) NOT NULL ,"
                + "`gateway` VARCHAR(255) NOT NULL,"
                + " PRIMARY KEY (`profile_name`,`network`)"
                + ");";
    this.executeSQL(strSQL,
                    function(event){
                        Mojo.Log.error("table vpn_route created"); 
                    },                    
                    function(event){
                        Mojo.Log.error("error when creating table vpn_route"); 
                    });

}


VpnManager.prototype.executeSQL = function(strSQL, resultHandler, errorHandler){
    this.db.transaction(
        (
            function (transaction){
                transaction.executeSql(strSQL+" GO;", [], resultHandler, errorHandler);
            }
        ).bind(this) 
    );    
}

VpnManager.prototype.executeSQLs = function(sqlArr, resultHandler, errorHandler){
    this.db.transaction(
        (
            function (transaction){
                for (i = 0; i<sqlArr.length; i++){
                    Mojo.Log.error("SQL ("+(i+1)+"/"+sqlArr.length+"): "+sqlArr[i]);
                    if (i == sqlArr.length-1){
                        // we want call result Handler only once
                        transaction.executeSql(sqlArr[i]+" GO;", [], resultHandler, errorHandler);
                    }else{
                        transaction.executeSql(sqlArr[i]+" GO;", [], function(){}, errorHandler);                        
                    }
                }
            }
        ).bind(this) 
    );

}

VpnManager.prototype.addListener = function(method){
    this.listeners[ this.listeners.length ] = method;
}

VpnManager.prototype.loadProfiles = function( controller, upatedProfileHandler , tableErrorHandler){
    Mojo.Log.error("load profiles... ");
    loadRoutes = this.loadRoutes.bind(this);
    refreshProfileInfo = this.refreshProfileInfo.bind(this);
    this.executeSQL("SELECT *, 'UNDEFINED' AS `state` FROM `vpn_profile`",
                    function(transaction, result){
                        if (result.rows){
                            Mojo.Log.error("loaded profiles: "+result.rows.length); 
                            for (i = 0; i< result.rows.length; i++){
                                item = Object.clone(result.rows.item(i));
                                loadRoutes(controller, item,
                                        function( profile ){
                                            refreshProfileInfo(controller, profile, upatedProfileHandler, tableErrorHandler);
                                        }
                                        , tableErrorHandler);
                            }
                        }else{
                            tableErrorHandler({message:"bad db result ", code:-1});
                        }                        
                    },
                    tableErrorHandler);
}

VpnManager.prototype.loadRoutes = function(controller, profile, upatedProfileHandler, errorHandler){
    Mojo.Log.error("load routes... ["+profile.name+"]"); 
    this.executeSQL("SELECT * FROM `vpn_route` WHERE `profile_name` = '"+profile.name+"';",
                    function(transaction, result){
                        if (result.rows){
                            profile.routes = [];
                            for (i = 0; i< result.rows.length; i++){
                                profile.routes[i] = Object.clone(result.rows.item(i));
                            }
                            upatedProfileHandler( profile );
                        }else{
                            errorHandler({message:"bad db result ", code:-1});
                        }                        
                    },
                    errorHandler);    
}

VpnManager.prototype.refreshProfileInfo = function(controller, profile, upatedProfileHandler, errorHandler){
    Mojo.Log.error("refresh profile info... ["+profile.name+"]"); 
    controller.serviceRequest('luna://cz.karry.vpnc',
		                               {
		                                  method: 'connectionInfo',	
		                                  parameters:
										  {
											name: profile.name
		                                  },
		                                  onSuccess: function(result){
                                                Mojo.Log.error("get info... "+Object.toJSON(result)); 
                                                profile.state = result.state;
                                                profile.log = result.log;
                                                profile.localAddress = result.localAddress
                                                Mojo.Log.error("return... "+Object.toJSON(profile)); 
                                                upatedProfileHandler( profile );
                                            },
		                                  onFailure: errorHandler
	                                   }
									   );    
}

VpnManager.prototype.disconnect = function(controller, profile, errorHandler){

    handler = this.connectionStateChanged.bind(this);
    controller.serviceRequest('luna://cz.karry.vpnc',
		                               {
		                                  method: 'disconnectVpn',	
		                                  parameters:
										  {
											name: profile.name
		                                  },
		                                  onSuccess: function(msg){
                                            handler(controller, msg, profile);
                                          },
		                                  onFailure: errorHandler
	                                   }
									   );
}
VpnManager.prototype.connect = function(controller, profile, errorHandler){

    handler = this.connectionStateChanged.bind(this);
    controller.serviceRequest('luna://cz.karry.vpnc',
		                               {
		                                  method: 'connectVpn',	
		                                  parameters:
										  {
											type: profile.type,
											host: profile.host,
											name: profile.name,
											user: profile.user,
											pass: profile.password
		                                  },
		                                  onSuccess: function(msg){
                                            handler(controller, msg, profile);
                                          },
		                                  onFailure: errorHandler
	                                   }
									   );
}

VpnManager.prototype.connectionStateChanged = function(controller, obj, profile){
    
    if (profile.name != obj.profileName){
        Mojo.Log.error("profile names isn't equals " + profile.name+", "+obj.profileName);
        return;
    }
    
    //$('msg').innerHTML = "connected, result: "+Object.toJSON(obj);
    profile.state = obj.state;
    profile.log = obj.log;
    profile.localAddress = obj.localAddress;
    try{
        for (i = 0; i< this.listeners.length ; i++){
            this.listeners[i]();
        }
    }catch(ex){
        Mojo.Log.error("exeption while sending notification (2)... " + ex);          
    }    

	if (obj.state == "CONNECTED" && obj.stateChanged){
        for (i = 0; i<profile.routes.length; i++){
            var network = profile.routes[i].network;
            var gateway = profile.routes[i].gateway;
            controller.serviceRequest('luna://cz.karry.vpnc',
                                               {
                                                  method: 'addRoute',	
                                                  parameters:
                                                  {
                                                    network: network,
                                                    gateway: gateway
                                                  },
                                                  onSuccess: function(){
                                                    Mojo.Log.error("error set route... " + e);   
                                                    },
                                                  onFailure: function(e){
                                                    Mojo.Log.error("error set route... " + e);   
                                                  }
                                               }
                                               );
        }
	}	
}

VpnManager.prototype.editProfile = function(profile, successHandler, errorHandler){
    var sqlArr =  [];
    index = 0;
    sqlArr[index++] = "DELETE FROM `vpn_route` WHERE `profile_name` = '"+profile.name+"'; ";
    sqlArr[index++] = "DELETE FROM `vpn_profile` WHERE `name` = '"+profile.name+"'; ";
    sqlArr[index++] =  "INSERT INTO `vpn_profile` (`name`,`type`,`host`,`user`,`password`) "
                + "VALUES ('"+profile.name+"','"+profile.type+"','"+profile.host+"','"+profile.user+"','"+profile.password+"');";
                
    if (profile.routes.length > 0){
        strSQL = "INSERT INTO `vpn_route` (`profile_name`, `network`, `gateway`) VALUES ";
        for (i = 0; i<profile.routes.length; i++){
            strSQL = strSQL + "('"+profile.name+"','"+profile.routes[i].network+"','"+profile.routes[i].gateway+"')";
            strSQL = strSQL + ((i == profile.routes.length - 1)?";":",");
        }
        sqlArr[index++] = strSQL;
    }
    
    if (!profile.state)
        profile.state = "INACTIVE";
    
    l = this.listeners;
    this.executeSQLs(sqlArr, function(a,b){
        try{
            for (i = 0; i< l.length ; i++){
                l[i](profile, null);
            }
        }catch(ex){
            Mojo.Log.error("exeption while sending notification... " + ex);            
        }
        successHandler(a,b);
    }, errorHandler);  
}

VpnManager.prototype.deleteProfile = function(profile, successHandler, errorHandler){
    var sqlArr =  [];
    index = 0;
    sqlArr[index++] = "DELETE FROM `vpn_route` WHERE `profile_name` = '"+profile.name+"'; ";
    sqlArr[index++] = "DELETE FROM `vpn_profile` WHERE `name` = '"+profile.name+"'; ";
    
    l = this.listeners;
    this.executeSQLs(sqlArr, function(a,b){
        try{
            for (i = 0; i< l.length ; i++){
                l[i]( null, profile );
            }
        }catch(ex){
            Mojo.Log.error("exeption while sending notification... " + ex);            
        }
        try{
            successHandler(a,b);
        }catch(ex){
            Mojo.Log.error("exeption while sending SQL success notification... " + ex);            
        }
    }, errorHandler);  
}
