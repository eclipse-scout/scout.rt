/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DummyLookupCall, FormSpecHelper, OutlineSpecHelper} from '../../src/testing/index';
import {
  arrays, BusyIndicator, Button, DateField, DatePickerPopup, Desktop, DesktopNotification, DesktopTab, Event, FileChooser, Form, FormMenu, GroupBox, ListBox, MessageBox, MessageBoxes, ObjectOrChildModel, Outline, OutlineViewButton, Popup,
  scout, SmartField, SmartFieldPopup, Status, StringField, strings, Tooltip, UnsavedFormChangesForm, Widget, WidgetPopup, WrappedFormField
} from '../../src/index';

describe('Desktop', () => {
  let session: SandboxSession, desktop: Desktop, outlineHelper: OutlineSpecHelper, formHelper: FormSpecHelper;

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
    let ntfc: DesktopNotification, parent = new Widget();

    beforeEach(() => {
      parent.session = session;
      ntfc = scout.create(DesktopNotification, {
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
      expect(desktop.$notifications).not.toBe(null);
    });

    it('schedules addNotification when desktop is not rendered', () => {
      scout.create(DesktopNotification, {
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
      let event = new Event<DesktopNotification>();
      event.source = ntfc;
      desktop._onNotificationRemove(event); // this method is usually called after fade-out animation asynchronously
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
      desktop.bringOutlineToFront();
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
      desktop.bringOutlineToFront();
      expect(desktop.outline).toBe(outline2);
      expect(outline1.rendered).toBe(false);
      expect(outline2.rendered).toBe(true);
      expect(form.rendered).toBe(false);

      // switch back to the outline with the modal form
      desktop.setOutline(outline1);
      desktop.bringOutlineToFront();
      expect(desktop.outline).toBe(outline1);
      expect(outline1.rendered).toBe(true);
      expect(outline2.rendered).toBe(false);

      // and check that the form is properly rendered
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
      jasmine.clock().tick(0);
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
      let msgBox = scout.create(MessageBox, {
        parent: outline,
        displayParent: outline
      });
      msgBox.open();
      let fileChooser = scout.create(FileChooser, {
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

      let msgBox = scout.create(MessageBox, {
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
      let fileChooser = scout.create(FileChooser, {
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
            objectType: Outline,
            modelClass: 'a.cbdke',
            title: 'Title',
            id: '123',
            dialogs: [{
              objectType: Form,
              displayHint: Form.DisplayHint.DIALOG,
              modal: true,
              rootGroupBox: {
                objectType: GroupBox
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
            objectType: Outline,
            modelClass: 'a.cbdke',
            title: 'Title',
            id: '123',
            messageBoxes: [{
              objectType: MessageBox
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
            objectType: Outline,
            modelClass: 'a.cbdke',
            title: 'Title',
            id: '123',
            fileChoosers: [{
              objectType: FileChooser
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

  describe('showForm', () => {

    beforeEach(() => {
      session._renderDesktop();
    });

    it('shows and activates the form', () => {
      expect(desktop.activeForm).toBe(null);

      let form = formHelper.createFormWithOneField();
      expect(form.isShown()).toBe(false);
      desktop.showForm(form);
      expect(form.rendered).toBe(true);
      expect(desktop.activeForm).toBe(form);
      expect(form.isShown()).toBe(true);

      let anotherForm = formHelper.createFormWithOneField();
      desktop.showForm(anotherForm);
      expect(form.rendered).toBe(true);
      expect(desktop.activeForm).toBe(anotherForm);
      expect(form.isShown()).toBe(true);
    });

    it('adds a view to the bench if displayHint is View', () => {
      let form = formHelper.createFormWithOneField();
      let tabBox = desktop.bench.getTabBox('C');
      form.displayHint = Form.DisplayHint.VIEW;
      desktop.showForm(form);

      expect(form.rendered).toBe(true);
      expect(form.parent).toBe(tabBox);
      expect(form.isShown()).toBe(true);
      expect(form.$container.parent()[0]).toBe(tabBox.$viewContent[0]);
    });
  });

  describe('hideForm', () => {
    beforeEach(() => {
      session._renderDesktop();
    });

    it('hides the form', () => {
      let form = formHelper.createFormWithOneField();
      desktop.showForm(form);
      expect(form.isShown()).toBe(true);

      desktop.hideForm(form);
      expect(form.rendered).toBe(false);
      expect(form.isShown()).toBe(false);
      expect(form.destroyed).toBe(false);
    });

    it('removes the view from the bench if displayHint is View', () => {
      let form = formHelper.createFormWithOneField();
      form.displayHint = Form.DisplayHint.VIEW;
      desktop.showForm(form);
      expect(form.isShown()).toBe(true);

      desktop.hideForm(form);
      expect(form.rendered).toBe(false);
      expect(form.isShown()).toBe(false);
      expect(form.destroyed).toBe(false);
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

      let messagebox = scout.create(MessageBox, {
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

      let messagebox = scout.create(MessageBox, {
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

      let messagebox = scout.create(MessageBox, {
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

      let fileChooser = scout.create(FileChooser, {
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

      let fileChooser = scout.create(FileChooser, {
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

      let fileChooser = scout.create(FileChooser, {
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
      expect(desktop.activeForm).toBe(view);

      outline.selectNode(outline.nodes[0]);
      desktop.bringOutlineToFront();
      expect(desktop.activeForm).toBeNull();

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

    it('must not be a removed form', () => {
      const outline = outlineHelper.createOutlineWithOneDetailForm();
      const page = outline.nodes[0];

      desktop.setOutline(outline);
      outline.selectNode(page);

      const form = formHelper.createFormWithOneField({
        displayHint: Form.DisplayHint.DIALOG,
        displayParent: page.detailForm
      });

      desktop.showForm(form);
      expect(desktop.activeForm).toBe(form);

      outline.selectNode(outline.nodes[1]);
      desktop.bringOutlineToFront();
      expect(desktop.activeForm).toBeNull();

      outline.selectNode(page);
      expect(desktop.activeForm).toBe(form);
    });
  });

  describe('createFormExclusive', () => {
    beforeEach(() => {
      session._renderDesktop();
    });

    it('doesn\'t open the form if there is already a form open with the same exclusive key', async () => {
      let form = session.desktop.createFormExclusive(() => formHelper.createViewWithOneField(), 5);
      expect(form.exclusiveKey()).toBe(5);

      await form.open();
      expect(form.rendered).toBe(true);

      let formCreated = false;
      let form2 = session.desktop.createFormExclusive(() => {
        formCreated = true;
        return formHelper.createViewWithOneField();
      }, () => 5);
      expect(formCreated).toBe(false);
      expect(form2).toBe(form);
    });

    it('opens the form if there is already an exclusive form open but with a different exclusive key', async () => {
      let form = session.desktop.createFormExclusive(() => formHelper.createViewWithOneField(), 5);
      await form.open();
      expect(form.rendered).toBe(true);

      let form2 = session.desktop.createFormExclusive(() => formHelper.createViewWithOneField(), 3);
      await form2.open();
      expect(form2).not.toBe(form);
      expect(form2.rendered).toBe(true);
    });

    it('activates the existing form if another form with the same exclusive key should be opened', async () => {
      let form = session.desktop.createFormExclusive(() => formHelper.createViewWithOneField(), 5);
      await form.open();
      expect(form.rendered).toBe(true);
      expect(session.desktop.activeForm).toBe(form);

      session.desktop.activateForm(null);
      expect(session.desktop.activeForm).toBe(null);

      let form2 = session.desktop.createFormExclusive(() => formHelper.createViewWithOneField(), 5);
      await form2.open();
      expect(form2).toBe(form);
      expect(form2.rendered).toBe(true);
      expect(session.desktop.activeForm).toBe(form);
    });

    it('also works if existing forms weren\'t opened using createFormExclusive', async () => {
      let form = formHelper.createViewWithOneField({exclusiveKey: 5});
      await form.open();
      expect(form.rendered).toBe(true);

      let form2 = session.desktop.createFormExclusive(() => formHelper.createViewWithOneField(), 3);
      await form2.open();
      expect(form2).not.toBe(form);
      expect(form2.rendered).toBe(true);
    });

    it('also works with exclusiveKey being a function', async () => {
      let form = formHelper.createViewWithOneField({exclusiveKey: () => 3});
      expect(form.exclusiveKey()).toBe(3);
      await form.open();

      let form2 = session.desktop.createFormExclusive(() => formHelper.createViewWithOneField(), () => 3);
      expect(form2.exclusiveKey()).toBe(3);
      await form2.open();
      expect(form2).toBe(form);

      let form3 = session.desktop.createFormExclusive(() => formHelper.createViewWithOneField(), 3);
      expect(form3.exclusiveKey()).toBe(3);
      await form3.open();
      expect(form3).toBe(form);

      let form4 = session.desktop.createFormExclusive(Form, {parent: session.desktop}, () => 3);
      expect(form4.exclusiveKey()).toBe(3);
      await form4.open();
      expect(form4).toBe(form);

      let form5 = session.desktop.createFormExclusive(Form, {parent: session.desktop}, 3);
      expect(form5.exclusiveKey()).toBe(3);
      await form5.open();
      expect(form5).toBe(form);
    });

    it('also considers forms whose display parent is not the desktop', async () => {
      let outline = outlineHelper.createOutlineWithOneDetailForm();
      let form = session.desktop.createFormExclusive(() => formHelper.createViewWithOneField({displayParent: outline}), 5);
      await form.open();

      // Should only activate form
      let form2 = session.desktop.createFormExclusive(() => formHelper.createViewWithOneField(), 5);
      await form2.open();
      expect(form2).toBe(form);
      expect(form2.rendered).toBe(true);
      expect(session.desktop.activeForm).toBe(form2);

      // New form with first form as display parent
      let form3 = session.desktop.createFormExclusive(() => formHelper.createViewWithOneField({displayParent: form}), 3);
      await form3.open();
      expect(form3).not.toBe(form);
      expect(form3.rendered).toBe(true);
      expect(session.desktop.activeForm).toBe(form3);

      form2.activate();
      expect(session.desktop.activeForm).toBe(form2);

      // Should only activate form3
      let form4 = session.desktop.createFormExclusive(() => formHelper.createViewWithOneField(), 3);
      await form4.open();
      expect(form4).toBe(form3);
      expect(form4.rendered).toBe(true);
      expect(session.desktop.activeForm).toBe(form4);
    });

    class PersonForm extends Form {
    }

    class PersonFormExt extends PersonForm {
    }

    class CompanyForm extends Form {
    }

    it('opens the form even if the exclusive key is the same but the form from a different class', async () => {
      let form = session.desktop.createFormExclusive(PersonForm, {parent: session.desktop}, 5);
      await form.open();
      expect(session.desktop.activeForm).toBe(form);

      // Same class, same key
      let form2 = session.desktop.createFormExclusive(PersonForm, {parent: session.desktop}, 5);
      await form2.open();
      expect(form2).toBe(form);
      expect(session.desktop.activeForm).toBe(form2);

      // Different class, same key
      let form3 = session.desktop.createFormExclusive(CompanyForm, {parent: session.desktop}, 5);
      await form3.open();
      expect(form3).not.toBe(form2);
      expect(session.desktop.activeForm).toBe(form3);

      // Same class, different key
      let form4 = session.desktop.createFormExclusive(CompanyForm, {parent: session.desktop}, 3);
      await form4.open();
      expect(form4).not.toBe(form3);
      expect(session.desktop.activeForm).toBe(form4);

      // Plain form class
      let plainForm = session.desktop.createFormExclusive(Form, {parent: session.desktop}, 10);
      await plainForm.open();
      expect(plainForm).not.toBe(form4);
      expect(session.desktop.activeForm).toBe(plainForm);

      let plainForm2 = session.desktop.createFormExclusive(Form, {parent: session.desktop}, 10);
      await plainForm2.open();
      expect(plainForm2).toBe(plainForm);
      expect(session.desktop.activeForm).toBe(plainForm2);
    });

    it('doesn\'t open the form if the exclusive key is the same and the form inherits from the existing form', async () => {
      // Use case: PersonForm is replaced by the object factory, code works with PersonForm but actually a PersonFormExt is created
      let formExt = session.desktop.createFormExclusive(PersonFormExt, {parent: session.desktop}, 3);
      await formExt.open();
      expect(session.desktop.activeForm).toBe(formExt);

      let personForm = session.desktop.createFormExclusive(PersonForm, {parent: session.desktop}, 3);
      await personForm.open();
      expect(personForm).toBe(formExt);
      expect(session.desktop.activeForm).toBe(formExt);

      // Different key
      let personForm2 = session.desktop.createFormExclusive(PersonForm, {parent: session.desktop}, 5);
      await personForm2.open();
      expect(personForm2).not.toBe(personForm);
      expect(session.desktop.activeForm).toBe(personForm2);
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
        jasmine.clock().tick(0);
        desktop.bench.$container.trigger('animationend');
        jasmine.clock().tick(0);
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
        scout.create(FormMenu, {
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

      let desktopTab = desktop.bench.getViewTab(modalView) as DesktopTab;
      // @ts-expect-error
      desktopTab._onCloseOther();

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

      let unsavedFormChangesForm = arrays.find(desktop.children, child => child instanceof UnsavedFormChangesForm) as UnsavedFormChangesForm;
      expect(unsavedFormChangesForm instanceof UnsavedFormChangesForm).toBe(true);
      let openFormsField = (unsavedFormChangesForm.rootGroupBox.fields[0] as GroupBox).fields[0] as ListBox<Form>;
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
      let unsavedFormChangesForm = arrays.find(desktop.children, child => child instanceof UnsavedFormChangesForm) as UnsavedFormChangesForm;
      expect(unsavedFormChangesForm instanceof UnsavedFormChangesForm).toBe(true);
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
      let unsavedFormChangesForm = arrays.find(desktop.children, child => child instanceof UnsavedFormChangesForm) as UnsavedFormChangesForm;
      expect(unsavedFormChangesForm instanceof UnsavedFormChangesForm).toBe(true);
      let openFormsField = (unsavedFormChangesForm.rootGroupBox.fields[0] as GroupBox).fields[0] as ListBox<Form>;
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
      let msgBox = scout.create(MessageBox, {
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

      let fileChooser = scout.create(FileChooser, {
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
      let unsavedFormChangesForm = arrays.find(desktop.children, child => child instanceof UnsavedFormChangesForm) as UnsavedFormChangesForm;
      expect(unsavedFormChangesForm instanceof UnsavedFormChangesForm).toBe(true);
      let openFormsField = (unsavedFormChangesForm.rootGroupBox.fields[0] as GroupBox).fields[0] as ListBox<Form>;
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

      let field = modalDialog.rootGroupBox.fields[0] as StringField;
      field.setValue('Foo');

      promises.push(view1.whenClose());
      promises.push(view2.whenClose());
      promises.push(modalDialog.whenSave());
      promises.push(modalDialog.whenClose());

      spyOn(modalDialog, 'ok').and.callThrough();

      desktop.cancelViews([view1, view2]);

      // UnsavedFormChangesForm should be the last child
      let unsavedFormChangesForm = arrays.find(desktop.children, child => child instanceof UnsavedFormChangesForm) as UnsavedFormChangesForm;
      expect(unsavedFormChangesForm instanceof UnsavedFormChangesForm).toBe(true);
      let openFormsField = (unsavedFormChangesForm.rootGroupBox.fields[0] as GroupBox).fields[0] as ListBox<Form>;
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
      let unsavedFormChangesForm = arrays.find(desktop.children, child => child instanceof UnsavedFormChangesForm) as UnsavedFormChangesForm;
      expect(unsavedFormChangesForm instanceof UnsavedFormChangesForm).toBe(true);
      let openFormsField = (unsavedFormChangesForm.rootGroupBox.fields[0] as GroupBox).fields[0] as ListBox<Form>;
      expect(openFormsField.id).toBe('OpenFormsField');
      openFormsField.when('lookupCallDone').then(() => {
        expect(openFormsField.value.length).toBe(1);
        expect(openFormsField.value[0]).toEqual(view2);
        unsavedFormChangesForm.ok();
        jasmine.clock().tick(10);
        // validation message should be displayed since view2 is in invalid state
        expect(session.$entryPoint.find('.messagebox').length).toBe(1);
        desktop.messageBoxes[0].yesButton.doAction();
        jasmine.clock().tick(10);
        // uncheck all entries to not save the unsaved changes
        openFormsField.setValue(null);
        unsavedFormChangesForm.ok();
        jasmine.clock().tick(10);
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

      let field = modalDialog.rootGroupBox.fields[0] as StringField;
      field.setValidator(value => {
        if (strings.equalsIgnoreCase(value, 'Foo')) {
          throw new Error('Validation failed');
        }
        return value;
      });
      field.touch();
      field.setValue('Foo');

      jasmine.clock().install();
      desktop.cancelViews([view1, view2]);
      // UnsavedFormChangesForm should be the last child
      let unsavedFormChangesForm = arrays.find(desktop.children, child => child instanceof UnsavedFormChangesForm) as UnsavedFormChangesForm;
      expect(unsavedFormChangesForm instanceof UnsavedFormChangesForm).toBe(true);
      let openFormsField = (unsavedFormChangesForm.rootGroupBox.fields[0] as GroupBox).fields[0] as ListBox<Form>;
      expect(openFormsField.id).toBe('OpenFormsField');
      openFormsField.when('lookupCallDone').then(() => {
        expect(openFormsField.value.length).toBe(1);
        expect(openFormsField.value[0]).toEqual(view2);
        unsavedFormChangesForm.ok();
        jasmine.clock().tick(10);
        // validation message should be displayed since view2 is in invalid state
        expect(session.$entryPoint.find('.messagebox').length).toBe(1);
        desktop.messageBoxes[0].yesButton.doAction();
        jasmine.clock().tick(10);
        // uncheck all entries to not save the unsaved changes
        openFormsField.setValue(null);
        unsavedFormChangesForm.ok();
        jasmine.clock().tick(10);
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
      let form = scout.create(Form, {
        parent: session.desktop,
        id: 'outerForm',
        displayHint: Form.DisplayHint.VIEW,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: WrappedFormField,
            id: 'wrappedFormField',
            innerForm: {
              id: 'innerForm',
              objectType: Form,
              rootGroupBox: {
                objectType: GroupBox,
                fields: [{
                  id: 'myButton',
                  objectType: Button,
                  keyStroke: 'ctrl-1',
                  keyStrokeScope: 'outerForm'
                }]
              }
            }
          }]
        }
      });
      desktop.showForm(form);
      let innerForm = form.widget('wrappedFormField', WrappedFormField).innerForm;
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
      let modalDialog = scout.create(Form, {
        parent: desktop,
        displayHint: Form.DisplayHint.DIALOG,
        modal: true,
        rootGroupBox: {
          objectType: GroupBox
        }
      });
      desktop.showForm(modalDialog, 0);

      // Test with message-box
      MessageBoxes.createOk(desktop).withBody('foo').buildAndOpen();
      let $glassPanes = modalDialog.$container.find('.glasspane');
      expect($glassPanes.length > 0).toBe(true);
      let messageBox = scout.widget(desktop.$container.find('.messagebox')[0]) as MessageBox;
      messageBox.close();

      // Test with file-chooser
      let fileChooser = scout.create(FileChooser, {
        parent: desktop
      });
      fileChooser.open();
      $glassPanes = modalDialog.$container.find('.glasspane');
      expect($glassPanes.length > 0).toBe(true);
      fileChooser.close();

      // Test with busy-indicator
      let busyIndicator = scout.create(BusyIndicator, {
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
      let viewA = scout.create(Form, {
        parent: desktop,
        displayHint: Form.DisplayHint.VIEW,
        modal: true,
        rootGroupBox: {
          objectType: GroupBox
        }
      });
      let viewB = scout.create(Form, {
        parent: desktop,
        displayHint: Form.DisplayHint.VIEW,
        modal: true,
        rootGroupBox: {
          objectType: GroupBox
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
      let helpPopup = scout.create(WidgetPopup, {
        parent: viewB,
        content: {
          objectType: StringField
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

  describe('views', () => {
    let formModel = {
      objectType: Form,
      displayHint: Form.DisplayHint.VIEW,
      rootGroupBox: {
        objectType: GroupBox
      }
    };

    it('are displayed when desktop is rendered', () => {
      session = sandboxSession({
        desktop: {
          navigationVisible: true,
          headerVisible: true,
          benchVisible: true,
          views: [
            {
              id: 'form1',
              title: 'form1',
              ...formModel
            },
            {
              id: 'form2',
              title: 'form2',
              ...formModel
            }
          ]
        }
      });
      let desktop = session.desktop;
      expect(desktop.bench.getTabBox('C').getViews().length).toBe(2);
      expect(desktop.bench.getTabBox('C').currentView).toBe(desktop.views[1]);
      expect(desktop.views[0].rendered).toBe(false);
      expect(desktop.views[1].rendered).toBe(true);
    });

    it('are not registered multiple times if view is also shown manually', () => {
      session = sandboxSession({
        desktop: {
          navigationVisible: true,
          headerVisible: true,
          benchVisible: true,
          views: [
            {
              id: 'form1',
              title: 'form1',
              ...formModel
            },
            {
              id: 'form2',
              title: 'form2',
              ...formModel
            }
          ]
        }
      });
      let desktop = session.desktop;
      expect(desktop.bench.getTabBox('C').getViews().length).toBe(2);
      expect(desktop.views[1].rendered).toBe(true);

      // Do nothing if showForm is called even though desktop already contains it (may happen in certain situations in Scout classic, #162954).
      desktop.showForm(desktop.views[1]);
      expect(desktop.bench.getTabBox('C').getViews().length).toBe(2);
      expect(desktop.views.length).toBe(2);
    });
  });

  describe('selectedViewTabs', () => {
    let formModel: ObjectOrChildModel<Form> = {
      objectType: Form,
      displayHint: Form.DisplayHint.VIEW,
      displayViewId: 'E',
      rootGroupBox: {
        objectType: GroupBox
      }
    };

    it('allows to select a specific view on startup', () => {
      session = sandboxSession({
        desktop: {
          navigationVisible: true,
          headerVisible: true,
          benchVisible: true,
          selectedViewTabs: ['form2'],
          views: [
            {
              id: 'form1',
              ...formModel
            },
            {
              id: 'form2',
              ...formModel
            }
          ]
        }
      });
      let desktop = session.desktop;
      expect(desktop.bench.getTabBox('E').currentView.id).toBe('form2');
    });

    it('is cleaned up correctly when views are destroyed', () => {
      session = sandboxSession({
        desktop: {
          navigationVisible: true,
          headerVisible: true,
          benchVisible: true,
          selectedViewTabs: ['form1'],
          views: [
            {
              id: 'form1',
              ...formModel
            }
          ]
        }
      });
      let desktop = session.desktop;
      expect(desktop.selectedViewTabs.get('E') instanceof Form).toBe(true);
      expect(desktop.selectedViewTabs.get('E').id).toBe('form1');

      desktop.views[0].close();
      expect(desktop.selectedViewTabs.size).toBe(0);
    });

    it('allows to select a specific outline based view on startup', () => {
      session = sandboxSession({
        desktop: {
          navigationVisible: true,
          headerVisible: true,
          benchVisible: true,
          outline: 'outline1',
          viewButtons: [
            {
              objectType: OutlineViewButton,
              outline: {
                id: 'outline1',
                objectType: Outline,
                selectedViewTabs: ['form2'],
                views: [
                  {
                    id: 'form1',
                    ...formModel
                  },
                  {
                    id: 'form2',
                    ...formModel
                  }
                ]
              }
            },
            {
              objectType: OutlineViewButton,
              outline: {
                id: 'outline2',
                objectType: Outline,
                selectedViewTabs: ['o2form2'],
                views: [
                  {
                    id: 'o2form1',
                    ...formModel
                  },
                  {
                    id: 'o2form2',
                    ...formModel
                  }
                ]
              }
            }
          ]
        }
      });
      let desktop = session.desktop;
      expect(desktop.outline.id).toBe('outline1');
      expect(desktop.bench.getTabBox('E').currentView.id).toBe('form2');

      desktop.setOutline((desktop.viewButtons[1] as OutlineViewButton).outline);
      expect(desktop.outline.id).toBe('outline2');
      expect(desktop.bench.getTabBox('E').currentView.id).toBe('o2form2');
    });

    it('are used to restore the views when outline is changed', () => {
      session = sandboxSession({
        desktop: {
          navigationVisible: true,
          headerVisible: true,
          benchVisible: true,
          outline: 'outline1',
          views: [
            {
              id: 'desktopform',
              ...formModel
            }
          ],
          viewButtons: [
            {
              objectType: OutlineViewButton,
              outline: {
                id: 'outline1',
                objectType: Outline,
                selectedViewTabs: ['form2'],
                views: [
                  {
                    id: 'form1',
                    ...formModel
                  },
                  {
                    id: 'form2',
                    ...formModel
                  }
                ]
              }
            },
            {
              objectType: OutlineViewButton,
              outline: {
                id: 'outline2',
                objectType: Outline,
                selectedViewTabs: ['o2form2'],
                views: [
                  {
                    id: 'o2form1',
                    ...formModel
                  },
                  {
                    id: 'o2form2',
                    ...formModel
                  }
                ]
              }
            }
          ]
        }
      });
      let desktop = session.desktop;
      expect(desktop.outline.id).toBe('outline1');
      expect(desktop.bench.getTabBox('E').currentView.id).toBe('form2');

      desktop.activateForm(desktop.widget('form1') as Form);
      expect(desktop.bench.getTabBox('E').currentView.id).toBe('form1');

      desktop.setOutline((desktop.viewButtons[1] as OutlineViewButton).outline);
      expect(desktop.outline.id).toBe('outline2');
      expect(desktop.bench.getTabBox('E').currentView.id).toBe('o2form2');

      desktop.activateForm(desktop.widget('o2form1') as Form);
      expect(desktop.bench.getTabBox('E').currentView.id).toBe('o2form1');

      desktop.setOutline((desktop.viewButtons[0] as OutlineViewButton).outline);
      expect(desktop.bench.getTabBox('E').currentView.id).toBe('form1');

      desktop.setOutline((desktop.viewButtons[1] as OutlineViewButton).outline);
      expect(desktop.bench.getTabBox('E').currentView.id).toBe('o2form1');

      // As soon as a desktop view is activated in the same area, the outline based selection is removed
      desktop.activateForm(desktop.widget('desktopform') as Form);
      expect(desktop.bench.getTabBox('E').currentView.id).toBe('desktopform');

      desktop.setOutline((desktop.viewButtons[0] as OutlineViewButton).outline);
      expect(desktop.bench.getTabBox('E').currentView.id).toBe('desktopform');

      desktop.setOutline((desktop.viewButtons[1] as OutlineViewButton).outline);
      expect(desktop.bench.getTabBox('E').currentView.id).toBe('desktopform');
    });
  });

  describe('overlays', () => {

    beforeEach(() => {
      session.inspector = true;
      jasmine.clock().install();
    });

    afterEach(() => {
      jasmine.clock().uninstall();
    });

    it('displays tooltips behind popups', () => {
      let desktop = session.desktop;
      desktop.render(session.$entryPoint);

      let $overlays;

      let view = scout.create(Form, {
        parent: desktop,
        displayHint: Form.DisplayHint.VIEW,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [
            {id: 'Field1', objectType: StringField, tooltipText: 'Tooltip 1'},
            {id: 'Field2', objectType: StringField, tooltipText: 'Tooltip 2'},
            {id: 'Field3', objectType: DateField}
          ]
        }
      });
      desktop.showForm(view);
      desktop.validateLayout(); // needed to prevent the responsive manager from automatically closing tooltips

      $overlays = desktop.$overlaySeparator.nextAll();
      expect($overlays.length).toBe(0);

      // Tooltip
      view.widget('Field1', StringField).fieldStatus.togglePopup();
      $overlays = desktop.$overlaySeparator.nextAll();
      expect($overlays.length).toBe(1);
      expect(scout.widget($overlays.eq(0))).toBeInstanceOf(Tooltip);

      // Date picker popup -> expect the popup to be above the tooltip
      view.widget('Field3', DateField).activate();
      jasmine.clock().tick(500);
      $overlays = desktop.$overlaySeparator.nextAll();
      expect($overlays.length).toBe(2);
      expect(scout.widget($overlays.eq(0))).toBeInstanceOf(Tooltip);
      expect($overlays.eq(0).text()).toBe('Tooltip 1');
      expect(scout.widget($overlays.eq(1))).toBeInstanceOf(DatePickerPopup);

      // Another tooltip -> expect the second tooltip to be above the first tooltip, but below the popup
      view.widget('Field2', StringField).fieldStatus.togglePopup();
      $overlays = desktop.$overlaySeparator.nextAll();
      expect($overlays.length).toBe(3);
      expect(scout.widget($overlays.eq(0))).toBeInstanceOf(Tooltip);
      expect($overlays.eq(0).text()).toBe('Tooltip 1');
      expect(scout.widget($overlays.eq(1))).toBeInstanceOf(Tooltip);
      expect($overlays.eq(1).text()).toBe('Tooltip 2');
      expect(scout.widget($overlays.eq(2))).toBeInstanceOf(DatePickerPopup);
    });

    it('orders overlays relative to their context', () => {
      let desktop = session.desktop;
      desktop.render(session.$entryPoint);

      let $overlays;

      let view1 = scout.create(Form, {
        id: 'View1',
        parent: desktop,
        displayHint: Form.DisplayHint.VIEW,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [
            {id: 'Field1', objectType: StringField, tooltipText: 'Tooltip V1-1'},
            {id: 'Field2', objectType: StringField, tooltipText: 'Tooltip V1-2'},
            {id: 'Field3', objectType: SmartField, lookupCall: DummyLookupCall}
          ]
        }
      });
      let view1Field1 = view1.rootGroupBox.widget('Field1', StringField);
      let view1Field2 = view1.rootGroupBox.widget('Field2', StringField);
      let view1Field3 = view1.rootGroupBox.widget('Field3', SmartField);
      let dialog1 = scout.create(Form, {
        id: 'Dialog1',
        parent: view1,
        displayHint: Form.DisplayHint.DIALOG,
        modal: false,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [
            {id: 'Field1', objectType: StringField, tooltipText: 'Tooltip D1-1'},
            {id: 'Field2', objectType: StringField, tooltipText: 'Tooltip D1-2'},
            {id: 'Field3', objectType: SmartField, lookupCall: DummyLookupCall}
          ]
        }
      });
      let dialog1Field1 = dialog1.rootGroupBox.widget('Field1', StringField);
      let dialog1Field2 = dialog1.rootGroupBox.widget('Field2', StringField);
      let popup1 = scout.create(WidgetPopup, {
        id: 'Popup1',
        parent: dialog1,
        anchor: dialog1.widget('Field1'),
        closeOnOtherPopupOpen: false,
        animateOpening: false,
        animateRemoval: false,
        content: {
          id: 'Popup1Form',
          objectType: Form,
          displayHint: Form.DisplayHint.VIEW,
          rootGroupBox: {
            objectType: GroupBox,
            fields: [
              {id: 'Field1', objectType: StringField, tooltipText: 'Tooltip P1-1'},
              {id: 'Field2', objectType: StringField, tooltipText: 'Tooltip P1-2'},
              {id: 'Field3', objectType: SmartField, lookupCall: DummyLookupCall}
            ]
          }
        }
      });
      let popup1Content = popup1.content as Form;
      let popup1Field1 = popup1Content.rootGroupBox.widget('Field1', StringField);
      let popup1Field2 = popup1Content.rootGroupBox.widget('Field2', StringField);
      let popup1Field3 = popup1Content.rootGroupBox.widget('Field3', SmartField);
      let dialog2 = scout.create(Form, {
        id: 'Dialog2',
        parent: popup1,
        displayHint: Form.DisplayHint.DIALOG,
        modal: false,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [
            {id: 'Field1', objectType: StringField, tooltipText: 'Tooltip D2-1'},
            {id: 'Field2', objectType: StringField, tooltipText: 'Tooltip D2-2'},
            {id: 'Field3', objectType: SmartField, lookupCall: DummyLookupCall}
          ]
        }
      });
      let dialog2Field1 = dialog2.rootGroupBox.widget('Field1', StringField);
      let dialog2Field2 = dialog2.rootGroupBox.widget('Field2', StringField);

      // --------------

      view1.open();
      jasmine.clock().tick(500);
      dialog1.open();
      jasmine.clock().tick(500);
      popup1.open();
      jasmine.clock().tick(500);
      dialog2.open();
      jasmine.clock().tick(500);

      $overlays = desktop.$overlaySeparator.nextAll();
      expect(scout.widget($overlays.eq(0))).toBe(dialog1);
      expect(scout.widget($overlays.eq(1))).toBe(popup1);
      expect(scout.widget($overlays.eq(2))).toBe(dialog2);
      expect($overlays.length).toBe(3);

      // --------------

      view1Field1.fieldStatus.togglePopup();
      view1Field2.fieldStatus.togglePopup();
      view1Field3.activate();
      jasmine.clock().tick(500);
      view1Field3.popup.animateRemoval = false;

      let popup2 = scout.create(WidgetPopup, {
        id: 'Popup2',
        parent: view1,
        // (no anchor)
        closeOnOtherPopupOpen: false,
        animateOpening: false,
        animateRemoval: false,
        content: {
          id: 'Popup2Form',
          objectType: Form,
          displayHint: Form.DisplayHint.VIEW,
          rootGroupBox: {
            objectType: GroupBox,
            fields: [
              {id: 'Field1', objectType: StringField, tooltipText: 'Tooltip P2-1'},
              {id: 'Field2', objectType: StringField, tooltipText: 'Tooltip P2-2'},
              {id: 'Field3', objectType: SmartField, lookupCall: DummyLookupCall}
            ]
          }
        }
      });
      let popup2Content = popup2.content as Form;
      let popup2Field1 = popup2Content.rootGroupBox.widget('Field1', StringField);
      let popup2Field2 = popup2Content.rootGroupBox.widget('Field2', StringField);
      popup2.open();

      popup1Field1.fieldStatus.togglePopup();
      popup1Field2.fieldStatus.togglePopup();

      dialog2Field1.fieldStatus.togglePopup();
      dialog2Field2.fieldStatus.togglePopup();

      dialog1Field1.fieldStatus.togglePopup();
      dialog1Field2.fieldStatus.togglePopup();

      let popup3 = scout.create(Popup, {
        id: 'Popup3',
        parent: dialog1,
        // (no anchor)
        closeOnOtherPopupOpen: false,
        animateOpening: false,
        animateRemoval: false
      });
      popup3.open();

      popup2Field1.fieldStatus.togglePopup();
      popup2Field2.fieldStatus.togglePopup();

      popup1Field3.activate();
      jasmine.clock().tick(500);
      popup1Field3.popup.animateRemoval = false;

      // --------------

      $overlays = desktop.$overlaySeparator.nextAll();
      let overlayWidgets = $overlays.toArray().map(elem => scout.widget(elem));

      // Expected overlays:
      // Tooltip V1-1
      // Tooltip V1-2
      // Dialog1
      // Tooltip D1-1
      // Tooltip D1-2
      // Popup1
      // Tooltip P1-1
      // Tooltip P1-2
      // SmartFieldPopup
      // Dialog2
      // Tooltip D2-1
      // Tooltip D2-2
      // Popup2
      // Tooltip P2-1
      // Tooltip P2-2
      // Popup3

      expect(overlayWidgets[0]).toBeInstanceOf(Tooltip);
      expect(overlayWidgets[0].text).toBe('Tooltip V1-1');
      expect(overlayWidgets[1]).toBeInstanceOf(Tooltip);
      expect(overlayWidgets[1].text).toBe('Tooltip V1-2');
      expect(overlayWidgets[2]).toBe(dialog1);
      expect(overlayWidgets[3]).toBeInstanceOf(Tooltip);
      expect(overlayWidgets[3].text).toBe('Tooltip D1-1');
      expect(overlayWidgets[4]).toBeInstanceOf(Tooltip);
      expect(overlayWidgets[4].text).toBe('Tooltip D1-2');
      expect(overlayWidgets[5]).toBe(popup1);
      expect(overlayWidgets[6]).toBeInstanceOf(Tooltip);
      expect(overlayWidgets[6].text).toBe('Tooltip P1-1');
      expect(overlayWidgets[7]).toBeInstanceOf(Tooltip);
      expect(overlayWidgets[7].text).toBe('Tooltip P1-2');
      expect(overlayWidgets[8]).toBeInstanceOf(SmartFieldPopup);
      expect(overlayWidgets[9]).toBe(dialog2);
      expect(overlayWidgets[10]).toBeInstanceOf(Tooltip);
      expect(overlayWidgets[10].text).toBe('Tooltip D2-1');
      expect(overlayWidgets[11]).toBeInstanceOf(Tooltip);
      expect(overlayWidgets[11].text).toBe('Tooltip D2-2');
      expect(overlayWidgets[12]).toBe(popup2);
      expect(overlayWidgets[13]).toBeInstanceOf(Tooltip);
      expect(overlayWidgets[13].text).toBe('Tooltip P2-1');
      expect(overlayWidgets[14]).toBeInstanceOf(Tooltip);
      expect(overlayWidgets[14].text).toBe('Tooltip P2-2');
      expect(overlayWidgets[15]).toBe(popup3);
      expect(overlayWidgets.length).toBe(16);
    });

    it('always opens message boxes on top of everything', () => {
      let desktop = session.desktop;
      desktop.render(session.$entryPoint);

      let $overlays;

      let dialog1 = scout.create(Form, {
        id: 'Dialog1',
        parent: desktop,
        displayHint: Form.DisplayHint.DIALOG,
        modal: false,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [
            {id: 'Field1', objectType: StringField, tooltipText: 'Tooltip D1-1'},
            {id: 'Field2', objectType: StringField, tooltipText: 'Tooltip D1-2'},
            {id: 'Field3', objectType: SmartField, lookupCall: DummyLookupCall}
          ]
        }
      });
      let dialog1Field1 = dialog1.rootGroupBox.widget('Field1', StringField);
      let dialog1Field2 = dialog1.rootGroupBox.widget('Field2', StringField);
      let dialog2 = scout.create(Form, {
        id: 'Popup2',
        parent: dialog1,
        displayHint: Form.DisplayHint.DIALOG,
        modal: false,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [
            {id: 'Field1', objectType: StringField, tooltipText: 'Tooltip D2-1'},
            {id: 'Field2', objectType: StringField, tooltipText: 'Tooltip D2-2'},
            {id: 'Field3', objectType: SmartField, lookupCall: DummyLookupCall}
          ]
        }
      });
      let dialog2Field1 = dialog2.rootGroupBox.widget('Field1', StringField);
      let dialog2Field2 = dialog2.rootGroupBox.widget('Field2', StringField);

      // --------------

      dialog1.open();
      jasmine.clock().tick(500);
      dialog2.open();
      jasmine.clock().tick(500);

      $overlays = desktop.$overlaySeparator.nextAll();
      expect(scout.widget($overlays.eq(0))).toBe(dialog1);
      expect(scout.widget($overlays.eq(1))).toBe(dialog2);
      expect($overlays.length).toBe(2);

      // --------------

      dialog2Field1.fieldStatus.togglePopup();
      dialog1Field1.fieldStatus.togglePopup();

      MessageBoxes.openOk(desktop, 'Test');

      dialog2Field2.fieldStatus.togglePopup();
      dialog1Field2.fieldStatus.togglePopup();

      // --------------

      $overlays = desktop.$overlaySeparator.nextAll();
      let overlayWidgets = $overlays.toArray().map(elem => scout.widget(elem));

      // Expected overlays:
      // Dialog1
      // Tooltip D1-1
      // Tooltip D1-2
      // Dialog2
      // Tooltip D2-1
      // Tooltip D2-2
      // MessageBox

      expect(overlayWidgets[0]).toBe(dialog1);
      expect(overlayWidgets[1]).toBeInstanceOf(Tooltip);
      expect(overlayWidgets[1].text).toBe('Tooltip D1-1');
      expect(overlayWidgets[2]).toBeInstanceOf(Tooltip);
      expect(overlayWidgets[2].text).toBe('Tooltip D1-2');
      expect(overlayWidgets[3]).toBe(dialog2);
      expect(overlayWidgets[4]).toBeInstanceOf(Tooltip);
      expect(overlayWidgets[4].text).toBe('Tooltip D2-1');
      expect(overlayWidgets[5]).toBeInstanceOf(Tooltip);
      expect(overlayWidgets[5].text).toBe('Tooltip D2-2');
      expect(overlayWidgets[6]).toBeInstanceOf(MessageBox);
      expect(overlayWidgets.length).toBe(7);
    });

    it('renders the tooltip of an form with initial error status correctly', () => {
      let desktop = session.desktop;
      desktop.render(session.$entryPoint);

      let $overlays;

      let dialog = scout.create(Form, {
        id: 'Dialog',
        parent: desktop,
        displayHint: Form.DisplayHint.DIALOG,
        modal: false,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [
            {id: 'Field1', objectType: StringField, tooltipText: 'Tooltip D1-1'},
            {id: 'Field2', objectType: StringField, tooltipText: 'Tooltip D1-2', errorStatus: Status.error('Invalid value')},
            {id: 'Field3', objectType: SmartField, lookupCall: DummyLookupCall}
          ]
        }
      });

      // --------------

      dialog.open();
      jasmine.clock().tick(500);

      $overlays = desktop.$overlaySeparator.nextAll();
      let overlayWidgets = $overlays.toArray().map(elem => scout.widget(elem));

      // Expected overlays:
      // Dialog1
      // Tooltip Invalid value

      expect(overlayWidgets[0]).toBe(dialog);
      expect(overlayWidgets[1]).toBeInstanceOf(Tooltip);
      expect(overlayWidgets[1].text).toBe('Invalid value');
      expect(overlayWidgets.length).toBe(2);
    });

    it('never renders overlays outside desktop', () => {
      let desktop = session.desktop;
      desktop.render(session.$entryPoint);
      desktop.addNotification(scout.create(DesktopNotification, {
        parent: desktop
      }));
      let smartField = scout.create(SmartField, {
        parent: desktop.bench, // Draw inside bench to not influence the overlays
        lookupCall: DummyLookupCall
      });
      smartField.render();
      smartField.activate();
      jasmine.clock().tick(500);
      smartField.popup.animateRemoval = false;
      expect(smartField.popup.$container.parent()[0]).toBe(desktop.$container[0]);
    });
  });
});
