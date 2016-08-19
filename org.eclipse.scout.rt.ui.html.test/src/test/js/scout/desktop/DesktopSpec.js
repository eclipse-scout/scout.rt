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
  var session, desktop, outlineHelper, formHelper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession({
      desktop: {
        navigationVisible: true,
        headerVisible: true,
        benchVisible: true
      },
      renderDesktop: false
    });
    outlineHelper = new scout.OutlineSpecHelper(session);
    formHelper = new scout.FormSpecHelper(session);
    desktop = session.desktop;
    desktop.viewButtons = [];
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
  });

  describe('notification', function() {
    var ntfc,
      parent = new scout.Widget();

    beforeEach(function() {
      session._renderDesktop();
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

    beforeEach(function() {
      session._renderDesktop();
    });

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

    beforeEach(function() {
      session._renderDesktop();
    });

    it('controls visibility of the bench', function() {
      expect(desktop.benchVisible).toBe(true);
      expect(desktop.bench.rendered).toBe(true);

      desktop.bench.animateRemoval = false; // disable animation because there won't be any animationEnd event
      desktop.setBenchVisible(false);
      expect(desktop.benchVisible).toBe(false);
      expect(desktop.bench).toBeFalsy();

      desktop.setBenchVisible(true);
      expect(desktop.benchVisible).toBe(true);
      expect(desktop.bench.rendered).toBe(true);
    });

    it('removes the content after the animation', function() {
      var form = formHelper.createFormWithOneField();
      var tabBox = desktop.bench.getTabBox('C');
      form.displayHint = scout.Form.DisplayHint.VIEW;
      desktop.showForm(form, desktop);

      expect(form.rendered).toBe(true);
      expect(form.parent).toBe(tabBox);
      expect(form.$container.parent()[0]).toBe(tabBox.$viewContent[0]);
      expect(desktop.benchVisible).toBe(true);
      expect(desktop.bench.rendered).toBe(true);

      // Removal is animated -> does not remove it immediately but after the animation so that the content is visible while the animation runs
      desktop.setBenchVisible(false);
      expect(desktop.benchVisible).toBe(false);
      desktop.hideForm(form);
      // Not removed yet and still linked, will be done after animation
      expect(desktop.bench.rendered).toBe(true); // FIXME [awe, cgu] 6.1 -> see Desktop.js#_removeBench -> this.bench = null
      expect(form.rendered).toBe(true);
      expect(form.parent).toBe(tabBox);

      // trigger actual remove
      jasmine.clock().tick();
      desktop.bench.removalPending = false;
      desktop.bench._removeInternal();
      expect(desktop.bench).toBeFalsy();
      expect(form.rendered).toBe(false);
    });

  });

  describe('navigationVisible', function() {

    beforeEach(function() {
      session._renderDesktop();
    });

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
      expect(detailForm.$container.parent()[0]).toBe(desktop.bench.getTabBox('C').$viewContent[0]);

      // Outline is not visible anymore, but detail form is
      desktop.setNavigationVisible(false);
      // Force removal of navigation
      desktop.onLayoutAnimationComplete();
      expect(desktop.navigationVisible).toBe(false);
      expect(desktop.navigation).toBeFalsy();
      expect(outline.rendered).toBe(false);
      expect(detailForm.rendered).toBe(true);
      expect(detailForm.$container.parent()[0]).toBe(desktop.bench.getTabBox('C').$viewContent[0]);
    });

  });

  describe('headerVisible', function() {

    beforeEach(function() {
      session._renderDesktop();
    });

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

  describe('showForm', function() {

    beforeEach(function() {
      session._renderDesktop();
    });

    it('adds a view to the bench if displayHint is View', function() {
      var form = formHelper.createFormWithOneField();
      var tabBox = desktop.bench.getTabBox('C');
      form.displayHint = scout.Form.DisplayHint.VIEW;
      desktop.showForm(form, desktop);

      expect(form.rendered).toBe(true);
      expect(form.parent).toBe(tabBox);
      expect(form.$container.parent()[0]).toBe(tabBox.$viewContent[0]);
    });

  });

  describe('displayStyle', function() {

    describe('COMPACT', function() {

      beforeEach(function() {
        desktop.displayStyle = scout.Desktop.DisplayStyle.COMPACT;
        // Flags currently only set by server, therefore we need to set them here as well
        desktop.navigationVisible = true;
        desktop.benchVisible = false;
        desktop.headerVisible = false;
        session._renderDesktop();
      });

      it('shows bench and hides navigation if a view is open', function() {
        var form = formHelper.createViewWithOneField();
        expect(form.rendered).toBe(false);
        expect(desktop.navigationVisible).toBe(true);
        expect(desktop.benchVisible).toBe(false);
        expect(desktop.headerVisible).toBe(false);

        desktop.showForm(form, desktop);
        expect(form.rendered).toBe(true);
        expect(desktop.navigationVisible).toBe(false);
        expect(desktop.benchVisible).toBe(true);
        expect(desktop.headerVisible).toBe(true);
      });

      it('hides bench and shows navigation if the last view gets closed', function() {
        var form1 = formHelper.createViewWithOneField();
        var form2 = formHelper.createViewWithOneField();
        expect(form1.rendered).toBe(false);
        expect(form2.rendered).toBe(false);
        expect(desktop.navigationVisible).toBe(true);
        expect(desktop.benchVisible).toBe(false);
        expect(desktop.headerVisible).toBe(false);

        // open first form, bench is shown
        desktop.showForm(form1, desktop);
        expect(form1.rendered).toBe(true);
        expect(form2.rendered).toBe(false);
        expect(desktop.navigationVisible).toBe(false);
        expect(desktop.benchVisible).toBe(true);
        expect(desktop.headerVisible).toBe(true);

        // open second form, bench is still shown
        desktop.showForm(form2, desktop);
        expect(form1.rendered).toBe(true);
        expect(form2.rendered).toBe(true);
        expect(desktop.navigationVisible).toBe(false);
        expect(desktop.benchVisible).toBe(true);
        expect(desktop.headerVisible).toBe(true);

        // close first form, bench is still shown
        desktop.hideForm(form1, desktop);
        expect(form1.rendered).toBe(false);
        expect(form2.rendered).toBe(true);
        expect(desktop.navigationVisible).toBe(false);
        expect(desktop.benchVisible).toBe(true);
        expect(desktop.headerVisible).toBe(true);

        // disable remove animation, otherwise form would not be removed immediately
        desktop.bench.animateRemoval = false;

        // close second form, bench is not shown anymore
        desktop.hideForm(form2, desktop);
        expect(form1.rendered).toBe(false);
        expect(form2.rendered).toBe(false);
        expect(desktop.navigationVisible).toBe(true);
        expect(desktop.benchVisible).toBe(false);
        expect(desktop.headerVisible).toBe(false);
      });

    });
  });

});
