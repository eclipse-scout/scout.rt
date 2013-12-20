(function(){

  if( rwt.client.Client.isMobileSafari() ) {
    var Style = rwt.html.Style;

    var setOverflowScrolling = function( target, value ) {
      var property = Style.BROWSER_PREFIX + "overflow-scrolling";
      Style.setStyleProperty( target, property, value );
    };

    // patch iframe
    rwt.qx.Class.__initializeClass( rwt.widgets.base.Iframe);
    var iframeProto = rwt.widgets.base.Iframe.prototype;
    var originalIframeFunc = iframeProto._applyElement;
    iframeProto._applyElement = function() {
      originalIframeFunc.apply(this, arguments);
        arguments[0].style.overflow="auto";
    }

    // patch scrollable
    rwt.qx.Class.__initializeClass( rwt.widgets.base.Scrollable );
    var proto = rwt.widgets.base.Scrollable.prototype;
    var orgFunc = proto._configureClientArea;
    proto._configureClientArea = function() {
      orgFunc.apply( this, arguments );
      setOverflowScrolling( this._clientArea, "touch" );
    };
    var orgOnScroll = proto._onscroll;
    var mouseTimer = new rwt.client.Timer( 200 );
    proto._onscroll = function() {
      orgOnScroll.apply( this, arguments );
      // give empty object as original event, otherwise faking mouse events won't work
      rwt.runtime.MobileWebkitSupport._disableMouse( {} );
      mouseTimer.restart(); // re-enable mouse in 200ms
    };
    mouseTimer.addEventListener( "interval", function() {
      rwt.runtime.MobileWebkitSupport._enableMouse( {} );
      mouseTimer.stop();
    } );

    // fix render glitches:
    var style = document.createElement( "style");
    style.type = 'text/css';
    style.innerHTML = "*:not(html) { -webkit-transform: translate3d(0, 0, 0); }";
    document.getElementsByTagName( "head")[ 0 ].appendChild( style );

  }

}());
