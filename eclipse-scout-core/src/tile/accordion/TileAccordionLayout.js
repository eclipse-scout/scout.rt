/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import {AccordionLayout, HtmlComponent} from '../../index';

export default class TileAccordionLayout extends AccordionLayout {

  constructor(tileAccordion, options) {
    super(options);
    this.tileAccordion = tileAccordion;
  }

  layout($container) {
    super.layout($container);
    this._updateFilterFieldMaxWidth($container);
  }

  _updateFilterFieldMaxWidth($container) {
    let htmlComp = HtmlComponent.get($container),
      width = htmlComp.availableSize().subtract(htmlComp.insets()).width;
    this.tileAccordion.$filterFieldContainer.css('--filter-field-max-width', (width * 0.6) + 'px');
  }
}
