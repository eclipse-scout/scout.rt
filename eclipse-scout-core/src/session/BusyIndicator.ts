/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, BoxButtons, BusyIndicatorEventMap, ClickActiveElementKeyStroke, CloseKeyStroke, Event, FocusRule, GlassPaneRenderer, InitModelOf, keys, KeyStrokeContext, scout, strings, Widget, WidgetModel} from '../index';

export interface BusyIndicatorModel extends WidgetModel {
  cancellable?: boolean;
  showTimeout?: number;
  label?: string;
  details?: string;
}

export class BusyIndicator extends Widget implements BusyIndicatorModel {
  declare model: BusyIndicatorModel;
  declare eventMap: BusyIndicatorEventMap;
  declare self: BusyIndicator;

  cancellable: boolean;
  showTimeout: number;
  label: string;
  details: string;

  cancelButton: Action;
  boxButtons: BoxButtons;
  protected _glassPaneRenderer: GlassPaneRenderer;
  $content: JQuery;
  $buttons: JQuery;
  $label: JQuery;
  $details: JQuery;
  protected _busyIndicatorTimeoutId: number;

  constructor() {
    super();
    this.cancellable = true;
    this.showTimeout = 2500;
    this.label = null;
    this.details = null;
    this.cancelButton = null;
    this.boxButtons = null;
    this._glassPaneRenderer = null;
    this.inheritAccessibility = false; // do not inherit enabled-state. BusyIndicator must always be enabled even if parent is disabled

    this.$content = null;
    this.$buttons = null;
    this.$label = null;
    this.$details = null;
    this._busyIndicatorTimeoutId = 0;

    this._addWidgetProperties(['boxButtons', 'cancelButton']);
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStrokes([
      new ClickActiveElementKeyStroke(this, [keys.SPACE, keys.ENTER]),
      new CloseKeyStroke(this, (() => {
        if (!this.cancelButton) {
          return null;
        }
        return this.cancelButton.$container;
      }))
    ]);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.label = scout.nvl(this.label, this.session.text('ui.PleaseWait_'));
    if (this.cancellable) {
      this.boxButtons = scout.create(BoxButtons, {parent: this});
      this.cancelButton = this.boxButtons.addButton({text: this.session.text('Cancel')});
      this.cancelButton.one('action', event => this._onCancelClick(event));
    }
  }

  override render($parent?: JQuery) {
    // Use entry point by default
    $parent = $parent || this.entryPoint();
    super.render($parent);
  }

  protected override _render() {
    // Render busy indicator (still hidden by CSS, will be shown later in setTimeout.
    // But don't use .hidden, otherwise the box' size cannot be calculated correctly!)
    this.$container = this.$parent.appendDiv('busyindicator invisible');

    let $handle = this.$container.appendDiv('drag-handle');
    this.$container.draggable($handle);

    this.$content = this.$container.appendDiv('busyindicator-content');
    this.$label = this.$content.appendDiv('busyindicator-label');
    this.$details = this.$content.appendDiv('busyindicator-details');

    if (this.cancellable) {
      this.boxButtons.render();
      this.$buttons = this.boxButtons.$container;
      this.$buttons.addClass('busyindicator-buttons');
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

    // Render modality glass-panes
    this._glassPaneRenderer = new GlassPaneRenderer(this);
    this._glassPaneRenderer.renderGlassPanes();
    this._glassPaneRenderer.eachGlassPane($glassPane => $glassPane.addClass('busy'));
  }

  protected override _postRender() {
    super._postRender();
    this.session.focusManager.installFocusContext(this.$container, FocusRule.AUTO);
  }

  protected override _remove() {
    // Remove busy box (cancel timer in case it was not fired yet)
    clearTimeout(this._busyIndicatorTimeoutId);

    // Remove glasspane
    this._glassPaneRenderer.eachGlassPane($glassPane => $glassPane.removeClass('busy'));
    this._glassPaneRenderer.removeGlassPanes();
    this.session.focusManager.uninstallFocusContext(this.$container);

    super._remove();
  }

  setLabel(label: string) {
    this.setProperty('label', label);
  }

  protected _renderLabel() {
    this.$label.text(this.label || '');
  }

  setDetails(details: string) {
    this.setProperty('details', details);
  }

  protected _renderDetails() {
    this.$details
      .html(strings.nl2br(this.details))
      .setVisible(!!this.details);
  }

  protected _position() {
    this.$container.cssMarginLeft(-this.$container.outerWidth() / 2);
  }

  /**
   * Used by CloseKeyStroke.js
   */
  close() {
    if (this.cancelButton && this.cancelButton.$container && this.session.focusManager.requestFocus(this.cancelButton.$container)) {
      this.cancelButton.$container.focus();
      this.cancelButton.doAction();
    }
  }

  protected _onCancelClick(event: Event) {
    this.trigger('cancel', event);
  }

  /**
   * Sets the busy indicator into cancelled state.
   */
  cancelled() {
    if (this.rendered) { // not closed yet
      this.$label.addClass('cancelled');
      if (this.$buttons) {
        this.$buttons.remove();
      }
      this.$content.addClass('no-buttons');
    }
  }
}
