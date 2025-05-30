/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, fields, FormField, HtmlComponent, InitModelOf, NumberField, objects, PropertyChangeEvent, scout, Slider, SliderFieldEventMap, SliderFieldLayout, SliderFieldModel, ValueField} from '../../../index';

export class SliderField extends NumberField implements SliderFieldModel {
  declare model: SliderFieldModel;
  declare eventMap: SliderFieldEventMap;

  step: number;
  valueEditable: boolean;
  sliderTabbable: boolean;

  slider: Slider;
  $valueLabel: JQuery;
  fieldHtmlComp: HtmlComponent;

  protected _syncingValue = false;

  constructor() {
    super();
    this.fieldHtmlComp = null;
    this.$valueLabel = null;

    this.fieldStyle = FormField.FieldStyle.CLASSIC;
    this.clearable = ValueField.Clearable.NEVER;
    this.gridDataHints.horizontalAlignment = -1;
    this.updateDisplayTextOnModify = true;

    this.minValue = 0;
    this.maxValue = 100;

    this.step = 1;
    this.valueEditable = true;
    this.sliderTabbable = true;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this.slider = scout.create(Slider, {
      parent: this,
      minValue: this.minValue,
      maxValue: this.maxValue,
      value: this.value,
      step: this.step,
      tabbable: this.sliderTabbable,
      valueEditable: this.valueEditable
    });
    this.slider.on('propertyChange', this._onSliderPropertyChange.bind(this));

    this.on('propertyChange:displayText', event => this._syncDisplayTextToSlider(event.newValue));
  }

  protected override _render() {
    this.addContainer(this.$parent, 'slider-field', new SliderFieldLayout(this));
    this.addLabel();
    this.addMandatoryIndicator();

    let $fieldContainer = this.$container.appendDiv();
    this.fieldHtmlComp = HtmlComponent.install($fieldContainer, this.session);

    this.slider.render($fieldContainer);
    aria.linkElementWithLabel(this.slider.$container, this.$label);

    let $field = fields.makeTextField($fieldContainer, 'field');
    $fieldContainer.append($field);

    this.$valueLabel = $fieldContainer.appendDiv('slider-value');

    this.addFieldContainer($fieldContainer);
    this.addField($field);
    this.addStatus();
  }

  protected override _remove() {
    super._remove();
    this.$valueLabel = null;
    this.fieldHtmlComp = null;
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderValueEditable();
  }

  protected override _renderDisplayText() {
    super._renderDisplayText();
    this.$valueLabel.text(this.displayText);
  }

  protected override _renderClearable() {
    // nop (slider value should never be clearable)
  }

  setStep(step: number) {
    this.setProperty('step', step);
  }

  protected _setStep(step: number) {
    this._setProperty('step', step);
    if (!objects.isNullOrUndefined(step)) {
      this.slider.setStep(step);
    }
  }

  setValueEditable(valueEditable: boolean) {
    this.setProperty('valueEditable', valueEditable);
  }

  protected _renderValueEditable() {
    this.$fieldContainer.toggleClass('value-editable', !!this.valueEditable);
    this.$field.setVisible(this.valueEditable);
    this.$valueLabel.setVisible(!this.valueEditable);

    this.fieldHtmlComp.invalidateLayoutTree(false);
  }

  setSliderTabbable(sliderTabbable: boolean) {
    this.setProperty('sliderTabbable', sliderTabbable);
  }

  protected _setSliderTabbable(sliderTabbable: boolean) {
    this._setProperty('sliderTabbable', sliderTabbable);
    this.slider.setTabbable(sliderTabbable);
  }

  protected override _setMinValue(minValue: number) {
    super._setMinValue(minValue);
    // This null check is required. this.slider is created after super._init is called. Since minValue is a property from NumberField it will be set in super.init which would lead to a NPE.
    if (this.slider) {
      this.slider.setMinValue(minValue);
    }
  }

  protected override _setMaxValue(maxValue: number) {
    super._setMaxValue(maxValue);
    // This null check is required. this.slider is created after super._init is called. Since maxValue is a property from NumberField it will be set in super.init which would lead to a NPE.
    if (this.slider) {
      this.slider.setMaxValue(maxValue);
    }
  }

  protected override _validateValue(value: number): number {
    return super._validateValue(scout.nvl(value, this.minValue));
  }

  protected _onSliderPropertyChange(event: PropertyChangeEvent) {
    if (event.propertyName === 'value' && !this._syncingValue) {
      this.setValue(event.newValue);
    } else if (event.propertyName === 'focused') {
      this.setFocused(event.newValue);
    }
  }

  protected _syncDisplayTextToSlider(displayText: string) {
    try {
      this._syncingValue = true;
      this.slider.setValue(this.parseValue(displayText));
    } catch (error) {
      // nop
    } finally {
      this._syncingValue = false;
    }
  }
}
