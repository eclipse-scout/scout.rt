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
import {arrays, events, graphics, HtmlComponent, Mode, ModeSelectorEventMap, ModeSelectorLayout, ModeSelectorModel, PropertyChangeEvent, Widget} from '../index';
import {SwipeCallbackEvent} from '../util/events';

export default class ModeSelector<T> extends Widget implements ModeSelectorModel<T> {
  declare model: ModeSelectorModel<T>;
  declare eventMap: ModeSelectorEventMap<T>;

  modes: Mode<T>[];
  selectedMode: Mode<T>;
  $slider: JQuery;

  /**
   * When a new mode is set, the new one is marked as selected while the old one is deselected. This triggers the modePropertyChangeHandler.
   * In this case the handler must not react on the selection event. Otherwise the value is first set to null (because the old is deselected) and then to the new value.
   * Setting a new mode should not trigger two change events.
   */
  protected _isModeChanging: boolean;

  protected _modePropertyChangeHandler: (event: PropertyChangeEvent<any>) => void;

  constructor() {
    super();
    this._addWidgetProperties(['modes', 'selectedMode']);
    this._addPreserveOnPropertyChangeProperties(['selectedMode']);

    this.modes = [];
    this.selectedMode = null;
    this.$slider = null;

    this._isModeChanging = false;
    this._modePropertyChangeHandler = this._onModePropertyChange.bind(this);
  }

  protected override _init(model: ModeSelectorModel<T>) {
    super._init(model);
    this._setModes(this.modes);
    this._setSelectedMode(this.selectedMode);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('mode-selector');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new ModeSelectorLayout(this));
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderSlider();
    this._renderModes();
  }

  setModes(modes: Mode<T>[]) {
    this.setProperty('modes', modes);
  }

  protected _setModes(modes: Mode<T>[]) {
    this.modes.forEach(mode => mode.off('propertyChange', this._modePropertyChangeHandler));
    this._setProperty('modes', arrays.ensure(modes));
    this.modes.forEach(mode => {
      mode.on('propertyChange', this._modePropertyChangeHandler);
      if (mode.selected) {
        this.setSelectedMode(mode);
      }
    });
  }

  protected _renderSlider() {
    this.$slider = this.$container.appendDiv('mode-slider');
  }

  protected _renderModes() {
    this.modes.forEach(mode => {
      mode.render();
      this._registerDragHandlers(mode.$container);
    });
    this._updateMarkers();
  }

  setSelectedMode(selectedMode: Mode<T>) {
    this.setProperty('selectedMode', selectedMode);
  }

  protected _setSelectedMode(selectedMode: Mode<T>) {
    this._isModeChanging = true;
    if (this.selectedMode && this.selectedMode !== selectedMode) {
      this.selectedMode.setSelected(false);
    }
    if (selectedMode && !selectedMode.selected) {
      selectedMode.setSelected(true);
    }
    this._setProperty('selectedMode', selectedMode);
    this._isModeChanging = false;
    this._updateMarkers();
  }

  protected _onModePropertyChange(event: PropertyChangeEvent<any, Mode<T>>) {
    if (event.propertyName === 'selected' && !this._isModeChanging) {
      this.setSelectedMode(event.newValue ? event.source : null);
    } else if (event.propertyName === 'visible') {
      this._updateMarkers();
    } else if (event.propertyName === 'enabled') {
      this._updateSlider();
    }
  }

  protected _updateMarkers() {
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

  protected _updateSlider() {
    if (!this.$slider) {
      return;
    }
    let selectedModePosX = 0, selectedModeWidth = 0;
    if (this.selectedMode && this.selectedMode.$container) {
      selectedModePosX = graphics.position(this.selectedMode.$container).x;
      selectedModeWidth = graphics.size(this.selectedMode.$container, {exact: true}).width;
    }
    this.$slider.cssLeft(selectedModePosX);
    this.$slider.cssWidth(selectedModeWidth);
    this.$slider.setVisible(this.selectedMode && this.selectedMode.$container && this.selectedMode.enabled);
  }

  protected _registerDragHandlers($mode: JQuery) {
    let className = 'mode-selector-dragging';
    let onDown = (e: SwipeCallbackEvent) => this.enabledComputed && this.selectedMode && this.selectedMode.$container === $mode && this.modes.filter(m => m.isVisible() && m.enabled).length > 1;
    let onMove = (e: SwipeCallbackEvent) => {
      let maxX = this.$container.width() - $mode.outerWidth();
      let minX = 0;
      let newModeLeft = Math.max(Math.min(e.newLeft, maxX), minX); // limit to the size of the ModeSelector
      this.$container.children().addClass(className);
      if (newModeLeft !== e.originalLeft) {
        this.$slider.cssLeft(newModeLeft);
      }
      return newModeLeft;
    };
    let onUp = (e: SwipeCallbackEvent) => {
      this.$container.children().removeClass(className);
      let newSelectedMode = this._computeNewSelectedMode(e);
      if (!newSelectedMode || newSelectedMode === this.selectedMode || !newSelectedMode.enabled) {
        this._updateSlider(); // move back to original position
      } else {
        this.setSelectedMode(newSelectedMode); // updates the slider position
      }
    };
    events.onSwipe($mode, className, onDown, onMove, onUp);
  }

  protected _computeNewSelectedMode(e: SwipeCallbackEvent): Mode<T> {
    if (e.direction === 0 || Math.abs(e.deltaX) <= 5) {
      // ignore if the slide is below threshold
      return this.selectedMode;
    }
    if (e.direction < 0) {
      // slide left: use left end of slider
      return this._findModeByPos(e.newLeft);
    }
    // slide right: use right end of slider
    return this._findModeByPos(e.newLeft + this.$slider.width());
  }

  protected _findModeByPos(pos: number): Mode<T> {
    let visibleModes = this.modes.filter(m => m.isVisible());
    for (let i = visibleModes.length - 1; i >= 0; i--) {
      let mode = visibleModes[i];
      let modePosX = Math.floor(graphics.position(mode.$container).x);
      if (pos >= modePosX) {
        let modeWidth = graphics.size(mode.$container).width;
        let modeEndX = modePosX + modeWidth;
        if (pos <= modeEndX) {
          return mode;
        }
      }
    }
    return null;
  }

  findModeById(id: string): Mode<T> {
    return arrays.find(this.modes, mode => mode.id === id);
  }

  findModeByRef(ref: T): Mode<T> {
    return arrays.find(this.modes, mode => mode.ref === ref);
  }

  selectModeById(id: string) {
    this.setSelectedMode(this.findModeById(id));
  }

  selectModeByRef(ref: T) {
    this.setSelectedMode(this.findModeByRef(ref));
  }
}