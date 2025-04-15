/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, Device, events, HtmlComponent, InitModelOf, KeyStrokeContext, objects, SliderEventMap, SliderLayout, SliderModel, SliderNavigationKeyStroke, SliderShiftNavigationKeyStroke, Widget} from '../index';
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
  $sliderTrack: JQuery<HTMLDivElement>;
  $sliderThumb: JQuery<HTMLDivElement>;

  constructor() {
    super();

    this.value = null;
    this.minValue = null;
    this.maxValue = null;
    this.step = null;
    this.tabbable = true;

    this.$sliderTrack = null;
    this.$sliderThumb = null;
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
    this.$container = this.$parent.appendDiv('slider');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new SliderLayout(this));
    this.$sliderTrack = this.$container.appendDiv('slider-track') as JQuery<HTMLDivElement>;
    this.$sliderThumb = this.$container.appendDiv('slider-thumb') as JQuery<HTMLDivElement>;

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
    this.$sliderTrack = null;
    this.$sliderThumb = null;
    this.$window
      .off('mousemove touchmove', this._mouseMoveHandler)
      .off('mouseup touchend touchcancel', this._mouseUpHandler);
    super._remove();
  }

  setValue(value: number) {
    this.setProperty('value', value);
  }

  protected _renderValue() {
    this._setThumbPosition(this._valueToLocalPosition(this._limitValue(this.value)));
    this.$container.attr('valuenow', this.value);
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
    this.$sliderThumb.cssLeft(position);
    this.$sliderTrack.cssWidth(position);
  }

  move(moveBy: number) {
    this.setValue(this._normalizeValue(this.value + moveBy));
  }

  /** @internal */
  _update() {
    if (this.rendered) {
      this._setThumbPosition(this._valueToLocalPosition(this._normalizeValue(this.value)));
    }
  }

  protected _valueToLocalPosition(value: number) {
    if (this.maxValue === this.minValue) {
      return this.$container.cssWidth() / 2;
    }

    return (value - this.minValue) / (this.maxValue - this.minValue) * this.$container.cssWidth();
  }

  protected _localPositionToValue(position: number) {
    return position / this.$container.cssWidth() * (this.maxValue - this.minValue) + this.minValue;
  }

  protected _normalizeValue(value: number) {
    return this._limitValue(this._calculateSteppedValue(value));
  }

  protected _limitValue(value: number) {
    return Math.min(Math.max(value, this.minValue), this.maxValue);
  }

  protected _calculateSteppedValue(value: number) {
    if (objects.isNullOrUndefined(this.step) || this.step === 0) {
      return value;
    }
    const stepsFromMin = Math.round((value - this.minValue) / this.step);
    const steppedValue = stepsFromMin * this.step + this.minValue;
    return Math.round(steppedValue * Slider.FLOATING_POINT_ERROR_CORRECTION) / Slider.FLOATING_POINT_ERROR_CORRECTION;
  }
}

