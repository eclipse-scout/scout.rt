/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {HtmlField, scout} from '../../../../src/index';

describe('HtmlField', () => {
  let session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('acceptInput', () => {
    it('does not change field value and displayText', () => {
      let field = scout.create('HtmlField', {
        parent: session.desktop,
        scrollBarEnabled: true
      });
      field.render();
      field.setValue('<ul>\n' +
        '  <li>AppLink: <span class="app-link" data-ref="param1=XY&param2=YZ">Click me</span></li>\n' +
        '  <li>HTML Link: <a href="https://www.eclipse.org/scout" target="_blank">eclipse.org/scout</a></li>\n' +
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
});
