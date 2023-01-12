/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AppLinkKeyStroke, HtmlComponent, InitModelOf, KeyStrokeContext, LabelEventMap, LabelModel, strings, Widget} from '../index';
import $ from 'jquery';

export class Label extends Widget implements LabelModel {
  declare model: LabelModel;
  declare eventMap: LabelEventMap;
  declare self: Label;

  value: string;
  htmlEnabled: boolean;
  scrollable: boolean;

  constructor() {
    super();
    this.value = null;
    this.htmlEnabled = false;
    this.scrollable = false;
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStroke(new AppLinkKeyStroke(this, this._onAppLinkAction));
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.resolveTextKeys(['value']);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv();
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderValue();
    this._renderScrollable();
  }

  setValue(value: string) {
    this.setProperty('value', value);
  }

  protected _renderValue() {
    let value = this.value || '';
    if (this.htmlEnabled) {
      this.$container.html(value);

      // Find app links and add handlers
      this.$container.find('.app-link').on('click', this._onAppLinkAction.bind(this));

      // Add handler to images to update the layout when the images are loaded
      this.$container.find('img')
        .on('load', this._onImageLoad.bind(this))
        .on('error', this._onImageError.bind(this));
    } else {
      this.$container.html(strings.nl2br(value));
    }

    // Because this method replaces the content, the scroll bars might have to be added or removed
    if (this.rendered) { // (only necessary if already rendered, otherwise it is done by renderProperties)
      this._uninstallScrollbars();
      this._renderScrollable();
    }

    this.invalidateLayoutTree();
  }

  setHtmlEnabled(htmlEnabled: boolean) {
    this.setProperty('htmlEnabled', htmlEnabled);
  }

  protected _renderHtmlEnabled() {
    // Render the value again when html enabled changes dynamically
    this._renderValue();
  }

  setScrollable(scrollable: boolean) {
    this.setProperty('scrollable', scrollable);
  }

  protected _renderScrollable() {
    if (this.scrollable) {
      this._installScrollbars();
    } else {
      this._uninstallScrollbars();
    }
  }

  protected _onAppLinkAction(event: JQuery.ClickEvent | JQuery.KeyboardEventBase) {
    let $target = $(event.delegateTarget);
    let ref = $target.data('ref');
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
