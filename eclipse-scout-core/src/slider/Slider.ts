/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, Device, events, graphics, HtmlComponent, InitModelOf, KeyStrokeContext, SliderEventMap, SliderLayout, SliderModel, SliderNavigationKeyStroke, SliderShiftNavigationKeyStroke, Widget} from '../index';
import $ from 'jquery';

export class Slider extends Widget implements SliderModel {
  declare model: SliderModel;
  declare eventMap: SliderEventMap;
  declare self: Slider;

  protected static CLICK_TOLERANCE = 12;
  protected static FLOATING_POINT_ERROR_CORRECTION = 100000000;

  value: number;
  minValue: number;
  maxValue: number;
  step: number;
  tabbable: boolean;

  protected _mouseMoveHandler = this._onMouseMove.bind(this);
  protected _mouseUpHandler = this._onMouseUp.bind(this);

  $window: JQuery<Window>;
  $track: JQuery<HTMLDivElement>;
  $thumb: JQuery<HTMLDivElement>;

  constructor() {
    super();

    this.value = null;
    this.minValue = null;
    this.maxValue = null;
    this.step = null;
    this.tabbable = true;

    this.$track = null;
    this.$thumb = null;
  }

  protected override _init(options: InitModelOf<this>) {
    super._init(options);
    this.value = options.value;
    this.minValue = options.minValue;
    this.maxValue = options.maxValue;
    this.step = options.step;
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this.registerKeyStrokes([
      new SliderNavigationKeyStroke(this),
      new SliderShiftNavigationKeyStroke(this)
    ]);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('slider')
      .on('blur', this._onBlur.bind(this))
      .on('focus', this._onFocus.bind(this));
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new SliderLayout(this));

    this.$track = this.$container.appendDiv('slider-track');
    this.$thumb = this.$container.appendDiv('slider-thumb');

    this.$window = this.$container.window();
    this.$container.on('mousedown touchstart', this._onMouseDown.bind(this));

