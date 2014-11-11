// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TreeCompact = function() {
  scout.TreeCompact.parent.call(this);
  this._$filter;
  this._$nodesDiv;
  this._$viewport;
  this._domMap = {};
};
scout.inherits(scout.TreeCompact, scout.ModelAdapter);

scout.TreeCompact.prototype._render = function($parent) {
  this.$container = $parent.appendDIV('compact-tree');
  this._$filter = $('<input>').
    attr('type', 'text').
    attr('placeholder', 'Filter...').
    addClass('text-field').
    appendTo(this.$container).
    keyup(this._onKeyup.bind(this));
  var $viewport = $.makeDIV('viewport').appendTo(this.$container);
  this._$nodesDiv = $.makeDIV('nodes').appendTo($viewport);
  this._$viewport = scout.Scrollbar2.install($viewport);
  this._renderNodes();
};

scout.TreeCompact.prototype._renderNodes = function(filter) {
  var i, j, node, $node, childNode, $childNode;
  for (i=0; i<this.nodes.length; i++) {
    node = this.nodes[i];
    $node = $.makeDIV('section').attr('id', 'node-' + node.id).appendTo(this._$nodesDiv);
    $.makeDIV('title').appendTo($node).text(node.text);
    this._domMap[node.id] = $node;
    for (j=0; j<node.childNodes.length; j++) {
      childNode = node.childNodes[j];
      $childNode = $.makeDIV('process').text(childNode.text).attr('id', 'node-' + childNode.id).appendTo($node);
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
};

scout.TreeCompact.prototype._onKeyup = function() {
  var filter = this._$filter.val();
  if (filter) {
    $.log.debug('filter nodes='+filter);
    this._filterNodes(filter);
  } else {
    $.log.debug('expand all nodes');
    this._expandAllNodes();
  }
  this._updateNodes();
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

scout.TreeCompact.prototype._filterNodes = function(filter) {
  var i, j, node, childNode, expanded,
    regexp = new RegExp(filter, 'i');
  // pass 1: wenn lvl1.text matches -> expanded
  // pass 2: wenn lvl2.text matches
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
