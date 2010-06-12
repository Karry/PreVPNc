

function VpnManager(){
    this.dbConnect();
}

VpnManager.instance = null;

VpnManager.getInstance = function(){
    if (!this.instance)
        this.instance = new VpnManager();
    return this.instance;
};

VpnManager.prototype.dbConnect = function(){
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
    this.executeSQL(strSQL,function(){},function(){});     
    var strSQL =  "CREATE TABLE IF NOT EXISTS `vpn_routes` ("
                + "`profile_name` VARCHAR(255) NOT NULL ,"
                + "`network` VARCHAR(255) NOT NULL ,"
                + "`gateway` VARCHAR(255) NOT NULL,"
                + " PRIMARY KEY (`profile_name`,`network`)"
                + ");";
    this.executeSQL(strSQL,function(){},function(){});     
}


VpnManager.prototype.executeSQL = function(strSQL, resultHandler, errorHandler){
    this.db.transaction(
        (
            function (transaction){
                transaction.executeSql(strSQL, [], resultHandler, errorHandler);
            }
        ).bind(this) 
    );    
}

VpnManager.prototype.loadProfiles = function( trackHandler , tableErrorHandler){
    this.executeSQL("SELECT vpn_profile.*, 'UNDEFINED' AS `state` FROM vpn_profile",
                    function(transaction, result){
                        if (result.rows){
                            for (i = 0; i< result.rows.length; i++){
                                item = result.rows.item(i);
                                this.refreshProfileInfo(item,trackHandler, tableErrorHandler);
                            }
                        }else{
                            tableErrorHandler({message:"bad db result ", code:-1});
                        }                        
                    },
                    tableErrorHandler);
}

VpnManager.prototype.refreshProfileInfo = function(profile, upatedProfileHandler, errorHandler){
    this.controller.serviceRequest('luna://cz.karry.vpnc',
		                               {
		                                  method: 'connectionInfo',	
		                                  parameters:
										  {
											name: item.name
		                                  },
		                                  onSuccess: function(result){
                                                item.state = result.state;
                                                item.log = result.log;
                                                item.localAddress = result.localAddress
                                                upatedProfileHandler( item );
                                            },
		                                  onFailure: errorHandler
	                                   }
									   );    
}

VpnManager.prototype.editProfile = function(profile, successHandler, errorHandler){
    // TODO: add some code here
}
