/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FullModelOf, ModeSelector, ModeSelectorField, ValueFieldAdapter, Widget} from '../../../index';

export class ModeSelectorFieldAdapter extends ValueFieldAdapter {
  declare widget: ModeSelectorField<any>;

  protected override _createWidget(model: FullModelOf<ModeSelectorField<any>>): Widget {
    this._addModeSelector(model);
    return super._createWidget(model);
  }

  protected _addModeSelector(model: FullModelOf<ModeSelectorField<any>>) {
    model.modeSelector = {
      objectType: ModeSelector
    };
  }

  /** @internal */
  override _postCreateWidget() {
    this.widget.modeSelector.setModes(this.widget.modes);
    delete this.widget.modes;
  }
}
