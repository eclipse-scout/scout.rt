// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TreeCompact = function() {
  scout.TreeCompact.parent.call(this);
  this._$filter;
  this._$nodesDiv;
  this._$scrollable;
  this._domMap = {};
  this._selectedNode = -1;
};
scout.inherits(scout.TreeCompact, scout.ModelAdapter);

scout.TreeCompact.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('compact-tree');
  this._$filter = $('<input>').
    attr('type', 'text').
    attr('placeholder', this.session.text('FilterBy')).
    addClass('text-field').
    appendTo(this.$container).
    on('input', this._onInput.bind(this)).
    keydown(this._onKeydown.bind(this));

  var $nodesWrapperDiv = $.makeDiv('nodes-wrapper').appendTo(this.$container);
  this._$scrollable = scout.scrollbars.install($nodesWrapperDiv, {invertColors:true, borderless:true});
  this._$nodesDiv = $.makeDiv('nodes').appendTo(this._$scrollable);
  this._renderNodes();
};

scout.TreeCompact.prototype._renderNodes = function(filter) {
  var i, j, node, $node, childNode, $childNode;
  for (i=0; i<this.nodes.length; i++) {
    node = this.nodes[i];
    $node = $.makeDiv('section').appendTo(this._$nodesDiv);
    $.makeDiv('title').appendTo($node).text(node.text);
    this._domMap[node.id] = $node;
    for (j=0; j<node.childNodes.length; j++) {
      childNode = node.childNodes[j];
      $childNode = $.makeDiv('process').
        data('node', childNode).
        click(this._onNodeClick.bind(this)).
        text(childNode.text).
        appendTo($node);
      this._domMap[childNode.id] = $childNode;
    }
  }
};

scout.TreeCompact.prototype._updateNodes = function() {
  var i, j, node, childNode, $dom;
  for (i=0; i<this.nodes.length; i++) {
    node = this.nodes[i];
    $dom = this._domMap[node.id];
    $dom.setVisible(node.visible);
    if (node.visible) {
      for (j=0; j<node.childNodes.length; j++) {
        childNode = node.childNodes[j];
        $dom = this._domMap[childNode.id];
        $.log.debug('updateNode id=' + childNode.id + ' visible=' + childNode.visible);
        $dom.setVisible(childNode.visible);
      }
    }
  }
  scout.scrollbars.update(this._$scrollable);
};

scout.TreeCompact.prototype._onInput = function(event) {
  var filter = this._$filter.val();
  if (filter) {
    $.log.debug('filter nodes='+filter);
    this._filterNodes(filter);
  } else {
    $.log.debug('expand all nodes');
    this._expandAllNodes();
  }
  this._selectedNode = -1;
  this._$nodesDiv.find('.selected').removeClass('selected');
  this._updateNodes();
};

scout.TreeCompact.prototype._onKeydown = function(event) {
  switch (event.which) {
    case scout.keys.UP:
      this._moveSelection(-1);
      return false;
    case scout.keys.DOWN:
      this._moveSelection(1);
      return false;
    case scout.keys.ENTER:
      if (this._selectedNode !== -1) {
        var node = this._$nodesDiv.find('.selected').data('node');
        this._selectNode(node);
      }
      return false;
    default:
      return true;
  }
};

scout.TreeCompact.prototype._moveSelection = function(diff) {
  var oldSelectedNode = this._selectedNode,
    tmpSelectedNode = this._selectedNode + diff,
    $nodes = this._$nodesDiv.find('.process:visible'),
    numNodes = $nodes.length;
  if (tmpSelectedNode >= numNodes) {
    tmpSelectedNode = 0;
  } else if (tmpSelectedNode < 0) {
    tmpSelectedNode = numNodes - 1;
  }
  if (oldSelectedNode !== tmpSelectedNode) {
    $.log.debug('_moveSelection to node with index='+ tmpSelectedNode);
    this._selectedNode = tmpSelectedNode;
    $($nodes[oldSelectedNode]).removeClass('selected');
    $($nodes[tmpSelectedNode]).addClass('selected');
  }
};

scout.TreeCompact.prototype._expandAllNodes = function() {
  var i, j, node, childNode;
  for (i=0; i<this.nodes.length; i++) {
    node = this.nodes[i];
    node.expanded = true;
    node.visible = true;
    for (j=0; j<node.childNodes.length; j++) {
      childNode = node.childNodes[j];
      childNode.visible = true;
    }
  }
};

scout.TreeCompact.prototype._matches = function(node, regexp) {
  $.log.debug('text=' + node.text + ' id=' + node.id + ' regexp=' + regexp + ' matches=' + node.text.match(regexp));
  return node.text.match(regexp) !== null;
};

/**
 * Sets the visible and expanded state of the nodes depending on the given filter.
 * The rules applied here are:
 * 1. when filter matches a top-level root -> expand all child-nodes
 * 2. when filter matches a child-node -> expand parent-node
 * 3. when parent-node doesn't match and has no child-nodes that match -> make parent-node invisible
 * @param filter
 */
scout.TreeCompact.prototype._filterNodes = function(filter) {
  var i, j, node, childNode, expanded,
    regexp = new RegExp(filter, 'i');
  for (i=0; i<this.nodes.length; i++) {
    node = this.nodes[i];
    expanded = this._matches(node, regexp);
    node.expanded = expanded;
    for (j=0; j<node.childNodes.length; j++) {
      childNode = node.childNodes[j];
      if (expanded) {
        // show all sub nodes
        // note: we do not read 'node.expanded' here, since that variable may change
        // when a sub node becomes visible.
        childNode.visible = true;
      } else {
        // only visible when sub node matches
        // must expand parent node
        childNode.visible = this._matches(childNode, regexp);
        if (childNode.visible) {
          node.expanded = true;
        }
      }
    }
    // if node is node is not expanded yet, make invisible
    node.visible = node.expanded;
  }
};

scout.TreeCompact.prototype._onNodeClick = function(event) {
  var node = $(event.target).data('node');
  this._selectNode(node);
};

scout.TreeCompact.prototype._selectNode = function(node) {
  $.log.debug('_selectNode id=' + node.id + ' text=' + node.text);
  this.session.send('nodeAction', this.id, {'nodeId': node.id});
};


