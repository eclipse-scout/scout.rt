scout.MenuNavigateUp = function(outline, node, menuTypes) {
  scout.MenuNavigateUp.parent.call(this, outline, node);
  this._text1 = 'Back';
  this._text2 = 'Up';
  this.menuTypes = menuTypes;
};
scout.inherits(scout.MenuNavigateUp, scout.AbstractOutlineNavigationMenu);

scout.MenuNavigateUp.prototype._isDetail = function() {
  return !this.node.detailFormVisible;
};

scout.MenuNavigateUp.prototype._toggleDetail = function() {
  return true;
};

scout.MenuNavigateUp.prototype._menuEnabled = function() {
    return true;
};

scout.MenuNavigateUp.prototype._drill = function() {
  var parentNode = this.node.parentNode;
  if (parentNode) {
    $.log.debug('drill up to node ' + parentNode);
    this.outline._navigateUp = true;
    this.outline.setNodesSelected(parentNode);
    this.outline.setNodeExpanded(parentNode, undefined, false);
  } else {
    $.log.debug('show default detail form');
    this.outline._showDefaultDetailForm();
  }
};
