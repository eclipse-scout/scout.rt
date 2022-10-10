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
import {ViewButton} from '../../index';

export default class OutlineViewButton extends ViewButton {

  constructor() {
    super();
    this.outline = null;
    this._addWidgetProperties('outline');
    this._addPreserveOnPropertyChangeProperties(['outline']);
    this._addCloneProperties(['outline']);
  }

  _init(model) {
    super._init(model);
    this._setOutline(this.outline);
  }

  _setOutline(outline) {
    this._setProperty('outline', outline);
    if (this.outline) {
      this.outline.setIconId(this.iconId);
    }
  }

  _setIconId(iconId) {
    this._setProperty('iconId', iconId);
    if (this.outline) {
      this.outline.setIconId(this.iconId);
    }
  }

  /**
   * @override
   */
  _doAction() {
    super._doAction();
    if (this.outline) {
      this.session.desktop.setOutline(this.outline);
      this.session.desktop.bringOutlineToFront();
    }
  }

  onOutlineChange(outline) {
    let selected = !!outline && this.outline === outline;
    this.setSelected(selected);
  }
}
