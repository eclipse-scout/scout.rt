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
import {graphics} from '../index';
import {AbstractLayout} from '../index';
import {HtmlComponent} from '../index';
import * as $ from 'jquery';

export default class FormLayout extends AbstractLayout {

constructor(form) {
  super();
  this.form = form;
}


layout($container) {
  var htmlContainer = HtmlComponent.get($container),
    htmlRootGb = this._htmlRootGroupBox(),
    rootGbSize;

  this.form.validateLogicalGrid();

  rootGbSize = htmlContainer.availableSize()
    .subtract(htmlContainer.insets())
    .subtract(htmlRootGb.margins());

  if (this.form.isDialog()) {
    rootGbSize.height -= this._titleHeight();
  }

  $.log.isTraceEnabled() && $.log.trace('(FormLayout#layout) rootGbSize=' + rootGbSize);
  htmlRootGb.setSize(rootGbSize);
}

preferredLayoutSize($container, options) {
  options = options || {};
  var htmlContainer = HtmlComponent.get($container),
    htmlRootGb = this._htmlRootGroupBox(),
    prefSize;

  this.form.validateLogicalGrid();

  var titleHeight = this._titleHeight();
  if (options.heightHint) {
    options.heightHint -= titleHeight;
  }
  prefSize = htmlRootGb.prefSize(options)
    .add(htmlContainer.insets())
    .add(htmlRootGb.margins());
  prefSize.height += titleHeight;

  return prefSize;
}

_htmlRootGroupBox() {
  var $rootGroupBox = this.form.$container.children('.root-group-box');
  return HtmlComponent.get($rootGroupBox);
}

_titleHeight() {
  if (this.form.$header && this.form.$header.css('position') !== 'absolute') {
    return graphics.prefSize(this.form.$header, true).height;
  }
  return 0;
}
}
