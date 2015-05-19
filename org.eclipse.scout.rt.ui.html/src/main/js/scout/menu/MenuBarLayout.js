scout.MenuBarLayout = function(menuBar) {
  scout.MenuBarLayout.parent.call(this);
  this._menuBar = menuBar;
  this._ellipsis;
};
scout.inherits(scout.MenuBarLayout, scout.AbstractLayout);

/**
 * @override AbstractLayout.js
 */
scout.MenuBarLayout.prototype.layout = function($container) {
  $.log.info('(MenuBarLayout#layout) elem=' + scout.graphics.debugOutput($container) + ' overflown=' + isOverflown());

  // FIXME AWE: damit das funktioniert müssen wir zuerst immer alle items in der menu-bar rendern.
  // Erst danach dürfen wir sie entfernen (remove)

  this._removeEllipsis();
  this._menuBar.updateItems(this._menuBar.menuItems, true);


  if (isOverflown()) {
    var menuItems = this._menuBar.menuItems;
    menuItems.forEach(function(menuItem) {
      if (this._ellipsis) {
        overflow.call(this, menuItem);
      } else {
        var bounds = scout.graphics.bounds(menuItem.$container),
          rightBounds = bounds.x + bounds.width;
        if (rightBounds > $container[0].clientWidth) {
          this._createEllipsis($container);
          overflow.call(this, menuItem);
        }
      }
    }, this);

    if (this._ellipsis) {
      this._ellipsis.render($container);
    }
  }

  function overflow(menuItem) {
    menuItem.remove();
    // FIXME AWE: hier eine property setzen, für popup/overflow/style
    this._ellipsis.childActions.push(menuItem);
  }

  function isOverflown() {
    return $container[0].scrollWidth >  $container[0].clientWidth;
  }
};


scout.MenuBarLayout.prototype._createEllipsis = function($container) {
  this._ellipsis = this._menuBar.session.createUiObject({
    objectType: 'Menu',
    iconId: 'font:\uF0CA'
  });
};

scout.MenuBarLayout.prototype._removeEllipsis = function() {
  if (this._ellipsis) {
    this._ellipsis.remove(); // FIXME AWE: do not destroy childActions! check!
    this._ellipsis = null;
  }
};

/**
 * @override AbstractLayout.js
 */
scout.MenuBarLayout.prototype.preferredLayoutSize = function($container) {
  $.log.info('(MenuBarLayout#preferredLayoutSize)');

  var prefSize,
    oldWidth = $container.css('width'),
    containerInsets = scout.graphics.getInsets($container, {includeMargin: true});

  $container.css('width', '100%');
  prefSize = scout.graphics.getVisibleSize($container);
  prefSize.width -= containerInsets.left;
  prefSize.width -= containerInsets.right;
  $container.css('width', oldWidth);
  return prefSize;
};

