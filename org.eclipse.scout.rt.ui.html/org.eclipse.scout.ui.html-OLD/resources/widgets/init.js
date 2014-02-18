// SCOUT GUI 0.2
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
//
// start all scouts after loading
//

$(document).ready(function () {
  //every browser tab has its own tabId (random number)
  var tabId = ''+new Date().getTime();
  $('.scout').each(function () {
    //depending on the page concept, each div may have a different portlet part id.
    var portletPartId = (typeof $(this).attr("data-partid") === "undefined") ? "0" : $(this).attr("data-partid");
    var sessionPartId = ''+portletPartId+'.'+tabId;
    var scout = new Scout($(this), sessionPartId);
    scout.init();
  });
});
// old
