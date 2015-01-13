scout.MenuNavigateUp = function(outline, node) {
  scout.MenuNavigateUp.parent.call(this, outline, node);
  this._text1 = 'Back';
  this._text2 = 'Up';
};
scout.inherits(scout.MenuNavigateUp, scout.AbstractOutlineNavigationMenu);

scout.MenuNavigateUp.prototype._isDetail = function() {
  return !this.node.detailFormVisible;
};

scout.MenuNavigateUp.prototype._toggleDetail = function() {
  return true;
};

/**
 * Returns true when current node has either a parentNode or if current node is a
 * top-level node without a parent and the outline has a default detail-form.
 */
scout.MenuNavigateUp.prototype._menuEnabled = function() {
  var parentNode = this.node.parentNode;
  return !!parentNode || !!this.outline.defaultDetailForm;
};

scout.MenuNavigateUp.prototype._drill = function() {
  var parentNode = this.node.parentNode;
  if (parentNode) {
    $.log.debug('drill up to node ' + parentNode);
    this.outline._navigateUp = true;
    this.outline.setNodesSelected(parentNode);
    this.outline.setNodeExpanded(parentNode, undefined, false);
  } else {
    $.log.debug('show default detail-form');
    this.outline.setNodesSelected([]);
  }
};
