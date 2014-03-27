// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
//

//@include("src/main/js/Scout.js");
//@include("src/main/js/Session.js");
//@include("src/main/js/Event.js");
//@include("src/main/js/Locale.js");
//@include("src/main/js/desktop/Desktop.js");
//@include("src/main/js/desktop/DesktopBench.js");
//@include("src/main/js/desktop/DesktopMatrix.js");
//@include("src/main/js/desktop/DesktopTable.js");
//@include("src/main/js/desktop/DesktopTableChart.js");
//@include("src/main/js/desktop/DesktopTableGraph.js");
//@include("src/main/js/desktop/DesktopTableHeader.js");
//@include("src/main/js/desktop/DesktopTableMap.js");
//@include("src/main/js/desktop/DesktopTableOrganize.js");
//@include("src/main/js/desktop/DesktopToolButton.js");
//@include("src/main/js/desktop/DesktopTreeContainer.js");
//@include("src/main/js/desktop/DesktopTree.js");
//@include("src/main/js/desktop/DesktopViewButton.js");
//@include("src/main/js/desktop/DesktopViewButtonBar.js");
//@include("src/main/js/desktop/DesktopViewButtonOwn.js");
//@include("src/main/js/form/Form.js");
//@include("src/main/js/menu/Menu.js");
//@include("src/main/js/menu/MenuHeader.js");
//@include("src/main/js/scrollbar/Scrollbar.js");
//@include("src/main/js/text/DecimalFormat.js");
//@include("src/main/js/text/DateFormat.js");

$(document).ready(function() {
  var tabId = '' + new Date().getTime();
  $('.scout').each(function() {
    var portletPartId = $(this).data('partid') || '0',
      sessionPartId = [portletPartId, tabId].join('.');
    var session = new Scout.Session($(this), sessionPartId);
    session.init();
  });
});
