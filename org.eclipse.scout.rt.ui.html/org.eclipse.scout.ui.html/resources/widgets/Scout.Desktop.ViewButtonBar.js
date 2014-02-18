// SCOUT GUI 0.2
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
//
// view namespace and container
//

Scout.Desktop.ViewButtonBar = function (scout, $desktop, viewButtons) {
  //  create container
  var $desktopView = $desktop.appendDiv('DesktopViews');

  //  add view-item, all before #viewAdd
  for (var i = 0; i < viewButtons.length; i++) {
    new Scout.Desktop.ViewButton(scout, $desktopView, viewButtons[i]);
  }

  //  create logo and plus sign
  $desktopView.appendDiv('ViewAdd').on('click', '', addOwnView);
  $desktopView.appendDiv('ViewLogo').delay(1000).animateAVCSD('width', 55, null, null, 1000);

  // set this for later usage
  this.$div = $desktopView;


  function addOwnView (event) {
    var name = $desktopView.children('.selected').text().split('(');
    var c = name.length > 1 ? parseInt(name[1], 0) + 1 : 2;
    //TODO widget id?
    var viewButton={"id":"ownView"+c, "text":name[0] + ' (' + c + ')'};
    new Scout.Desktop.OwnViewButton(scout, $desktopView, viewButton);
  }

};
