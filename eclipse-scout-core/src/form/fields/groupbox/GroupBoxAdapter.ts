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
import {CompositeFieldAdapter, FormField, GridData, GroupBox, ResponsiveManager, Widget} from '../../../index';
import {ChildModelOf, FullModelOf} from '../../../scout';

export default class GroupBoxAdapter extends CompositeFieldAdapter {
  declare widget: GroupBox;

  constructor() {
    super();
    this._addRemoteProperties(['expanded']);
  }

  protected override _initModel(m: ChildModelOf<Widget>, parent: Widget): FullModelOf<Widget> {
    let model = super._initModel(m, parent);
    // Set logical grid to null -> Calculation happens on server side
    model.logicalGrid = null;
    return model;
  }

  /**
   * Replace method on responsive handler.
   * @internal
   */
  override _postCreateWidget() {
    super._postCreateWidget();

    if (this.widget.responsiveHandler) {
      this.widget.responsiveHandler.setAllowedStates([ResponsiveManager.ResponsiveState.NORMAL, ResponsiveManager.ResponsiveState.CONDENSED]);
      this.widget.responsiveHandler.getGridData = this._getGridData;
      this.widget.responsiveHandler.setGridData = this._setGridData;
    }
  }

  protected _getGridData(field: FormField): GridData {
    return new GridData(field.gridData);
  }

  protected _setGridData(field: FormField, gridData: GridData) {
    field._setGridData(gridData);
  }
}
