(function(){

  if( rwt.client.Client.isMobileSafari() ) {
    var Style = rwt.html.Style;

    var setOverflowScrolling = function( target, value ) {
      var property = Style.BROWSER_PREFIX + "overflow-scrolling";
      Style.setStyleProperty( target, property, value );
    };

    // patch scrollable
    rwt.qx.Class.__initializeClass( rwt.widgets.base.Scrollable );
    var proto = rwt.widgets.base.Scrollable.prototype;
    var orgFunc = proto._configureClientArea;
    proto._configureClientArea = function() {
      orgFunc.apply( this, arguments );
      setOverflowScrolling( this._clientArea, "touch" );
    };

    // fix render render gitches:
    var style = document.createElement( "style");
    style.type = 'text/css';
    style.innerHTML = "*:not(html) { -webkit-transform: translate3d(0, 0, 0); }";
    document.getElementsByTagName( "head")[ 0 ].appendChild( style );

  }

}());
