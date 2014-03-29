// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.DesktopViewButtonBar = function(scout, $parent, viewButtons) {
  this.$div;

  //  create container
  var $desktopView = $parent.appendDiv('DesktopViews');
  this.$div = $desktopView;

  //  add view-item, all before #viewAdd
  for (var i = 0; i < viewButtons.length; i++) {
    new Scout.DesktopViewButton(scout, $desktopView, viewButtons[i]);
  }

  //  create logo and plus sign
  $desktopView.appendDiv('ViewAdd').on('click', '', addOwnView);
  $desktopView.appendDiv('ViewLogo').delay(1000).animateAVCSD('width', 55, null, null, 1000);

  function addOwnView() {
    var name = $desktopView.children('.selected').text().split('(');
    var c = name.length > 1 ? parseInt(name[1], 0) + 1 : 2;
    //TODO widget id?
    var viewButton = {
      "id": "ownView" + c,
      "text": name[0] + ' (' + c + ')'
    };
    new Scout.DesktopViewButtonOwn(scout, $desktopView, viewButton);
  }

};
