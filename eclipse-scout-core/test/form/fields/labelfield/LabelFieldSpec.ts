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
import {FormSpecHelper} from '../../../../src/testing/index';
import {LabelField} from '../../../../src';

describe('LabelField', () => {
  let session: SandboxSession;
  let helper: FormSpecHelper;
  let field: LabelField;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    field = helper.createField(LabelField);
  });

  describe('HtmlEnabled', () => {

    it('if false, encodes html in display text', () => {
      field.htmlEnabled = false;
      field.displayText = '<b>Hello</b>';
      field.render();
      expect(field.$field.html()).toBe('&lt;b&gt;Hello&lt;/b&gt;');
    });

    it('if true, does not encode html in display text', () => {
      field.htmlEnabled = true;
      field.displayText = '<b>Hello</b>';
      field.render();
      expect(field.$field.html()).toBe('<b>Hello</b>');
    });

    it('if false, replaces \n with br tag and encodes other text', () => {
      field.htmlEnabled = false;
      field.displayText = '<b>Hello</b>\nGoodbye';
      field.render();
      expect(field.$field.html()).toBe('&lt;b&gt;Hello&lt;/b&gt;<br>Goodbye');
    });

    it('if true, does not replace \n with br tag and does not encode other text', () => {
      field.htmlEnabled = true;
      field.displayText = '<b>Hello</b>\nGoodbye';
      field.render();
      expect(field.$field.html()).toBe('<b>Hello</b>\nGoodbye');
    });
  });

  describe('acceptInput', () => {

    /**
     * If acceptInput wasn't overridden this test would call parseValue and set the touched property.
     */
    it('must be a NOP operation', () => {
      field.setValue('foo');
      field.markAsSaved();
      expect(field.touched).toBe(false);
      field.acceptInput();
      expect(field.touched).toBe(false);
    });

  });

});
