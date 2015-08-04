scout.StringFieldKeyStrokeAdapter = function(stringField) {
  scout.StringFieldKeyStrokeAdapter.parent.call(this, stringField);
  this.registerKeyStroke(new scout.StringFieldEnterKeyStroke());
  this.registerKeyStroke(new scout.StringFieldCtrlEnterKeyStroke(stringField));
};
scout.inherits(scout.StringFieldKeyStrokeAdapter, scout.FormFieldKeyStrokeAdapter);

scout.StringFieldKeyStrokeAdapter.prototype.drawKeyBox = function(drawedKeys) {
  scout.StringFieldKeyStrokeAdapter.parent.prototype.drawKeyBox.call(this, drawedKeys);
};

scout.StringFieldKeyStrokeAdapter.prototype.preventBubbleUp = function(event) {
  if (this.preventBubbleUpKeys.indexOf(event.which) > -1) {
    return true;
  }
  return false;
};

scout.StringFieldEnterKeyStroke = function() {
  scout.StringFieldEnterKeyStroke.parent.call(this);
  this.keyStroke = 'ENTER';
  this.drawHint = false;
  this.initKeyStrokeParts();
};
scout.inherits(scout.StringFieldEnterKeyStroke, scout.KeyStroke);

scout.StringFieldEnterKeyStroke.prototype.handle = function(event) {};

scout.StringFieldEnterKeyStroke.prototype.accept = function(event) {
  var acceptKey = scout.StringFieldEnterKeyStroke.parent.prototype.accept.call(this, event);
  var elementType = document.activeElement.tagName.toLowerCase();
  if (acceptKey && elementType === 'textarea') {
    return true;
  }
  return false;
};

scout.StringFieldEnterKeyStroke.prototype.checkAndDrawKeyBox = function($container, drawedKeys) {
  if (drawedKeys[this.keyStrokeName()]) {
    return;
  }
  var elementType = document.activeElement.tagName.toLowerCase();
  if (this.drawHint) {
    this._drawKeyBox($container);
  }
  if (elementType === 'textarea') {
    drawedKeys[this.keyStrokeName()] = true;
  }
};

scout.StringFieldCtrlEnterKeyStroke = function(field) {
  scout.StringFieldCtrlEnterKeyStroke.parent.call(this);
  this.keyStroke = 'control-enter';
  this._field = field;
  this.initKeyStrokeParts();
};
scout.inherits(scout.StringFieldCtrlEnterKeyStroke, scout.KeyStroke);

scout.StringFieldCtrlEnterKeyStroke.prototype.accept = function(event) {
  var acceptKey = scout.StringFieldCtrlEnterKeyStroke.parent.prototype.accept.call(this, event);
  if (acceptKey && this._field.hasAction) {
    return true;
  }
  return false;
};

scout.StringFieldCtrlEnterKeyStroke.prototype.checkAndDrawKeyBox = function($container, drawedKeys) {
  if (this._field.hasAction) {
    scout.StringFieldCtrlEnterKeyStroke.parent.prototype.checkAndDrawKeyBox.call(this, $container, drawedKeys);
  }
};

scout.StringFieldCtrlEnterKeyStroke.prototype.handle = function(event) {
  this._field._onIconClick();
};
