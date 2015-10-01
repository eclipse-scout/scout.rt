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

    // FIXME AWE: discuss with C.GU: postMessage is an async call - how to test this with Jasmine?
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
