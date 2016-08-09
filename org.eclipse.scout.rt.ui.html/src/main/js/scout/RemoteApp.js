scout.RemoteApp = function() {

};
scout.inherits(scout.RemoteApp, scout.App);

scout.RemoteApp.prototype._doBootstrap = function(options) {
  return [
    scout.logging.bootstrap(),
    scout.device.bootstrap(),
    scout.defaultValues.bootstrap(),
    scout.fonts.bootstrap(options.fonts)
  ];
};

/**
 * @override
 */
scout.RemoteApp.prototype._createSession = function($entryPoint, options) {
  options = options || {};
  options.remote = true;
  var session = new scout.Session($entryPoint, options);
  session.init();
  return session;
};

/**
 * @override
 */
scout.RemoteApp.prototype._init = function(options) {

  scout.Widget.prototype.createFromProperty = function(propertyName, value) {
    // Was ist das für ein Fall? Manchmal existiert das Widget schon (Menu 133 BusinessForm MainBox)
    if (value instanceof scout.Widget) {
      return value;
    }

    // Remote Case
    var remoteAdapter = findRemoteAdapter(this);
    if (remoteAdapter &&                           // Wenn das widget (oder ein parent davon) ein remote adapter hat, sind auch die properties remotable
        this._isPropertyRemotable(propertyName)) { // True, wenn es für diese managed property einen Remote Adapter gibt
      return this.session.getOrCreateWidget(value, remoteAdapter, this); // value ist ein String, enthält remote object ID
    }

    // Default: Local-Fall
    value.parent = this;
    return scout.create(value);

    function findRemoteAdapter(widget) {
      while (widget) {
        if (widget.remoteAdapter) {
          return widget.remoteAdapter;
        }
        widget = widget.parent;
      }
      return null;
    }
  };

  scout.RemoteApp.parent.prototype._init.call(this, options);
};

