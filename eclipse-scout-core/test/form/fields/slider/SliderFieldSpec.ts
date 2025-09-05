/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {SliderField} from '../../../../src/index';
import {FormSpecHelper} from '../../../../src/testing/index';
import {InitModelOf} from '../../../../src/scout';

describe('SliderField', () => {
  let session: SandboxSession, helper: FormSpecHelper, field: SliderField;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    field = createField(createModel());
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
    jasmine.Ajax.uninstall();
  });

  function createField(model?: InitModelOf<SliderField>): SliderField {
    let field = new SliderField();
    field.init(model);
    return field;
  }

  function createModel(): InitModelOf<SliderField> {
    return helper.createFieldModel();
  }

  describe('slider', () => {

    it('sets the slider value', () => {
      field.render();
      field.setValue(25);

      expect(field.value).toBe(25);
      expect(field.slider.value).toBe(25);
      expect(field.displayText).toBe('25');

      field.slider.setValue(30);
      expect(field.value).toBe(30);
      expect(field.slider.value).toBe(30);
      expect(field.displayText).toBe('30');
    });

    it('moves the thumb', () => {
      field.render();

      expect(field.value).toBe(0);
      expect(field.slider.value).toBe(0);
      expect(field.displayText).toBe('0');

      field.slider.move(10);
      expect(field.value).toBe(10);
      expect(field.slider.value).toBe(10);
      expect(field.displayText).toBe('10');
    });

    it('moves the thumb to the max with inappropriate step size', () => {
      field.render();

      field.setMinValue(0);
      field.setMaxValue(5);
      field.setStep(4);

      expect(field.value).toBe(0);
      expect(field.slider.value).toBe(0);
      expect(field.displayText).toBe('0');

      field.slider.move(5);
      expect(field.value).toBe(5);
      expect(field.slider.value).toBe(5);
      expect(field.displayText).toBe('5');
    });

    it('limits the min & max value', () => {
      field.render();
      field.setMinValue(-2.5);
      field.setMaxValue(4);

      field.slider.move(-100);
      expect(field.value).toBe(-2.5);

      field.slider.move(200);
      expect(field.value).toBe(4);
    });

    it('steps the value', () => {
      field.render();

      field.setMinValue(0);
      field.setMaxValue(10);
      field.setStep(2);

      field.slider.move(2.5);
      expect(field.value).toBe(2);
    });

    it('can be read only', () => {
      field.render();
      expect(field.$valueLabel.isVisible()).toBe(false);
      expect(field.$field.isVisible()).toBe(true);

      field.setValueEditable(false);

      expect(field.$valueLabel.isVisible()).toBe(true);
      expect(field.$field.isVisible()).toBe(false);
    });

    it('can be tabbable', () => {
      field.render();
      expect(field.slider.$container.isTabbable()).toBe(true);
      field.setSliderTabbable(false);
      expect(field.slider.$container.isTabbable()).toBe(false);
    });

    it('syncs the display text with the slider', () => {
      field.render();
      field.setDisplayText('3');
      expect(field.slider.value).toBe(3);

      field.slider.setValue(4);
      expect(field.displayText).toBe('4');
    });
  });

});
