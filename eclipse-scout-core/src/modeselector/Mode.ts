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
import {Action, ModeModel} from '../index';

export default class Mode<T> extends Action implements ModeModel<T> {
  declare model: ModeModel<T>;

  ref: T;

  constructor() {
    super();

    this.selected = false;
    this.ref = null;
  }

  protected override _init(model: ModeModel<T>) {
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
