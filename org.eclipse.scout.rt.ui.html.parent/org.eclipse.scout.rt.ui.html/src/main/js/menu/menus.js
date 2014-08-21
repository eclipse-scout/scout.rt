scout.menus = {
  filter: function(menus, types) {
    if (!menus) {
      return;
    }
    if (types && !Array.isArray(types)) {
      types = [types];
    }

    var filteredMenus = [];
    var hasValidMenus = false;
    var separatorCount = 0;
    for (var i = 0; i < menus.length; i++) {
      var menu = menus[i];

      var childMenus = menu.childMenus;
      if (childMenus && childMenus.length > 0) {
        childMenus = scout.menus.filter(menu.childMenus, types);
        if (childMenus.length === 0) {
          continue;
        }
      }

      if (!menu.visible) {
        continue;
      }
      if (!scout.menus.checkType(menu, types)) {
        continue;
      }

      if (menu.separator) {
        separatorCount++;
      }

      filteredMenus.push(menu);
    }

    //Ignore menus with only separators
    if (separatorCount == filteredMenus.length) {
      return [];
    }

    return filteredMenus;
  },
  checkType: function(menu, types) {
    if (!types) {
      return true;
    }
    if (!menu.menuTypes) {
      return false;
    }

    for (var j = 0; j < types.length; j++) {
      if (menu.menuTypes.indexOf(types[j]) > -1) {
        return true;
      }
    }
  },
  showContextMenu: function(menus, $parent, $clicked, left, right, top) {
    var i, $menuContainer = $('.menu-container', $parent);

    if ($menuContainer.length) {
      removeMenu();
    }

    if (!menus || menus.length === 0) {
      return;
    }

    $menuContainer = $parent.appendDiv('', 'menu-container');
    $clicked.addClass('menu-open');

    for (i = 0; i < menus.length; i++) {
      var menu = menus[i];
      if (menu.separator) {
        continue;
      }

      menu.sendAboutToShow();

      if (left !== undefined) {
        $menuContainer.css('left', left);
      }
      if (right !== undefined) {
        $menuContainer.css('right', right);
      }
      if (top !== undefined) {
        $menuContainer.css('top', top);
      }

      $menuContainer.appendDiv('', 'menu-item', menu.text)
        .data('menu', menu)
        .on('click', '', onItemClicked);
    }

    // every user action will close menu; menu is removed in 'click' event, see onMenuItemClicked()
    var closingEvents = 'mousedown.contextMenu keydown.contextMenu mousewheel.contextMenu';
    $(document).one(closingEvents, removeMenu);
    $menuContainer.one(closingEvents, $.suppressEvent);

    function removeMenu() {
      // close container
      $menuContainer.remove();
      $clicked.addClass('menu-open');

      // Remove all cleanup handlers
      $(document).off('.contextMenu');
    }

    function onItemClicked() {
      var menu = $(this).data('menu');
      menu.sendMenuAction();
    }

  },
  /**
   * menus may change at any time -> wait for server response before showing any menus
   */
  showContextMenuWithWait: function(session, func) {
    if (session.offline) {
      //Do not show context menus in offline mode, they won't work
      return;
    }

    if (session.areRequestsPending() || session.areEventsQueued()) {
      session.listen().done(onEventsProcessed);
    } else {
      func();
    }

    function onEventsProcessed() {
      func();
    }
  }
};
