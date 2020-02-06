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
import {FieldStatus, scout, Status, StringField} from '../../../src/index';
import {FormSpecHelper} from '@eclipse-scout/testing';

describe('FieldStatus', function() {
  var session, helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
  });

  describe('parent changes visibility', function() {
    var formField, model;

    beforeEach(function() {
      model = helper.createFieldModel();
      formField = new StringField();
      formField.init(model);
      formField.render();
      formField.setErrorStatus({
        severity: Status.Severity.ERROR,
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

  /**
   * Test for the case where we had an error-status with a message before and then a status with an empty message is set.
   * In that case the tooltip must be closed. Set ticket 250554.
   */
  it('must hide tooltip when new status has no message', function() {
    var model = helper.createFieldModel();
    var formField = new StringField();
    formField.init(model);
    formField.render();

    // same structure as MultiStatus.java received from UI-server
    var status1 = new Status({
      message: 'Foo',
      severity: Status.Severity.ERROR,
      children: {
        message: 'Foo',
        severity: Status.Severity.ERROR
      }
    });
    formField.setErrorStatus(status1);
    expect(session.desktop.$container.find('.tooltip').length).toBe(1);

    // same structure as MultiStatus.java which has no children anymore
    var status2 = new Status({
      message: '',
      severity: Status.Severity.OK});
    formField.setErrorStatus(status2);
    expect(session.desktop.$container.find('.tooltip').length).toBe(0);
  });

});
