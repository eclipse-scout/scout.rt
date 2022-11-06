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
import {StringField} from '../../../../src/index';
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
});
