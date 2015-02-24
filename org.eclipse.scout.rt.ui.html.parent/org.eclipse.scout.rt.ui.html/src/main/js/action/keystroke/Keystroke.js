scout.Keystroke = function() {
  scout.Keystroke.parent.call(this);
  this.keystroke;
  this.keystrokeKeyPart;
  this.ctrl = false;
  this.alt = false;
  this.shift = false;
  this.meta = false;
};
scout.inherits(scout.Keystroke, scout.Action);

scout.Keystroke.prototype._remove = function() {
  scout.Action.parent.prototype._remove.call(this);
};

/**
 * If processing should continue return true otherwise return false.
 * Default: do not bubble up-> false
 */
scout.Keystroke.prototype.handle = function(event) {
  this.sendDoAction();
  return false;
};

scout.Keystroke.prototype.accept = function(event) {
  if (this.ignore()) {
    return false;
  }
  if (event && event.ctrlKey === this.ctrl && event.altKey === this.alt && event.metaKey === this.meta && event.altKey === this.shift && event.which === this.keystrokeKeyPart) {
    return true;
  }
  return false;
};

scout.Keystroke.prototype.ignore = function() {
  return false;
};

scout.Keystroke._syncKeystroke = function(keystroke) {
  // When model's 'keystroke' property changes, also update keystroke parts
  this.keystroke = keystroke;
  this.initKeystrokeParts();
};

scout.Keystroke.prototype.initKeystrokeParts = function() {
  this.alt = undefined;
  this.ctrl = undefined;
  this.meta = undefined;
  this.shift = undefined;
  this.keystrokeKeyPart = undefined;
  if (!this.keystroke) {
    return;
  }
  var keystrokeParts = this.keystroke.split('-');
  for (var i = 0; i < keystrokeParts.length; i++) {
    var part = keystrokeParts[i];
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
      this.keystrokeKeyPart = scout.keys[part.toUpperCase()];
    }
  }
};
