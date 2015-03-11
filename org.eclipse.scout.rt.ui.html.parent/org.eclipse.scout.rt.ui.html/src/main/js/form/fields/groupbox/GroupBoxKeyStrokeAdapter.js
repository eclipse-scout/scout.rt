scout.GroupBoxKeyStrokeAdapter = function(field) {
  scout.GroupBoxKeyStrokeAdapter.parent.call(this, field);
};

scout.inherits(scout.GroupBoxKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.GroupBoxKeyStrokeAdapter.prototype.registerKeyStroke = function(keyStroke) {
  this.keyStrokes.push(keyStroke);
};

scout.GroupBoxKeyStrokeAdapter.prototype.drawKeyBox = function() {
  if (this.keyBoxDrawn) {
    return;
  }

  this.keyBoxDrawn = true;
  var offsetLeft = 4;
  for (var i = 0; i < this.keyStrokes.length; i++) {
    if (!this.keyStrokes[i].drawHint) {
      continue;
    }
    if (this.keyStrokes[i].addHintOnLabel) {
      this.keyStrokes[i].addHintOnLabel();
    } else {
      var keyBoxText = scout.codesToKeys[this.keyStrokes[i].keystrokeKeyPart];
      scout.KeyStrokeUtil.drawSingleKeyBoxItem(offsetLeft, '0', this._field.$container, this.ctrl, this.alt, this.shift);
    }

  }
  //Draw Mnemonics
};

scout.GroupBoxKeyStrokeAdapter.prototype.removeKeyBox = function() {
  this.keyBoxDrawn = false;
  for (var i = 0; i < this.keyStrokes.length; i++) {
    if (this.keyStrokes[i].removeHintOnLabel) {
      this.keyStrokes[i].removeHintOnLabel();
    }
  }
  $('.key-box', this._field.$container).remove();
  $('.key-box-additional', this._field.$container).remove();
};
