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
import {Action} from '../index';

export default class Mode extends Action {

  constructor() {
    super();

    this.selected = false;
    this.ref = null; // Arbitrary reference value, can be used to find and select modes (see ModeSelector.js)
  }

  _init(model) {
    model.owner = model.parent;
    super._init(model);
  }

  _render() {
    super._render();
    this.$container.addClass('button mode');
  }

  _renderProperties() {
    super._renderProperties();
    this._renderSelected();
  }

  setSelected(selected) {
    this.setProperty('selected', selected);
  }

  _renderSelected() {
    this.$container.select(this.selected);
  }

  /**
   * @Override Action.js
   */
  doAction() {
    if (!this.prepareDoAction()) {
      return false;
    }

    if (!this.selected) {
      this.setSelected(true);
    }

    return true;
  }

  /**
   * @Override Action.js
   */
  toggle() {
    if (!this.selected) {
      this.setSelected(true);
    }
  }

  _renderIconId() {
    super._renderIconId();

    this._updateLabelAndIconStyle();
    // Invalidate layout because mode may now be longer or shorter
    this.invalidateLayoutTree();
  }

  _renderText() {
    super._renderText();

    this._updateLabelAndIconStyle();
    // Invalidate layout because mode may now be longer or shorter
    this.invalidateLayoutTree();
  }

  _updateLabelAndIconStyle() {
    let hasText = !!this.text;
    this.get$Icon().toggleClass('with-label', hasText);
  }
}
