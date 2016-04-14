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
/* global FormSpecHelper */
describe("FormMenu", function() {
  var session, desktop, helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    desktop = {
      $parent: session.$entryPoint,
      $toolContainer: session.$entryPoint.appendDiv('desktop-tool-box').hide()
    };
  });

  function createAction(model) {
    model.form = helper.createFormWithOneField();
    model.desktop = desktop;

    var action = new scout.FormMenu();
    action.init(model);
    action.position = function() {};
    return action;
  }

  function findToolContainer() {
    return $('.popup');
  }

  describe("onModelPropertyChange", function() {

    describe("selected", function() {

      it("opens and closes the tool container", function() {
        var action = createAction(createSimpleModel('FormMenu', session));
        action.render(session.$entryPoint);
        expect(findToolContainer()).not.toExist();

        var event = createPropertyChangeEvent(action, {
          "selected": true
        });
        action.onModelPropertyChange(event);
        expect(findToolContainer()).toBeVisible();

        event = createPropertyChangeEvent(action, {
          "selected": false
        });
        action.onModelPropertyChange(event);
        expect(findToolContainer()).not.toExist();
      });

    });

  });

});
