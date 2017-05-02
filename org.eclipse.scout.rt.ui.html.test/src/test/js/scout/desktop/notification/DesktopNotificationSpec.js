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
describe('DesktopNotification', function() {
  var session, helper, $sandbox,
    parent = new scout.Widget();

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = session.$entryPoint;
    parent.session = session;
  });

  it('will fade in, be added to the desktop and be renderd upon show() ', function() {
    var notification = scout.create('DesktopNotification', {
      parent: parent,
      id: 'foo',
      duration: -1
    });
    spyOn(notification, 'fadeIn').and.callThrough();
    notification.show();
    expect(notification.rendered).toBe(true);
    expect(notification.fadeIn).toHaveBeenCalled();
    expect(session.desktop.notifications[0]).toBe(notification);
  });

  it('will fade out and be removed from the dektop upon hide()', function() {
    var notification = scout.create('DesktopNotification', {
      parent: parent,
      id: 'foo',
      duration: -1
    });
    spyOn(notification, 'fadeOut').and.callThrough();
    notification.show();
    notification.hide();
    expect(notification.fadeOut).toHaveBeenCalled();
    expect(session.desktop.notifications[0]).toBe(undefined);
  });

  it('_init copies properties from event (model)', function() {
    var notification = new scout.DesktopNotification();
    notification.init({
      parent: parent,
      id: 'foo',
      duration: 123,
      closable: true,
      status: {
        message: 'bar',
        severity: scout.Status.Severity.OK
      }
    });
    expect(notification.id).toBe('foo');
    expect(notification.duration).toBe(123);
    expect(notification.closable).toBe(true);
    expect(notification.status.message).toBe('bar');
    expect(notification.status.severity).toBe(scout.Status.Severity.OK);
  });

  it('has close-icon when notification is closable', function() {
    var notification = scout.create('DesktopNotification', {
      parent: parent,
      id: 'foo',
      duration: 123,
      closable: true,
      status: {
        message: 'bar',
        severity: scout.Status.Severity.OK
      }
    });
    notification.render($sandbox);
    expect(notification.$container.find('.closer').length).toBe(1);
    expect(notification.$container.find('.desktop-notification-content').text()).toBe('bar');
    expect(notification.$container.hasClass('ok')).toBe(true);
  });

});
