/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HtmlField, scout} from '../../../../src/index';

describe('HtmlField', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('acceptInput', () => {
    it('does not change field value and displayText', () => {
      let field = scout.create(HtmlField, {
        parent: session.desktop,
        scrollBarEnabled: true
      });
      field.render();
      field.setValue('<ul>\n' +
        '  <li>AppLink: <span class="app-link" data-ref="param1=XY&param2=YZ">Click me</span></li>\n' +
        '  <li>HTML Link: <a href="https://eclipse.dev/scout/" target="_blank">eclipse.dev/scout/</a></li>\n' +
        '</ul>\n' +
        '<!-- This is an invisible comment -->\n');
      let origValue = field.value;
      let origDisplayText = field.displayText;
      field.acceptInput();

      // value and displayText are unchanged after acceptInput
      expect(field.value).toBe(origValue);
      expect(field.displayText).toBe(origDisplayText);
    });
  });

  describe('empty css class', () => {
    it('is added if field is empty', () => {
      let field = scout.create(HtmlField, {
        parent: session.desktop
      });
      field.render();
      expect(field.$field).toHaveClass('empty');
    });

    it('is not added if field is not empty', () => {
      let field = scout.create(HtmlField, {
        parent: session.desktop,
        value: 'a'
      });
      field.render();
      expect(field.$field).not.toHaveClass('empty');
    });

    it('toggles if value toggles', () => {
      let field = scout.create(HtmlField, {
        parent: session.desktop
      });
      field.render();
      expect(field.$field).toHaveClass('empty');

      field.setValue('a');
      expect(field.$field).not.toHaveClass('empty');

      field.setValue(null);
      expect(field.$field).toHaveClass('empty');
    });
  });
});
