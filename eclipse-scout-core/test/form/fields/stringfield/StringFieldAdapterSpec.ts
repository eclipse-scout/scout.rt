/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {StringField, StringFieldAdapter} from '../../../../src/index';
import {FormSpecHelper} from '../../../../src/testing/index';
import {FullModelOf, InitModelOf} from '../../../../src/scout';

describe('StringFieldAdapter', () => {
  let session: SandboxSession, helper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
    jasmine.Ajax.uninstall();
  });

  function createField(model: InitModelOf<StringField>): StringField {
    let field = new StringField();
    field.init(model);
    return field;
  }

  function createModel(): FullModelOf<StringField> {
    return helper.createFieldModel() as FullModelOf<StringField>;
  }

  describe('onModelPropertyChange', () => {

    describe('insertText', () => {

      it('may be called multiple times with the same text', () => {
        let field = createField(createModel());
        linkWidgetAndAdapter(field, 'StringFieldAdapter');
        field.render();
        let $fieldElement = field.$field[0] as HTMLInputElement;
        expect($fieldElement.value).toBe('');

        let event = createPropertyChangeEvent(field, {
          insertText: 'hello'
        });
        field.modelAdapter.onModelPropertyChange(event);
        expect($fieldElement.value).toBe('hello');

        event = createPropertyChangeEvent(field, {
          insertText: 'hello'
        });
        field.modelAdapter.onModelPropertyChange(event);
        expect($fieldElement.value).toBe('hellohello');
      });
    });
  });

  describe('current menu types', () => {

    it('is initialized correctly', () => {
      let model = {
        id: '123',
        objectType: 'StringField',
        session: session,
        currentMenuTypes: ['ValueField.Null']
      };
      let adapter = new StringFieldAdapter();
      adapter.init(model);
      let field = adapter.createWidget(model, session.desktop) as StringField;

      expect(field.getCurrentMenuTypes()).toEqual(['ValueField.Null']);
    });
  });
});
