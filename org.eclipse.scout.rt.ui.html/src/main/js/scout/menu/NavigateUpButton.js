scout.NavigateUpButton = function(outline, node) {
  scout.NavigateUpButton.parent.call(this, outline, node);
  this._text1 = 'Back';
  this._text2 = 'Up';
  this.id = 'NavigateUpButton';
  this.keyStroke = 'BACKSPACE';
};
scout.inherits(scout.NavigateUpButton, scout.AbstractNavigationButton);

scout.NavigateUpButton.prototype._isDetail = function() {
  // Button is in "detail mode" if there are both detail form and detail table visible and detail form _is_ hidden.
  return !!(this.node.detailFormVisible && this.node.detailForm &&
    this.node.detailTableVisible && this.node.detailTable && !this.node.detailFormVisibleByUi);
};

scout.NavigateUpButton.prototype._toggleDetail = function() {
  return true;
};

/**
 * Returns true when current node has either a parentNode or if current node is a
 * top-level node without a parent and the outline has a default detail-form.
 */
scout.NavigateUpButton.prototype._buttonEnabled = function() {
  var parentNode = this.node.parentNode;
  return !!parentNode || !! this.outline.defaultDetailForm;
};

scout.NavigateUpButton.prototype._drill = function() {
  var parentNode = this.node.parentNode;
  if (parentNode) {
    $.log.debug('drill up to node ' + parentNode);
    this.outline.navigateUpInProgress = true;
    this.outline.setNodesSelected(parentNode);
    this.outline.setNodeExpanded(parentNode, false);
  } else {
    $.log.debug('show default detail-form');
    this.outline.setNodesSelected([]);
  }
};
