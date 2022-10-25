    var fnKeyPressed = function(){
          handleError("Sorry! Function Keys are disabled in this page.");
    }
    var ctrlKeyPressed = function(){
      if(BrowserDetect.browser=="Explorer" || BrowserDetect.browser=="Safari" ){
          alert("You can't press 'Shift', 'Ctrl' or 'Alt' keys in this page.");
      }else{
          handleError("Sorry! You can't press 'Shift', 'Ctrl' or 'Alt' keys in this page.");
       }
    }
    var splKeyPressed = function(){
      handleError("Sorry! You can't press 'Shift', 'Ctrl' or 'Alt' keys in this page.");
    }
    shortcut.add("F1",fnKeyPressed);
    shortcut.add("F2",fnKeyPressed);
    shortcut.add("F3",fnKeyPressed);
    shortcut.add("F4",fnKeyPressed);
    shortcut.add("F5",fnKeyPressed);
    shortcut.add("F6",fnKeyPressed);
    shortcut.add("F7",fnKeyPressed);
    shortcut.add("F8",fnKeyPressed);
    shortcut.add("F9",fnKeyPressed);
    shortcut.add("F10",fnKeyPressed);
    shortcut.add("F11",fnKeyPressed);
    shortcut.add("F12",fnKeyPressed);
    shortcut.add("Ctrl",ctrlKeyPressed);
    shortcut.add("Shift",splKeyPressed);
    shortcut.add("Alt",splKeyPressed);
    shortcut.add("meta",splKeyPressed);

    var BrowserDetect = {
      init: function () {
        this.browser = this.searchString(this.dataBrowser) || "An unknown browser";
        this.version = this.searchVersion(navigator.userAgent)
          || this.searchVersion(navigator.appVersion)
          || "an unknown version";
        this.OS = this.searchString(this.dataOS) || "an unknown OS";
      },
      searchString: function (data) {
        for (var i=0;i<data.length;i++) {
          var dataString = data[i].string;
          var dataProp = data[i].prop;
          this.versionSearchString = data[i].versionSearch || data[i].identity;
          if (dataString) {
            if (dataString.indexOf(data[i].subString) != -1)
              return data[i].identity;
          }
          else if (dataProp)
            return data[i].identity;
        }
      },
      searchVersion: function (dataString) {
        var index = dataString.indexOf(this.versionSearchString);
        if (index == -1) return;
        return parseFloat(dataString.substring(index+this.versionSearchString.length+1));
      },
      dataBrowser: [
        {
          string: navigator.userAgent,
          subString: "Chrome",
          identity: "Chrome"
        },
        {   string: navigator.userAgent,
          subString: "OmniWeb",
          versionSearch: "OmniWeb/",
          identity: "OmniWeb"
        },
        {
          string: navigator.vendor,
          subString: "Apple",
          identity: "Safari",
          versionSearch: "Version"
        },
        {
          prop: window.opera,
          identity: "Opera",
          versionSearch: "Version"
        },
        {
          string: navigator.vendor,
          subString: "iCab",
          identity: "iCab"
        },
        {
          string: navigator.vendor,
          subString: "KDE",
          identity: "Konqueror"
        },
        {
          string: navigator.userAgent,
          subString: "Firefox",
          identity: "Firefox"
        },
        {
          string: navigator.vendor,
          subString: "Camino",
          identity: "Camino"
        },
        {   // for newer Netscapes (6+)
          string: navigator.userAgent,
          subString: "Netscape",
          identity: "Netscape"
        },
        {
          string: navigator.userAgent,
          subString: "MSIE",
          identity: "Explorer",
          versionSearch: "MSIE"
        },
        {
          string: navigator.userAgent,
          subString: "Gecko",
          identity: "Mozilla",
          versionSearch: "rv"
        },
        {     // for older Netscapes (4-)
          string: navigator.userAgent,
          subString: "Mozilla",
          identity: "Netscape",
          versionSearch: "Mozilla"
        }
      ],
      dataOS : [
        {
          string: navigator.platform,
          subString: "Win",
          identity: "Windows"
        },
        {
          string: navigator.platform,
          subString: "Mac",
          identity: "Mac"
        },
        {
             string: navigator.userAgent,
             subString: "iPhone",
             identity: "iPhone/iPod"
        },
        {
          string: navigator.platform,
          subString: "Linux",
          identity: "Linux"
        }
      ]
    };
    BrowserDetect.init();
    
    document.oncontextmenu=RightMouseDown;
    document.onmousedown = mouseDown; 

      $(document).bind("taphold", function (e) {      
          if(BrowserDetect.browser=="Safari"){
              e.preventDefault();
              handleError("Sorry! Long tap is disabled in this page!");
              return false;            
          }
      });
      
     $('titleContainer').ontouchmove = function(e) {
      if(BrowserDetect.browser=="Safari"){
        e.preventDefault();
      }
     }

    function mouseDown(e) {
      if (e.which==3) {//rightClick
        handleError("Sorry! Right click is disabled in this page!");    
      }
    }
    
    function RightMouseDown() { 
       handleError("Sorry! Right click is disabled in this page!");
      return false;
    }    