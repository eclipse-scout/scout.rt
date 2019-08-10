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
scout.ModeSelectorField = function() {
  scout.ModeSelectorField.parent.call(this);
  // modes will be moved to the ModeSelector after the adapters are created (only required in scout classic)
  // see ModeSelectorFieldAdapter.js
  this._addWidgetProperties(['modeSelector', 'modes']);
  this._modeSelectorPropertyChangeHandler = this._onModeSelectorPropertyChange.bind(this);
};
scout.inherits(scout.ModeSelectorField, scout.ValueField);

scout.ModeSelectorField.prototype._render = function() {
  this.addContainer(this.$parent, 'mode-selector-field');
  this.addLabel();
  this.addStatus();
  if (this.modeSelector) {
    this._renderModeSelector();
    this.modeSelector.on('propertyChange', this._modeSelectorPropertyChangeHandler);
  }
};

// Will also be called by model adapter on property change event
scout.ModeSelectorField.prototype._renderModeSelector = function() {
  this.modeSelector.render();
  this.addField(this.modeSelector.$container);
};

scout.ModeSelectorField.prototype._removeModeSelector = function() {
  this.modeSelector.remove();
  this._removeField();
};

scout.ModeSelectorField.prototype._onModeSelectorPropertyChange = function(event) {
  if (event.propertyName === 'selectedMode') {
    if (event.newValue) {
      this.setValue(event.newValue.ref);
    } else if (this.modeSelector.selectedMode && this.modeSelector.selectedMode.id) {
      this.setValue(this.modeSelector.selectedMode.id);
    } else {
      this.setValue(null);
    }
  }
};
