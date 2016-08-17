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
describe('BrowserFieldAdapter', function() {

  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  it('sends postMessage on post message', function() {
    var model = createSimpleModel('BrowserField', session, 'foo');
    var adapter = scout.create('BrowserFieldAdapter', model);
    var browserField = adapter.createWidget(model, session.desktop);
    browserField.render(session.$entryPoint);

    // TODO [6.1] awe: discuss with C.GU: postMessage is an async call - how to test this with Jasmine?
    // window.postMessage('hello world', '*');
    browserField._onPostMessage({
      data: 'hello world',
      origin: 'foo'});

    sendQueuedAjaxCalls();

    var event = new scout.Event(browserField.id, 'postMessage', {
      data: 'hello world',
      origin: 'foo'
    });
    expect(mostRecentJsonRequest()).toContainEvents(event);
  });

});
