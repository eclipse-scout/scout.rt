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
      if (menus[i].iconId) {
        $div.appendDiv('', 'menu-button')
          .attr('id', menus[i].id)
          .attr('data-icon', menus[i].iconId)
          .attr('data-label', menus[i].text)
          .on('click', '', onMenuItemClicked)
          .mouseenter(onHoverIn);
      } else {
        $div.appendDiv('', 'menu-item', menus[i].text)
          .attr('id', menus[i].id)
          .on('click', '', onMenuItemClicked);
      }
    }

    // wrap menu-buttons and add div for label
    $('.menu-button', $div).wrapAll('<div class="menu-buttons"></div>');
    var $menuButton = $('.menu-buttons',  $div);
    $menuButton.mouseleave(onHoverOut);
    $menuButton.appendDiv('', 'menu-buttons-label');
    $div.append($menuButton);

    // size menu
    // TODO: flackert...
    var h = $div.widthToContent(150);
  }

  var that = this;

  function onHoverIn() {
    var $container = $(this).parent().parent();
    $container.css('height', 'auto');
    $('.menu-buttons-label', $container)
      .text($(this).data('label'))
      .heightToContent(150);
  }

  function onHoverOut() {
    var $container = $(this).parent().parent();

    $('.menu-buttons-label', $container)
      .stop()
      .animateAVCSD('height', 0, null, function() { $(this).text(''); }, 150);
  }

  function onMenuItemClicked() {
    that.session.send('menuAction', $(this).attr('id'));
  }
};
