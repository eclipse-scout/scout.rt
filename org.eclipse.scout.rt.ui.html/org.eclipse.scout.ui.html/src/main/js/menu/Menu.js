// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
scout.Menu = function(session, menu, x, y) {
  // remove (without animate) old menu
  $('#MenuSelect, #MenuControl').remove();

  // create 2 container, animate do not allow overflow
  var $menuSelect = $('body').appendDiv('MenuSelect')
    .css('left', x + 28).css('top', y - 3);
  var $menuControl = $('body').appendDiv('MenuControl')
    .css('left', x - 7).css('top', y - 3);

  var onHoverIn = function() {
    $('#MenuButtonsLabel').text($(this).data('label'));
  };

  var onHoverOut = function() {
    $('#MenuButtonsLabel').text('');
  };

  // create menu-item and menu-button
  for (var i = 0; i < menu.length; i++) {
    if (menu[i].iconId) {
      $menuSelect.appendDiv('', 'menu-button')
        .attr('id', menu[i].id)
        .attr('data-icon', menu[i].iconId)
        .attr('data-label', menu[i].text)
        .on('click', '', onMenuItemClicked)
        .hover(onHoverIn, onHoverOut);
    } else {
      $menuSelect.appendDiv('', 'menu-item', menu[i].text)
      .attr('id', menu[i].id)
      .on('click', '', onMenuItemClicked);
    }
  }

  // wrap menu-buttons and add one div for label
  $('.menu-button').wrapAll('<div id="MenuButtons"></div>');
  $('#MenuButtons').appendDiv('MenuButtonsLabel');

  // show menu on top
  var menuTop = $menuSelect.offset().top;
  var menuHeight = $menuSelect.height(),
    windowHeight = $(window).height();

  if (menuTop + menuHeight > windowHeight) {
    $menuSelect.css('top', menuTop - menuHeight + 27);
  }

  // animated opening
  var w = $menuSelect.css('width');
  $menuSelect.css('width', 0).animateAVCSD('width', w);

  // every user action will close menu
  $('*').one('mousedown keydown mousewheel', removeMenu);
  //FIXME listeners are not removed afterwards, according to jquery manual one automatically removes but it does not seem to work
  //FIXME do we need to add namespace to events in order to not accidentally remove listeners from other apps / portlets?

  function removeMenu() {
    $menuSelect.animateAVCSD('width', 0,
      function() {
        $menuControl.remove();
        $menuSelect.remove();
      });
    return true;
  }

  function onMenuItemClicked() {
    session.send(scout.Menu.EVENT_MENU_ACTION, $(this).attr('id'));
    return false;
  }
};

scout.Menu.EVENT_MENU_ACTION = "menuAction";
