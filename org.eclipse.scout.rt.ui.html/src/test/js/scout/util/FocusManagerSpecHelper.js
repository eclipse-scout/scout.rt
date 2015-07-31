var FocusManagerSpecHelper = function() {};

FocusManagerSpecHelper.prototype.handlersRegistered = function($comp) {
  var i,
    expectedHandlers = ['keydown', 'focusin', 'focusout', 'hide'],
    handlerCount = 0,
    events = $._data($comp[0], 'events'),
    expectedCount = expectedHandlers.length;
  if (events) {
    for (i = 0; i < expectedCount; i++) {
      if (events[expectedHandlers[i]]) {
        handlerCount++;
      }
    }
  }
  return handlerCount === expectedCount;
};

