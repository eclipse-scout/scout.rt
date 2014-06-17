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

      // TODO cru: if menu closed, will be removed
      $clicked.addClass('menu-open');

      if (menus[i].iconId) {
        $menuContainer.appendDiv('', 'menu-button menu-item', menu.text)
          .data('menu', menus[i])
          .attr('data-icon', menu.iconId)
          .attr('data-label', menu.text)
          .on('click', '', onItemClicked)
          .hover(onHoverIn, onHoverOut);
      } else {
        $menuContainer.appendDiv('', 'menu-item', menu.text)
          .data('menu', menu)
          .on('click', '', onItemClicked);
      }
    }

    // wrap icon menus
    $('.menu-button', $menuContainer).wrapAll('<div class="menu-buttons"></div>');
    var $menuButtons = $('.menu-buttons', $menuContainer);
    $menuButtons.appendDiv('', 'menu-buttons-label');
    $menuContainer.append($menuButtons);

    // animated opening
    $menuContainer.css('height', 0).heightToContent(150);

    // every user action will close menu; menu is removed in 'click' event, see onMenuItemClicked()
    var closingEvents = 'mousedown.contextMenu keydown.contextMenu mousewheel.contextMenu';
    $(document).one(closingEvents, removeMenu);
    $menuContainer.one(closingEvents, $.suppressEvent);

    function removeMenu() {
      // Animate
      var h = $menuContainer.outerHeight();
      $menuContainer.animateAVCSD('height', 0,
        function() {
          $(this).remove();
          $clicked.removeClass('menu-open');
        }, null, 150);

      // Remove all cleanup handlers
      $(document).off('.contextMenu');
    }

    function onHoverIn() {
      var $container = $(this).parent().parent();
      $container.css('height', 'auto');
      $('.menu-buttons-label', $container)
        .text($(this).text())
        .heightToContent(150);
    }

    function onHoverOut() {
      var $container = $(this).parent().parent();

      $('.menu-buttons-label', $container)
        .stop()
        .animateAVCSD('height', 0, null, function() {
          $(this).text('');
        }, 150);
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
