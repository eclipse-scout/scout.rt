/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, InitModelOf, ModeModel} from '../index';

export class Mode<TRef = any> extends Action implements ModeModel<TRef> {
  declare model: ModeModel<TRef>;

  ref: TRef;

  constructor() {
    super();

    this.selected = false;
    this.ref = null;
  }

  protected override _init(model: InitModelOf<this>) {
    model.owner = model.parent;
    super._init(model);
  }

  protected override _render() {
    super._render();
    this.$container.addClass('button mode');
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderSelected();
  }

  protected override _renderSelected() {
    this.$container.select(this.selected);
  }

  override doAction(): boolean {
    if (!this.prepareDoAction()) {
      return false;
    }

    if (!this.selected) {
      this.setSelected(true);
    }

    return true;
  }

  override toggle() {
    if (!this.selected) {
      this.setSelected(true);
    }
  }

  protected override _renderIconId() {
    super._renderIconId();

    this._updateLabelAndIconStyle();
    // Invalidate layout because mode may now be longer or shorter
    this.invalidateLayoutTree();
  }

  protected override _renderText() {
    super._renderText();

    this._updateLabelAndIconStyle();
    // Invalidate layout because mode may now be longer or shorter
    this.invalidateLayoutTree();
  }

  protected _updateLabelAndIconStyle() {
    let hasText = !!this.text;
    this.get$Icon().toggleClass('with-label', hasText);
  }
}
