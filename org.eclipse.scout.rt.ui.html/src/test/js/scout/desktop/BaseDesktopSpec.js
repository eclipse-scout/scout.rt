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
describe('BaseDesktop', function() {
  var session, helper, $sandbox, desktop, ntfc,
    parent = new scout.Widget();

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    desktop = session.desktop;
    parent.session = session;
    ntfc = scout.create('DesktopNotification', {
      id: 'theID',
      parent: desktop,
      status: {}
    });
  });

  it('addNotification', function() {
    spyOn(ntfc, 'fadeIn');
    desktop.addNotification(ntfc);
    expect(ntfc.fadeIn).toHaveBeenCalled();
    expect(desktop.notifications.indexOf(ntfc)).toBe(0);
    expect(desktop.$container.find('.notifications').length).toBe(1);
    expect(desktop.$notification).not.toBe(null);
  });

  it('removeNotification with object', function() {
    spyOn(ntfc, 'fadeOut');
    desktop.addNotification(ntfc); // first add -> create $notifications DIV
    desktop.removeNotification(ntfc);
    expect(ntfc.fadeOut).toHaveBeenCalled();
  });

  it('removeNotification with (string) ID', function() {
    spyOn(ntfc, 'fadeOut');
    desktop.addNotification(ntfc); // first add -> create $notifications DIV
    desktop.removeNotification('theID');
    expect(ntfc.fadeOut).toHaveBeenCalled();
  });

  it('_onNotificationRemoved - last notifications removes $notifications DIV', function() {
    desktop.addNotification(ntfc); // first add -> create $notifications DIV
    desktop._onNotificationRemoved(ntfc);
    expect(desktop.notifications.length).toBe(0);
    expect(desktop.$container.find('.notifications').length).toBe(0);
    expect(desktop.$notifications).toBe(null);
  });
});
