scout.KeyStroke = function() {
  scout.KeyStroke.parent.call(this);
  this.keyStroke;
  this.keyStrokeKeyPart;
  this.ctrl = false;
  this.alt = false;
  this.shift = false;
  this.meta = false;
};
scout.inherits(scout.KeyStroke, scout.Action);

scout.KeyStroke.prototype._remove = function() {
  scout.Action.parent.prototype._remove.call(this);
};

/**
 * If processing should continue return true otherwise return false.
 * Default: do not bubble up-> false
 */
scout.KeyStroke.prototype.handle = function(event) {
  this.sendDoAction();
  return false;
};

scout.KeyStroke.prototype.accept = function(event) {
  if (this.ignore()) {
    return false;
  }
  if (event && event.ctrlKey === this.ctrl && event.altKey === this.alt && event.metaKey === this.meta && event.altKey === this.shift && event.which === this.keyStrokeKeyPart) {
    return true;
  }
  return false;
};

scout.KeyStroke.prototype.ignore = function() {
  return false;
};

scout.KeyStroke._syncKeyStroke = function(keyStroke) {
  // When model's 'keystroke' property changes, also update keystroke parts
  this.keyStroke = keyStroke;
  this.initKeyStrokeParts();
};

scout.KeyStroke.prototype.initKeyStrokeParts = function() {
  this.alt = undefined;
  this.ctrl = undefined;
  this.meta = undefined;
  this.shift = undefined;
  this.keyStrokeKeyPart = undefined;
  if (!this.keyStroke) {
    return;
  }
  var keyStrokeParts = this.keyStroke.split('-');
  for (var i = 0; i < keyStrokeParts.length; i++) {
    var part = keyStrokeParts[i];
    // see org.eclipse.scout.rt.client.ui.action.keystroke.KeyStrokeNormalizer
    if (part === 'alternate') {
      this.alt = true;
    } else if (part === 'control') {
      this.ctrl = true;
    } else if (part === 'meta') {
      this.meta = true;
    } else if (part === 'shift') {
      this.shift = true;
    } else {
      this.keyStrokeKeyPart = scout.keys[part.toUpperCase()];
    }
  }
};
