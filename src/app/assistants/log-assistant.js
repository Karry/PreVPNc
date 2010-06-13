


function LogAssistant(params){
    if (params.profile)
        this.profile = params.profile;
}

LogAssistant.prototype.setup = function(){
    
    if (this.profile){
        $('log').innerHTML = this.profile.log;
        $('name').innerHTML = this.profile.name;
        $('state').innerHTML = this.profile.state;
    }
}

