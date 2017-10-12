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
      parent.session = session;
      ntfc = scout.create('DesktopNotification', {
        id: 'theID',
        parent: desktop,
        status: {}
      });
    });

    it('is rendered when desktop is rendered', function() {
      desktop.notifications.push(ntfc);
      expect(desktop.notifications.indexOf(ntfc)).toBe(0);
      expect(ntfc.rendered).toBe(false);

      session._renderDesktop();
      expect(ntfc.rendered).toBe(true);
    });

    it('may be added with addNotification', function() {
      session._renderDesktop();
      spyOn(ntfc, 'fadeIn');
      desktop.addNotification(ntfc);
      expect(ntfc.fadeIn).toHaveBeenCalled();
      expect(desktop.notifications.indexOf(ntfc)).toBe(0);
      expect(desktop.$container.find('.desktop-notifications').length).toBe(1);
      expect(desktop.$notification).not.toBe(null);
    });

    it('schedules addNotification when desktop is not rendered', function() {
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
      session._renderDesktop();
      spyOn(ntfc, 'fadeOut');
      desktop.addNotification(ntfc); // first add -> create $notifications DIV
      desktop.removeNotification(ntfc);
      expect(ntfc.fadeOut).toHaveBeenCalled();
    });

    it('removeNotification with (string) ID', function() {
      session._renderDesktop();
      spyOn(ntfc, 'fadeOut');
      desktop.addNotification(ntfc); // first add -> create $notifications DIV
      desktop.removeNotification('theID');
      expect(ntfc.fadeOut).toHaveBeenCalled();
    });

    it('_onNotificationRemove - last notifications removes $notifications DIV', function() {
      session._renderDesktop();
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

  describe('activateForm', function() {

    beforeEach(function() {
      session._renderDesktop();
    });

    var desktopOverlayHtmlElements = function() {
      return desktop.$container.children('.overlay-separator').nextAll().toArray();
    };

    var formElt = function(form) {
      return form.$container[0];
    };

    var widgetHtmlElements = function(forms) {
      var formElts = [];
      forms.forEach(function(form) {
        formElts.push(form.$container[0]);
      });
      return formElts;
    };

    it('brings non-modal dialog in front upon activation', function() {
      expect(desktop.activeForm).toBeUndefined();

      var dialog0 = formHelper.createFormWithOneField();
      dialog0.modal = false;
      desktop.showForm(dialog0);

      var dialog1 = formHelper.createFormWithOneField();
      dialog1.modal = false;
      desktop.showForm(dialog1);

      var dialog2 = formHelper.createFormWithOneField();
      dialog2.modal = false;
      desktop.showForm(dialog2);

      // expect dialogs to be in the same order as opened
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, dialog1, dialog2]));
      expect(desktop.activeForm).toBe(dialog2);

      desktop.activateForm(dialog0);
      // expect dialog0 to be on top (= last in the DOM)
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog2, dialog0]));
      expect(desktop.activeForm).toBe(dialog0);
    });

    it('keeps the order of other non-modal dialogs even when one of them is the display-parent of the dialog to activate', function() {
      expect(desktop.activeForm).toBeUndefined();

      var dialog0 = formHelper.createFormWithOneField();
      dialog0.setCssClass('DIALOG0');
      dialog0.modal = false;
      desktop.showForm(dialog0);

      var dialog1 = formHelper.createFormWithOneField();
      dialog1.setCssClass('DIALOG1');
      dialog1.modal = false;
      dialog1.parent = dialog0;
      dialog1.displayParent = dialog0;
      desktop.showForm(dialog1);

      var dialog2 = formHelper.createFormWithOneField();
      dialog2.setCssClass('DIALOG2');
      dialog2.modal = false;
      desktop.showForm(dialog2);

      desktop.activateForm(dialog1);
      // expect dialog1 to be on top but it's parent still behind dialog2
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, dialog2, dialog1]));
      expect(desktop.activeForm).toBe(dialog1);
    });

    it('activates outline when activating dialog of other outline', function() {
      var outline1 = outlineHelper.createOutline(outlineHelper.createModelFixture(3, 2));
      desktop.setOutline(outline1);
      var outline2 = outlineHelper.createOutline(outlineHelper.createModelFixture(3, 2));

      var dialog1 = formHelper.createFormWithOneField();
      dialog1.setCssClass('DIALOG1');
      dialog1.modal = false;
      dialog1.parent = outline1;
      dialog1.displayParent = outline1;
      desktop.showForm(dialog1);

      var dialog2 = formHelper.createFormWithOneField();
      dialog2.setCssClass('DIALOG2');
      dialog2.modal = false;
      dialog2.parent = dialog1;
      dialog2.displayParent= dialog1;
      desktop.showForm(dialog2);

      // expect dialogs to be in the same order as opened
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog2]));
      expect(desktop.activeForm).toBe(dialog2);
      expect(dialog2.displayParent).toBe(dialog1);

      desktop.setOutline(outline2);
      // expect all dialogs hidden
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([]));
      expect(desktop.activeForm).toBe(undefined);

      desktop.activateForm(dialog1);
      // expect outline 1 to be activated
      expect(desktop.outline).toBe(outline1);
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog2, dialog1]));
    });

    it('activates outline when activating child dialog of other\'s outline dialog', function() {
      var outline1 = outlineHelper.createOutline(outlineHelper.createModelFixture(3, 2));
      desktop.setOutline(outline1);
      var outline2 = outlineHelper.createOutline(outlineHelper.createModelFixture(3, 2));

      var dialog1 = formHelper.createFormWithOneField();
      dialog1.setCssClass('DIALOG1');
      dialog1.modal = false;
      dialog1.parent = outline1;
      dialog1.displayParent= outline1;
      desktop.showForm(dialog1);

      var dialog2 = formHelper.createFormWithOneField();
      dialog2.setCssClass('DIALOG2');
      dialog2.modal = false;
      dialog2.parent = dialog1;
      dialog2.displayParent= dialog1;
      desktop.showForm(dialog2);

      // expect dialogs to be in the same order as opened
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog2]));
      expect(desktop.activeForm).toBe(dialog2);
      expect(dialog2.displayParent).toBe(dialog1);

      desktop.setOutline(outline2);
      // expect all dialogs hidden
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([]));
      expect(desktop.activeForm).toBe(undefined);

      desktop.activateForm(dialog2);
      // expect outline 1 to be activated
      expect(desktop.outline).toBe(outline1);
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog2]));
    });

    it('does not bring non-modal dialog in front of desktop-modal dialog', function() {
      expect(desktop.activeForm).toBeUndefined();

      var dialog0 = formHelper.createFormWithOneField();
      dialog0.modal = false;
      desktop.showForm(dialog0);

      var dialog1 = formHelper.createFormWithOneField();
      dialog1.modal = false;
      desktop.showForm(dialog1);

      var desktopModalDialog = formHelper.createFormWithOneField();
      desktopModalDialog.modal = true;
      desktop.showForm(desktopModalDialog);

      // expect dialogs to be in the same order as opened
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, dialog1, desktopModalDialog]));

      desktop.activateForm(dialog0);
      // expect dialog0 to be in front of other non-modal dialog but behind desktop-modal dialog
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog0, desktopModalDialog]));
    });

    it('brings non-modal dialog in front of other non-modal dialog and it\'s modal child-dialog', function() {
      expect(desktop.activeForm).toBeUndefined();

      var dialog0 = formHelper.createFormWithOneField();
      dialog0.modal = false;
      desktop.showForm(dialog0);

      var dialog1 = formHelper.createFormWithOneField();
      dialog1.modal = false;
      desktop.showForm(dialog1);

      var dialog2 = formHelper.createFormWithOneField();
      dialog2.modal = true;
      dialog2.parent = dialog1;
      dialog2.displayParent = dialog1;
      desktop.showForm(dialog2);

      // expect dialogs to be in the same order as opened
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, dialog1, dialog2]));

      desktop.activateForm(dialog0);
      // expect dialog0 to be on top (= last in the DOM)
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog2, dialog0]));
      expect(desktop.activeForm).toBe(dialog0);
    });

    it('brings complete hierarchy of a non-modal dialog with 2-levels of modal child dialogs in front', function() {
      expect(desktop.activeForm).toBeUndefined();

      var dialog0 = formHelper.createFormWithOneField();
      dialog0.modal = false;
      desktop.showForm(dialog0);

      var dialog1 = formHelper.createFormWithOneField();
      dialog1.modal = false;
      desktop.showForm(dialog1);

      var parentDialog = formHelper.createFormWithOneField();
      parentDialog.modal = false;
      desktop.showForm(parentDialog);

      var modalChildDialogLevel1 = formHelper.createFormWithOneField();
      modalChildDialogLevel1.modal = true;
      modalChildDialogLevel1.parent = parentDialog;
      modalChildDialogLevel1.displayParent = parentDialog;
      desktop.showForm(modalChildDialogLevel1);

      var modalChildDialogLevel2 = formHelper.createFormWithOneField();
      modalChildDialogLevel2.modal = true;
      modalChildDialogLevel2.parent = modalChildDialogLevel1;
      modalChildDialogLevel2.displayParent = modalChildDialogLevel1;
      desktop.showForm(modalChildDialogLevel2);

      // expect dialogs to be in the same order as opened
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, dialog1, parentDialog, modalChildDialogLevel1, modalChildDialogLevel2]));
      expect(desktop.activeForm).toBe(modalChildDialogLevel2);

      desktop.activateForm(dialog0);
      // expect dialog0 to be on top (= last in the DOM)
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, parentDialog, modalChildDialogLevel1, modalChildDialogLevel2, dialog0]));
      expect(desktop.activeForm).toBe(dialog0);

      desktop.activateForm(dialog1);
      // expect dialog1 to be on top (= last in the DOM)
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([parentDialog, modalChildDialogLevel1, modalChildDialogLevel2, dialog0, dialog1]));
      expect(desktop.activeForm).toBe(dialog1);

      desktop.activateForm(parentDialog);
      // expect complete hierarchy beginning with parentDialog to be on top (= last in the DOM)
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, dialog1, parentDialog, modalChildDialogLevel1, modalChildDialogLevel2]));
      expect(desktop.activeForm).toBe(modalChildDialogLevel2);

      desktop.activateForm(dialog0);
      // expect dialog0 to be on top (= last in the DOM)
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, parentDialog, modalChildDialogLevel1, modalChildDialogLevel2, dialog0]));
      expect(desktop.activeForm).toBe(dialog0);

      desktop.activateForm(modalChildDialogLevel1);
      // expect complete hierarchy beginning with parentDialog to be on top (= last in the DOM)
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog0, parentDialog, modalChildDialogLevel1, modalChildDialogLevel2]));
      expect(desktop.activeForm).toBe(modalChildDialogLevel2);

      desktop.activateForm(dialog0);
      // expect dialog0 to be on top (= last in the DOM)
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, parentDialog, modalChildDialogLevel1, modalChildDialogLevel2, dialog0]));
      expect(desktop.activeForm).toBe(dialog0);

      desktop.activateForm(modalChildDialogLevel2);
      // expect complete hierarchy beginning with parentDialog to be on top (= last in the DOM)
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog0, parentDialog, modalChildDialogLevel1, modalChildDialogLevel2]));
      expect(desktop.activeForm).toBe(modalChildDialogLevel2);
    });

    it('keeps position of dialog\'s messagebox relative to it\'s parent dialog while reordering dialogs', function() {
      expect(desktop.activeForm).toBeUndefined();

      var dialog0 = formHelper.createFormWithOneField();
      dialog0.modal = false;
      desktop.showForm(dialog0);

      var dialog1 = formHelper.createFormWithOneField();
      dialog1.modal = false;
      desktop.showForm(dialog1);

      var dialog2 = formHelper.createFormWithOneField();
      dialog2.modal = false;
      desktop.showForm(dialog2);

      var messagebox = scout.create('MessageBox', {
        parent: dialog2,
        displayParent: dialog2,
        header: 'Title',
        body: 'hello',
        severity: scout.Status.Severity.INFO,
        yesButtonText: 'OK'
      });
      messagebox.open();

      // expect dialogs and messagebox to be in the same order as opened
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, dialog1, dialog2, messagebox]));
      expect(desktop.activeForm).toBe(dialog2);

      desktop.activateForm(dialog0);
      // expect messagebox between dialog2 and dialog0
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog2, messagebox, dialog0]));
      expect(desktop.activeForm).toBe(dialog0);

      desktop.activateForm(dialog1);
      // expect messagebox between dialog2 and dialog0
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog2, messagebox, dialog0, dialog1]));
      expect(desktop.activeForm).toBe(dialog1);

      desktop.activateForm(dialog2);
      // expect messagebox to be on top
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, dialog1, dialog2, messagebox]));
      expect(desktop.activeForm).toBe(dialog2);
    });

    it('brings dialog with messagebox on top upon mousedown on messagebox', function() {
      expect(desktop.activeForm).toBeUndefined();

      var dialog0 = formHelper.createFormWithOneField();
      dialog0.modal = false;
      desktop.showForm(dialog0);

      var messagebox = scout.create('MessageBox', {
        parent: dialog0,
        displayParent: dialog0,
        header: 'Title',
        body: 'hello',
        severity: scout.Status.Severity.INFO,
        yesButtonText: 'OK'
      });
      messagebox.open();

      var dialog1 = formHelper.createFormWithOneField();
      dialog1.modal = false;
      desktop.showForm(dialog1);

      var dialog2 = formHelper.createFormWithOneField();
      dialog2.modal = false;
      desktop.showForm(dialog2);

      // expect dialogs and messagebox to be in the same order as opened
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, messagebox, dialog1, dialog2]));
      expect(desktop.activeForm).toBe(dialog2);

      messagebox.$container.mousedown();
      // expect dialog0 and messagebox on top
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog2, dialog0, messagebox]));
      expect(desktop.activeForm).toBe(dialog0);
    });

    it('keeps desktop\'s messagebox on top while reordering dialogs', function() {
      expect(desktop.activeForm).toBeUndefined();

      var dialog0 = formHelper.createFormWithOneField();
      dialog0.modal = false;
      desktop.showForm(dialog0);

      var dialog1 = formHelper.createFormWithOneField();
      dialog1.modal = false;
      desktop.showForm(dialog1);

      var dialog2 = formHelper.createFormWithOneField();
      dialog2.modal = false;
      desktop.showForm(dialog2);

      var messagebox = scout.create('MessageBox', {
        parent: desktop,
        displayParent: desktop,
        header: 'Title',
        body: 'hello',
        severity: scout.Status.Severity.INFO,
        yesButtonText: 'OK'
      });
      messagebox.open();

      // expect dialogs and messagebox to be in the same order as opened
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, dialog1, dialog2, messagebox]));
      expect(desktop.activeForm).toBe(dialog2);

      desktop.activateForm(dialog0);
      // expect messagebox to stay on top
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog2, dialog0, messagebox]));
      expect(desktop.activeForm).toBe(dialog0);

      desktop.activateForm(dialog1);
      // expect messagebox to stay on top
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog2, dialog0, dialog1, messagebox]));
      expect(desktop.activeForm).toBe(dialog1);

      desktop.activateForm(dialog2);
      // expect messagebox to stay on top
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, dialog1, dialog2, messagebox]));
      expect(desktop.activeForm).toBe(dialog2);
    });

    it('keeps position of dialog\'s fileChooser relative to it\'s parent dialog while reordering dialogs', function() {
      expect(desktop.activeForm).toBeUndefined();

      var dialog0 = formHelper.createFormWithOneField();
      dialog0.modal = false;
      desktop.showForm(dialog0);

      var dialog1 = formHelper.createFormWithOneField();
      dialog1.modal = false;
      desktop.showForm(dialog1);

      var dialog2 = formHelper.createFormWithOneField();
      dialog2.modal = false;
      desktop.showForm(dialog2);

      var fileChooser = scout.create('FileChooser', {
        parent: dialog2,
        displayParent: dialog2
      });
      fileChooser.open();

      // expect dialogs and fileChooser to be in the same order as opened
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, dialog1, dialog2, fileChooser]));
      expect(desktop.activeForm).toBe(dialog2);

      desktop.activateForm(dialog0);
      // expect fileChooser between dialog2 and dialog0
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog2, fileChooser, dialog0]));
      expect(desktop.activeForm).toBe(dialog0);

      desktop.activateForm(dialog1);
      // expect fileChooser between dialog2 and dialog0
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog2, fileChooser, dialog0, dialog1]));
      expect(desktop.activeForm).toBe(dialog1);

      desktop.activateForm(dialog2);
      // expect fileChooser to be on top
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, dialog1, dialog2, fileChooser]));
      expect(desktop.activeForm).toBe(dialog2);
    });

    it('brings dialog with filechooser on top upon mousedown on filechooser', function() {
      expect(desktop.activeForm).toBeUndefined();

      var dialog0 = formHelper.createFormWithOneField();
      dialog0.modal = false;
      desktop.showForm(dialog0);

      var fileChooser = scout.create('FileChooser', {
        parent: dialog0,
        displayParent: dialog0
      });
      fileChooser.open();

      var dialog1 = formHelper.createFormWithOneField();
      dialog1.modal = false;
      desktop.showForm(dialog1);

      var dialog2 = formHelper.createFormWithOneField();
      dialog2.modal = false;
      desktop.showForm(dialog2);

      // expect dialogs and fileChooser to be in the same order as opened
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, fileChooser, dialog1, dialog2]));
      expect(desktop.activeForm).toBe(dialog2);

      fileChooser.$container.mousedown();
      // expect dialog0 and fileChooser on top
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog2, dialog0, fileChooser]));
      expect(desktop.activeForm).toBe(dialog0);
    });

    it('does not change position of desktop\'s fileChooser while reordering dialogs', function() {
      expect(desktop.activeForm).toBeUndefined();

      var dialog0 = formHelper.createFormWithOneField();
      dialog0.modal = false;
      desktop.showForm(dialog0);

      var dialog1 = formHelper.createFormWithOneField();
      dialog1.modal = false;
      desktop.showForm(dialog1);

      var dialog2 = formHelper.createFormWithOneField();
      dialog2.modal = false;
      desktop.showForm(dialog2);

      var fileChooser = scout.create('FileChooser', {
        parent: desktop,
        displayParent: desktop
      });
      fileChooser.open();

      // expect dialogs and fileChooser to be in the same order as opened
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, dialog1, dialog2, fileChooser]));
      expect(desktop.activeForm).toBe(dialog2);

      desktop.activateForm(dialog0);
      // expect fileChooser to stay on top
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog2, dialog0, fileChooser]));
      expect(desktop.activeForm).toBe(dialog0);

      desktop.activateForm(dialog1);
      // expect fileChooser to stay on top
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog2, dialog0, dialog1, fileChooser]));
      expect(desktop.activeForm).toBe(dialog1);

      desktop.activateForm(dialog2);
      // expect fileChooser to stay on top
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, dialog1, dialog2, fileChooser]));
      expect(desktop.activeForm).toBe(dialog2);
    });

    it('activates parent view upon activation of child dialog', function() {
      expect(desktop.activeForm).toBeUndefined();

      var tabBox = desktop.bench.getTabBox('C');

      var viewForm0 = formHelper.createFormWithOneField();
      viewForm0.displayHint = scout.Form.DisplayHint.VIEW;
      desktop.showForm(viewForm0);

      var dialog0 = formHelper.createFormWithOneField();
      dialog0.modal = false;
      dialog0.parent = viewForm0;
      dialog0.displayParent = viewForm0;
      desktop.showForm(dialog0);

      // expect viewForm0 as currentView and dialog0 in the DOM
      expect(tabBox.currentView).toEqual(viewForm0);
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0]));

      var viewForm1 = formHelper.createFormWithOneField();
      viewForm1.displayHint = scout.Form.DisplayHint.VIEW;
      desktop.showForm(viewForm1);

      // expect viewForm2 as currentView and dialog0 removed from the DOM
      expect(tabBox.currentView).toEqual(viewForm1);
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([]));

      desktop.activateForm(dialog0);
      // expect viewForm0 as currentView and dialog0 in the DOM again
      expect(tabBox.currentView).toEqual(viewForm0);
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0]));
    });
  });

  describe('activeForm', function() {

    beforeEach(function() {
      session._renderDesktop();
    });

    it('will be set to the display parent form if dialog closes', function() {
      var dialogParent = formHelper.createFormWithOneField({
        displayHint: 'dialog'
      });
      var dialog = formHelper.createFormWithOneField({
        displayHint: 'dialog',
        displayParent: dialogParent
      });
      dialogParent.open();
      expect(desktop.activeForm).toBe(dialogParent);

      dialog.open();
      expect(desktop.activeForm).toBe(dialog);

      dialog.close();
      expect(desktop.activeForm).toBe(dialogParent);
    });

    it('will be set to currentView if dialog closes and there is no display parent form', function() {
      var view = formHelper.createFormWithOneField({
        displayHint: 'view'
      });
      var dialog = formHelper.createFormWithOneField({
        displayHint: 'dialog'
      });
      view.open();
      expect(desktop.activeForm).toBe(view);

      dialog.open();
      expect(desktop.activeForm).toBe(dialog);

      dialog.close();
      expect(desktop.activeForm).toBe(view);
    });

    it('will be set to undefined if dialog closes and there is no currentView and no display parent', function() {
      var dialog = formHelper.createFormWithOneField({
        displayHint: 'dialog'
      });
      dialog.open();
      expect(desktop.activeForm).toBe(dialog);

      dialog.close();
      expect(desktop.activeForm).toBeUndefined();
    });

    it('must not be the detail form', function() {
      var outline = outlineHelper.createOutlineWithOneDetailForm();
      desktop.setOutline(outline);
      outline.selectNodes(outline.nodes[0]);
      var detailForm = outline.nodes[0].detailForm;
      var dialog = formHelper.createFormWithOneField({
        displayHint: 'dialog'
      });
      dialog.open();
      expect(desktop.activeForm).toBe(dialog);

      dialog.close();
      expect(desktop.activeForm).toBeUndefined();
    });

    it('must not be the detail form even if it is the display parent', function() {
      var outline = outlineHelper.createOutlineWithOneDetailForm();
      desktop.setOutline(outline);
      outline.selectNodes(outline.nodes[0]);
      var detailForm = outline.nodes[0].detailForm;
      var dialog = formHelper.createFormWithOneField({
        displayHint: 'dialog',
        displayParent: detailForm
      });
      dialog.open();
      expect(desktop.activeForm).toBe(dialog);

      dialog.close();
      expect(desktop.activeForm).toBeUndefined();
    });

    it('must be a form', function() {
      var outline = outlineHelper.createOutlineWithOneDetailTable();
      desktop.setOutline(outline);
      outline.selectNodes(outline.nodes[0]);
      var dialog = formHelper.createFormWithOneField({
        displayHint: 'dialog'
      });
      dialog.open();
      expect(desktop.activeForm).toBe(dialog);

      dialog.close();
      expect(desktop.activeForm).toBeUndefined();
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
