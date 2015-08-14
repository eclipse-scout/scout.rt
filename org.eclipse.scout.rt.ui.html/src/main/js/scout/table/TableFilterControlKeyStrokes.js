scout.TableFilterControlKeyStrokes = function(field) {
  scout.TableFilterControlKeyStrokes.parent.call(this);
  this.drawHint = true;
  this._field = field;
  this.initKeyStrokeParts();
};
scout.inherits(scout.TableFilterControlKeyStrokes, scout.KeyStroke);
/**
 * @Override scout.KeyStroke
 */
scout.TableFilterControlKeyStrokes.prototype.handle = function(event) {
  // set focus
  var $input = $('.control-filter', this._field.$container);
  //TODO nbu check if keyStroke should be registered when no filter control is available.
  if ($input[0]) {
    $input.focus();
    var length = scout.helpers.nvl($input.val(), '').length;
    $input[0].setSelectionRange(length, length);
  }
};
/**
 * @Override scout.KeyStroke
 */
scout.TableFilterControlKeyStrokes.prototype._drawKeyBox = function($container, drawedKeys) {
  var activeElement = document.activeElement;
  var elementType = activeElement.tagName.toLowerCase();
  var $filterinput = $('.control-filter', this._field.$container);
  if ((elementType === 'textarea' || elementType === 'input') && $filterinput[0] !== activeElement) {
    return;
  }
  if ($filterinput.length && !scout.keyStrokeBox.keyStrokesAlreadyDrawn(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.A, scout.keys.Z) && !scout.keyStrokeBox.keyStrokesAlreadyDrawn(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys[0], scout.keys[9])) {
    var filterInputPosition = $filterinput.position();
    var top = $filterinput.css('margin-top').replace("px", "");
    var left = filterInputPosition.left + parseInt($filterinput.css('margin-left').replace("px", ""), 0) + 4;
    $filterinput.beforeDiv('key-box char', 'a - z').css('left', left + 'px');
    scout.keyStrokeBox.keyStrokeRangeDrawn(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys[0], scout.keys[9]);
    scout.keyStrokeBox.keyStrokeRangeDrawn(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.A, scout.keys.Z);
  }
};

/**
 * @Override scout.KeyStroke
 */
scout.TableFilterControlKeyStrokes.prototype.checkAndDrawKeyBox = function($container, drawedKeys) {
  this._drawKeyBox($container, drawedKeys);
};
/**
 * @Override scout.KeyStroke
 */
scout.TableFilterControlKeyStrokes.prototype.accept = function(event) {
  var elementType = document.activeElement.tagName.toLowerCase();

  if (document.activeElement.className !== 'control-filter' &&(elementType === 'textarea' || elementType === 'input')) {
    return false;
  }

  return event && ((event.which >= 65 && event.which <= 90) || (event.which >= 48 && event.which <= 57)) && // a-z
  event.ctrlKey === this.ctrl && event.altKey === this.alt && event.shiftKey === this.shift;
};
