/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ValueField, AppLinkKeyStroke} from '@eclipse-scout/core';
import * as $ from 'jquery';

export default class SvgField extends ValueField {

constructor() {
  super();
}


_render() {
  this.addContainer(this.$parent, 'svg-field');
  this.addLabel();
  this.addField(this.$parent.makeDiv());
  this.addMandatoryIndicator();
  this.addStatus();
}

_renderProperties() {
  super._renderProperties();
  this._renderSvgDocument();
}

/**
 * @override FormField.js
 */
_initKeyStrokeContext() {
  super._initKeyStrokeContext();
  this.keyStrokeContext.registerKeyStroke(new AppLinkKeyStroke(this, this._onAppLinkAction));
}

_renderSvgDocument() {
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

_onAppLinkAction(event) {
  var $target = $(event.delegateTarget);
  var ref = $target.data('ref');
  this._triggerAppLinkAction(ref);
  event.preventDefault();
}

_triggerAppLinkAction(ref) {
  this.trigger('appLinkAction', {
    ref: ref
  });
}
}
