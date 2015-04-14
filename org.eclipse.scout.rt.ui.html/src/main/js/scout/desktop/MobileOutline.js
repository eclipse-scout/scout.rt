scout.MobileOutline = function() {
  scout.MobileOutline.parent.call(this);
  this._breadcrumb = true;
  this._currentDetailForm = null;
};
scout.inherits(scout.MobileOutline, scout.Outline);

scout.MobileOutline.prototype._render = function($parent) {
  //FIXME CGU improve this, it should not be necesary to modify the parent
  $parent.addClass('navigation-breadcrumb');
  scout.MobileOutline.parent.prototype._render.call(this, $parent);
};

scout.MobileOutline.prototype._showDefaultDetailForm = function() {

};

scout.MobileOutline.prototype._updateOutlineTab = function(node) {
  //FIXME CGU same code as in Outline.js, merge, rename updateOutlineTab
  if (!node) {
    throw new Error('called updateOutlineTab without node, should call showDefaultDetailForm instead?');
  }
  if (this.session.desktop.outline !== this) {
    throw new Error('called updateOutlineTab but event affects another outline');
  }

  // Unlink detail form if it was closed.
  // May happen in the following case:
  // The form gets closed on execPageDeactivated.
  // No pageChanged event will be fired because the deactivated page is not selected anymore
  var prefSize;
  if (node.detailForm && node.detailForm.destroyed) {
    node.detailForm = null;
  }

  if (this._currentDetailForm) {
    this._currentDetailForm.remove();
    this._currentDetailForm = null;
  }

  if (node.detailForm && node.detailFormVisible && node.detailFormVisibleByUi) {
    node.detailForm.render(node.$node);
    node.detailForm.htmlComp.pixelBasedSizing = true;
    prefSize = node.detailForm.htmlComp.getPreferredSize();
    node.detailForm.$container.height(prefSize.height);
    node.detailForm.$container.width(node.$node.width());
    node.detailForm.htmlComp.layout();
    this._currentDetailForm = node.detailForm;
  } else {
    // Temporary set menubar to invisible and await response to recompute visibility again
    // This is necessary because when moving the menubar to the selected node, the menubar probably has shows the wrong menus.
    // On client side we do not know which menus belong to which page.
    // The other solution would be to never show outline menus, instead show the menus of the table resp. show the table itself.
    this.menuBar.hiddenByUi = true;
    this.menuBar.updateVisibility();
    waitForServer(this.session, function() {
      this.menuBar.hiddenByUi = false;
      this.menuBar.updateVisibility();
    }.bind(this));
  }

  // Move menubar to the selected node
  this.menuBar.$container.appendTo(node.$node);

  function waitForServer(session, func) {
    if (session.areRequestsPending() || session.areEventsQueued()) {
      session.listen().done(func);
    } else {
      func();
    }
  }
};

scout.MobileOutline.prototype._filterMenus = function(allowedTypes) {
  if (this._currentDetailForm) {
    // Don't display menus of the tree if a detail form is shown, because the detail form has its own menubar
    return [];
  }
  return scout.MobileOutline.parent.prototype._filterMenus.call(this, allowedTypes);
};

scout.MobileOutline.prototype.onResize = function() {
  scout.MobileOutline.parent.prototype.onResize.call(this);

  if (this._currentDetailForm) {
    this._currentDetailForm.onResize();
  }
};
