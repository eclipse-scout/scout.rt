/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FormSpecHelper, OutlineSpecHelper} from '../../src/testing/index';
import {arrays, Desktop, Device, Form, MessageBoxes, RemoteEvent, scout, Status, strings, Widget} from '../../src/index';

describe('Desktop', () => {
  let session, desktop, outlineHelper, formHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession({
      desktop: {
        navigationVisible: true,
        headerVisible: true,
        benchVisible: true
      },
      renderDesktop: false
    });
    outlineHelper = new OutlineSpecHelper(session);
    formHelper = new FormSpecHelper(session);
    desktop = session.desktop;
    desktop.viewButtons = [];
  });

  describe('notification', () => {
    let ntfc,
      parent = new Widget();

    beforeEach(() => {
      parent.session = session;
      ntfc = scout.create('DesktopNotification', {
        id: 'theID',
        parent: desktop,
        status: {}
      });
    });

    it('is rendered when desktop is rendered', () => {
      desktop.notifications.push(ntfc);
      expect(desktop.notifications.indexOf(ntfc)).toBe(0);
      expect(ntfc.rendered).toBe(false);

      session._renderDesktop();
      expect(ntfc.rendered).toBe(true);
    });

    it('may be added with addNotification', () => {
      session._renderDesktop();
      spyOn(ntfc, 'fadeIn').and.callThrough();
      desktop.addNotification(ntfc);
      expect(ntfc.fadeIn).toHaveBeenCalled();
      expect(desktop.notifications.indexOf(ntfc)).toBe(0);
      expect(desktop.$container.find('.desktop-notifications').length).toBe(1);
      expect(desktop.$notification).not.toBe(null);
    });

    it('schedules addNotification when desktop is not rendered', () => {
      scout.create('DesktopNotification', {
        parent: desktop,
        status: {
          severity: Status.Severity.OK,
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

    it('removeNotification with object', () => {
      session._renderDesktop();
      spyOn(ntfc, 'fadeOut').and.callThrough();
      desktop.addNotification(ntfc); // first add -> create $notifications DIV
      desktop.removeNotification(ntfc);
      expect(ntfc.fadeOut).toHaveBeenCalled();
    });

    it('removeNotification with (string) ID', () => {
      session._renderDesktop();
      spyOn(ntfc, 'fadeOut').and.callThrough();
      desktop.addNotification(ntfc); // first add -> create $notifications DIV
      desktop.removeNotification('theID');
      expect(ntfc.fadeOut).toHaveBeenCalled();
    });

    it('_onNotificationRemove - last notifications removes $notifications DIV', () => {
      session._renderDesktop();
      desktop.addNotification(ntfc); // first add -> create $notifications DIV
      desktop.removeNotification(ntfc);
      expect(desktop.notifications.length).toBe(0);
      desktop._onNotificationRemove(ntfc); // this method is usually called after fade-out animation asynchronously
      expect(desktop.$container.find('.desktop-notifications').length).toBe(0);
      expect(desktop.$notifications).toBe(null);
    });
  });

  describe('outline', () => {

    beforeEach(() => {
      session._renderDesktop();
    });

    it('is displayed in desktop navigation', () => {
      let model = outlineHelper.createModelFixture(3, 2);
      let outline = outlineHelper.createOutline(model);

      expect(desktop.outline).toBeFalsy();
      expect(outline.rendered).toBe(false);

      desktop.setOutline(outline);
      expect(desktop.outline).toBe(outline);
      expect(outline.rendered).toBe(true);
      expect(outline.$container.parent()[0]).toBe(desktop.navigation.$body[0]);
    });

    it('activates modal views when being rendered', () => {
      let outline1 = outlineHelper.createOutline(outlineHelper.createModelFixture(3, 2));
      let outline2 = outlineHelper.createOutline(outlineHelper.createModelFixture(3, 2));

      desktop.setOutline(outline1);
      desktop.bringOutlineToFront(outline1);
      expect(desktop.outline).toBe(outline1);
      expect(outline1.rendered).toBe(true);

      // create a new modal view and activate it
      let form = formHelper.createFormWithOneField({
        parent: outline1,
        displayHint: Form.DisplayHint.VIEW,
        displayParent: outline1,
        modal: true
      });
      desktop.showForm(form);
      expect(form.rendered).toBe(true);

      // switch the outline
      desktop.setOutline(outline2);
      desktop.bringOutlineToFront(outline2);
      expect(desktop.outline).toBe(outline2);
      expect(outline1.rendered).toBe(false);
      expect(outline2.rendered).toBe(true);
      expect(form.rendered).toBe(false);

      // switch back to the outline with the modal form
      desktop.setOutline(outline1);
      desktop.bringOutlineToFront(outline1);
      expect(desktop.outline).toBe(outline1);
      expect(outline1.rendered).toBe(true);
      expect(outline2.rendered).toBe(false);

      // and check that the form is propertly rendered
      expect(form.rendered).toBe(true);
    });

  });

  describe('benchVisible', () => {

    beforeEach(() => {
      session._renderDesktop();
    });

    it('controls visibility of the bench', () => {
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

    it('removes the content after the animation', () => {
      jasmine.clock().install();
      let form = formHelper.createFormWithOneField();
      let tabBox = desktop.bench.getTabBox('C');
      form.displayHint = Form.DisplayHint.VIEW;
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
      expect(desktop.bench._rendered).toBe(true);
      expect(form._rendered).toBe(true);
      expect(form.parent).toBe(tabBox);

      // trigger actual remove
      jasmine.clock().tick();
      desktop.bench.removalPending = false;
      desktop.bench._removeInternal();
      expect(desktop.bench).toBeFalsy();
      expect(form.rendered).toBe(false);
      jasmine.clock().uninstall();
    });

  });

  describe('navigationVisible', () => {

    it('controls visibility of the navigation', () => {
      session._renderDesktop();
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

    it('only affects content in navigation, not in bench or header', () => {
      session._renderDesktop();
      let outline = outlineHelper.createOutlineWithOneDetailForm();
      let detailForm = outline.nodes[0].detailForm;
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

    it('does not remove dialogs, message boxes and file choosers with display parent outline', done => {
      session._renderDesktop();
      let outline = outlineHelper.createOutline();
      desktop.setOutline(outline);
      let msgBox = scout.create('MessageBox', {
        parent: outline,
        displayParent: outline
      });
      msgBox.open();
      let fileChooser = scout.create('FileChooser', {
        parent: outline,
        displayParent: outline
      });
      fileChooser.open();
      let dialog = formHelper.createFormWithOneField({
        parent: outline,
        displayParent: outline
      });
      let promises = [];
      promises.push(dialog.open());
      let view = formHelper.createFormWithOneField({
        parent: outline,
        displayHint: Form.DisplayHint.VIEW,
        displayParent: outline,
        modal: true
      });
      promises.push(view.open());
      $.promiseAll(promises).then(() => {
        expect(desktop.navigationVisible).toBe(true);
        expect(desktop.navigation.rendered).toBe(true);
        expect(desktop.navigation.$body.children('.glasspane').length).toBe(4); // Every glass pane renderer added one
        expect(session.focusManager._glassPaneTargets.length).toBe(8); // Every glass pane renderer added one for navigation and bench
        expect(dialog.rendered).toBe(true);
        expect(view.rendered).toBe(true);
        expect(msgBox.rendered).toBe(true);
        expect(fileChooser.rendered).toBe(true);

        // Outline is not visible anymore, but form, msg box and file chooser still are
        desktop.setNavigationVisible(false);
        // Force removal of navigation
        desktop.onLayoutAnimationComplete();
        expect(desktop.navigationVisible).toBe(false);
        expect(desktop.navigation).toBeFalsy();
        expect(outline.rendered).toBe(false);
        expect(dialog.rendered).toBe(true);
        expect(view.rendered).toBe(true);
        expect(msgBox.rendered).toBe(true);
        expect(fileChooser.rendered).toBe(true);

        // Make it visible again and expect that glass pane is correctly reverted
        desktop.setNavigationVisible(true);
        desktop.onLayoutAnimationComplete();
        expect(desktop.navigationVisible).toBe(true);
        expect(desktop.navigation.rendered).toBe(true);
        expect(desktop.navigation.$body.children('.glasspane').length).toBe(4);
        expect(session.focusManager._glassPaneTargets.length).toBe(8);

        dialog.close();
        view.close();
        msgBox.close();
        fileChooser.close();
        expect(desktop.navigation.$body.children('.glasspane').length).toBe(0);
        expect(session.focusManager._glassPaneTargets.length).toBe(0);
      })
        .catch(fail)
        .always(done);
    });

    it('does not remove message boxes with display parent outline', () => {
      session._renderDesktop();
      let outline = outlineHelper.createOutline();
      desktop.setOutline(outline);

      let msgBox = scout.create('MessageBox', {
        parent: outline,
        displayParent: outline
      });
      msgBox.open();
      expect(desktop.navigationVisible).toBe(true);
      expect(desktop.navigation.rendered).toBe(true);
      expect(desktop.navigation.$body.children('.glasspane').length).toBe(1); // Every glass pane renderer added one
      expect(session.focusManager._glassPaneTargets.length).toBe(2); // Every glass pane renderer added one for navigation and bench
      expect(msgBox.rendered).toBe(true);

      // Outline is not visible anymore, but form, msg box and file chooser still are
      desktop.setNavigationVisible(false);
      // Force removal of navigation
      desktop.onLayoutAnimationComplete();
      expect(desktop.navigationVisible).toBe(false);
      expect(desktop.navigation).toBeFalsy();
      expect(outline.rendered).toBe(false);
      expect(msgBox.rendered).toBe(true);

      // Make it visible again and expect that glass pane is correctly reverted
      desktop.setNavigationVisible(true);
      desktop.onLayoutAnimationComplete();
      expect(desktop.navigationVisible).toBe(true);
      expect(desktop.navigation.rendered).toBe(true);
      expect(desktop.navigation.$body.children('.glasspane').length).toBe(1);
      expect(session.focusManager._glassPaneTargets.length).toBe(2);

      msgBox.close();
      expect(desktop.navigation.$body.children('.glasspane').length).toBe(0);
      expect(session.focusManager._glassPaneTargets.length).toBe(0);
    });

    it('does not remove file choosers with display parent outline', () => {
      session._renderDesktop();
      let outline = outlineHelper.createOutline();
      desktop.setOutline(outline);
      let fileChooser = scout.create('FileChooser', {
        parent: outline,
        displayParent: outline
      });
      fileChooser.open();
      expect(desktop.navigationVisible).toBe(true);
      expect(desktop.navigation.rendered).toBe(true);
      expect(desktop.navigation.$body.children('.glasspane').length).toBe(1); // Every glass pane renderer added one
      expect(session.focusManager._glassPaneTargets.length).toBe(2); // Every glass pane renderer added one for navigation and bench
      expect(fileChooser.rendered).toBe(true);

      // Outline is not visible anymore, but form, msg box and file chooser still are
      desktop.setNavigationVisible(false);
      // Force removal of navigation
      desktop.onLayoutAnimationComplete();
      expect(desktop.navigationVisible).toBe(false);
      expect(desktop.navigation).toBeFalsy();
      expect(outline.rendered).toBe(false);
      expect(fileChooser.rendered).toBe(true);

      // Make it visible again and expect that glass pane is correctly reverted
      desktop.setNavigationVisible(true);
      desktop.onLayoutAnimationComplete();
      expect(desktop.navigationVisible).toBe(true);
      expect(desktop.navigation.rendered).toBe(true);
      expect(desktop.navigation.$body.children('.glasspane').length).toBe(1);
      expect(session.focusManager._glassPaneTargets.length).toBe(2);

      fileChooser.close();
      expect(desktop.navigation.$body.children('.glasspane').length).toBe(0);
      expect(session.focusManager._glassPaneTargets.length).toBe(0);
    });

    it('does not remove dialogs with display parent outline even when rendered along with the outline', () => {
      // Simulate startup / reload case
      session = sandboxSession({
        desktop: {
          navigationVisible: true,
          headerVisible: true,
          benchVisible: true,
          outline: {
            objectType: 'Outline',
            modelClass: 'a.cbdke',
            title: 'Title',
            id: '123',
            dialogs: [{
              objectType: 'Form',
              displayHint: Form.DisplayHint.DIALOG,
              modal: true,
              rootGroupBox: {
                objectType: 'GroupBox'
              }
            }]
          }
        }
      });
      desktop = session.desktop;
      let outline = desktop.outline;
      let dialog = outline.dialogs[0];
      expect(desktop.navigationVisible).toBe(true);
      expect(desktop.navigation.rendered).toBe(true);
      expect(dialog.rendered).toBe(true);

      // Outline is not visible anymore, but form, msg box and file chooser still are
      desktop.setNavigationVisible(false);
      // Force removal of navigation
      desktop.onLayoutAnimationComplete();
      expect(desktop.navigationVisible).toBe(false);
      expect(desktop.navigation).toBeFalsy();
      expect(outline.rendered).toBe(false);
      expect(dialog.rendered).toBe(true);

      dialog.close();
    });

    it('does not remove message boxes with display parent outline even when rendered along with the outline', () => {
      // Simulate startup / reload case
      session = sandboxSession({
        desktop: {
          navigationVisible: true,
          headerVisible: true,
          benchVisible: true,
          outline: {
            objectType: 'Outline',
            modelClass: 'a.cbdke',
            title: 'Title',
            id: '123',
            messageBoxes: [{
              objectType: 'MessageBox'
            }]
          }
        }
      });
      desktop = session.desktop;
      let outline = desktop.outline;
      let msgBox = outline.messageBoxes[0];
      expect(desktop.navigationVisible).toBe(true);
      expect(desktop.navigation.rendered).toBe(true);
      expect(msgBox.rendered).toBe(true);

      // Outline is not visible anymore, but form, msg box and file chooser still are
      desktop.setNavigationVisible(false);
      // Force removal of navigation
      desktop.onLayoutAnimationComplete();
      expect(desktop.navigationVisible).toBe(false);
      expect(desktop.navigation).toBeFalsy();
      expect(outline.rendered).toBe(false);
      expect(msgBox.rendered).toBe(true);

      msgBox.close();
    });

    it('does not remove file choosers with display parent outline even when rendered along with the outline', () => {
      // Simulate startup / reload case
      session = sandboxSession({
        desktop: {
          navigationVisible: true,
          headerVisible: true,
          benchVisible: true,
          outline: {
            objectType: 'Outline',
            modelClass: 'a.cbdke',
            title: 'Title',
            id: '123',
            fileChoosers: [{
              objectType: 'FileChooser'
            }]
          }
        }
      });
      desktop = session.desktop;
      let outline = desktop.outline;
      let fileChooser = outline.fileChoosers[0];
      expect(desktop.navigationVisible).toBe(true);
      expect(desktop.navigation.rendered).toBe(true);
      expect(fileChooser.rendered).toBe(true);

      // Outline is not visible anymore, but form, msg box and file chooser still are
      desktop.setNavigationVisible(false);
      // Force removal of navigation
      desktop.onLayoutAnimationComplete();
      expect(desktop.navigationVisible).toBe(false);
      expect(desktop.navigation).toBeFalsy();
      expect(outline.rendered).toBe(false);
      expect(fileChooser.rendered).toBe(true);

      fileChooser.close();
    });

  });

  describe('headerVisible', () => {

    beforeEach(() => {
      session._renderDesktop();
    });

    it('controls visibility of the header', () => {
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

    it('correctly restores view tabs', () => {
      let form = formHelper.createViewWithOneField();
      form.title = 'title';
      desktop.showForm(form);
      let tab = desktop.header.tabArea.tabs[0];
      expect(tab.view).toBe(form);
      expect(tab.rendered).toBe(true);
      expect(tab.selected).toBe(true);

      desktop.setHeaderVisible(false);
      expect(tab.view).toBe(form);
      expect(tab.rendered).toBe(false);
      expect(tab.selected).toBe(true);

      desktop.setHeaderVisible(true);
      tab = desktop.header.tabArea.tabs[0];
      expect(tab.view).toBe(form);
      expect(tab.rendered).toBe(true);
      expect(tab.selected).toBe(true);
    });

  });

  describe('geolocation', () => {

    let browserImpl;
    if (!navigator.geolocation) {
      navigator.geolocation = {
        getCurrentPosition: () => {
        }
      };
    }
    browserImpl = navigator.geolocation.getCurrentPosition;

    beforeEach(() => {
      navigator.geolocation.getCurrentPosition = (success, error) => {
        success({
          coords: {
            latitude: 1,
            longitude: 1
          }
        });
      };
      jasmine.Ajax.install();
      jasmine.clock().install();
    });

    it('asks the browser for its geographic location', () => {
      expect(Device.get().supportsGeolocation()).toBe(true);
      let message = {
        events: [{
          target: session.desktop.id,
          type: 'requestGeolocation'
        }]
      };
      linkWidgetAndAdapter(session.desktop, 'DesktopAdapter');
      session._processSuccessResponse(message);
      sendQueuedAjaxCalls();

      let requestData = mostRecentJsonRequest();
      let expectedEvent = new RemoteEvent(session.desktop.id, 'geolocationDetermined', {
        latitude: 1,
        longitude: 1
      });
      expect(requestData).toContainEvents(expectedEvent);
    });

    afterEach(() => {
      navigator.geolocation.getCurrentPosition = browserImpl;
      jasmine.Ajax.uninstall();
      jasmine.clock().uninstall();
    });

  });

  describe('showForm', () => {

    beforeEach(() => {
      session._renderDesktop();
    });

    it('shows and activates the form', () => {
      expect(desktop.activeForm).toBe(null);

      let form = formHelper.createFormWithOneField();
      desktop.showForm(form);
      expect(form.rendered).toBe(true);
      expect(desktop.activeForm).toBe(form);

      let anotherForm = formHelper.createFormWithOneField();
      desktop.showForm(anotherForm);
      expect(form.rendered).toBe(true);
      expect(desktop.activeForm).toBe(anotherForm);
    });

    it('adds a view to the bench if displayHint is View', () => {
      let form = formHelper.createFormWithOneField();
      let tabBox = desktop.bench.getTabBox('C');
      form.displayHint = Form.DisplayHint.VIEW;
      desktop.showForm(form);

      expect(form.rendered).toBe(true);
      expect(form.parent).toBe(tabBox);
      expect(form.$container.parent()[0]).toBe(tabBox.$viewContent[0]);
    });
  });

  describe('activateForm', () => {

    beforeEach(() => {
      session._renderDesktop();
    });

    let desktopOverlayHtmlElements = () => desktop.$container.children('.overlay-separator').nextAll().toArray();

    let widgetHtmlElements = forms => {
      let formElts = [];
      forms.forEach(form => {
        formElts.push(form.$container[0]);
      });
      return formElts;
    };

    it('brings non-modal dialog in front upon activation', () => {
      expect(desktop.activeForm).toBe(null);

      let dialog0 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog0);

      let dialog1 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog1);

      let dialog2 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog2);

      // expect dialogs to be in the same order as opened
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, dialog1, dialog2]));
      expect(desktop.activeForm).toBe(dialog2);

      desktop.activateForm(dialog0);
      // expect dialog0 to be on top (= last in the DOM)
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog2, dialog0]));
      expect(desktop.activeForm).toBe(dialog0);
    });

    it('keeps the order of other non-modal dialogs even when one of them is the display-parent of the dialog to activate', () => {
      expect(desktop.activeForm).toBe(null);

      let dialog0 = formHelper.createFormWithOneField({
        modal: false,
        cssClass: 'DIALOG0'
      });
      desktop.showForm(dialog0);

      let dialog1 = formHelper.createFormWithOneField({
        modal: false,
        cssClass: 'DIALOG1',
        parent: dialog0,
        displayParent: dialog0
      });
      desktop.showForm(dialog1);

      let dialog2 = formHelper.createFormWithOneField({
        modal: false,
        cssClass: 'DIALOG2'
      });
      desktop.showForm(dialog2);

      desktop.activateForm(dialog1);
      // expect dialog1 to be on top but it's parent still behind dialog2
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, dialog2, dialog1]));
      expect(desktop.activeForm).toBe(dialog1);
    });

    it('activates outline when activating dialog of other outline', () => {
      let outline1 = outlineHelper.createOutline(outlineHelper.createModelFixture(3, 2));
      desktop.setOutline(outline1);
      let outline2 = outlineHelper.createOutline(outlineHelper.createModelFixture(3, 2));

      let dialog1 = formHelper.createFormWithOneField({
        modal: false
      });
      dialog1.setCssClass('DIALOG1');
      dialog1.parent = outline1;
      dialog1.displayParent = outline1;
      desktop.showForm(dialog1);

      let dialog2 = formHelper.createFormWithOneField({
        modal: false
      });
      dialog2.setCssClass('DIALOG2');
      dialog2.parent = dialog1;
      dialog2.displayParent = dialog1;
      desktop.showForm(dialog2);

      // expect dialogs to be in the same order as opened
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog2]));
      expect(desktop.activeForm).toBe(dialog2);
      expect(dialog2.displayParent).toBe(dialog1);

      desktop.setOutline(outline2);
      // expect all dialogs hidden
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([]));
      expect(desktop.activeForm).toBe(null);

      desktop.activateForm(dialog1);
      // expect outline 1 to be activated
      expect(desktop.outline).toBe(outline1);
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog2, dialog1]));
    });

    it('activates outline when activating child dialog of other\'s outline dialog', () => {
      let outline1 = outlineHelper.createOutline(outlineHelper.createModelFixture(3, 2));
      desktop.setOutline(outline1);
      let outline2 = outlineHelper.createOutline(outlineHelper.createModelFixture(3, 2));

      let dialog1 = formHelper.createFormWithOneField({
        modal: false
      });
      dialog1.setCssClass('DIALOG1');
      dialog1.parent = outline1;
      dialog1.displayParent = outline1;
      desktop.showForm(dialog1);

      let dialog2 = formHelper.createFormWithOneField({
        modal: false
      });
      dialog2.setCssClass('DIALOG2');
      dialog2.parent = dialog1;
      dialog2.displayParent = dialog1;
      desktop.showForm(dialog2);

      // expect dialogs to be in the same order as opened
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog2]));
      expect(desktop.activeForm).toBe(dialog2);
      expect(dialog2.displayParent).toBe(dialog1);

      desktop.setOutline(outline2);
      // expect all dialogs hidden
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([]));
      expect(desktop.activeForm).toBe(null);

      desktop.activateForm(dialog2);
      // expect outline 1 to be activated
      expect(desktop.outline).toBe(outline1);
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog2]));
    });

    it('does not bring non-modal dialog in front of desktop-modal dialog', () => {
      expect(desktop.activeForm).toBe(null);

      let dialog0 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog0);

      let dialog1 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog1);

      let desktopModalDialog = formHelper.createFormWithOneField({
        modal: true
      });
      desktop.showForm(desktopModalDialog);

      // expect dialogs to be in the same order as opened
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, dialog1, desktopModalDialog]));

      desktop.activateForm(dialog0);
      // expect dialog0 to be in front of other non-modal dialog but behind desktop-modal dialog
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog0, desktopModalDialog]));
    });

    it('brings non-modal dialog in front of other non-modal dialog and it\'s modal child-dialog', () => {
      expect(desktop.activeForm).toBe(null);

      let dialog0 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog0);

      let dialog1 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog1);

      let dialog2 = formHelper.createFormWithOneField({
        modal: true,
        parent: dialog1,
        displayParent: dialog1
      });

      desktop.showForm(dialog2);

      // expect dialogs to be in the same order as opened
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, dialog1, dialog2]));

      desktop.activateForm(dialog0);
      // expect dialog0 to be on top (= last in the DOM)
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog2, dialog0]));
      expect(desktop.activeForm).toBe(dialog0);
    });

    it('brings complete hierarchy of a non-modal dialog with 2-levels of modal child dialogs in front', () => {
      expect(desktop.activeForm).toBe(null);

      let dialog0 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog0);

      let dialog1 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog1);

      let parentDialog = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(parentDialog);

      let modalChildDialogLevel1 = formHelper.createFormWithOneField({
        modal: true,
        parent: parentDialog,
        displayParent: parentDialog
      });

      desktop.showForm(modalChildDialogLevel1);

      let modalChildDialogLevel2 = formHelper.createFormWithOneField({
        modal: true,
        parent: modalChildDialogLevel1,
        displayParent: modalChildDialogLevel1
      });
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

    it('keeps position of dialog\'s messagebox relative to it\'s parent dialog while reordering dialogs', () => {
      expect(desktop.activeForm).toBe(null);

      let dialog0 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog0);

      let dialog1 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog1);

      let dialog2 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog2);

      let messagebox = scout.create('MessageBox', {
        parent: dialog2,
        displayParent: dialog2,
        header: 'Title',
        body: 'hello',
        severity: Status.Severity.INFO,
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

    it('brings dialog with messagebox on top upon mousedown on messagebox', () => {
      expect(desktop.activeForm).toBe(null);

      let dialog0 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog0);

      let messagebox = scout.create('MessageBox', {
        parent: dialog0,
        displayParent: dialog0,
        header: 'Title',
        body: 'hello',
        severity: Status.Severity.INFO,
        yesButtonText: 'OK'
      });
      messagebox.open();

      let dialog1 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog1);

      let dialog2 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog2);

      // expect dialogs and messagebox to be in the same order as opened
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, messagebox, dialog1, dialog2]));
      expect(desktop.activeForm).toBe(dialog2);

      messagebox.$container.mousedown();
      // expect dialog0 and messagebox on top
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog2, dialog0, messagebox]));
      expect(desktop.activeForm).toBe(dialog0);
    });

    it('keeps desktop\'s messagebox on top while reordering dialogs', () => {
      expect(desktop.activeForm).toBe(null);

      let dialog0 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog0);

      let dialog1 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog1);

      let dialog2 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog2);

      let messagebox = scout.create('MessageBox', {
        parent: desktop,
        displayParent: desktop,
        header: 'Title',
        body: 'hello',
        severity: Status.Severity.INFO,
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

    it('keeps position of dialog\'s fileChooser relative to it\'s parent dialog while reordering dialogs', () => {
      expect(desktop.activeForm).toBe(null);

      let dialog0 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog0);

      let dialog1 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog1);

      let dialog2 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog2);

      let fileChooser = scout.create('FileChooser', {
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

    it('brings dialog with filechooser on top upon mousedown on filechooser', () => {
      expect(desktop.activeForm).toBe(null);

      let dialog0 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog0);

      let fileChooser = scout.create('FileChooser', {
        parent: dialog0,
        displayParent: dialog0
      });
      fileChooser.open();

      let dialog1 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog1);

      let dialog2 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog2);

      // expect dialogs and fileChooser to be in the same order as opened
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0, fileChooser, dialog1, dialog2]));
      expect(desktop.activeForm).toBe(dialog2);

      fileChooser.$container.mousedown();
      // expect dialog0 and fileChooser on top
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog1, dialog2, dialog0, fileChooser]));
      expect(desktop.activeForm).toBe(dialog0);
    });

    it('does not change position of desktop\'s fileChooser while reordering dialogs', () => {
      expect(desktop.activeForm).toBe(null);

      let dialog0 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog0);

      let dialog1 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog1);

      let dialog2 = formHelper.createFormWithOneField({
        modal: false
      });
      desktop.showForm(dialog2);

      let fileChooser = scout.create('FileChooser', {
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

    it('activates parent view upon activation of child dialog', () => {
      expect(desktop.activeForm).toBe(null);

      let tabBox = desktop.bench.getTabBox('C');

      let viewForm0 = formHelper.createFormWithOneField();
      viewForm0.displayHint = Form.DisplayHint.VIEW;
      desktop.showForm(viewForm0);

      let dialog0 = formHelper.createFormWithOneField({
        modal: false
      });
      dialog0.parent = viewForm0;
      dialog0.displayParent = viewForm0;
      desktop.showForm(dialog0);

      // expect viewForm0 as currentView and dialog0 in the DOM
      expect(tabBox.currentView).toEqual(viewForm0);
      expect(desktopOverlayHtmlElements()).toEqual(widgetHtmlElements([dialog0]));

      let viewForm1 = formHelper.createFormWithOneField();
      viewForm1.displayHint = Form.DisplayHint.VIEW;
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

  describe('activeForm', () => {

    beforeEach(() => {
      session._renderDesktop();
    });

    it('will be set to the display parent form if dialog closes', done => {
      let dialogParent = formHelper.createFormWithOneField({
        displayHint: 'dialog'
      });
      let dialog = formHelper.createFormWithOneField({
        displayHint: 'dialog',
        displayParent: dialogParent
      });
      dialogParent.open()
        .then(() => {
          expect(desktop.activeForm).toBe(dialogParent);
        })
        .then(dialog.open.bind(dialog))
        .then(() => {
          expect(desktop.activeForm).toBe(dialog);

          dialog.close();
          expect(desktop.activeForm).toBe(dialogParent);
        })
        .catch(fail)
        .always(done);
    });

    it('will send the outline to back', () => {
      let outline = outlineHelper.createOutline(outlineHelper.createModelFixture(3, 2));
      desktop.setOutline(outline);

      let view = formHelper.createFormWithOneField({
        displayHint: Form.DisplayHint.VIEW
      });

      desktop.showForm(view);
      outline.selectNode(outline.nodes[0]);
      desktop.bringOutlineToFront();

      desktop.activateForm(view);
      outline.deselectAll();

      expect(desktop.activeForm).toBe(view);
      expect(view.attached).toBe(true);
    });

    it('will be set to currentView if dialog closes and there is no display parent form', done => {
      let view = formHelper.createFormWithOneField({
        displayHint: 'view'
      });
      let dialog = formHelper.createFormWithOneField({
        displayHint: 'dialog'
      });
      view.open()
        .then(() => {
          expect(desktop.activeForm).toBe(view);
        })
        .then(dialog.open.bind(dialog))
        .then(() => {
          expect(desktop.activeForm).toBe(dialog);

          dialog.close();
          expect(desktop.activeForm).toBe(view);
        })
        .catch(fail)
        .always(done);
    });

    it('will be set to undefined if dialog closes and there is no currentView and no display parent', done => {
      let dialog = formHelper.createFormWithOneField({
        displayHint: 'dialog'
      });
      dialog.open()
        .then(() => {
          expect(desktop.activeForm).toBe(dialog);

          dialog.close();
          expect(desktop.activeForm).toBe(null);
        })
        .catch(fail)
        .always(done);
    });

    it('must not be the detail form', done => {
      let outline = outlineHelper.createOutlineWithOneDetailForm();
      desktop.setOutline(outline);
      outline.selectNodes(outline.nodes[0]);
      let dialog = formHelper.createFormWithOneField({
        displayHint: 'dialog'
      });
      dialog.open()
        .then(() => {
          expect(desktop.activeForm).toBe(dialog);

          dialog.close();
          expect(desktop.activeForm).toBe(null);
        })
        .catch(fail)
        .always(done);
    });

    it('must not be the detail form even if it is the display parent', done => {
      let outline = outlineHelper.createOutlineWithOneDetailForm();
      desktop.setOutline(outline);
      outline.selectNodes(outline.nodes[0]);
      let detailForm = outline.nodes[0].detailForm;
      let dialog = formHelper.createFormWithOneField({
        displayHint: 'dialog',
        displayParent: detailForm
      });
      dialog.open()
        .then(() => {
          expect(desktop.activeForm).toBe(dialog);

          dialog.close();
          expect(desktop.activeForm).toBe(null);
        })
        .catch(fail)
        .always(done);
    });

    it('must be a form', done => {
      let outline = outlineHelper.createOutlineWithOneDetailTable();
      desktop.setOutline(outline);
      outline.selectNodes(outline.nodes[0]);
      let dialog = formHelper.createFormWithOneField({
        displayHint: 'dialog'
      });
      dialog.open()
        .then(() => {
          expect(desktop.activeForm).toBe(dialog);

          dialog.close();
          expect(desktop.activeForm).toBe(null);
        })
        .catch(fail)
        .always(done);
    });

  });

  describe('displayStyle', () => {

    describe('COMPACT', () => {

      beforeEach(() => {
        desktop.displayStyle = Desktop.DisplayStyle.COMPACT;
        // Flags currently only set by server, therefore we need to set them here as well
        desktop.navigationVisible = true;
        desktop.benchVisible = false;
        desktop.headerVisible = false;
        session._renderDesktop();
      });

      it('shows bench and hides navigation if a view is open', () => {
        let form = formHelper.createViewWithOneField();
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

      it('opens the bench again if a view is shown right after the last view was closed', () => {
        jasmine.clock().install();
        let form = formHelper.createViewWithOneField();
        let form2 = formHelper.createViewWithOneField();
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
        jasmine.clock().uninstall();
      });

      it('hides bench and shows navigation if the last view gets closed', () => {
        let form1 = formHelper.createViewWithOneField();
        let form2 = formHelper.createViewWithOneField();
        expect(form1.rendered).toBe(false);
        expect(form2.rendered).toBe(false);
        expect(desktop.navigationVisible).toBe(true);
        expect(desktop.benchVisible).toBe(false);
        expect(desktop.headerVisible).toBe(false);

        // open first form, bench is shown
        desktop.showForm(form1);
        expect(form1.rendered).toBe(true);
        expect(form1.attached).toBe(true);
        expect(form2.rendered).toBe(false);
        expect(desktop.navigationVisible).toBe(false);
        expect(desktop.benchVisible).toBe(true);
        expect(desktop.headerVisible).toBe(true);

        // open second form, bench is still shown
        desktop.showForm(form2);
        expect(form1.rendered).toBe(true);
        expect(form1.attached).toBe(false);
        expect(form2.rendered).toBe(true);
        expect(form2.attached).toBe(true);
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

      it('shows outline dialog again when switching from bench to navigation', () => {
        let outline = outlineHelper.createOutline();
        desktop.setOutline(outline);
        let dialog = formHelper.createFormWithOneField({
          displayParent: desktop.outline
        });
        let view = formHelper.createViewWithOneField();
        expect(dialog.rendered).toBe(false);
        expect(desktop.navigationVisible).toBe(true);
        expect(desktop.benchVisible).toBe(false);

        // Show dialog -> navigation still visible
        dialog.show();
        expect(dialog.displayParent).toBe(desktop.outline);
        expect(view.rendered).toBe(false);
        expect(dialog.rendered).toBe(true);
        expect(dialog.attached).toBe(true);
        expect(desktop.navigationVisible).toBe(true);
        expect(desktop.benchVisible).toBe(false);

        // Show view -> switch to bench
        view.show();
        expect(view.rendered).toBe(true);
        expect(dialog.rendered).toBe(true);
        expect(dialog.attached).toBe(false);
        expect(desktop.navigationVisible).toBe(false);
        expect(desktop.benchVisible).toBe(true);

        // Disable remove animation, otherwise view would not be removed immediately
        desktop.bench.animateRemoval = false;

        // Close view -> switch to navigation -> dialog needs to be shown again
        view.close();
        expect(view.rendered).toBe(false);
        expect(dialog.rendered).toBe(true);
        expect(dialog.attached).toBe(true);
        expect(desktop.navigationVisible).toBe(true);
        expect(desktop.benchVisible).toBe(false);
      });

      it('does not bring activateForm to fail for fake views', () => {
        let form = formHelper.createFormWithOneField();
        form.displayHint = Form.DisplayHint.VIEW;
        scout.create('FormMenu', {
          parent: desktop,
          form: form
        });

        // Does not really work (meaning menu is not selected) but must not throw an exception either
        form.activate();
        expect().nothing();
      });

    });
  });

  describe('cancelViewsMenu', () => {

    let view1, view2, view3, promises;

    beforeEach(() => {
      session._renderDesktop();

      promises = [];

      view1 = formHelper.createViewWithOneField({
        title: 'view1'
      });
      view2 = formHelper.createViewWithOneField({
        title: 'view2'
      });
      view3 = formHelper.createViewWithOneField({
        title: 'view3'
      });

      desktop.showForm(view1);
      desktop.showForm(view2);
      desktop.showForm(view3);

      spyOn(view1, 'close').and.callThrough();
      spyOn(view2, 'close').and.callThrough();
      spyOn(view3, 'close').and.callThrough();

      spyOn(view1, 'ok').and.callThrough();
      spyOn(view2, 'ok').and.callThrough();
      spyOn(view3, 'ok').and.callThrough();
    });

    afterEach(() => {
      view1.close();
      view2.close();
      view3.close();
    });

    it('check open tabs', () => {
      expect(desktop.bench.getViews()).toEqual([view1, view2, view3]);
    });

    it('close all open tabs on desktop', done => {
      promises.push(view1.whenClose());
      promises.push(view2.whenClose());
      promises.push(view3.whenClose());

      desktop.cancelViews([view1, view2, view3]);

      $.promiseAll(promises).then(() => {
        expect(view1.close).toHaveBeenCalled();
        expect(view2.close).toHaveBeenCalled();
        expect(view3.close).toHaveBeenCalled();
        expect(desktop.views).toEqual([]);
      })
        .catch(fail)
        .always(done);
    });

    it('close some open tabs on desktop', done => {
      promises.push(view1.whenClose());
      promises.push(view2.whenClose());

      desktop.cancelViews([view1, view2]);

      $.promiseAll(promises).then(() => {
        expect(view1.close).toHaveBeenCalled();
        expect(view2.close).toHaveBeenCalled();
        expect(view3.close).not.toHaveBeenCalled();
        expect(desktop.bench.getViews()).toEqual([view3]);
      })
        .catch(fail)
        .always(done);
    });

    it('close others and expect to not cancel the display parent of a modal form', done => {
      desktop.activateForm(view1);
      expect(view1.rendered).toBe(true);
      let modalView = formHelper.createViewWithOneField({
        title: 'viewModal',
        displayParent: view1,
        modal: true
      });
      desktop.showForm(modalView);

      promises.push(view2.whenClose());
      promises.push(view3.whenClose());

      desktop.bench.getViewTab(modalView)._onCloseOther();

      $.promiseAll(promises).then(() => {
        expect(view1.close).not.toHaveBeenCalled();
        expect(view2.close).toHaveBeenCalled();
        expect(view3.close).toHaveBeenCalled();
        expect(desktop.bench.getViews()).toEqual([view1, modalView]);
      })
        .catch(fail)
        .always(done);
    });

    it('close tabs and save unsaved changes', done => {
      view2.rootGroupBox.fields[0].setValue('Foo');

      promises.push(view1.whenClose());
      promises.push(view2.whenSave());
      promises.push(view2.whenClose());

      desktop.cancelViews([view1, view2]);

      // UnsavedFormChangesForm should be the last child
      let unsavedFormChangesForm = arrays.last(desktop.children);
      expect(unsavedFormChangesForm.objectType).toBe('scout.UnsavedFormChangesForm');
      let openFormsField = unsavedFormChangesForm.rootGroupBox.fields[0].fields[0];
      expect(openFormsField.id).toBe('OpenFormsField');
      openFormsField.when('lookupCallDone').then(() => {
        // default is all selected, view2 should be saved
        expect(openFormsField.value.length).toBe(1);
        expect(openFormsField.value[0]).toEqual(view2);
        unsavedFormChangesForm.ok();
      });

      $.promiseAll(promises).then(() => {
        expect(view1.close).toHaveBeenCalled();
        expect(view2.ok).toHaveBeenCalled();
        expect(desktop.bench.getViews()).toEqual([view3]);
      })
        .catch(fail)
        .always(done);
    });

    it('close tabs and cancel UnsavedFormChangesForm', done => {
      view2.rootGroupBox.fields[0].setValue('Foo');

      desktop.cancelViews([view1, view2]);
      // UnsavedFormChangesForm should be the last child
      let unsavedFormChangesForm = arrays.last(desktop.children);
      expect(unsavedFormChangesForm.objectType).toBe('scout.UnsavedFormChangesForm');
      unsavedFormChangesForm.whenPostLoad().then(() => {
        unsavedFormChangesForm.close();
      });

      unsavedFormChangesForm.whenClose().then(() => {
        expect(desktop.bench.getViews()).toEqual([view1, view2, view3]);
      })
        .catch(fail)
        .always(done);
    });

    it('close tabs and dont save unsaved changes', done => {
      view2.rootGroupBox.fields[0].setValue('Foo');

      promises.push(view1.whenClose());
      promises.push(view2.whenClose());

      desktop.cancelViews([view1, view2]);

      // UnsavedFormChangesForm should be the last child
      let unsavedFormChangesForm = arrays.last(desktop.children);
      expect(unsavedFormChangesForm.objectType).toBe('scout.UnsavedFormChangesForm');
      let openFormsField = unsavedFormChangesForm.rootGroupBox.fields[0].fields[0];
      expect(openFormsField.id).toBe('OpenFormsField');
      openFormsField.when('lookupCallDone').then(() => {
        expect(openFormsField.value.length).toBe(1);
        openFormsField.setValue(null);
        unsavedFormChangesForm.ok();
      });

      promises.push(unsavedFormChangesForm.whenClose());

      $.promiseAll(promises).then(() => {
        expect(view1.close).toHaveBeenCalled();
        expect(view2.ok).not.toHaveBeenCalled();
        expect(view2.close).toHaveBeenCalled();
        expect(desktop.bench.getViews()).toEqual([view3]);
      })
        .catch(fail)
        .always(done);
    });

    it('close tabs when one tab has an open message box', () => {
      let msgBox = scout.create('MessageBox', {
        parent: view3,
        displayParent: view3
      });
      msgBox.open();
      spyOn(msgBox, 'close').and.callThrough();
      expect(msgBox.rendered).toBe(true);

      jasmine.clock().install();

      desktop.cancelViews([view2, view3]);

      jasmine.clock().tick(10);

      expect(msgBox.close).toHaveBeenCalled();
      expect(view3.close).toHaveBeenCalled();
      expect(view2.close).toHaveBeenCalled();
      expect(desktop.bench.getViews()).toEqual([view1]);
      jasmine.clock().uninstall();
    });

    it('close tabs with open file chooser and save unsaved changes', done => {
      desktop.showForm(view2);
      view2.rootGroupBox.fields[0].setValue('Foo');

      let fileChooser = scout.create('FileChooser', {
        parent: view2,
        displayParent: view2
      });
      fileChooser.open();
      expect(fileChooser.rendered).toBe(true);

      spyOn(fileChooser, 'close').and.callThrough();

      promises.push(view1.whenClose());
      promises.push(view2.whenClose());

      desktop.cancelViews([view1, view2]);

      // UnsavedFormChangesForm should be the last child
      let unsavedFormChangesForm = arrays.last(desktop.children);
      expect(unsavedFormChangesForm.objectType).toBe('scout.UnsavedFormChangesForm');
      let openFormsField = unsavedFormChangesForm.rootGroupBox.fields[0].fields[0];
      expect(openFormsField.id).toBe('OpenFormsField');
      openFormsField.when('lookupCallDone').then(() => {
        // default is all selected, view2 should be saved
        expect(openFormsField.value.length).toBe(1);
        expect(openFormsField.value[0]).toEqual(view2);
        unsavedFormChangesForm.ok();
      });

      $.promiseAll(promises).then(() => {
        expect(view1.close).toHaveBeenCalled();
        expect(view2.ok).toHaveBeenCalled();
        expect(fileChooser.close).toHaveBeenCalled();
        expect(desktop.bench.getViews()).toEqual([view3]);
      })
        .catch(fail)
        .always(done);
    });

    it('close tabs when one tab has an open modal dialog with unsaved changes', done => {
      let modalDialog = formHelper.createFormWithOneField({
        parent: view2,
        displayParent: view2
      });
      desktop.showForm(view2);
      expect(view2.rendered).toBe(true);
      desktop.showForm(modalDialog);
      expect(modalDialog.rendered).toBe(true);

      modalDialog.rootGroupBox.fields[0].setValue('Foo');

      promises.push(view1.whenClose());
      promises.push(view2.whenClose());
      promises.push(modalDialog.whenSave());
      promises.push(modalDialog.whenClose());

      spyOn(modalDialog, 'ok').and.callThrough();

      desktop.cancelViews([view1, view2]);

      // UnsavedFormChangesForm should be the last child
      let unsavedFormChangesForm = arrays.last(desktop.children);
      expect(unsavedFormChangesForm.objectType).toBe('scout.UnsavedFormChangesForm');
      let openFormsField = unsavedFormChangesForm.rootGroupBox.fields[0].fields[0];
      expect(openFormsField.id).toBe('OpenFormsField');
      openFormsField.when('lookupCallDone').then(() => {
        expect(openFormsField.value.length).toBe(1);
        // the sub-form has unsaved changes (modalDialg), in the unsavedFormChangesForm the parent-view should be displayed
        expect(openFormsField.value[0]).toEqual(view2);
        unsavedFormChangesForm.ok();
      });

      $.promiseAll(promises).then(() => {
        expect(view1.close).toHaveBeenCalled();
        expect(view2.ok).toHaveBeenCalled();
        expect(modalDialog.ok).toHaveBeenCalled();
        expect(desktop.bench.getViews()).toEqual([view3]);
      })
        .catch(fail)
        .always(done);
    });

    it('close tabs when one tab has an open modal dialog without unsaved changes', done => {
      let modalDialog = formHelper.createFormWithOneField({
        parent: view2,
        displayParent: view2,
        modal: true
      });
      desktop.showForm(view2);
      expect(view2.rendered).toBe(true);
      desktop.showForm(modalDialog);
      expect(modalDialog.rendered).toBe(true);

      promises.push(view1.whenClose());
      promises.push(view2.whenClose());
      promises.push(modalDialog.whenClose());

      spyOn(modalDialog, 'close').and.callThrough();
      spyOn(modalDialog, 'ok').and.callThrough();

      desktop.cancelViews([view1, view2]);

      $.promiseAll(promises).then(() => {
        expect(view1.close).toHaveBeenCalled();
        expect(view2.close).toHaveBeenCalled();
        expect(view2.ok).not.toHaveBeenCalled();
        expect(modalDialog.close).toHaveBeenCalled();
        expect(modalDialog.ok).not.toHaveBeenCalled();
        expect(desktop.bench.getViews()).toEqual([view3]);
      })
        .catch(fail)
        .always(done);
    });

    it('close tabs when one tab has invalid unsaved changes', () => {
      view2.rootGroupBox.fields[0].setValidator(value => {
        if (strings.equalsIgnoreCase(value, 'Foo')) {
          throw new Error('Validation failed');
        }
        return value;
      });
      view2.rootGroupBox.fields[0].touch();
      view2.rootGroupBox.fields[0].setValue('Foo');

      jasmine.clock().install();
      desktop.cancelViews([view1, view2]);

      // UnsavedFormChangesForm should be the last child
      let unsavedFormChangesForm = arrays.last(desktop.children);
      expect(unsavedFormChangesForm.objectType).toBe('scout.UnsavedFormChangesForm');
      let openFormsField = unsavedFormChangesForm.rootGroupBox.fields[0].fields[0];
      expect(openFormsField.id).toBe('OpenFormsField');
      openFormsField.when('lookupCallDone').then(() => {
        expect(openFormsField.value.length).toBe(1);
        expect(openFormsField.value[0]).toEqual(view2);
        unsavedFormChangesForm.ok();
        jasmine.clock().tick();
        // validation message should be displayed since view2 is in invalid state
        expect(session.$entryPoint.find('.messagebox').length).toBe(1);
        desktop.messageBoxes[0].yesButton.doAction();
        jasmine.clock().tick();
        // untick all entries to not save the unsaved changes
        openFormsField.setValue(null);
        unsavedFormChangesForm.ok();
      });

      jasmine.clock().tick(10);

      expect(view1.ok).not.toHaveBeenCalled();
      expect(view1.close).toHaveBeenCalled();
      expect(view2.ok).not.toHaveBeenCalled();
      expect(view2.close).toHaveBeenCalled();
      expect(desktop.bench.getViews()).toEqual([view3]);
      jasmine.clock().uninstall();
    });

    it('close tabs when one tab has a child with invalid unsaved changes', () => {
      let modalDialog = formHelper.createFormWithOneField({
        parent: view2,
        displayParent: view2,
        modal: true
      });
      desktop.showForm(view2);
      expect(view2.rendered).toBe(true);
      desktop.showForm(modalDialog);
      expect(modalDialog.rendered).toBe(true);

      spyOn(modalDialog, 'close').and.callThrough();
      spyOn(modalDialog, 'ok').and.callThrough();

      modalDialog.rootGroupBox.fields[0].setValidator(value => {
        if (strings.equalsIgnoreCase(value, 'Foo')) {
          throw new Error('Validation failed');
        }
        return value;
      });
      modalDialog.rootGroupBox.fields[0].touch();
      modalDialog.rootGroupBox.fields[0].setValue('Foo');

      jasmine.clock().install();
      desktop.cancelViews([view1, view2]);
      // UnsavedFormChangesForm should be the last child
      let unsavedFormChangesForm = arrays.last(desktop.children);
      expect(unsavedFormChangesForm.objectType).toBe('scout.UnsavedFormChangesForm');
      let openFormsField = unsavedFormChangesForm.rootGroupBox.fields[0].fields[0];
      expect(openFormsField.id).toBe('OpenFormsField');
      openFormsField.when('lookupCallDone').then(() => {
        expect(openFormsField.value.length).toBe(1);
        expect(openFormsField.value[0]).toEqual(view2);
        unsavedFormChangesForm.ok();
        jasmine.clock().tick();
        // validation message should be displayed since view2 is in invalid state
        expect(session.$entryPoint.find('.messagebox').length).toBe(1);
        desktop.messageBoxes[0].yesButton.doAction();
        jasmine.clock().tick();
        // untick all entries to not save the unsaved changes
        openFormsField.setValue(null);
        unsavedFormChangesForm.ok();
      });

      jasmine.clock().tick(10);

      expect(modalDialog.ok).not.toHaveBeenCalled();
      expect(modalDialog.close).toHaveBeenCalled();
      expect(view1.ok).not.toHaveBeenCalled();
      expect(view1.close).toHaveBeenCalled();
      expect(view2.ok).not.toHaveBeenCalled();
      expect(view2.close).toHaveBeenCalled();
      expect(desktop.bench.getViews()).toEqual([view3]);
      jasmine.clock().uninstall();
    });

  });

  describe('modal form', () => {

    let view1;

    beforeEach(() => {
      session._renderDesktop();

      view1 = formHelper.createViewWithOneField({
        title: 'view01',
        modal: false
      });

      desktop.showForm(view1);

      spyOn(view1, 'close').and.callThrough();

      spyOn(view1, 'ok').and.callThrough();
    });

    afterEach(() => {
      view1.close();
    });

    it('of a simple form.', () => {

      let viewModal = formHelper.createViewWithOneField({
        title: 'viewModal',
        parent: view1,
        displayParent: view1,
        modal: true
      });

      desktop.showForm(viewModal);

      desktop.showForm(viewModal);
      expect(viewModal.rendered).toBe(true);

      desktop.activateForm(view1);
      expect(view1.rendered).toBe(true);
      expect(view1.$container.children('.glasspane').length).toBe(1);

      viewModal.close();
      expect(view1.$container.children('.glasspane').length).toBe(0);

    });

    it('of a wrapped form', () => {
      let form = scout.create('Form', {
        parent: session.desktop,
        id: 'outerForm',
        displayHint: Form.DisplayHint.VIEW,
        rootGroupBox: {
          objectType: 'GroupBox',
          fields: [{
            objectType: 'WrappedFormField',
            id: 'wrappedFormField',
            innerForm: {
              id: 'innerForm',
              objectType: 'Form',
              rootGroupBox: {
                objectType: 'GroupBox',
                fields: [{
                  id: 'myButton',
                  objectType: 'Button',
                  keyStroke: 'ctrl-1',
                  keyStrokeScope: 'outerForm'
                }]
              }
            }
          }]
        }
      });
      desktop.showForm(form);
      let innerForm = form.widget('wrappedFormField').innerForm;

      let viewModal = formHelper.createViewWithOneField({
        title: 'viewModal',
        parent: innerForm,
        displayParent: innerForm,
        modal: true
      });

      desktop.showForm(viewModal);

      desktop.activateForm(form);
      expect(form.rendered).toBe(true);
      expect(form.$container.children('.glasspane').length).toBe(1);

      viewModal.close();
      expect(form.$container.children('.glasspane').length).toBe(0);

    });

  });

  describe('glassPanes', () => {

    /**
     * This test makes sure glass panes are rendered correctly for message-boxes and file-choosers
     * when opened over a modal dialog. This case occurred in ticket #274353.
     */
    it('modal dialog should not be clickable when desktop-modal message-box is opened', () => {
      let desktop = session.desktop;
      desktop.render(session.$entryPoint);
      let modalDialog = scout.create('Form', {
        parent: desktop,
        displayHint: Form.DisplayHint.DIALOG,
        modal: true,
        rootGroupBox: {
          objectType: 'GroupBox'
        }
      });
      desktop.showForm(modalDialog, 0);

      // Test with message-box
      MessageBoxes.createOk(desktop).withBody('foo').buildAndOpen();
      let $glassPanes = modalDialog.$container.find('.glasspane');
      expect($glassPanes.length > 0).toBe(true);
      let messageBox = scout.widget(desktop.$container.find('.messagebox')[0]);
      messageBox.close();

      // Test with file-chooser
      let fileChooser = scout.create('FileChooser', {
        parent: desktop
      });
      fileChooser.open();
      $glassPanes = modalDialog.$container.find('.glasspane');
      expect($glassPanes.length > 0).toBe(true);
      fileChooser.close();

      // Test with busy-indicator
      let busyIndicator = scout.create('BusyIndicator', {
        parent: desktop
      });
      busyIndicator.render();
      $glassPanes = modalDialog.$container.find('.glasspane');
      expect($glassPanes.length > 0).toBe(true);
      busyIndicator.remove();
    });

    /**
     * This test creates two modal views, the second view opens a message-box
     * modal to the second view. #274353
     */
    it('message-box in modal-view should be clickable (have no glass-pane)', () => {
      let desktop = session.desktop;
      desktop.render(session.$entryPoint);
      let viewA = scout.create('Form', {
        parent: desktop,
        displayHint: Form.DisplayHint.VIEW,
        modal: true,
        rootGroupBox: {
          objectType: 'GroupBox'
        }
      });
      let viewB = scout.create('Form', {
        parent: desktop,
        displayHint: Form.DisplayHint.VIEW,
        modal: true,
        rootGroupBox: {
          objectType: 'GroupBox'
        }
      });
      desktop.showForm(viewA, 0);
      desktop.showForm(viewB, 1);
      MessageBoxes.createOk(viewB).withBody('foo').buildAndOpen();

      // trigger re-rendering of glass-panes. This is what happens in the ticket,
      // when the table cell-editor is closed.
      session.focusManager.rerenderGlassPanes();

      let $messageBox = desktop.$container.find('.messagebox');
      expect($messageBox.length).toBe(1);
      expect($messageBox.find('.glasspane').length).toBe(0);

      // Additionally to the case in the ticket, open a popup which makes use of the
      // glass-pane filter: should not have an effect on modality.
      let helpPopup = scout.create('WidgetPopup', {
        parent: viewB,
        widget: {
          objectType: 'StringField'
        }
      });
      let glassPaneTargetFilter = (target, element) => {
        return target !== helpPopup.$container[0];
      };
      desktop.addGlassPaneTargetFilter(glassPaneTargetFilter);
      helpPopup.open();

      session.focusManager.rerenderGlassPanes();
      desktop.$container.find('.messagebox');
      expect($messageBox.length).toBe(1);
      expect($messageBox.find('.glasspane').length).toBe(0);
    });

  });
});
