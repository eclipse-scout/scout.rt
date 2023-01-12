/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
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

    it('accepts the value', () => {
      field.render();
      field.setValue(25);

      expect(field.value).toBe(null);
      expect(field.slider.value).toBe(25);
      expect(field.displayText).toBe('');
      field.acceptInput();
      expect(field.displayText).toBe('25');

      field.slider.setValue(30);
      field.acceptInput();
      expect(field.value).toBe(null);
      expect(field.slider.value).toBe(30);
      expect(field.displayText).toBe('30');
    });

  });

});
