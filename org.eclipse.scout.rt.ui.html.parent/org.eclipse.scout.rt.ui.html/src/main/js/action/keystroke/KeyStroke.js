scout.KeyStroke = function() {
  scout.KeyStroke.parent.call(this);
  this.keyStroke;
  this.keyStrokeKeyPart;
  this.ctrl = false;
  this.alt = false;
  this.shift = false;
  this.bubbleUp=false;
  this.drawHint=false;
};
scout.inherits(scout.KeyStroke, scout.Action);

scout.KeyStroke.prototype.init = function(model, session) {
  scout.KeyStroke.parent.prototype.init.call(this, model, session);
  this.initKeyStrokeParts();
};

scout.KeyStroke.prototype._remove = function() {
  scout.KeyStroke.parent.prototype._remove.call(this);
};

scout.KeyStroke.prototype.handle = function(event) {
  this.sendDoAction();
};

scout.KeyStroke.prototype.accept = function(event) {
  if (this.ignore(event)) {
    return false;
  }
  if (event && event.ctrlKey === this.ctrl && event.altKey === this.alt && event.shiftKey === this.shift && event.which === this.keyStrokeKeyPart) {
    return true;
  }
  return false;
};

scout.KeyStroke.prototype.ignore = function(event) {
  return false;
};

scout.KeyStroke._syncKeyStroke = function(keyStroke) {
  // When model's 'keystroke' property changes, also update keystroke parts
  this.keyStroke = keyStroke;
  this.initKeyStrokeParts();
};

scout.KeyStroke.prototype.initKeyStrokeParts = function() {
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
    } else if (part === 'shift') {
      this.shift = true;
    } else {
      this.keyStrokeKeyPart = scout.keys[part.toUpperCase()];
    }
  }
};

scout.KeyStroke.prototype.checkAndDrawKeyBox = function($container, drawedKeys){
  if(drawedKeys[this.keyStrokeName()]){
    return;
  }
  if(this.drawHint){
    this._drawKeyBox($container);
    drawedKeys[this.keyStrokeName()]=true;
  }
};

scout.KeyStroke.prototype._drawKeyBox = function($container){
  if (!this.drawHint) {
    return;
  }
  var keyBoxText = scout.codesToKeys[this.keystrokeKeyPart];
  scout.keyStrokeBox.drawSingleKeyBoxItem(4, keyBoxText, $container, this.ctrl, this.alt, this.shift);
};

scout.KeyStroke.prototype.removeKeyBox = function($container){
  $('.key-box', $container).remove();
  $('.key-box-additional', $container).remove();
};

scout.KeyStroke.prototype.keyStrokeName = function(){
  var name = this.ctrl ? 'ctrl+':'';
  name += this.alt ? 'alt+':'' ;
  name += this.shift ? 'shift+':'';
  return name + this.keyStrokeKeyPart;
};
