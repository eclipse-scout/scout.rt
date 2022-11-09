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
import {EventHandler, Mode, ModeSelector, ModeSelectorFieldModel, PropertyChangeEvent, SomeRequired, ValueField} from '../../../index';

export class ModeSelectorField<TValue> extends ValueField<TValue> implements ModeSelectorFieldModel<TValue> {
  declare model: ModeSelectorFieldModel<TValue>;
  declare initModel: SomeRequired<this['model'], 'parent' | 'modeSelector'>;

  modeSelector: ModeSelector<TValue>;
  modes: Mode<TValue>[];

  protected _selectedModeChangeHandler: EventHandler<PropertyChangeEvent<Mode<TValue>, ModeSelector>>;

  constructor() {
    super();
    // modes will be moved to the ModeSelector after the adapters are created (only required in scout classic)
    // see ModeSelectorFieldAdapter.js
    this._addWidgetProperties(['modeSelector', 'modes']);
    this._selectedModeChangeHandler = this._onSelectedModeChange.bind(this);
  }

  protected override _render() {
    this.addContainer(this.$parent, 'mode-selector-field');
    this.addLabel();
    this.addStatus();
    if (this.modeSelector) {
      this._renderModeSelector();
      this.modeSelector.on('propertyChange:selectedMode', this._selectedModeChangeHandler);
    }
  }

  // Will also be called by model adapter on property change event
  protected _renderModeSelector() {
    this.modeSelector.render();
    this.addField(this.modeSelector.$container);
  }

  protected _removeModeSelector() {
    this.modeSelector.remove();
    this._removeField();
  }

  protected _onSelectedModeChange(event: PropertyChangeEvent<Mode<TValue>, ModeSelector<TValue>>) {
    if (event.newValue) {
      this.setValue(event.newValue.ref);
    } else if (this.modeSelector.selectedMode && this.modeSelector.selectedMode.id) {
      this.setValue(this.modeSelector.selectedMode.id as TValue);
    } else {
      this.setValue(null);
    }
  }
}
