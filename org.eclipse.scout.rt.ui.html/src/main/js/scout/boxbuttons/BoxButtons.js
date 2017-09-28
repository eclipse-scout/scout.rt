/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Button utility class for a set of buttons, where each button has an option value.
 *
 * Constructor arguments:
 *
 * @param $parent
 *          required
 * @param onClickHandler
 *          optional, global onClickHandler to attach to each button without a specific clickHandler
 */
scout.BoxButtons = function($parent, onClickHandler) {
  if (!$parent) {
    throw new Error('Missing $parent');
  }
  $parent.addClass('box-buttons');

  this._$parent = $parent;
  this._onClickHandler = onClickHandler;

  this._$buttons = [];
};

/**
 * @param opts
 *          [text]     required,  text of button to add
 *          [tabIndex] optional,  tabindex to assign (default '0')
 *          [enabled]  optional,  if button should be enabled or not (default true)
 *          [onClick]  optional,  function to be executed when button is clicked.
 *                                This function does _not_ have to check by itself if the button is
 *                                enabled. If this argument is omitted, the global onClickHandler is
 *                                used (see constructor).
 *          [needsClick] optional true or false, default is false. This is a hint for the fastclick
 *                                library. It sets an additional CSS class on the DIV element of the
 *                                button. This class prevents fastclick from messing with click events
 *                                which are sometimes required to programatically trigger an action on
 *                                a HTML element such as the input type=file element.
 *          [option]   optional,  a string that is assigned to be button and is passed
 *                                to the global onClickHandler as an argument.
 */
scout.BoxButtons.prototype.addButton = function(opts) {
  opts = opts || {};

  var $button = this._$parent.appendDiv()
    .text(opts.text)
    .addClass('box-button')
    .unfocusable()
    .setEnabled(scout.nvl(opts.enabled, true));

  if (opts.needsClick) {
    $button.addClass('needsclick');
  }

  if (!scout.device.supportsTouch()) {
    $button.attr('tabindex', opts.tabIndex || '0');
  }

  if (opts.onClick) {
    var onClick = opts.onClick;
    $button.on('click', function(event) {
      if ($.suppressEventIfDisabled(event)) {
        return;
      }
      onClick(event);
    });
  } else if (this._onClickHandler) {
    $button.on('click', this._onClick.bind(this));
  }
  $button.data('buttonOption', opts.option);

  this._$buttons.push($button);
  return $button;
};

scout.BoxButtons.prototype._onClick = function(event) {
  var $button = $(event.target);
  if ($.suppressEventIfDisabled(event, $button)) {
    return;
  }
  this._onClickHandler(event, $button.data('buttonOption'));
};

scout.BoxButtons.prototype.updateButtonWidths = function(availableWidth) {
  // Find all visible buttons
  var $visibleButtons = [];
  this._$buttons.forEach(function($button) {
    if ($button.isVisible()) {
      $visibleButtons.push($button);
    }
  });

  var hasVisibleButtons = $visibleButtons.length > 0;
  this._$parent.toggleClass('empty', !hasVisibleButtons);

  // Manually calculate equal width fore each button, adding remaining pixels to last button.
  // (We don't use CSS percentage values, because sometimes browser calculations lead to wrong results.)
  availableWidth = availableWidth || this._$parent.width();
  var w = Math.floor(availableWidth / $visibleButtons.length);
  $visibleButtons.forEach(function($button, index) {
    if (index === $visibleButtons.length - 1) {
      w = availableWidth;
    } else {
      availableWidth -= w;
    }
    $button.outerWidth(w);
  });
};

scout.BoxButtons.prototype.buttonCount = function() {
  return this._$buttons.length;
};
