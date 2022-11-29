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
import {scout} from '../../../src/index';
import {OutlineSpecHelper} from '../../../src/testing/index';

describe('SearchOutlineAdapter', () => {
  let helper, session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new OutlineSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe('onModelPropertyChange', () => {

    describe('requestFocusQueryField', () => {

      it('may be called multiple times', () => {
        let outline = scout.create(createSimpleModel('SearchOutline', session));
        linkWidgetAndAdapter(outline, 'SearchOutlineAdapter');
        outline.render();

        session.$entryPoint.focus();
        expect(document.activeElement).toBe(session.$entryPoint[0]);
        let event = createPropertyChangeEvent(outline, {
          requestFocusQueryField: null
        });
        outline.modelAdapter.onModelPropertyChange(event);
        expect(document.activeElement).toBe(outline.$queryField[0]);

        session.$entryPoint.focus();
        expect(document.activeElement).toBe(session.$entryPoint[0]);
        event = createPropertyChangeEvent(outline, {
          requestFocusQueryField: null
        });
        outline.modelAdapter.onModelPropertyChange(event);
        expect(document.activeElement).toBe(outline.$queryField[0]);
      });
    });
  });
});
