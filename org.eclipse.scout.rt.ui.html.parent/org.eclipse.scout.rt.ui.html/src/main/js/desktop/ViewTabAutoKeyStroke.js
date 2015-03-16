scout.ViewTabAutoKeyStroke = function(enabled, tabs, keyStroke) {
  scout.ViewTabAutoKeyStroke.parent.call(this);
  this._enabled = enabled;
  this._tabs = tabs;
  this.keyStroke = keyStroke;
  this.initKeyStrokeParts();
  this.keyBoxDrawed = false;
  this.drawHint=true;
};
scout.inherits(scout.ViewTabAutoKeyStroke, scout.KeyStroke);

/**
 * @Override scout.KeyStroke
 */
scout.ViewTabAutoKeyStroke.prototype.handle = function(event) {
  if (this._tabs.length === 0 || (event.which !== 57 && event.which !== 48 && event.which - 48 > this._tabs.length)) {
    return;
  }
  if (event.which === 57) {
    //if 9 is hit go to last view tab
    this._tabs[this._tabs.length - 1].$container.trigger('click');
  } else if (event.which === 48) {
    //if 0 is hit go to first view tab
    this._tabs[0].$container.trigger('click');
  } else {
    this._tabs[event.which - 49].$container.trigger('click');
  }
  event.preventDefault();
};

/**
 * @Override scout.KeyStroke
 */
scout.ViewTabAutoKeyStroke.prototype.accept = function(event) {
  if (this._enabled && event && event.which >= 48 && event.which <= 57 && // 0-9
    event.ctrlKey === this.ctrl && event.altKey === this.alt && event.shiftKey === this.shift) {
    return true;
  }
  return false;
};
/**
 * @Override scout.KeyStroke
 */
scout.ViewTabAutoKeyStroke.prototype.checkAndDrawKeyBox = function($container, drawedKeys){
  if(scout.keyStrokeBox.keyStrokesAlreadyDrawn(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys[0], scout.keys[9])){
    return;
  }
  if(this.drawHint){
    this._drawKeyBox($container);
    drawedKeys[this.keyStrokeName()]=true;
    scout.keyStrokeBox.keyStrokeRangeDrawn(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys[0], scout.keys[9]);
  }
};
/**
 * @Override scout.KeyStroke
 */
scout.ViewTabAutoKeyStroke.prototype._drawKeyBox = function($container) {
  if (this.keyBoxDrawed) {
    return;
  }
  if (this._enabled && this._tabs) {
    for (var i = 0; i < this._tabs.length; i++) {
      //TODO nbu fix bug->table is not first tab but displayed as first.
      var offsetLeft = 4,
        firstField = true;
      if (i === 0) {
        scout.keyStrokeBox.drawSingleKeyBoxItem(offsetLeft, '0', this._tabs[i].$container, this.ctrl, this.alt, this.shift);
        firstField = false;
      }
      if (i < 8) {
        scout.keyStrokeBox.drawSingleKeyBoxItem(offsetLeft, i + 1, this._tabs[i].$container,this.ctrl, this.alt, this.shift);
        firstField = false;
      }
      if (i + 1 === this._tabs.length) {
        scout.keyStrokeBox.drawSingleKeyBoxItem(offsetLeft, 9, this._tabs[i].$container,  this.ctrl, this.alt, this.shift);
        firstField = false;
      }
    }
    this.keyBoxDrawed = true;
  }
};
/**
 * @Override scout.KeyStroke
 */
scout.ViewTabAutoKeyStroke.prototype.removeKeyBox = function() {
  if (!this.keyBoxDrawed) {
    return;
  }
  for (var i = 0; i < this._tabs.length; i++) {
    $('.key-box', this._tabs[i].$container).remove();
    $('.key-box-additional', this._tabs[i].$container).remove();
  }
  this.keyBoxDrawed = false;
};
