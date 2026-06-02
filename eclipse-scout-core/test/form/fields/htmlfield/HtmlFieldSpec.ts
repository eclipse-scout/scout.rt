/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, Form, GroupBox, HtmlField, PopupWindow, scout} from '../../../../src/index';

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

  describe('nonce in popup window', () => {
    let origNonce = null;
    beforeEach(() => {
      origNonce = App.get().nonce;
    });

    afterEach(() => {
      App.get().nonce = origNonce;
    });

    it('is correctly replaced', () => {
      const rootNonce = 'orig-nonce';
      App.get().nonce = rootNonce;
      const form = scout.create({
        objectType: Form,
        rootGroupBox: {
          id: 'root',
          objectType: GroupBox,
          fields: [{
            id: 'html',
            objectType: HtmlField
          }]
        },
        parent: session.desktop
      });
      form.render();
      const popup = new PopupWindow(window, form);
      popup.nonce = 'expected-nonce';
      const field = form.widget('html') as HtmlField;
      field.setDisplayText(`<script nonce="${rootNonce}"></script>`);
      expect(field.$field.html()).toEqual(`<script nonce="${popup.nonce}"></script>`);
    });
  });

  describe('has-text css class', () => {
    it('is not added if field is empty', () => {
      let field = scout.create(HtmlField, {
        parent: session.desktop
      });
      field.render();
      expect(field.$field).not.toHaveClass('has-text');
    });

    it('is added if field is not empty', () => {
      let field = scout.create(HtmlField, {
        parent: session.desktop,
        value: 'a'
      });
      field.render();
      expect(field.$field).toHaveClass('has-text');
    });

    it('toggles if value toggles', () => {
      let field = scout.create(HtmlField, {
        parent: session.desktop
      });
      field.render();
      expect(field.$field).not.toHaveClass('has-text');

      field.setValue('a');
      expect(field.$field).toHaveClass('has-text');

      field.setValue(null);
      expect(field.$field).not.toHaveClass('has-text');
    });
  });
});
