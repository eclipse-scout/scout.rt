scout.MenuNavigateUp = function() {
  scout.MenuNavigateUp.parent.call(this);
  this._text1 = 'Back';
  this._text2 = 'Up';
};
scout.inherits(scout.MenuNavigateUp, scout.AbstractOutlineNavigationMenu);

scout.MenuNavigateUp.prototype._isDetail = function(node) {
  return !node.detailFormVisible;
};

scout.MenuNavigateUp.prototype._toggleDetail = function() {
  return true;
};

// Menu is enabled when level of node is > 0
scout.MenuNavigateUp.prototype._menuEnabled = function(node) {
  var $node = this.outline.$nodeById(node.id);
  return $node.attr('data-level') >= 0;
};

scout.MenuNavigateUp.prototype._drill = function(node) {
  var parentNode = node.parentNode;
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
