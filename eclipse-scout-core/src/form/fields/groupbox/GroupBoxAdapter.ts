/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {CompositeFieldAdapter, GridData, ResponsiveManager} from '../../../index';

export default class GroupBoxAdapter extends CompositeFieldAdapter {

  constructor() {
    super();
    this._addRemoteProperties(['expanded']);
  }

  /**
   * @override
   */
  _initModel(model, parent) {
    model = super._initModel(model, parent);
    // Set logical grid to null -> Calculation happens on server side
    model.logicalGrid = null;

    return model;
  }

  // Replace method on responsive handler.
  _postCreateWidget() {
    super._postCreateWidget();

    if (this.widget.responsiveHandler) {
      this.widget.responsiveHandler.setAllowedStates([ResponsiveManager.ResponsiveState.NORMAL, ResponsiveManager.ResponsiveState.CONDENSED]);
      this.widget.responsiveHandler.getGridData = this._getGridData;
      this.widget.responsiveHandler.setGridData = this._setGridData;
    }
  }

  destroy() {
    super.destroy();
  }

  _getGridData(field) {
    return new GridData(field.gridData);
  }

  _setGridData(field, gridData) {
    field._setGridData(gridData);
  }
}
