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
import {ModeSelector, ModeSelectorField, ValueFieldAdapter, Widget} from '../../../index';
import {FullModelOf} from '../../../scout';

export default class ModeSelectorFieldAdapter extends ValueFieldAdapter {
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
