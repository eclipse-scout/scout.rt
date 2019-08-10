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
scout.ModeSelectorFieldAdapter = function() {
  scout.ModeSelectorFieldAdapter.parent.call(this);
};
scout.inherits(scout.ModeSelectorFieldAdapter, scout.ValueFieldAdapter);

scout.ModeSelectorFieldAdapter.prototype._createWidget = function(model) {
  this._addModeSelector(model);
  return scout.ModeSelectorFieldAdapter.parent.prototype._createWidget.call(this, model);
};

scout.ModeSelectorFieldAdapter.prototype._addModeSelector = function(model) {
  model.modeSelector = {
    objectType: 'ModeSelector'
  };
};

scout.ModeSelectorFieldAdapter.prototype._postCreateWidget = function() {
  this.widget.modeSelector.setModes(this.widget.modes);
  delete this.widget.modes;
};
