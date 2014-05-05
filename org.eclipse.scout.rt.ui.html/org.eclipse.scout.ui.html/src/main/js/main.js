scout.init = function (userAgent) {
  var tabId = '' + new Date().getTime();
  $('.scout').each(function() {
    var portletPartId = $(this).data('partid') || '0',
      sessionPartId = [portletPartId, tabId].join('.');
    var session = new scout.Session($(this), sessionPartId);
    if (userAgent) {
      session.userAgent = userAgent;
    }
    session.init();
  });
};
