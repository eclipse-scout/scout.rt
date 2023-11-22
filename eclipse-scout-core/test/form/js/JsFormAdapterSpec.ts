/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormSpecHelper} from '../../../src/testing';
import {AdapterData, Desktop, DesktopAdapter, DisplayHint, Form, FormModel, ObjectFactory, RemoteEvent} from '../../../src';
import Deferred = JQuery.Deferred;

describe('JsFormAdapter', () => {
  let session: SandboxSession, helper: FormSpecHelper, desktop: Desktop, desktopAdapter: DesktopAdapter;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    desktop = session.desktop;
    linkWidgetAndAdapter(desktop, 'DesktopAdapter');
    desktopAdapter = desktop.modelAdapter as DesktopAdapter;
  });

  class JsForm extends Form {

  }

  class LoadingJsForm extends Form {
    deferred: Deferred<any>;
    closeInPostLoad: boolean;

    constructor() {
      super();
      this.deferred = $.Deferred();
    }

    protected override _load(): JQuery.Promise<object> {
      return this.deferred.promise();
    }

    protected override _postLoad(): JQuery.Promise<void> {
      if (this.closeInPostLoad) {
        // Actually we should use close here but since close just delegates to the server, we keep it simple and directly call destroy
        this.destroy();
      }
      return super._postLoad();
    }
  }

  ObjectFactory.get().registerNamespace('jsformspec', {JsForm, LoadingJsForm});

  function createAndRegisterFormModel(id: string, jsFormObjectType: string) {
    registerAdapterData([createAdapterData(id, jsFormObjectType)], session);
  }

  function createFormShowEvent(id: string): RemoteEvent {
    return {
      target: desktopAdapter.id,
      displayParent: desktopAdapter.id,
      form: id,
      type: 'formShow'
    };
  }

  function createAdapterData(id: string, jsFormObjectType: string, jsFormModel?: FormModel) {
    return {
      objectType: 'JsForm',
      id: id,
      jsFormObjectType: jsFormObjectType,
      jsFormModel: jsFormModel
    };
  }

  it('blocks rendering until loading is complete', async () => {
    createAndRegisterFormModel('10', 'jsformspec.LoadingJsForm');
    let formShowEvent = new RemoteEvent(desktopAdapter.id, 'formShow', {
      form: '10',
      displayParent: desktopAdapter.id
    });
    desktopAdapter.onModelAction(formShowEvent);
    let form = session.getModelAdapter('10').widget as LoadingJsForm;
    expect(form instanceof LoadingJsForm).toBe(true);
    expect(form.rendered).toBe(false);

    form.deferred.resolve();
    await form.whenPostLoad();
    expect(form.rendered).toBe(true);
  });

  it('does not register forms twice on its displayParent if already set in the sessions startupResponse', async () => {
    // create a session with a desktop that already contains a JsForm, i.e. the form is already set in the desktops views/dialogs
    // this happens e.g. when the browser is reloaded while there are open forms on the server
    // await the form to be loaded and then return the desktop
    const startupSessionWithJsForm: ((displayHint: DisplayHint) => Promise<Desktop>) = async displayHint => {
      // create a session and process the startupResponse
      const s = sandboxSession();
      // using the startupResponse ensures that all widgets are created by the corresponding ModelAdapters, which is needed for the JsForm
      s._processStartupResponse({
        startupData: {
          clientSession: '7'
        },
        adapterData: {
          7: {desktop: '13'} as unknown as AdapterData,
          13: $.extend({objectType: 'Desktop'}, displayHint === Form.DisplayHint.VIEW ? {views: ['42']} : {dialogs: ['42']}),
          42: $.extend(createAdapterData('42', 'jsformspec.JsForm'), {displayHint})
        }
      });
      const d = s.desktop;
      const form = displayHint === Form.DisplayHint.VIEW ? d.views[0] : d.dialogs[0];

      expect(form).toBeDefined();
      await form.whenPostLoad();

      return d;
    };

    expect((await startupSessionWithJsForm(Form.DisplayHint.VIEW)).views.length).toBe(1);
    expect((await startupSessionWithJsForm(Form.DisplayHint.DIALOG)).dialogs.length).toBe(1);
  });

  it('does not try to show the form if form is closed during load', async () => {
    createAndRegisterFormModel('10', 'jsformspec.LoadingJsForm');
    let formShowEvent = new RemoteEvent(desktopAdapter.id, 'formShow', {
      form: '10',
      displayParent: desktopAdapter.id
    });
    desktopAdapter.onModelAction(formShowEvent);
    let form = session.getModelAdapter('10').widget as LoadingJsForm;
    expect(form instanceof LoadingJsForm).toBe(true);
    expect(form.rendered).toBe(false);

    form.closeInPostLoad = true;
    form.deferred.resolve();
    await form.whenPostLoad();
    expect(form.rendered).toBe(false);
    expect(form.destroyed).toBe(true);
  });

  it('opens the form exclusively if the model contains an exclusiveKey', async () => {
    let localJsForm = desktop.createFormExclusive(JsForm, {parent: desktop}, '123');
    await localJsForm.open();
    expect(desktop.getShownForms().length).toBe(1);
    expect(desktop.getShownForms()[0]).toBe(localJsForm);

    // ExclusiveKey is the same -> Does not open the form again
    let message = {
      adapterData: mapAdapterData([createAdapterData('js1', 'jsformspec.JsForm', {exclusiveKey: '123'})]),
      events: [createFormShowEvent('js1')]
    };
    session._processSuccessResponse(message);
    expect(desktop.getShownForms().length).toBe(1);
    expect(desktop.getShownForms()[0]).toBe(localJsForm);

    // ExclusiveKey is different -> opens the form
    message = {
      adapterData: mapAdapterData([createAdapterData('js2', 'jsformspec.JsForm', {exclusiveKey: '222'})]),
      events: [createFormShowEvent('js2')]
    };
    session._processSuccessResponse(message);
    let js2Form = desktop.session.getModelAdapter('js2').widget;
    await js2Form.when('render');
    expect(desktop.getShownForms().length).toBe(2);
    expect(desktop.getShownForms()[0]).toBe(localJsForm);
    expect(desktop.getShownForms()[1].id).toBe(js2Form.id);

    // A local form with the same exclusiveKey won't open
    let localJsForm2 = desktop.createFormExclusive(JsForm, {parent: desktop}, '222');
    await localJsForm2.open();
    expect(desktop.getShownForms().length).toBe(2);
    expect(desktop.getShownForms()[1].id).toBe(js2Form.id);

    // ExclusiveKey is the same but the adapter different -> opens the form
    message = {
      adapterData: mapAdapterData([createAdapterData('js3', 'jsformspec.JsForm', {exclusiveKey: '222'})]),
      events: [createFormShowEvent('js3')]
    };
    session._processSuccessResponse(message);
    let js3Form = desktop.session.getModelAdapter('js3').widget;
    await js3Form.when('render');
    expect(desktop.getShownForms().length).toBe(3);
    expect(desktop.getShownForms()[0]).toBe(localJsForm);
    expect(desktop.getShownForms()[1].id).toBe(js2Form.id);
    expect(desktop.getShownForms()[2].id).toBe(js3Form.id);
  });
});
