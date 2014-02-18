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
    var name = $desktopView.children('.selected').text().split('('),
      c = name.length > 1 ? parseInt(name[1], 0) + 1 : 2;

    var state = 'view-own',
      label = name[0] + ' (' + c + ')';

    //TODO method clickView is not defined yet
    var $view = $('#ViewAdd').beforeDiv('', 'view-item ' + state, label)
            .on('click', '', function(){/*clickView*/})
            .selectOne();

    $view.appendDiv('', 'view-remove').on('click', '', removeOwnView);

    var w = $view.width();
    $view.css('width', 0).animateAVCSD('width', w);
  }

  function removeOwnView (event) {
    $(this).parent()
      .animateAVCSD('width', 0, $.removeThis)
      .prev().click();

    return false;
  }
};