    aria.role(this.$container, 'slider');
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderValue();
    this._renderMinValue();
    this._renderMaxValue();
    this._renderStep();
    this._renderTabbable();
  }

  protected override _remove() {
    this.$track = null;
    this.$thumb = null;
    this.$window
      .off('mousemove touchmove', this._mouseMoveHandler)
      .off('mouseup touchend touchcancel', this._mouseUpHandler);
    super._remove();
  }

  protected _onFocus(event: JQuery.FocusEvent) {
    // When the slider is tabbable, it will receive the focus when the user clicks it. However, we only want to set
    // the 'focused' property when the focus happens during keyboard navigation, because this property is used by
    // the SliderField. Note that we cannot use the 'unfocusable' class, because then the keystrokes would not work.
    if (!this.$container?.hasClass('keyboard-navigation')) {
      return;
    }
    this.setFocused(true);
  }

  protected _onBlur(event: JQuery.BlurEvent) {
    this.setFocused(false);
  }

  setValue(value: number) {
    this.setProperty('value', value);
  }

  protected _renderValue() {
    this._setThumbPosition(this._valueToLocalPosition(this._limitValue(this.value)));
    this.$container.attr('aria-valuenow', this.value);
  }

  setMinValue(minValue: number) {
    this.setProperty('minValue', minValue);
  }

  protected _renderMinValue() {
    this._update();
    this.$container.attr('aria-valuemin', this.minValue);
  }

  setMaxValue(maxValue: number) {
    this.setProperty('maxValue', maxValue);
  }

  protected _renderMaxValue() {
    this._update();
    this.$container.attr('aria-valuemax', this.maxValue);
  }

  setStep(step: number) {
    this.setProperty('step', step);
  }

  protected _renderStep() {
    this._update();
  }

  setTabbable(tabbable: boolean) {
    this.setProperty('tabbable', tabbable);
  }

  protected _renderTabbable() {
    this.$container.setTabbable(this.tabbable && this.enabledComputed && !Device.get().supportsOnlyTouch());
  }

  protected override _renderEnabled() {
    super._renderEnabled();
    if (this.rendered) {
      this._renderTabbable();
    }
  }

  protected _onMouseDown(event: JQuery.MouseDownEvent | JQuery.TouchStartEvent) {
    if (!this.enabledComputed) {
      return null;
    }

    events.fixTouchEvent(event);

    this.$window
      .off('mousemove touchmove', this._mouseMoveHandler)
      .off('mouseup touchend touchcancel', this._mouseUpHandler)
      .on('mousemove touchmove', this._mouseMoveHandler)
      .on('mouseup touchend touchcancel', this._mouseUpHandler);
    $('iframe').addClass('dragging-in-progress');

    this._moveThumbTo(event.pageX);
  }

  protected _onMouseMove(event: JQuery.MouseMoveEvent | JQuery.TouchMoveEvent) {
    events.fixTouchEvent(event);
    this._moveThumbTo(event.pageX);
  }

  protected _onMouseUp(event: JQuery.MouseUpEvent | JQuery.TouchEndEvent | JQuery.TouchCancelEvent) {
    this.$window
      .off('mousemove touchmove', this._mouseMoveHandler)
      .off('mouseup touchend touchcancel', this._mouseUpHandler);
    $('iframe').removeClass('dragging-in-progress');
  }

  protected _moveThumbTo(pageX: number) {
    const value = this._normalizeValue(this._localPositionToValue(pageX - this.$container.offset().left));
    this._setThumbPosition(this._valueToLocalPosition(value));
    this.setValue(value);
  }

  protected _setThumbPosition(position: number) {
    const borderLeft = graphics.borders(this.$container).left;
    this.$thumb.cssLeft(position - borderLeft);
    this.$track.cssWidth(position - borderLeft);
  }

  move(moveBy: number) {
    this.setValue(this._normalizeValue(this.value + moveBy));
  }

  /** @internal called by SliderLayout */
  _update() {
    if (this.rendered) {
      this._setThumbPosition(this._valueToLocalPosition(this.value));
    }
  }

  protected _valueToLocalPosition(value: number) {
    if (this.maxValue === this.minValue) {
      return this.$container.cssWidth() / 2;
    }

    // Offset thumb on either side by half its width, so it does not stick out of the slider area
    const offset = this.$thumb.cssWidth() / 2;
    const minPosition = offset;
    const maxPosition = this.$container.cssWidth() - offset;

    let position = (value - this.minValue) / (this.maxValue - this.minValue) * (this.$container.cssWidth() - this.$thumb.cssWidth()) + offset;
    return Math.round(Math.max(minPosition, Math.min(maxPosition, position)));
  }

  protected _localPositionToValue(position: number) {
    const offset = this.$thumb.cssWidth() / 2;
    const minPosition = offset;
    const maxPosition = this.$container.cssWidth() - offset;

    if (position <= minPosition) {
      return this.minValue;
    }
    if (position >= maxPosition) {
      return this.maxValue;
    }
    return (position - offset) / (this.$container.cssWidth() - this.$thumb.cssWidth()) * (this.maxValue - this.minValue) + this.minValue;
  }

  protected _normalizeValue(value: number) {
    return this._limitValue(this._calculateSteppedValue(value));
  }

  protected _limitValue(value: number) {
    return Math.min(Math.max(value, this.minValue), this.maxValue);
  }

  protected _calculateSteppedValue(value: number) {
    if (!this.step) { // 0 or not set
      return value;
    }
    const stepsFromMin = Math.round((value - this.minValue) / this.step);
    const steppedValue = stepsFromMin * this.step + this.minValue;
    return Math.round(steppedValue * Slider.FLOATING_POINT_ERROR_CORRECTION) / Slider.FLOATING_POINT_ERROR_CORRECTION;
  }
}

