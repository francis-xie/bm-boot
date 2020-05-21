// Robert, 2015/04/24 , add for chrome deprecation for NPAPI.

function emisUtility() {
  try {
    console.log('you are using emisUtility for plugin functions');
  } catch(e){
  }

  this.getBasePath = (function(){
    var sFullPath = window.document.location.href;
    var sUriPath = window.document.location.pathname;
    var pos = sFullPath.indexOf(sUriPath);
    var prePath = sFullPath.substring(0, pos);
    var postPath = sUriPath.substring(0, sUriPath.substr(1).indexOf('/') + 1);
    var webPath = prePath + postPath + "/";
    return webPath;
  })();
	
	//this.version="3.11"; 
	// we use defined prototype to provide 'version' property by call emisServerX.exe to get real version of emisServerX.exe
	this.getVersion=function() {
	    var version_request = { 
			"type":"version"
		};	
		
		if( this.__sendCross(version_request,2000) ) {
			return this.jsonReturn.data;
		}
		return '';
	};
	
	
	this.jsonReturn;
	
	this.copyfile=function(srcFile,destFile) {

	    var sendData = { "type":"copyfile" , "param1":srcFile, "param2":destFile };	
		return this.__send(sendData);		 
	};
	
	this.exists = function ( target ) {
	    var sendData = { "type":"exists" , "param1":target };	
		if(this.__send(sendData)) {
			if( "true"== this.jsonReturn.data ) {
				return true;
			}
		}
		return false;
	};
	
	this.deletefile = function ( target ) {
	    var sendData = { "type":"deletefile" , "param1":target };	
		return this.__send(sendData);		 
	};
	
	this.filesize = function ( target ) {
	    var sendData = { "type":"filesize" , "param1":target };	
		if( this.__send(sendData) ) {
			return this.jsonReturn.data;
		}
		return 0;
	};
	
	this.filetime = function( target ) {
	    var sendData = { "type":"filetime" , "param1":target };	
		if( this.__send(sendData)) {
			return this.jsonReturn.data;
		}
		return "";
	};
	
	this.filetimefmt = function (target,fmt) {
	    var sendData = { "type":"filetimefmt" , "param1":target,"param2":fmt };	
		if( this.__send(sendData)) {
			return this.jsonReturn.data;
		}
		return "";
	};
	
	this.selectDir=function(caption) {
	    var sendData = { "type":"selectDir" , "param1":caption};	
		if( this.__send(sendData) ) {
			return this.jsonReturn.data;
		}
		return "";
	};
	
	this.getFileList=function(select_mask) {
	    var sendData = { "type":"getFileList" , "param1":select_mask};	
		if( this.__send(sendData) ) {
			return this.jsonReturn.data;
		}
		return "";
	};
	
	this.getUser=function() {
	    var sendData = { "type":"getUser" };	
		if( this.__send(sendData) ) {
			return this.jsonReturn.data;
		}
		return "";
	};

	this.readini = function (file, section, entry ) {
	    var sendData = { 
			"type":"readini",
			"param1":file,
			"param2":section,
			"param3":entry
		};	
		if( this.__send(sendData) ) {
			return this.jsonReturn.data;
		}
		return "";
	};
	
	// return boolean
	this.writeini = function (file, section, entry, value ) {
	    var sendData = { 
			"type":"writeini", 
			"param1":file,
			"param2":section,
			"param3":entry,
			"param4":value
		};	
		return this.__send(sendData);
	};
	
	this.mkdirs = function ( dir ) {
	    var sendData = { "type":"mkdirs" , "param1":dir};	
		return this.__send(sendData);		 
	};
	
	this.writeStringToFile = function(file,value) {
	    var sendData = { 
			"type":"writeStringToFile",
			"param1":file,
			"param2":value
		};	
		return this.__send(sendData);
	};
	
	this.readFileAsString = function (file) {
	    var sendData = { 
			"type":"readFileAsString",
			"param1":file
		};	
		if( this.__send(sendData) ) {
			return this.jsonReturn.data;
		}
		return "";
	};
	
	// return the hander code, if <= 32, then some error happen..... 
	this.execute = function( executor , parameter ) {
	    var sendData = { 
			"type":"execute",
			"param1":executor,
			"param2":parameter
		};	
		if( this.__send(sendData) ) {
			return this.jsonReturn.data;
		} else {
			return -1;
		}
	};
	
	// if success ,return Empty String.
	this.executewait = function( executor , parameter ) {
	    var sendData = { 
			"type":"executewait",
			"param1":executor,
			"param2":parameter
		};	
		if(this.__send(sendData)) {
			return '';
		} else {
			return 'error';
		}
	};	
	
	this.download = function( url , localfilename ) {
		return this.downloadx(url,localfilename,'');
	};		
	
	
	this.downloadx = function( url , localfilename ,sessionId ) {
		if( sessionId == undefined ) {
			sessionId = '';
		}
	    var sendData = { 
			"type":"download",
			"param1":url,
			"param2":localfilename,
			"param3":sessionId
		};	
		return this.__send(sendData);
	};
	

	
	////////////////////////////////////////////////////////////////////
	// deprecated 
	//
	//  these are very old function that download zip from site and unzip 
	//  as xml string , and pass it to Javascript object.
	//  we should never need it now.
	//  I just keep this interface , you don't need to test it.
	/////////////////////////////////////////////////////////////////////
	
	this.xmlData;
	
	// copyURL source must be a zip format
	// copyURL and copyURLx is merged into copyURLx
	this.copyURL = function( url , extractName ) {
		return this.copyURLx(url,extractName,'');
	};		
	
	
	// copyURL source must be a zip format
	this.copyURLx = function( url , extractName ,sessionId ) {
		if( sessionId == undefined ) {
			sessionId = '';
		}
		this.xmlData='';
		
	    var sendData = { 
			"type":"copyURL",
			"param1":url,
			"param2":extractName,
			"param3":sessionId
		};	
		if( this.__send(sendData) ) {
			this.xmlData=data;
			return true;
		}
		return false
		
	};		
	
	
    this.Get_asText = function () {
		return this.xmlData;
	};

	// not used , just keep interface
    this.Set_asText=function (value) {
	};
	
	
	// actually delay we don't need a real function
	// but we do have an implement. here we send to a no response port, so it will just timeout
	this.delay = function ( seconds ) {
	    var sendData = { 
			"type":"delay",
			"param1":seconds
		};	
		this.__send(sendData,seconds * 1000);
	};
	
	this.__send=function(sendData,timeout_v) {
	
	    var tokenrequest = { 
			"type":"gettoken"
		};	
		if( this.__sendCross(tokenrequest,timeout_v) ) {
			var oneTimeToken = this.jsonReturn.data;
			
			var encryptRequest= {
				"token" : oneTimeToken
			}
			if( this.__sendLocal( this.getBasePath + 'jsp/genplugintoken.jsp',encryptRequest ) ) {
				var returnToken = this.jsonReturn.data;
				sendData.token = returnToken;
				return this.__sendCross(sendData,timeout_v);
			}
		}
		return false;
	}
	
	this.__sendCross=function(sendData,timeout_v) {
	
		this.jsonReturn = null;
 
 
		//Notice , you have to open it, otherwise , IE will not work.
		jQuery.support.cors = true; 
		
		var request = $.ajax({
		  url: "http://localhost:9999/",
		  method: "POST",
		  
	      // Notice:
		  // The W3C XMLHttpRequest specification dictates that the charset is always UTF-8
		  // to work with delphi indy, we need set this, don't put charset=utf-8 here, Delphi7 Indy don't know how to handle it.
		  // and we transfer utf8 to widestring by we self.
		  contentType: 'application/x-www-form-urlencoded',
		  
		  data: sendData,
		  //crossDomain:true,
		  //xhrFields: {'withCredentials': true},
	      //headers: { 'x-my-custom-header': 'some value' },

		  // a default 10 second timeout
		  timeout: (timeout_v==undefined) ? 10000 : timeout_v,
		  async : false,
		  /*
		  success:function(data)
          {
			alert("Data from Server:"+data);
             //alert("Data from Server"+JSON.stringify(data));
          },
		  */
      error: function (jqXHR, textStatus, errorThrown) {
        try{
          console.log("Cross Domain AJAX request error " + errorThrown);
        } catch(e){
        }
      }
		  
		});
		
		if( request.status == 200 ) {
      if (typeof JSON != "undefined") {
        this.jsonReturn = JSON.parse(request.responseText);
      } else if (typeof jQuery != "undefined") {
        this.jsonReturn = jQuery.parseJSON(request.responseText);
      } else {
        this.jsonReturn = "";
      }
			if( this.jsonReturn.result == "success") {
				return true;
			}
		}
		return false;
		
	};
	
	this.__sendLocal=function( jsp, sendData,timeout_v) {
	
		this.jsonReturn = null;
		
		var request = $.ajax({
		  url: jsp ,
		  method: "POST",
		  data: sendData,
		  async : false,
      error: function (jqXHR, textStatus, errorThrown) {
        try {
          console.log("__sendLocal AJAX request error " + errorThrown);
        } catch(e){
        }
      }
		  
		});
		
		if( request.status == 200 ) {
      if (typeof JSON != "undefined") {
        this.jsonReturn = JSON.parse(request.responseText);
      } else if (typeof jQuery != "undefined") {
        this.jsonReturn = jQuery.parseJSON(request.responseText);
      } else {
        this.jsonReturn = "";
      }
			if( this.jsonReturn.result == "success") {
				return true;
			}
		}
		return false;
		
	};	

  this.version = this.getVersion();
};

/*emisUtility.prototype = {
	get version (){
		return this.getVersion();
	}
};*/


