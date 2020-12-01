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
import {BoxButtons, ClickActiveElementKeyStroke, CloseKeyStroke, FocusRule, GlassPaneRenderer, keys, KeyStrokeContext, scout, strings, Widget} from '../index';

export default class BusyIndicator extends Widget {

  constructor() {
    super();
    this.cancellable = true;
    this.showTimeout = 2500;
    this.label = null;
    this.details = null;
  }

  /**
   * @override
   */
  _createKeyStrokeContext() {
    return new KeyStrokeContext();
  }

  /**
   * @override
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStroke([
      new ClickActiveElementKeyStroke(this, [
        keys.SPACE, keys.ENTER
      ]),
      new CloseKeyStroke(this, (() => {
        return this.$cancelButton;
      }))
    ]);
  }

  _init(model) {
    super._init(model);
    this.label = scout.nvl(this.label, this.session.text('ui.PleaseWait_'));
  }

  render($parent) {
    // Use entry point by default
    $parent = $parent || this.entryPoint();
    super.render($parent);
  }

  _render() {
    // Render busy indicator (still hidden by CSS, will be shown later in setTimeout.
    // But don't use .hidden, otherwise the box' size cannot be calculated correctly!)
    this.$container = this.$parent.appendDiv('busyindicator invisible');

    let $handle = this.$container.appendDiv('drag-handle');
    this.$container.draggable($handle);

    this.$content = this.$container.appendDiv('busyindicator-content');
    this.$label = this.$content.appendDiv('busyindicator-label');
    this.$details = this.$content.appendDiv('busyindicator-details');

    if (this.cancellable) {
      this.$buttons = this.$container.appendDiv('busyindicator-buttons');
      let boxButtons = new BoxButtons(this.$buttons);
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
    this._busyIndicatorTimeoutId = setTimeout(() => {
      this.$container.removeClass('invisible').addClassForAnimation('animate-open');
      // Validate first focusable element
      // Maybe, this is not required if problem with single-button form is solved (see FormController.js)
      this.session.focusManager.validateFocus();
    }, this.showTimeout);

    // Render modality glasspanes
    this._glassPaneRenderer = new GlassPaneRenderer(this);
    this._glassPaneRenderer.renderGlassPanes();
    this._glassPaneRenderer.eachGlassPane($glassPane => {
      $glassPane.addClass('busy');
    });
  }

  _postRender() {
    super._postRender();
    this.session.focusManager.installFocusContext(this.$container, FocusRule.AUTO);
  }

  _remove() {
    // Remove busy box (cancel timer in case it was not fired yet)
    clearTimeout(this._busyIndicatorTimeoutId);

    // Remove glasspane
    this._glassPaneRenderer.eachGlassPane($glassPane => {
      $glassPane.removeClass('busy');
    });
    this._glassPaneRenderer.removeGlassPanes();
    this.session.focusManager.uninstallFocusContext(this.$container);

    super._remove();
  }

  setLabel(label) {
    this.setProperty('label', label);
  }

  _renderLabel() {
    this.$label.text(this.label || '');
  }

  setDetails(details) {
    this.setProperty('details', details);
  }

  _renderDetails() {
    this.$details
      .html(strings.nl2br(this.details))
      .setVisible(!!this.details);
  }

  _position() {
    this.$container.cssMarginLeft(-this.$container.outerWidth() / 2);
  }

  /**
   * Used by CloseKeyStroke.js
   */
  close() {
    if (this.$cancelButton && this.session.focusManager.requestFocus(this.$cancelButton)) {
      this.$cancelButton.focus();
      this.$cancelButton.click();
    }
  }

  _onCancelClick(event) {
    this.trigger('cancel', event);
  }

  /**
   * Sets the busy indicator into cancelled state.
   */
  cancelled() {
    if (this.rendered) { // not closed yet
      this.$label.addClass('cancelled');
      this.$buttons.remove();
      this.$content.addClass('no-buttons');
    }
  }
}
