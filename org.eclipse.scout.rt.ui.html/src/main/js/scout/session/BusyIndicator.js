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
scout.BusyIndicator = function() {
  scout.BusyIndicator.parent.call(this);
  this.cancellable = true;
  this.showTimeout = 2500;
  this.label;
  this.details;
};
scout.inherits(scout.BusyIndicator, scout.Widget);

/**
 * @override
 */
scout.BusyIndicator.prototype._createKeyStrokeContext = function() {
  return new scout.KeyStrokeContext();
};

/**
 * @override
 */
scout.BusyIndicator.prototype._initKeyStrokeContext = function() {
  scout.BusyIndicator.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke([
    new scout.ClickActiveElementKeyStroke(this, [
      scout.keys.SPACE, scout.keys.ENTER
    ]),
    new scout.CloseKeyStroke(this, function() {
      return this.$cancelButton;
    }.bind(this))
  ]);
};

scout.BusyIndicator.prototype._init = function(model) {
  scout.BusyIndicator.parent.prototype._init.call(this, model);
  this.label = scout.nvl(model.label, this.session.text('ui.PleaseWait_'));
};

scout.BusyIndicator.prototype._render = function($parent) {
  $parent = $parent || this.session.$entryPoint;
  this.$parent = $parent;

  // 1. Render modality glasspanes (must precede adding the busy indicator to the DOM)
  this._glassPaneRenderer = new scout.GlassPaneRenderer(this.session, this, true);
  this._glassPaneRenderer.renderGlassPanes();
  this._glassPaneRenderer.eachGlassPane(function($glassPane) {
    $glassPane.addClass('busy');
  });

  // 2. Render busy indicator (still hidden by CSS, will be shown later in setTimeout.
  // But don't use .hidden, otherwise the box' size cannot be calculated correctly!)
  this.$container = $parent.appendDiv('busyindicator invisible');

  var $handle = this.$container.appendDiv('drag-handle');
  this.$container.makeDraggable($handle);

  this.$content = this.$container.appendDiv('busyindicator-content');
  this.$label = this.$content.appendDiv('busyindicator-label');
  this.$details = this.$content.appendDiv('busyindicator-details');

  if (this.cancellable) {
    this.$buttons = this.$container.appendDiv('busyindicator-buttons');
    var boxButtons = new scout.BoxButtons(this.$buttons);
    this.$cancelButton = boxButtons.addButton({
      text: this.session.text('Cancel'),
      onClick: this._onCancelClick.bind(this)
    });
    this.$cancelButton.css('width', '100%');
  } else {
    this.$content.addClass('no-buttons');
  }

  // Render properties
  this._renderLabel();
  this._renderDetails();

  // Prevent resizing when message-box is dragged off the viewport
  this.$container.addClass('calc-helper');
  this.$container.css('min-width', this.$container.width());
  this.$container.removeClass('calc-helper');
  // Now that all texts, paddings, widths etc. are set, we can calculate the position
  this._position();

  // Show busy box with a delay of 2.5 seconds (configurable by this.showTimeout).
  this._busyIndicatorTimeoutId = setTimeout(function() {
    this.$container.removeClass('invisible').addClassForAnimation('animate-open');
    // Validate first focusable element
    // Maybe, this is not required if problem with single-button form is solved (see FormController.js)
    this.session.focusManager.validateFocus();
  }.bind(this), this.showTimeout);
};

scout.BusyIndicator.prototype._onCancelClick = function(event) {
  this.trigger('cancel', event);
};

scout.BusyIndicator.prototype._postRender = function() {
  scout.BusyIndicator.parent.prototype._postRender.call(this);
  this.session.focusManager.installFocusContext(this.$container, scout.focusRule.AUTO);
};

scout.BusyIndicator.prototype._remove = function() {
  // Remove busy box (cancel timer in case it was not fired yet)
  clearTimeout(this._busyIndicatorTimeoutId);

  // Remove glasspane
  this._glassPaneRenderer.eachGlassPane(function($glassPane) {
    $glassPane.removeClass('busy');
  });
  this._glassPaneRenderer.removeGlassPanes();
  this.session.focusManager.uninstallFocusContext(this.$container);

  scout.BusyIndicator.parent.prototype._remove.call(this);
};

scout.BusyIndicator.prototype._renderLabel = function() {
  this.$label.text(this.label || '');
};

scout.BusyIndicator.prototype._renderDetails = function() {
  this.$details.html(scout.strings.nl2br(this.details));
  this.$details.setVisible(this.details);
};

scout.BusyIndicator.prototype._position = function() {
  this.$container.cssMarginLeft(-this.$container.outerWidth() / 2);
};

/**
 * Used by CloseKeyStroke.js
 */
scout.BusyIndicator.prototype.close = function() {
  if (this.$cancelButton && this.session.focusManager.requestFocus(this.$cancelButton)) {
    this.$cancelButton.focus();
    this.$cancelButton.click();
  }
};

/**
 * Sets the busy indicator into cancelled state.
 */
scout.BusyIndicator.prototype.cancelled = function() {
  if (this.rendered) { // not closed yet
    this.$label.addClass('cancelled');
    this.$buttons.remove();
    this.$content.addClass('no-buttons');
  }
};
