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
import {AbstractLayout, graphics, HtmlComponent} from '../index';
import $ from 'jquery';

export default class FormLayout extends AbstractLayout {

  constructor(form) {
    super();
    this.form = form;
  }

  layout($container) {
    let htmlContainer = HtmlComponent.get($container),
      htmlRootGb = this._htmlRootGroupBox(),
      rootGbSize;

    this.form.validateLogicalGrid();

    rootGbSize = htmlContainer.availableSize()
      .subtract(htmlContainer.insets())
      .subtract(htmlRootGb.margins());

    rootGbSize.height -= this._headerHeight();

    $.log.isTraceEnabled() && $.log.trace('(FormLayout#layout) rootGbSize=' + rootGbSize);
    htmlRootGb.setSize(rootGbSize);
  }

  preferredLayoutSize($container, options) {
    options = options || {};
    let htmlContainer = HtmlComponent.get($container),
      htmlRootGb = this._htmlRootGroupBox(),
      prefSize;

    this.form.validateLogicalGrid();

    let titleHeight = this._headerHeight();
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
    let $rootGroupBox = this.form.$container.children('.root-group-box');
    return HtmlComponent.get($rootGroupBox);
  }

  _headerHeight() {
    if (this.form.$header && this.form.$header.css('position') !== 'absolute') {
      return graphics.prefSize(this.form.$header, true).height;
    }
    return 0;
  }
}
