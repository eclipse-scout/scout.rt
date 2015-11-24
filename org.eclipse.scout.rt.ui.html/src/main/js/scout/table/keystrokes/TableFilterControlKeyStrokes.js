/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
  var $input = $('.table-text-filter', this._field.$container);
  //TODO nbu check if keyStroke should be registered when no filter control is available.
  if ($input[0]) {
    $input.focus();
    var length = scout.nvl($input.val(), '').length;
    $input[0].setSelectionRange(length, length);
  }
};
/**
 * @Override scout.KeyStroke
 */
scout.TableFilterControlKeyStrokes.prototype._drawKeyBox = function($container, drawedKeys) {
  var activeElement = this._field.$container.activeElement(true),
    elementType = activeElement.tagName.toLowerCase(),
    $filterinput = $('.table-text-filter', this._field.$container);
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
  var activeElement = this._field.$container.activeElement(true),
    elementType = activeElement.tagName.toLowerCase();

  if (activeElement.className !== 'table-text-filter' && (elementType === 'textarea' || elementType === 'input')) {
    return false;
  }

  return event && ((event.which >= 65 && event.which <= 90) || (event.which >= 48 && event.which <= 57)) && // a-z
    event.ctrlKey === this.ctrl && event.altKey === this.alt && event.shiftKey === this.shift;
};
