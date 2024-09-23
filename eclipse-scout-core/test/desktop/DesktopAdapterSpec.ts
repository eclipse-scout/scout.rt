/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormSpecHelper, OutlineSpecHelper} from '../../src/testing/index';
import {Desktop, DesktopAdapter, Form, FormModel, GroupBox, GroupBoxModel, RemoteEvent} from '../../src/index';

describe('DesktopAdapter', () => {
  let session: SandboxSession, desktop: Desktop, outlineHelper: OutlineSpecHelper, formHelper: FormSpecHelper, desktopAdapter: DesktopAdapter;

  beforeEach(() => {
    setFixtures(sandbox());
    jasmine.Ajax.install();
    jasmine.clock().install();
    session = sandboxSession({
      desktop: {
        benchVisible: true
      },
      renderDesktop: false
    });
    outlineHelper = new OutlineSpecHelper(session);
    formHelper = new FormSpecHelper(session);
    desktop = session.desktop;
    linkWidgetAndAdapter(desktop, 'DesktopAdapter');
    desktopAdapter = desktop.modelAdapter as DesktopAdapter;
  });

  afterEach(() => {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createAndRegisterFormModel() {
    let groupBoxModel = createSimpleModel('GroupBox', session) as GroupBoxModel;
    groupBoxModel.mainBox = true;
    let formModel = createSimpleModel('Form', session) as FormModel;
    // @ts-expect-error
    formModel.rootGroupBox = groupBoxModel.id;
    registerAdapterData([formModel, groupBoxModel], session);
    return formModel;
  }

  describe('activateForm', () => {

    it('sends formActivateEvent', () => {
      let formModel = createAndRegisterFormModel();
      let formModel2 = createAndRegisterFormModel();
      let form = session.getOrCreateWidget(formModel.id, desktop) as Form;
      let form2 = session.getOrCreateWidget(formModel2.id, desktop) as Form;
      desktop.dialogs = [form, form2];
      session._renderDesktop();

      desktop.activateForm(form);
      expect(desktop.activeForm).toBe(form);

      sendQueuedAjaxCalls();
      let event = new RemoteEvent(desktopAdapter.id, 'formActivate', {
        formId: form.modelAdapter.id
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);

      desktop.activateForm(form2);
      expect(desktop.activeForm).toBe(form2);

      sendQueuedAjaxCalls();
      event = new RemoteEvent(desktopAdapter.id, 'formActivate', {
        formId: form2.modelAdapter.id
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it('can close and open new form in the same response', () => {
      let formModel = createAndRegisterFormModel();
      let form = session.getOrCreateWidget(formModel.id, desktop) as Form;
      desktop.dialogs = [form];
      session._renderDesktop();

      desktop.activateForm(form);
      expect(desktop.activeForm).toBe(form);

      expect(jasmine.Ajax.requests.count()).toBe(0);

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      // ------------------------

      // Close form and open new form --> new form must be activated

      let response: any = {
        adapterData: {
          '400': {
            displayHint: 'view',
            global: true,
            id: '400',
            objectType: 'Form',
            owner: '1',
            rootGroupBox: '401'
          },
          '401': {
            fields: [],
            id: '401',
            objectType: 'GroupBox',
            owner: '400'
          }
        },
        events: [
          {
            target: desktopAdapter.id,
            type: 'formHide',
            displayParent: desktopAdapter.id,
            form: formModel.id
          },
          {
            target: desktopAdapter.id,
            type: 'formShow',
            displayParent: desktopAdapter.id,
            form: '400'
          }
        ]
      };
      session._processSuccessResponse(response);
      sendQueuedAjaxCalls();

      let expectedEvents = [
        new RemoteEvent(desktopAdapter.id, 'formActivate', {
          formId: null
        }),
        new RemoteEvent(desktopAdapter.id, 'formActivate', {
          formId: '400'
        })
      ];
      expect(mostRecentJsonRequest()).toContainEventsExactly(expectedEvents);
      expect(jasmine.Ajax.requests.count()).toBe(2);

      // ------------------------

      // Only hide form --> no formActivate event is sent

      response = {
        events: [
          {
            target: desktopAdapter.id,
            type: 'formHide',
            displayParent: desktopAdapter.id,
            form: '400'
          }
        ]
      };
      session._processSuccessResponse(response);
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(2);
    });
  });

  describe('onFormShow', () => {

    it('activates form but does not send an activate form event', () => {
      session._renderDesktop();
      let formModel = createAndRegisterFormModel();

      let formShowEvent = new RemoteEvent(desktopAdapter.id, 'formShow', {
        form: formModel.id,
        displayParent: desktopAdapter.id
      });
      desktopAdapter.onModelAction(formShowEvent);
      let form = session.getModelAdapter(formModel.id).widget as Form;
      expect(form.rendered).toBe(true);
      expect(desktop.activeForm).toBe(form);

      sendQueuedAjaxCalls();

      // only the desktop ready request is in the queue
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(jasmine.Ajax.requests.mostRecent().params.includes('desktopReady')).toBe(true);
    });
  });

});
