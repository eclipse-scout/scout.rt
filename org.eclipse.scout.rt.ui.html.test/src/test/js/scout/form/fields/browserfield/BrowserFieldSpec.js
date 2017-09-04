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
describe('BrowserField', function() {

  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  it('postMessage', function() {
    var browserField = new scout.BrowserField();
    browserField.init(createSimpleModel('BrowserField', session, 'foo'));
    browserField.render(session.$entryPoint);

    // FIXME awe: discuss with C.GU: postMessage is an async call - how to test this with Jasmine?
    // window.postMessage('hello world', '*');
    browserField._onPostMessage({
      data: 'hello world',
      origin: 'foo',
      source: browserField.$field[0].contentWindow});

    sendQueuedAjaxCalls();

    var event = new scout.Event(browserField.id, 'postMessage', {
      data: 'hello world',
      origin: 'foo'
    });
    expect(mostRecentJsonRequest()).toContainEvents(event);
  });

});
