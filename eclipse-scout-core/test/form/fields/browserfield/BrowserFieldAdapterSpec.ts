/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {BrowserField, BrowserFieldAdapter, RemoteEvent, scout, Session, Widget} from '../../../../src/index';
import {ObjectType} from '../../../../src/ObjectFactory';

describe('BrowserFieldAdapter', () => {

  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  it('sends postMessage on message', () => {
    let model = createSimpleModel('BrowserField', session, 'foo') as {
      id: string; objectType: ObjectType<BrowserField>; parent: Widget; session: Session;
    };
    let adapter = scout.create(BrowserFieldAdapter, $.extend({}, model));
    let browserField = adapter.createWidget(model, session.desktop);
    browserField.render();

    // postMessage is an async call -> hard to test -> simulate it (window.postMessage('hello world', '*');)
    let iframe = browserField.$field[0] as HTMLIFrameElement;
    // @ts-ignore
    browserField._onMessage({
      data: 'hello world',
      origin: 'foo',
      source: iframe.contentWindow
    });

    sendQueuedAjaxCalls();

    let event = new RemoteEvent(browserField.id, 'postMessage', {
      data: 'hello world',
      origin: 'foo'
    });
    expect(mostRecentJsonRequest()).toContainEvents(event);
  });

});
