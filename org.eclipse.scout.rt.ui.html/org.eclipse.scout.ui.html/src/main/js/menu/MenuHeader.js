// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.MenuHeader = function ($header, desktopTable, x, y) {
  // create container
  var $menuHeader = $('body').appendDiv('MenuHeader')
        .css('left', x - 6).css('top', y - 10);

  // create buttons inside
  $menuHeader.appendDiv('MenuHeaderControl');
  $menuHeader.appendDiv('MenuHeaderSortUp').
    on('click', '', function () { desktopTable.sortUp($header); });
  $menuHeader.appendDiv('MenuHeaderSortDown').
    on('click', '', function () { desktopTable.sortDown($header); });
  $menuHeader.appendDiv('MenuHeaderFilter');
  $menuHeader.appendDiv('MenuHeaderAdd');
  $menuHeader.appendDiv('MenuHeaderMore');

    // copy flags to menu
  if ($header.hasClass('sort-up')) $menuHeader.addClass('sort-up');
  if ($header.hasClass('sort-down')) $menuHeader.addClass('sort-down');
  if ($header.hasClass('filter')) $menuHeader.addClass('filter');

  // animated opening
  var h = $menuHeader.css('height');
  $menuHeader.css('height', 32)
    .animateAVCSD('height', h);

  // every user action will close menu
  $('*').one('mousedown keydown mousewheel', removeMenu);
  function removeMenu (event) {
    $menuHeader.animateAVCSD('height', 32, $.removeThis);
    return true;
  }
};
