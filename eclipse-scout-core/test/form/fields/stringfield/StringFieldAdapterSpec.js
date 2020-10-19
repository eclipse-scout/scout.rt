/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {StringField} from '../../../../src/index';
import {FormSpecHelper} from '../../../../src/testing/index';

describe('StringFieldAdapter', () => {
  let session, helper;

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

  function createField(model) {
    let field = new StringField();
    field.init(model);
    return field;
  }

  function createModel() {
    return helper.createFieldModel();
  }

  describe('onModelPropertyChange', () => {

    describe('insertText', () => {

      it('may be called multiple times with the same text', () => {
        let field = createField(createModel());
        linkWidgetAndAdapter(field, 'StringFieldAdapter');
        field.render();
        expect(field.$field[0].value).toBe('');

        let event = createPropertyChangeEvent(field, {
          insertText: 'hello'
        });
        field.modelAdapter.onModelPropertyChange(event);
        expect(field.$field[0].value).toBe('hello');

        event = createPropertyChangeEvent(field, {
          insertText: 'hello'
        });
        field.modelAdapter.onModelPropertyChange(event);
        expect(field.$field[0].value).toBe('hellohello');
      });
    });
  });
});
