/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {keys} from '../../../index';
import {KeyStroke} from '../../../index';
import * as $ from 'jquery';
import {TagBar} from '../../../index';


/**
 * @param fieldAdapter acts as an interface so we can use the same key-stroke for TagField and TagFieldPopup.
 *
 */
export default class TagFieldNavigationKeyStroke extends KeyStroke {

constructor(fieldAdapter) {
  super();
  this.fieldAdapter = fieldAdapter;
  this.which = [keys.LEFT, keys.RIGHT];
  this.preventDefault = false;
  this.preventInvokeAcceptInputOnActiveValueField = true;
}


_accept(event) {
  var accepted = super._accept( event);
  if (!accepted) {
    return false;
  }
  return this.fieldAdapter.enabled();
}

handle(event) {
  if (event.which === keys.LEFT) {
    this._focusTagElement(-1);
  } else if (event.which === keys.RIGHT) {
    this._focusTagElement(1);
  }
}

_focusTagElement(direction) {
  var UNDEFINED = -1;
  var INPUT = -2;

  // find overflow-icon and all tag-elements
  var $focusTargets = TagBar.findFocusableTagElements(this.fieldAdapter.$container());
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
      TagBar.unfocusTagElement($focusTargets.eq(focusIndex));
      focusIndex = nextFocusIndex;
    }
  }

  if (focusIndex === UNDEFINED) {
    // leave focus untouched
  } else if (focusIndex === INPUT) {
    this.fieldAdapter.focus();
  } else {
    TagBar.focusTagElement($focusTargets.eq(focusIndex));
  }
}
}
