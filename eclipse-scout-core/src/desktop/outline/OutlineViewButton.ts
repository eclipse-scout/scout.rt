/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {InitModelOf, Outline, OutlineViewButtonEventMap, OutlineViewButtonModel, ViewButton} from '../../index';

export class OutlineViewButton extends ViewButton implements OutlineViewButtonModel {
  declare model: OutlineViewButtonModel;
  declare eventMap: OutlineViewButtonEventMap;
  declare self: OutlineViewButton;

  outline: Outline;

  constructor() {
    super();
    this.outline = null;
    this._addWidgetProperties('outline');
    this._addPreserveOnPropertyChangeProperties(['outline']);
    this._addCloneProperties(['outline']);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setOutline(this.outline);
  }

  protected _setOutline(outline: Outline) {
    this._setProperty('outline', outline);
    if (this.outline) {
      this.outline.setIconId(this.iconId);
    }
  }

  protected _setIconId(iconId: string) {
    this._setProperty('iconId', iconId);
    if (this.outline) {
      this.outline.setIconId(this.iconId);
    }
  }

  protected override _doAction() {
    super._doAction();
    if (this.outline) {
      this.session.desktop.setOutline(this.outline);
      this.session.desktop.bringOutlineToFront();
    }
  }

  onOutlineChange(outline: Outline) {
    let selected = !!outline && this.outline === outline;
    this.setSelected(selected);
  }
}
