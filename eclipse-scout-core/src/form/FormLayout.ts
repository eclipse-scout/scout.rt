/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, Dimension, FileChooser, Form, graphics, HtmlComponent, HtmlCompPrefSizeOptions} from '../index';
import $ from 'jquery';

export default class FormLayout extends AbstractLayout {
  form: Form | FileChooser;

  constructor(form: Form | FileChooser) {
    super();
    this.form = form;
  }

  override layout($container: JQuery) {
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

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
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

  protected _htmlRootGroupBox(): HtmlComponent {
    let $rootGroupBox = this.form.$container.children('.root-group-box');
    return HtmlComponent.get($rootGroupBox);
  }

  protected _headerHeight(): number {
    // @ts-ignore
    let $header: JQuery = this.form.$header;
    if ($header && $header.css('position') !== 'absolute') {
      return graphics.prefSize($header, true).height;
    }
    return 0;
  }
}
