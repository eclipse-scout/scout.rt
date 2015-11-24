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
/**
 * Keystroke to move the cursor into field field to table footer.
 *
 * Hint: This keystroke is not implemented as RangeKeyStroke.js because:
 *       a) the accepted keys are not rendered on F1, but a condensed 'a-z' instead;
 *       b) there is no need to evaluate a concrete key's propagation status when being rendered (because of (a))
 *
 */
scout.TableFocusFilterFieldKeyStroke = function(table) {
  scout.TableFocusFilterFieldKeyStroke.parent.call(this);
  this.field = table;

  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return event._$filterInput;
  }.bind(this);

  this.virtualKeyStrokeWhich = 'a-Z;a-z;0-9';
  this.preventDefault = false; // false so that the key is inserted into the search field.
  this.keyStrokeMode = scout.keyStrokeMode.DOWN;
};
scout.inherits(scout.TableFocusFilterFieldKeyStroke, scout.KeyStroke);

/**
 * @override KeyStroke.js
 */
scout.TableFocusFilterFieldKeyStroke.prototype._accept = function(event) {
  if (!this._isKeyStrokeInRange(event)) {
    return false;
  }

  var $filterInput = $('.table-text-filter', this.field.$container);
  if (!$filterInput.length) {
    return false;
  }

  var activeElement = this.field.$container.activeElement(true),
    activeElementType = activeElement.tagName.toLowerCase(),
    focusOnInputField = (activeElementType === 'textarea' || activeElementType === 'input');
  if (activeElement.className === 'table-text-filter' || !focusOnInputField) {
    event._$filterInput = $filterInput;
    this._isKeyStrokeInRange(event);
    return true;
  } else {
    return false;
  }
};

/**
 * @override KeyStroke.js
 */
scout.TableFocusFilterFieldKeyStroke.prototype.handle = function(event) {
  var $filterInput = event._$filterInput;

  // Focus the field and move cursor to the end.
  if (this.field.session.focusManager.requestFocus($filterInput)) {
    $filterInput.focus();

    var length = scout.nvl($filterInput.val(), '').length;
    $filterInput[0].setSelectionRange(length, length);
  }
};

/**
 * Returns a virtual key to represent this keystroke.
 */
scout.TableFocusFilterFieldKeyStroke.prototype.keys = function() {
  return [new scout.Key(this, this.virtualKeyStrokeWhich)];
};

/**
 * @override KeyStroke.js
 */
scout.TableFocusFilterFieldKeyStroke.prototype.renderKeyBox = function($drawingArea, event) {
  var $filterInput = event._$filterInput;

  var filterInputPosition = $filterInput.position();
  var top = $filterInput.css('margin-top').replace("px", "");
  var left = filterInputPosition.left + parseInt($filterInput.css('margin-left').replace("px", ""), 0) + 4;
  $filterInput.beforeDiv('key-box char', 'a - z').css('left', left + 'px');
  return $filterInput.parent();
};

scout.TableFocusFilterFieldKeyStroke.prototype._isKeyStrokeInRange = function(event) {
  if (event.which === this.virtualKeyStrokeWhich) {
    return true; // the event has this keystroke's 'virtual which part' in case it is rendered.
  }

  if (event.altKey | event.ctrlKey) {
    return false;
  }
  return (event.which >= scout.keys.a && event.which <= scout.keys.z) ||
    (event.which >= scout.keys.A && event.which <= scout.keys.Z) ||
    (event.which >= scout.keys['0'] && event.which <= scout.keys['9']);
};
