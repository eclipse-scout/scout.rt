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
import {arrays, events, HtmlComponent, ModeSelectorLayout, styles, Widget} from '../index';

export default class ModeSelector extends Widget {

  static SLIDER_PADDING = null; // Configured in sizes.css

  constructor() {
    super();
    this._addWidgetProperties(['modes', 'selectedMode']);
    this._addPreserveOnPropertyChangeProperties(['selectedMode']);

    this.modes = [];
    this.selectedMode = null;
    this.$slider = null;

    ModeSelector.SLIDER_PADDING = $.pxToNumber(styles.get('mode-slider-padding', 'padding-left')['paddingLeft']);

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
    this._renderSlider();
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

  _renderSlider() {
    this.$slider = this.$container.appendDiv('mode-slider');
    this._updateSlider();
  }

  _renderModes() {
    this.modes.forEach(mode => {
      mode.render();
      this._registerDragHandlers(mode.$container);
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
    this._setProperty('selectedMode', selectedMode);
    this._updateMarkers();
  }

  _onModePropertyChange(event) {
    if (event.propertyName === 'selected' && event.newValue) {
      this.setSelectedMode(event.source);
    } else if (event.propertyName === 'visible') {
      this._updateMarkers();
    } else if (event.propertyName === 'enabled') {
      this._updateSlider();
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
    this._updateSlider();
  }

  _updateSlider() {
    if (!this.$slider) {
      return;
    }
    if (!this.selectedMode) {
      this.$slider.setVisible(false);
      return;
    }

    let visibleNodes = this.modes.filter(m => m.isVisible());
    let index = visibleNodes.indexOf(this.selectedMode);
    let sliderVisible = index >= 0 && this.selectedMode.enabled; // do not use enabledComputed here as it still contains the old value
    this.$slider.setVisible(sliderVisible);
    if (!sliderVisible) {
      return;
    }

    let cssSliderWidth = '(100% - ' + 2 * ModeSelector.SLIDER_PADDING + 'px) / ' + visibleNodes.length;
    let sliderPosX = ModeSelector.SLIDER_PADDING + 'px + ((' + cssSliderWidth + ') * ' + index + ')';
    this.$slider.cssLeft('calc(' + sliderPosX + ')');
    this.$slider.cssWidth('calc(' + cssSliderWidth + ')');
  }

  _registerDragHandlers($mode) {
    let className = 'mode-selector-dragging';
    let onDown = /** @type {SwipeCallbackEvent} */e => this.selectedMode && this.selectedMode.$container === $mode;
    let onMove = /** @type {SwipeCallbackEvent} */e => {
      let maxX = this.$container.width() - $mode.outerWidth() - ModeSelector.SLIDER_PADDING + 1;
      let minX = ModeSelector.SLIDER_PADDING;
      let newModeLeft = Math.max(Math.min(e.newLeft, maxX), minX); // limit to the size of the ModeSelector
      this.$container.children().addClass(className);
      if (newModeLeft !== e.originalLeft) {
        this.$slider.cssLeft(newModeLeft);
      }
      return newModeLeft;
    };
    let onUp = /** @type {SwipeCallbackEvent} */e => {
      this.$container.children().removeClass(className);
      let visibleModes = this.modes.filter(m => m.isVisible());
      let modeWidth = this.$container.width() / visibleModes.length;
      let modeCenter = e.newLeft + (modeWidth / 2.0);
      let index = Math.floor(modeCenter / modeWidth);
      let newSelectedMode = visibleModes[index];
      if (newSelectedMode === this.selectedMode) {
        this._updateSlider(); // move back to original position
      } else {
        this.setSelectedMode(newSelectedMode); // updates the slider position
      }
    };
    events.onSwipe($mode, className, onDown, onMove, onUp);
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
