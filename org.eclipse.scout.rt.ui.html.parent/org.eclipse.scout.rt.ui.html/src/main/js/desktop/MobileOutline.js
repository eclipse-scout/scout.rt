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

  if (node.detailForm && node.detailFormVisible && !node.detailFormHiddenByUi) {
    node.detailForm.render(node.$node);
    node.detailForm.htmlComp.pixelBasedSizing = true;
    prefSize = node.detailForm.htmlComp.getPreferredSize();
    node.detailForm.$container.height(prefSize.height);
    node.detailForm.$container.width(node.$node.width());
    node.detailForm.htmlComp.layout();
    this._currentDetailForm = node.detailForm;
  }
};

scout.MobileOutline.prototype.onResize = function() {
  scout.MobileOutline.parent.prototype.onResize.call(this);

  if (this._currentDetailForm) {
    this._currentDetailForm.onResize();
  }
};
