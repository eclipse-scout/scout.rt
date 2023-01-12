/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BrowserField, BrowserFieldAdapter, RemoteEvent, scout} from '../../../../src/index';

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
    let model = createSimpleModel('BrowserField', session, 'foo');
    let adapter = scout.create(BrowserFieldAdapter, $.extend({}, model));
    let browserField = adapter.createWidget(model, session.desktop) as BrowserField;
    browserField.render();

    // postMessage is an async call -> hard to test -> simulate it (window.postMessage('hello world', '*');)
    let iframe = browserField.$field[0] as HTMLIFrameElement;
    // @ts-expect-error
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
