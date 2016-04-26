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
  var session, desktop, outlineHelper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession({desktop: {navigationVisible: true, headerVisible: true, benchVisible: true}});
    outlineHelper = new scout.OutlineSpecHelper(session);
    desktop = session.desktop;
    desktop.viewButtons = [];
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

  describe('outline', function() {
    it('gets displayed in desktop navigation ', function() {
      var model = outlineHelper.createModelFixture(3, 2);
      var outline = outlineHelper.createOutline(model);

      expect(desktop.outline).toBeFalsy();
      expect(outline.rendered).toBe(false);

      desktop.setOutline(outline);
      expect(desktop.outline).toBe(outline);
      expect(outline.rendered).toBe(true);
      expect(outline.$container.parent()[0]).toBe(desktop.navigation.$body[0]);
    });

  });

  describe('benchVisible', function() {
    it('controls visibility of the bench', function() {
      expect(desktop.benchVisible).toBe(true);
      expect(desktop.bench.rendered).toBe(true);

      desktop.setBenchVisible(false);
      expect(desktop.benchVisible).toBe(false);
      // Force removal of bench
      desktop.onLayoutAnimationComplete();
      expect(desktop.bench).toBeFalsy();

      desktop.setBenchVisible(true);
      expect(desktop.benchVisible).toBe(true);
      expect(desktop.bench.rendered).toBe(true);
    });

  });

  describe('navigationVisible', function() {
    it('controls visibility of the navigation', function() {
      expect(desktop.navigationVisible).toBe(true);
      expect(desktop.navigation.rendered).toBe(true);

      desktop.setNavigationVisible(false);
      // Force removal of navigation
      desktop.onLayoutAnimationComplete();
      expect(desktop.navigationVisible).toBe(false);
      expect(desktop.navigation).toBeFalsy();

      desktop.setNavigationVisible(true);
      expect(desktop.navigationVisible).toBe(true);
      expect(desktop.navigation.rendered).toBe(true);
    });

    it('only affects content in navigation, not in bench or header', function() {
      var outline = outlineHelper.createOutlineWithOneDetailForm();
      var detailForm = outline.nodes[0].detailForm;
      // because outline is the owner, it is parent as well if created by server -> simulate this
      detailForm.setParent(outline);

      desktop.setOutline(outline);
      outline.selectNodes(outline.nodes[0]);

      expect(desktop.navigationVisible).toBe(true);
      expect(desktop.navigation.rendered).toBe(true);
      expect(outline.rendered).toBe(true);
      expect(outline.$container.parent()[0]).toBe(desktop.navigation.$body[0]);
      expect(detailForm.rendered).toBe(true);
      expect(detailForm.$container.parent()[0]).toBe(desktop.bench.getViewArea('C').$viewContent[0]);

      // Outline is not visible anymore, but detail form is
      desktop.setNavigationVisible(false);
      // Force removal of navigation
      desktop.onLayoutAnimationComplete();
      expect(desktop.navigationVisible).toBe(false);
      expect(desktop.navigation).toBeFalsy();
      expect(outline.rendered).toBe(false);
      expect(detailForm.rendered).toBe(true);
      expect(detailForm.$container.parent()[0]).toBe(desktop.bench.getViewArea('C').$viewContent[0]);
    });

  });

  describe('headerVisible', function() {
    it('controls visibility of the header', function() {
      expect(desktop.headerVisible).toBe(true);
      expect(desktop.header.rendered).toBe(true);

      desktop.setHeaderVisible(false);
      // Force removal of header
      desktop.onLayoutAnimationComplete();
      expect(desktop.headerVisible).toBe(false);
      expect(desktop.header).toBeFalsy();

      desktop.setHeaderVisible(true);
      expect(desktop.headerVisible).toBe(true);
      expect(desktop.header.rendered).toBe(true);
    });

  });

});
