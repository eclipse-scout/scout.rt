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
describe('Desktop', function() {
  var session, desktop;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    desktop = session.desktop;
    desktop.viewButtons = [];
  });

  describe('_addNullOutline', function() {

    it('should add null-outline when outline of model doesn\'t exist', function() {
      var ovb, outline = null;
      desktop._addNullOutline(outline);
      expect(desktop.viewButtons.length).toBe(1);
      ovb = desktop.viewButtons[0];
      expect(desktop.outline).toBe(ovb.outline);
      expect(ovb.visibleInMenu).toBe(false);
    });

    it('shouldn\'t do anything when model already has an outline', function() {
      var outline = {};
      desktop.outline = outline;
      desktop._addNullOutline(outline);
      expect(desktop.outline).toBe(outline);
      expect(desktop.viewButtons.length).toBe(0);
    });

  });

  describe('notification', function() {
    var ntfc,
      parent = new scout.Widget();

    beforeEach(function() {
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

});
