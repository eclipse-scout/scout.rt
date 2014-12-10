/**
 * This specialised subclass is required to avoid flickering when we click on the "navigate up" button.
 * Without this menu, the server would send a property change for detailFormVisible after the button has pressed,
 * which results in a slight delay. With this class and the _navigateUp flag we can avoid this delay. However,
 * the price for this is code duplication, because the JS code does the same thing as the Java code on the server
 * side.
 */
scout.MenuNavigateUp = function() {
  scout.MenuNavigateUp.parent.call(this);
  this._addAdapterProperties('outline');
  this._clickBehavior = function() {};
  this._listener;
};
scout.inherits(scout.MenuNavigateUp, scout.Menu);

scout.MenuNavigateUp.prototype.init = function(model, session) {
  scout.MenuNavigateUp.parent.prototype.init.call(this, model, session);
  this._listener = function(event) {
    var text, $node, node = event.node;
    if (node.detailForm && !node.detailFormVisible) {
      this._clickBehavior = this._showDetailForm.bind(this, node);
      text = session.text('Back');
    } else {
      this._clickBehavior = this._drillUp.bind(this);
      text = session.text('Up');
    }
    $(this.$container).text(text);
    $node = this.outline.$nodeById(node.id);
    this.enabled = $node.attr('data-level') > 0;
    if (this.rendered) {
      this._renderEnabled(this.enabled);
    }
  }.bind(this);
  this.outline.events.on('outlineUpdated nodesSelected', this._listener);
};

scout.MenuNavigateUp.prototype._onMenuClicked = function(event) {
  this._clickBehavior.call(this);
};

scout.MenuNavigateUp.prototype._showDetailForm = function(node) {
  node.detailFormVisible = true;
  $.log.debug('show detail-form');
  this.outline._updateOutlineTab(node);
};

scout.MenuNavigateUp.prototype._drillUp = function() {
  var nodes, parentNode, $parentNode;
  this.outline._navigateUp = true;
  nodes = this.outline.selectedNodes();
  if (nodes.length >= 1) {
    parentNode = nodes[0].parentNode;
    $parentNode = this.outline.$nodeById(parentNode.id);
    $.log.debug('drill up to node ' + parentNode);

    this.outline.setNodesSelected([parentNode], [$parentNode]);
    this.outline.setNodeExpanded(parentNode, $parentNode, false);
  }
};

scout.MenuNavigateUp.prototype.dispose = function() {
  scout.MenuNavigateUp.parent.prototype.dispose.call(this);
  this.outline.events.off('outlineUpdated nodesSelected', this._listener);
};
