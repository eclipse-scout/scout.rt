scout.init = function(userAgent, objectFactories) {
  var tabId = '' + new Date().getTime();
  $('.scout').each(function() {
    var portletPartId = $(this).data('partid') || '0', sessionPartId = [ portletPartId, tabId ].join('.');
    var session = new scout.Session($(this), sessionPartId, userAgent);
    session.init();
    if (!objectFactories) {
      objectFactories = scout.defaultObjectFactories;
    }
    session.objectFactory.register(objectFactories);
  });
};
