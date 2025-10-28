/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AppLinkKeyStroke, events, FormField} from '@eclipse-scout/core';
import {SvgFieldEventMap, SvgFieldModel} from '../index';

export class SvgField extends FormField implements SvgFieldModel {
  declare model: SvgFieldModel;
  declare eventMap: SvgFieldEventMap;

  svgDocument: string;

  protected override _render() {
    this.addContainer(this.$parent, 'svg-field');
    this.addLabel();
    this.addMandatoryIndicator();
    this.addField(this.$parent.makeDiv());
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

  protected _onAppLinkAction(event: JQuery.TriggeredEvent) {
    events.triggerAppLinkAction(event, this._triggerAppLinkAction.bind(this));
    event.preventDefault();
  }

  protected _triggerAppLinkAction(ref: string) {
    this.trigger('appLinkAction', {
      ref: ref
    });
  }
}
