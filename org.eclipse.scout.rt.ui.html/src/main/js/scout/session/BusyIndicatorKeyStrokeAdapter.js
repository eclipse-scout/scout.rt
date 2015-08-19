scout.BusyIndicatorKeyStrokeAdapter = function(busyBox) {
  scout.BusyIndicatorKeyStrokeAdapter.parent.call(this, busyBox);
  this.registerKeyStroke(new scout.BusyIndicatorExecuteButtonKeyStroke('enter'));
  this.registerKeyStroke(new scout.BusyIndicatorExecuteButtonKeyStroke('space'));
};
scout.inherits(scout.BusyIndicatorKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);


scout.BusyIndicatorExecuteButtonKeyStroke = function(keyStroke) {
  scout.BusyIndicatorExecuteButtonKeyStroke.parent.call(this);
  this.keyStroke = keyStroke;
  this.drawHint = false;
  this.initKeyStrokeParts();
};
scout.inherits(scout.BusyIndicatorExecuteButtonKeyStroke, scout.KeyStroke);

scout.BusyIndicatorExecuteButtonKeyStroke.prototype.handle = function(event) {
  var activeElement = document.activeElement;
  if (activeElement) {
    $(activeElement).trigger({ type: 'click', which: 1 });
  }
};
