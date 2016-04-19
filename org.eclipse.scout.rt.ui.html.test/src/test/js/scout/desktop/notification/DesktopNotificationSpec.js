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
describe('DesktopNotification', function() {
  var session, helper, $sandbox,
    parent = new scout.Widget();

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = session.$entryPoint;
    parent.session = session;
  });

  it('_init copies properties from event (model)', function() {
    var ntfc = new scout.DesktopNotification();
    ntfc.init({
      parent: parent,
      id: 'foo',
      duration: 123,
      closable: true,
      status: {
        message: 'bar',
        severity: scout.Status.Severity.OK
      }
    });
    expect(ntfc.id).toBe('foo');
    expect(ntfc.duration).toBe(123);
    expect(ntfc.closable).toBe(true);
    expect(ntfc.status.message).toBe('bar');
    expect(ntfc.status.severity).toBe(scout.Status.Severity.OK);
  });

  it('has close-icon when notification is closable', function() {
    var ntfc = scout.create('DesktopNotification', {
      parent: parent,
      id: 'foo',
      duration: 123,
      closable: true,
      status: {
        message: 'bar',
        severity: scout.Status.Severity.OK
      }
    });
    ntfc.render($sandbox);
    expect(ntfc.$container.find('.closer').length).toBe(1);
    expect(ntfc.$container.find('.notification-content').text()).toBe('bar');
    expect(ntfc.$container.hasClass('ok')).toBe(true);
  });

});
