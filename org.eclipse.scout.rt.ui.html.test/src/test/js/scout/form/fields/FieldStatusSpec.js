/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
describe('FieldStatus', function() {
  var session, helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
  });

  describe('parent changes visibility', function() {
    var formField, model;

    beforeEach(function() {
      model = helper.createFieldModel();
      formField = new scout.StringField();
      formField.init(model);
      formField.render();
      formField.setErrorStatus({
        severity: scout.Status.Severity.ERROR,
        message: 'foo'
      });
    });

    it('when desktop becomes invisible popup must be removed', function() {
      expect(formField.fieldStatus.tooltip.rendered).toBe(true);

      // hide desktop -> tooltip must be removed too
      formField.parent.setVisible(false);
      expect(formField.fieldStatus.tooltip.rendered).toBe(false);

      // show desktop again -> tooltip must be rendered again
      // happens in a timeout, thus we must let the clock tick
      formField.parent.setVisible(true);
      jasmine.clock().tick();
      expect(formField.fieldStatus.tooltip.rendered).toBe(true);
    });

    /**
     * This test relies on a property change event for the 'parent' property, triggered by Widget.js.
     */
    it('must update listeners when one of its parent changes', function() {
      var groupBox = scout.create('GroupBox', {
        parent: session.desktop
      });
      groupBox.render();
      formField.setParent(groupBox);

      // hide groupBox -> tooltip must be removed too
      groupBox.setVisible(false);
      expect(formField.fieldStatus.tooltip.rendered).toBe(false);

      // show groupBox again -> tooltip must be rendered again
      // happens in a timeout, thus we must let the clock tick
      groupBox.setVisible(true);
      jasmine.clock().tick();
      expect(formField.fieldStatus.tooltip.rendered).toBe(true);
    });

    it('de-register all listeners when tooltip is destroyed', function() {
      // parents = StringField, Desktop, NullWidget, NullWidget (root parent)
      expect(formField.fieldStatus._parents.length).toBe(4);
      formField.setErrorStatus(null);
      expect(formField.fieldStatus._parents.length).toBe(0);
    });

  });

});
