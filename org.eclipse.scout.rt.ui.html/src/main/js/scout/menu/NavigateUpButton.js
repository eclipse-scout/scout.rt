scout.NavigateUpButton = function(outline, node) {
  scout.NavigateUpButton.parent.call(this, outline, node);
  this._text = 'Up';
  this.objectType = 'NavigateUpButton';
  this.keyStroke = 'BACKSPACE';
  this.initKeyStrokeParts();
};
scout.inherits(scout.NavigateUpButton, scout.AbstractNavigationButton);

/**
 * Returns true when current node has either a parentNode or if current node is a
 * top-level node without a parent and the outline has a default detail-form.
 */
scout.NavigateUpButton.prototype._buttonEnabled = function() {
  var node = this.outline.nodesMap[this.outline.selectedNodeIds[0]];

  if (node) {
    return !!node.parentNode || !! this.outline.defaultDetailForm;
  } else {
    return false;
  }
};

scout.NavigateUpButton.prototype._drill = function() {
  var node = this.outline.nodesMap[this.outline.selectedNodeIds[0]],
    parentNode = node.parentNode;

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
