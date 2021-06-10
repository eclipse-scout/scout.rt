/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AppLinkKeyStroke, scrollbars, ValueField} from '../../../index';
import $ from 'jquery';

export default class HtmlField extends ValueField {

  constructor() {
    super();
    this.scrollBarEnabled = false;
    this.preventInitialFocus = true;
    this.selectable = true;
  }

  /**
   * @override FormField.js
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStroke(new AppLinkKeyStroke(this, this._onAppLinkAction));
  }

  _render() {
    this.addContainer(this.$parent, 'html-field');
    this.addLabel();

    this.addField(this.$parent.makeDiv());
    this.addStatus();
  }

  _renderProperties() {
    super._renderProperties();

    this._renderScrollBarEnabled();
    this._renderScrollToAnchor();
    this._renderSelectable();
  }

  _readDisplayText() {
    return this.$field.html();
  }

  /**
   * @override
   */
  _renderDisplayText() {
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

  /**
   * @override
   */
  _renderFocused() {
    // NOP, don't add "focused" class. It doesn't look good when the label is highlighted but no cursor is visible.
  }

  setScrollBarEnabled(scrollBarEnabled) {
    this.setProperty('scrollBarEnabled', scrollBarEnabled);
  }

  _renderScrollBarEnabled() {
    if (this.scrollBarEnabled) {
      this._installScrollbars();
    } else {
      this._uninstallScrollbars();
    }
  }

  _renderScrollToAnchor() {
    if (!this.rendered) {
      this.session.layoutValidator.schedulePostValidateFunction(this._renderScrollToAnchor.bind(this));
      return;
    }
    let anchor = this.scrollToAnchor;
    if (this.scrollBarEnabled && anchor && this.$field.find(anchor)) {
      let anchorElem = this.$field.find('#'.concat(anchor));
      if (anchorElem && anchorElem.length > 0) {
        scrollbars.scrollTo(this.$field, anchorElem);
      }
    }
  }

  setSelectable(selectable) {
    this.setProperty('selectable', selectable);
  }

  _renderSelectable() {
    this.$container.toggleClass('selectable', !!this.selectable);
    // Allow this field to receive the focus when selecting text with the mouse. Otherwise, form
    // keystrokes would no longer work because the focus would automatically be set to the desktop
    // for lack of alternatives. The value -1 ensures the field is skipped when tabbing through
    // all form fields.
    this.$field.toggleAttr('tabindex', !!this.selectable, '-1');
  }

  _onAppLinkAction(event) {
    let $target = $(event.delegateTarget);
    let ref = $target.data('ref');
    this.triggerAppLinkAction(ref);
  }

  triggerAppLinkAction(ref) {
    this.trigger('appLinkAction', {
      ref: ref
    });
  }

  _onImageLoad(event) {
    this.invalidateLayoutTree();
  }

  _onImageError(event) {
    this.invalidateLayoutTree();
  }
}
