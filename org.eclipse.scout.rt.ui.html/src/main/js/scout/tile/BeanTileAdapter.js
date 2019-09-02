scout.BeanTileAdapter = function() {
  scout.BeanTileAdapter.parent.call(this);
};
scout.inherits(scout.BeanTileAdapter, scout.TileAdapter);

scout.BeanTileAdapter.prototype._onWidgetAppLinkAction = function(event) {
  this._send('appLinkAction', {
    ref: event.ref
  });
};

scout.BeanTileAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'appLinkAction') {
    this._onWidgetAppLinkAction(event);
  } else {
    scout.BeanTileAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};
