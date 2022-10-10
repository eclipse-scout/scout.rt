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
import {TileButton} from '../../../index';

export default class PageTileButton extends TileButton {

  constructor() {
    super();
    this.page = null;
  }

  _init(model) {
    super._init(model);

    this.label = this.page.text;
    this.iconId = this.page.overviewIconId;
    this.labelHtmlEnabled = this.page.htmlEnabled;

    this.on('click', event => {
      this.outline.selectNode(this.page);
    });
  }

  notifyPageChanged() {
    this.setLabel(this.page.text);
    this.setIconId(this.page.overviewIconId);
    this.setLabelHtmlEnabled(this.page.htmlEnabled);
  }
}
