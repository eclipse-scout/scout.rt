scout.DesktopMenu = function($parent, session) {
  this.session = session;

  //create container
  this.$container = $parent.appendDiv('', 'desktop-menu').data('this', this);
  this.$tree = this.$container.appendDiv('', 'desktop-menu-tree');
  this.$table = this.$container.appendDiv('', 'desktop-menu-table');
};

scout.DesktopMenu.prototype.addItems = function (menus, tree, selection) {
  var $div;

  if (tree) {
    $div = this.$tree;
  } else {
    $div = this.$table;
  }

  $div.empty();

  if (menus && menus.length > 0) {
    for (var i = 0; i < menus.length; i++) {
      if (menus[i].separator) {
        continue;
      }
        $div.appendDiv('', 'menu-item', menus[i].text)
          .attr('data-icon', menus[i].iconId)
          .attr('id', menus[i].id)
          .on('click', '', onMenuItemClicked);
    }

    // size menu
    var h = $div.widthToContent(150);
  }

  var that = this;

  function onMenuItemClicked() {
    that.session.send('menuAction', $(this).attr('id'));
  }
};

scout.DesktopMenu.prototype.contextMenu = function(menus, tree, $parent, $clicked, x, y) {
  var $menuContainer = $('.menu-container', $parent);

  if ($menuContainer.length) {
    removeMenu();
  }

  var $children = tree ? $('.desktop-menu-table').children() : $('.desktop-menu-table').children();
  if ($children.length) {

    $menuContainer = $parent.appendDiv('', 'menu-container');

    if (tree) {
      $menuContainer.css('right', x).css('top', y);
    } else {
      $menuContainer.css('left', x).css('top', y);
    }

    // TODO cru: if menu closed, will be removed
    $clicked.addClass('menu-open');

    $children.clone(true).appendTo($menuContainer);

    // collect icon menus
    $('.menu-item[data-icon]', $menuContainer)
      .wrapAll('<div class="menu-buttons"></div>')
      .mouseenter(onHoverIn);
    var $menuButton = $('.menu-buttons',  $menuContainer);
    $menuButton.mouseleave(onHoverOut);
    $menuButton.appendDiv('', 'menu-buttons-label');
    $menuContainer.append($menuButton);

    // animated opening
    $menuContainer.css('height', 0).heightToContent(150);

    // every user action will close menu; menu is removed in 'click' event, see onMenuItemClicked()
    var closingEvents = 'mousedown.contextMenu keydown.contextMenu mousewheel.contextMenu';
    $(document).one(closingEvents, removeMenu);
    $menuContainer.one(closingEvents, $.suppressEvent);
  }

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
      .animateAVCSD('height', 0, null, function() { $(this).text(''); }, 150);
  }

};

