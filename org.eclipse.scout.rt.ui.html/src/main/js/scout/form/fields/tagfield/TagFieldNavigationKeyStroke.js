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
scout.TagFieldNavigationKeyStroke = function(tagField) {
  scout.TagFieldNavigationKeyStroke.parent.call(this);
  this.field = tagField;
  this.which = [scout.keys.LEFT, scout.keys.RIGHT];
  this.preventDefault = false;
  this.preventInvokeAcceptInputOnActiveValueField = true;
};
scout.inherits(scout.TagFieldNavigationKeyStroke, scout.KeyStroke);

scout.TagFieldNavigationKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TagFieldNavigationKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }
  if (scout.strings.hasText(this.field._readDisplayText())) {
    return false;
  }
  return true;
};

scout.TagFieldNavigationKeyStroke.prototype.handle = function(event) {
  if (event.which === scout.keys.LEFT) {
    this._focusTagElement(-1);
  } else if (event.which === scout.keys.RIGHT) {
    this._focusTagElement(1);
  }
};

scout.TagFieldNavigationKeyStroke.prototype._focusTagElement = function(direction) {
  var UNDEFINED = -1;
  var INPUT = -2;

  // find overflow-icon and all tag-elements
  var $focusTargets = this.field.$fieldContainer.find('.tag-element:not(.hidden),.overflow-icon');
  var numTargets = $focusTargets.length;
  if (numTargets === 0) {
    return;
  }

  // check which element has the focus
  var focusIndex = UNDEFINED;
  $focusTargets.each(function(index) {
    var $element = $(this);
    if ($element.hasClass('focused')) {
      focusIndex = index;
    }
  });

  if (focusIndex === UNDEFINED) {
    // no tag-elements focused currently
    if (direction === -1) {
      focusIndex = numTargets - 1;
    }
  } else {
    var nextFocusIndex = focusIndex + direction;
    if (nextFocusIndex >= numTargets) {
      focusIndex = INPUT;
    } else if (nextFocusIndex < 0) {
      focusIndex = UNDEFINED;
    } else {
      $focusTargets.eq(focusIndex)
        .removeClass('focused')
        .setTabbable(false);
      focusIndex = nextFocusIndex;
    }
  }

  if (focusIndex === UNDEFINED) {
    // leave focus untouched
  } else if (focusIndex === INPUT) {
    this.field.$field.focus();
  } else {
    $focusTargets.eq(focusIndex)
    .addClass('focused')
    .setTabbable(true)
    .focus();
  }
};
