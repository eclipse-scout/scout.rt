/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.ModeSelector = function() {
  scout.ModeSelector.parent.call(this);
  this._addWidgetProperties(['modes', 'selectedMode']);
  this._addPreserveOnPropertyChangeProperties(['selectedMode']);

  this.modes = [];
  this.selectedMode = null;

  this._modePropertyChangeHandler = this._onModePropertyChange.bind(this);
};
scout.inherits(scout.ModeSelector, scout.Widget);

scout.ModeSelector.prototype._init = function(model) {
  scout.ModeSelector.parent.prototype._init.call(this, model);
  this._setModes(this.modes);
  this._setSelectedMode(this.selectedMode);
};

scout.ModeSelector.prototype._render = function() {
  this.$container = this.$parent.appendDiv('mode-selector');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.ModeSelectorLayout(this));
};

scout.ModeSelector.prototype._renderProperties = function() {
  scout.ModeSelector.parent.prototype._renderProperties.call(this);
  this._renderModes();
};

scout.ModeSelector.prototype.setModes = function(modes) {
  this.setProperty('modes', modes);
};

scout.ModeSelector.prototype._setModes = function(modes) {
  this.modes.forEach(function(mode) {
    mode.off('propertyChange', this._modePropertyChangeHandler);
  }, this);
  this._setProperty('modes', scout.arrays.ensure(modes));
  this.modes.forEach(function(mode) {
    mode.on('propertyChange', this._modePropertyChangeHandler);
    if (mode.selected) {
      this.setSelectedMode(mode);
    }
  }, this);
};

scout.ModeSelector.prototype._renderModes = function() {
  this.modes.forEach(function(mode) {
    mode.render();
  });
  this._updateMarkers();
};

scout.ModeSelector.prototype.setSelectedMode = function(selectedMode) {
  this.setProperty('selectedMode', selectedMode);
};

scout.ModeSelector.prototype._setSelectedMode = function(selectedMode) {
  if (this.selectedMode && this.selectedMode !== selectedMode) {
    this.selectedMode.setSelected(false);
  }
  if (selectedMode && !selectedMode.selected) {
    selectedMode.setSelected(true);
  }
  this._setProperty('selectedMode', selectedMode);
};

scout.ModeSelector.prototype._onModePropertyChange = function(event) {
  if (event.propertyName === 'selected' && event.newValue) {
    this.setSelectedMode(event.source);
  } else if (event.propertyName === 'visible') {
    this._updateMarkers();
  }
};

scout.ModeSelector.prototype._updateMarkers = function() {
  var visibleModes = [];
  this.modes.forEach(function(mode) {
    if (mode.rendered) {
      mode.$container.removeClass('first last');
      if (mode.isVisible()) {
        visibleModes.push(mode);
      }
    }
  });
  if (visibleModes.length) {
    visibleModes[0].$container.addClass('first');
    visibleModes[visibleModes.length - 1].$container.addClass('last');
  }
};

scout.ModeSelector.prototype.findModeById = function(id) {
  return scout.arrays.find(this.modes, function(mode) {
    return mode.id === id;
  });
};

scout.ModeSelector.prototype.findModeByRef = function(ref) {
  return scout.arrays.find(this.modes, function(mode) {
    return mode.ref === ref;
  });
};

scout.ModeSelector.prototype.selectModeById = function(id) {
  this.setSelectedMode(this.findModeById(id));
};

scout.ModeSelector.prototype.selectModeByRef = function(ref) {
  this.setSelectedMode(this.findModeByRef(ref));
};
