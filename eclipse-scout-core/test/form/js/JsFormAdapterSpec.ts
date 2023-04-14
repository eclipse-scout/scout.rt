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
import {Desktop, DesktopAdapter, Form, ObjectFactory, RemoteEvent} from '../../../src';
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

  ObjectFactory.get().registerNamespace('jsformspec', {JsForm});

  function createAndRegisterFormModel() {
    registerAdapterData([{
      id: '10',
      objectType: 'JsForm',
      jsFormObjectType: 'jsformspec.JsForm'
    }], session);
  }

  it('blocks rendering until loading is complete', async () => {
    createAndRegisterFormModel();
    let formShowEvent = new RemoteEvent(desktopAdapter.id, 'formShow', {
      form: '10',
      displayParent: desktopAdapter.id
    });
    desktopAdapter.onModelAction(formShowEvent);
    let form = session.getModelAdapter('10').widget as JsForm;
    expect(form instanceof JsForm).toBe(true);
    expect(form.rendered).toBe(false);

    form.deferred.resolve();
    await form.whenPostLoad();
    expect(form.rendered).toBe(true);
  });

  it('does not try to show the form if form is closed during load', async () => {
    createAndRegisterFormModel();
    let formShowEvent = new RemoteEvent(desktopAdapter.id, 'formShow', {
      form: '10',
      displayParent: desktopAdapter.id
    });
    desktopAdapter.onModelAction(formShowEvent);
    let form = session.getModelAdapter('10').widget as JsForm;
    expect(form instanceof JsForm).toBe(true);
    expect(form.rendered).toBe(false);

    form.closeInPostLoad = true;
    form.deferred.resolve();
    await form.whenPostLoad();
    expect(form.rendered).toBe(false);
    expect(form.destroyed).toBe(true);
  });
});
