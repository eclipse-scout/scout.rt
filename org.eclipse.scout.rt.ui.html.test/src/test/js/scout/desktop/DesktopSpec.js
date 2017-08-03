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
      expect(desktop.$container.find('.desktop-notifications').length).toBe(1);
      expect(desktop.$notification).not.toBe(null);
    });

    it('schedules addNotification when desktop is not rendered', function() {
      desktop.remove();
      scout.create('DesktopNotification', {
        parent: desktop,
        status: {
          severity: scout.Status.Severity.OK,
          message: 'Test'
        },
        duration: 3000,
        closable: true
      }).show();
      expect(desktop.notifications.length).toBe(1);
      expect(session.$entryPoint.find('.desktop-notifications').length).toBe(0);

      desktop.render(session.$entryPoint);
      expect(desktop.notifications.length).toBe(1);
      expect(desktop.$container.find('.desktop-notifications').length).toBe(1);
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

    it('_onNotificationRemove - last notifications removes $notifications DIV', function() {
      desktop.addNotification(ntfc); // first add -> create $notifications DIV
      desktop.removeNotification(ntfc);
      expect(desktop.notifications.length).toBe(0);
      desktop._onNotificationRemove(ntfc); // this method is usually called after fade-out animation asynchronously
      expect(desktop.$container.find('.desktop-notifications').length).toBe(0);
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
      desktop.showForm(form);

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
      expect(desktop.bench.rendered).toBe(true);
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

  describe('geolocation', function() {

    var browserImpl;
    if (!navigator.geolocation) {
      navigator.geolocation = {
        getCurrentPosition: function() {}
      };
    }
    browserImpl = navigator.geolocation.getCurrentPosition;

    beforeEach(function() {
      navigator.geolocation.getCurrentPosition = function(success, error) {
        success({
          coords: {
            latitude: 1,
            longitude: 1
          }
        });
      };
      jasmine.Ajax.install();
    });

    it('asks the browser for its geographic location', function() {
      expect(scout.device.supportsGeolocation()).toBe(true);
      var message = {
        events: [{
          target: session.desktop.id,
          type: 'requestGeolocation'
        }]
      };
      linkWidgetAndAdapter(session.desktop, 'DesktopAdapter');
      session._processSuccessResponse(message);
      sendQueuedAjaxCalls();

      var requestData = mostRecentJsonRequest();
      var expectedEvent = new scout.RemoteEvent(session.desktop.id, 'geolocationDetermined', {
        latitude: 1,
        longitude: 1
      });
      expect(requestData).toContainEvents(expectedEvent);
    });

    afterEach(function() {
      navigator.geolocation.getCurrentPosition = browserImpl;
      jasmine.Ajax.uninstall();
    });

  });

  describe('showForm', function() {

    beforeEach(function() {
      session._renderDesktop();
    });

    it('shows and activates the form', function() {
      expect(desktop.activeForm).toBeUndefined();

      var form = formHelper.createFormWithOneField();
      desktop.showForm(form);
      expect(form.rendered).toBe(true);
      expect(desktop.activeForm).toBe(form);

      var anotherForm = formHelper.createFormWithOneField();
      desktop.showForm(anotherForm);
      expect(form.rendered).toBe(true);
      expect(desktop.activeForm).toBe(anotherForm);
    });

    it('adds a view to the bench if displayHint is View', function() {
      var form = formHelper.createFormWithOneField();
      var tabBox = desktop.bench.getTabBox('C');
      form.displayHint = scout.Form.DisplayHint.VIEW;
      desktop.showForm(form);

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

        desktop.showForm(form);
        expect(form.rendered).toBe(true);
        expect(desktop.navigationVisible).toBe(false);
        expect(desktop.benchVisible).toBe(true);
        expect(desktop.headerVisible).toBe(true);
      });

      it('opens the bench again if a view is shown right after the last view was closed', function() {
        var form = formHelper.createViewWithOneField();
        var form2 = formHelper.createViewWithOneField();
        expect(form.rendered).toBe(false);
        expect(form2.rendered).toBe(false);
        expect(desktop.navigationVisible).toBe(true);
        expect(desktop.benchVisible).toBe(false);

        desktop.showForm(form);
        expect(form.rendered).toBe(true);
        expect(desktop.navigationVisible).toBe(false);
        expect(desktop.benchVisible).toBe(true);

        // Close the form and open another one while the bench is removing due to the close of the previous form
        form.close();
        desktop.showForm(form2);
        expect(desktop.bench.removalPending).toBe(true);
        jasmine.clock().tick();
        desktop.bench.$container.trigger('animationend');
        jasmine.clock().tick();
        expect(form2.rendered).toBe(true);
        expect(desktop.navigationVisible).toBe(false);
        expect(desktop.benchVisible).toBe(true);
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
        desktop.showForm(form1);
        expect(form1.rendered).toBe(true);
        expect(form2.rendered).toBe(false);
        expect(desktop.navigationVisible).toBe(false);
        expect(desktop.benchVisible).toBe(true);
        expect(desktop.headerVisible).toBe(true);

        // open second form, bench is still shown
        desktop.showForm(form2);
        expect(form1.rendered).toBe(true);
        expect(form2.rendered).toBe(true);
        expect(desktop.navigationVisible).toBe(false);
        expect(desktop.benchVisible).toBe(true);
        expect(desktop.headerVisible).toBe(true);

        // close first form, bench is still shown
        desktop.hideForm(form1);
        expect(form1.rendered).toBe(false);
        expect(form2.rendered).toBe(true);
        expect(desktop.navigationVisible).toBe(false);
        expect(desktop.benchVisible).toBe(true);
        expect(desktop.headerVisible).toBe(true);

        // disable remove animation, otherwise form would not be removed immediately
        desktop.bench.animateRemoval = false;

        // close second form, bench is not shown anymore
        desktop.hideForm(form2);
        expect(form1.rendered).toBe(false);
        expect(form2.rendered).toBe(false);
        expect(desktop.navigationVisible).toBe(true);
        expect(desktop.benchVisible).toBe(false);
        expect(desktop.headerVisible).toBe(false);
      });

    });
  });

});
