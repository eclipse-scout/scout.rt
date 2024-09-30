/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AppLinkKeyStroke, HtmlFieldEventMap, HtmlFieldModel, scrollbars, SelectAllTextInFieldKeyStroke, ValueField} from '../../../index';
import $ from 'jquery';

export class HtmlField extends ValueField<string> implements HtmlFieldModel {
  declare model: HtmlFieldModel;
  declare eventMap: HtmlFieldEventMap;
  declare self: HtmlField;

  scrollBarEnabled: boolean;
  selectable: boolean;
  scrollToAnchor: string;

  constructor() {
    super();
    this.scrollBarEnabled = false;
    this.scrollToAnchor = null;
    this.preventInitialFocus = true;
    this.selectable = true;
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this.keyStrokeContext.registerKeyStroke(new AppLinkKeyStroke(this, this._onAppLinkAction));
    this.keyStrokeContext.registerKeyStroke(new SelectAllTextInFieldKeyStroke(this));
  }

  protected override _render() {
    this.addContainer(this.$parent, 'html-field');
    this.addLabel();
    this.addField(this.$parent.makeDiv());
    this.addStatus();
  }

  protected override _renderProperties() {
    super._renderProperties();

    this._renderScrollBarEnabled();
    this._renderScrollToAnchor();
    this._renderSelectable();
    this._renderEmpty();
  }

  protected override _renderDisplayText() {
    if (!this.displayText) {
      this.$field.empty();
      return;
    }
    this.$field.html(this.displayText);

    // Add action to app-links
    this.$field.find('.app-link')
      .on('click', this._onAppLinkAction.bind(this));

    // Don't change focus when a link is clicked by mouse
    this.$field.find('a, .app-link')
      .attr('tabindex', '0')
      .unfocusable();

    // Add listener to images to update the layout when the images are loaded
    this.$field.find('img')
      .on('load', this._onImageLoad.bind(this))
      .on('error', this._onImageError.bind(this));

    // Because this method replaces the content, the scroll bars might have to be added or removed
    if (this.rendered) { // (only necessary if already rendered, otherwise it is done by renderProperties)
      this._uninstallScrollbars();
      this._renderScrollBarEnabled();
    }

    this.invalidateLayoutTree();
  }

  protected override _renderFocused() {
    // NOP, don't add "focused" class. It doesn't look good when the label is highlighted but no cursor is visible.
  }

  protected _renderEmpty() {
    this.$field.toggleClass('empty', this.empty);
  }

  setScrollBarEnabled(scrollBarEnabled: boolean) {
    this.setProperty('scrollBarEnabled', scrollBarEnabled);
  }

  protected _renderScrollBarEnabled() {
    if (this.scrollBarEnabled) {
      this._installScrollbars();
    } else {
      this._uninstallScrollbars();
    }
  }

  protected _renderScrollToAnchor() {
    if (!this.rendered) {
      this.session.layoutValidator.schedulePostValidateFunction(this._renderScrollToAnchor.bind(this));
      return;
    }
    if (this.scrollBarEnabled && this.scrollToAnchor) {
      let anchorElem = this.$field.find('#'.concat(this.scrollToAnchor));
      if (anchorElem && anchorElem.length > 0) {
        scrollbars.scrollTo(this.$field, anchorElem);
      }
    }
  }

  setSelectable(selectable: boolean) {
    this.setProperty('selectable', selectable);
  }

  protected _renderSelectable() {
    this.$container.toggleClass('selectable', !!this.selectable);
    // Allow this field to receive the focus when selecting text with the mouse. Otherwise, form
    // keystrokes would no longer work because the focus would automatically be set to the desktop
    // for lack of alternatives. The value -1 ensures the field is skipped when tabbing through
    // all form fields.
    this.$field.toggleAttr('tabindex', !!this.selectable, '-1');
  }

  protected _onAppLinkAction(event: JQuery.KeyboardEventBase | JQuery.ClickEvent) {
    let $target = $(event.delegateTarget);
    let ref = $target.data('ref') as string;
    this.triggerAppLinkAction(ref);
  }

  triggerAppLinkAction(ref: string) {
    this.trigger('appLinkAction', {
      ref: ref
    });
  }

  protected _onImageLoad(event: JQuery.TriggeredEvent) {
    this.invalidateLayoutTree();
  }

  protected _onImageError(event: JQuery.TriggeredEvent) {
    this.invalidateLayoutTree();
  }
}
