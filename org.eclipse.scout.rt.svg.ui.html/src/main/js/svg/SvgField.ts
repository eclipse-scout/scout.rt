/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AppLinkKeyStroke, FormField} from '@eclipse-scout/core';
import $ from 'jquery';
import {SvgFieldEventMap, SvgFieldModel} from '../index';

export class SvgField extends FormField implements SvgFieldModel {
  declare model: SvgFieldModel;
  declare eventMap: SvgFieldEventMap;

  svgDocument: string;

  protected override _render() {
    this.addContainer(this.$parent, 'svg-field');
    this.addLabel();
    this.addField(this.$parent.makeDiv());
    this.addMandatoryIndicator();
    this.addStatus();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderSvgDocument();
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this.keyStrokeContext.registerKeyStroke(new AppLinkKeyStroke(this, this._onAppLinkAction));
  }

  protected _renderSvgDocument() {
    if (!this.svgDocument) {
      this.$field.empty();
      return;
    }
    this.$field.html(this.svgDocument);
    this.$field.find('.app-link')
      .on('click', this._onAppLinkAction.bind(this))
      .attr('tabindex', '0')
      .unfocusable();
  }

  protected _onAppLinkAction(event: JQuery.KeyboardEventBase | JQuery.ClickEvent) {
    let $target = $(event.delegateTarget);
    let ref = $target.data('ref') as string;
    this._triggerAppLinkAction(ref);
    event.preventDefault();
  }

  protected _triggerAppLinkAction(ref: string) {
    this.trigger('appLinkAction', {
      ref: ref
    });
  }
}
