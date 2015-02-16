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

scout.Keystroke.prototype.init = function(model, session) {
  scout.FormField.parent.prototype.init.call(this, model, session);
  this.initKeystrokeParts();
};

scout.Keystroke.prototype._remove = function() {
  scout.Action.parent.prototype._remove.call(this);
};

scout.Keystroke.KEY_SEPARATOR = '-';

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

scout.Keystroke.prototype.keystrokeString = function(keystroke) {
  if (keystroke) {
    this.keystroke = keystroke;
    this.initKeystrokeParts();
  }
  return this.keystroke;
};

scout.Keystroke.drawKeyBox = function() {
  var keyBoxText = scout.codesToKeys[this.keystrokeKeyPart];
  //TODO nbu draw key box
};

/**
 * @see keys.js -> to determine key mapping
 */
scout.Keystroke.prototype.initKeystrokeParts = function() {
  if (!this.keystroke) {
    throw new Error('Can\'t separate keys from keystroke. keystroke is not set.');
  }
  var keystrokeParts = this.keystroke.split(scout.Keystroke.KEY_SEPARATOR);
  for (var i = 0; i < keystrokeParts.length; i++) {
    if (keystrokeParts[i] === 'alternate') {
      this.alt = true;
    } else if (keystrokeParts[i] === 'control') {
      this.ctrl = true;
    } else if (keystrokeParts[i] === 'meta') {
      this.meta = true;
    } else if (keystrokeParts[i] === 'shift') {
      this.shift = true;
    } else {
      this.keystrokeKeyPart = scout.keys[keystrokeParts[i].toUpperCase()];
    }
  }
};
