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

  /**
   * Test for the case where we had an error-status with a message before and then a status with an empty message is set.
   * In that case the tooltip must be closed. Set ticket 250554.
   */
  it('must hide tooltip when new status has no message', function() {
    var model = helper.createFieldModel();
    var formField = new scout.StringField();
    formField.init(model);
    formField.render();

    // same structure as MultiStatus.java received from UI-server
    var status1 = new scout.Status({
      message: 'Fehler',
      severity: scout.Status.Severity.ERROR,
      children: {
        message: 'Fehler',
        severity: scout.Status.Severity.ERROR
      }
    });
    formField.setErrorStatus(status1);
    expect(session.desktop.$container.find('.tooltip').length).toBe(1);

    // same structure as MultiStatus.java which has no children anymore
    var status2 = new scout.Status({
      message: '',
      severity: scout.Status.Severity.OK});
    formField.setErrorStatus(status2);
    expect(session.desktop.$container.find('.tooltip').length).toBe(0);
  });

});
