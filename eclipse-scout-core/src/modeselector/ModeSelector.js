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
import {arrays, HtmlComponent, ModeSelectorLayout, Widget} from '../index';

export default class ModeSelector extends Widget {

  constructor() {
    super();
    this._addWidgetProperties(['modes', 'selectedMode']);
    this._addPreserveOnPropertyChangeProperties(['selectedMode']);

    this.modes = [];
    this.selectedMode = null;

    this._modePropertyChangeHandler = this._onModePropertyChange.bind(this);
  }

  _init(model) {
    super._init(model);
    this._setModes(this.modes);
    this._setSelectedMode(this.selectedMode);
  }

  _render() {
    this.$container = this.$parent.appendDiv('mode-selector');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new ModeSelectorLayout(this));
  }

  _renderProperties() {
    super._renderProperties();
    this._renderModes();
  }

  setModes(modes) {
    this.setProperty('modes', modes);
  }

  _setModes(modes) {
    this.modes.forEach(function(mode) {
      mode.off('propertyChange', this._modePropertyChangeHandler);
    }, this);
    this._setProperty('modes', arrays.ensure(modes));
    this.modes.forEach(function(mode) {
      mode.on('propertyChange', this._modePropertyChangeHandler);
      if (mode.selected) {
        this.setSelectedMode(mode);
      }
    }, this);
  }

  _renderModes() {
    this.modes.forEach(mode => {
      mode.render();
    });
    this._updateMarkers();
  }

  setSelectedMode(selectedMode) {
    this.setProperty('selectedMode', selectedMode);
  }

  _setSelectedMode(selectedMode) {
    if (this.selectedMode && this.selectedMode !== selectedMode) {
      this.selectedMode.setSelected(false);
    }
    if (selectedMode && !selectedMode.selected) {
      selectedMode.setSelected(true);
    }
    this._updateMarkers();
    this._setProperty('selectedMode', selectedMode);
  }

  _onModePropertyChange(event) {
    if (event.propertyName === 'selected' && event.newValue) {
      this.setSelectedMode(event.source);
    } else if (event.propertyName === 'visible') {
      this._updateMarkers();
    }
  }

  _updateMarkers() {
    let visibleModes = [];
    let selectedModeIndex = -1;
    this.modes.forEach(mode => {
      if (mode.rendered) {
        mode.$container.removeClass('first last after-selected');
        if (mode.isVisible()) {
          visibleModes.push(mode);
          if (mode.selected) {
            selectedModeIndex = visibleModes.length - 1;
          }
        }
      }
    });
    if (visibleModes.length) {
      visibleModes[0].$container.addClass('first');
      visibleModes[visibleModes.length - 1].$container.addClass('last');
      if (selectedModeIndex >= 0 && selectedModeIndex < (visibleModes.length - 1)) {
        visibleModes[selectedModeIndex + 1].$container.addClass('after-selected');
      }
    }
  }

  findModeById(id) {
    return arrays.find(this.modes, mode => {
      return mode.id === id;
    });
  }

  findModeByRef(ref) {
    return arrays.find(this.modes, mode => {
      return mode.ref === ref;
    });
  }

  selectModeById(id) {
    this.setSelectedMode(this.findModeById(id));
  }

  selectModeByRef(ref) {
    this.setSelectedMode(this.findModeByRef(ref));
  }
}
