/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("SearchOutlineAdapter", function() {
  var helper, session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.OutlineSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe("onModelPropertyChange", function() {

    describe("requestFocusQueryField", function() {

      it("may be called multiple times", function() {
        var outline = scout.create(createSimpleModel('SearchOutline', session));
        linkWidgetAndAdapter(outline, 'SearchOutlineAdapter');
        outline.render();

        session.$entryPoint.focus();
        expect(document.activeElement).toBe(session.$entryPoint[0]);
        var event = createPropertyChangeEvent(outline, {
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
