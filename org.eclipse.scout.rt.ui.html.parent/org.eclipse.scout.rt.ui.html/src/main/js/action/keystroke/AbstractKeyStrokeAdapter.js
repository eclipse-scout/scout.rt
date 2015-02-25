scout.AbstractKeyStrokeAdapter = function(field) {
  this.$target = undefined; // set by KeystrokeManager
  this.controller = undefined; // set by KeystrokeManager
  this.keyStrokes = [];
  this._field = field;
  this.installModelKeystrokes();
  this.keyBoxDrawn = false;
  this.marginLeft = 50;
  this.additionalMarginLeft = 28;
};

scout.AbstractKeyStrokeAdapter.prototype.drawKeyBox = function() {
  var additionalKeyBox, altKeybox, shiftKeyBox, keyBox, metaKeyBox;
  if (this.keyBoxDrawn) {
    return;
  }
  this.keyBoxDrawn = true;
  var offset = 0;
  for (var i = 0; i < this.keyStrokes.length; i++) {
    if (!this.keyStrokes[i].drawHint) {
      continue;
    }
    var keyBoxText = scout.codesToKeys[this.keyStrokes[i].keystrokeKeyPart];

    var $boxToAlignKeyBox = this._field.$container;
    if (offset > 0) {
      $boxToAlignKeyBox.prependDiv('key-box-additional', ';');
      additionalKeyBox.css('left', '' + this._calcKeyboxSeparator(offset) + 'px');
    }
    if ($boxToAlignKeyBox && keyBoxText) {
      if (this.keyStrokes[i].ctrl) {
        $boxToAlignKeyBox.prependDiv('key-box ', 'ctrl');
        $boxToAlignKeyBox.prependDiv('key-box-additional', '+');
        offset++;
      }
      if (this.keyStrokes[i].alt) {
        altKeybox = $boxToAlignKeyBox.prependDiv('key-box ', 'alt');
        additionalKeyBox = $boxToAlignKeyBox.prependDiv('key-box-additional ', '+');
        if (offset > 0) {
          altKeybox.css('left', '' + this._calcKeybox(offset) + 'px');
          additionalKeyBox.css('left', '' + this._calcKeyboxSeparator(offset) + 'px');
        }
        offset++;
      }
      if (this.keyStrokes[i].shift) {
        shiftKeyBox = $boxToAlignKeyBox.prependDiv('key-box ', 'shift');
        additionalKeyBox = $boxToAlignKeyBox.prependDiv('key-box-additional ', '+');
        if (offset > 0) {
          shiftKeyBox.css('left', '' + this._calcKeybox(offset) + 'px');
          additionalKeyBox.css('left', '' + this._calcKeyboxSeparator(offset) + 'px');
        }
        offset++;
      }
      if (this.keyStrokes[i].meta) {
        metaKeyBox = $boxToAlignKeyBox.prependDiv('key-box ', 'meta');
        additionalKeyBox = $boxToAlignKeyBox.prependDiv('key-box-additional ', '+');
        if (offset > 0) {
          metaKeyBox.css('left', '' + this._calcKeybox(offset) + 'px');
          additionalKeyBox.css('left', '' + this._calcKeyboxSeparator(offset) + 'px');
        }
        offset++;
      }
      keyBox = $boxToAlignKeyBox.prependDiv('key-box ', keyBoxText);
      if (offset > 0) {
        keyBox.css('left', '' + this._calcKeybox(offset) + 'px');
      }
    }
  }
};

scout.AbstractKeyStrokeAdapter.prototype._calcKeyboxSeparator = function(offset) {
  return this.marginLeft * offset + this.additionalMarginLeft;
};

scout.AbstractKeyStrokeAdapter.prototype._calcKeybox = function(offset) {
  return this.marginLeft * offset;
};

scout.AbstractKeyStrokeAdapter.prototype.removeKeyBox = function() {
  this.keyBoxDrawn = false;
  $('.key-box', this._field.$container).remove();
  $('.key-box-additional', this._field.$container).remove();
};

scout.AbstractKeyStrokeAdapter.prototype.installModelKeystrokes = function() {
  if (this.keyStrokes.length > 0) {
    this.keyStrokes = this.keyStrokes.concat(this._field.keyStrokes);
  } else if (this._field.keyStrokes) {
    this.keyStrokes = this._field.keyStrokes;
  }
};
