scout.CompactTree = function() {
  scout.CompactTree.parent.call(this);
  this.$nodesContainer;
  this._scrolldirections = 'y';
};
scout.inherits(scout.CompactTree, scout.Tree);

/**
 * @override Tree.js
 */
scout.CompactTree.prototype._initTreeKeyStrokeContext = function() {
  this.keyStrokeContext.registerKeyStroke([
    new scout.CompactTreeUpKeyStroke(this),
    new scout.CompactTreeDownKeyStroke(this),
    new scout.CompactTreeLeftKeyStroke(this),
    new scout.CompactTreeRightKeyStroke(this)
  ]);
};

scout.CompactTree.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('compact-tree');

  var layout = new scout.TreeLayout(this);
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(layout);

  this.$data = this.$container.appendDiv('tree-data');
  scout.scrollbars.install(this.$data, {
    parent: this,
    borderless: true
  });
  this.menuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new scout.MenuItemsOrder(this.session, 'Tree')
  });
  this.menuBar.render(this.$container);

  this.$nodesContainer = this.$data.appendDiv('nodes');
  this._updateNodeDimensions();
  this._renderViewport();
  this.invalidateLayoutTree();
};

/**
 * @override
 */
scout.CompactTree.prototype._calculateCurrentViewRange = function() {
  this.viewRangeSize = this.visibleNodesFlat.length;
  return new scout.Range(0, Math.max(this.visibleNodesFlat.length, 0));
};

/**
 * @override
 */
scout.CompactTree.prototype.calculateViewRangeSize = function() {
  return this.visibleNodesFlat.length;
};

/**
 * @override
 */
scout.CompactTree.prototype._remove = function() {
  scout.scrollbars.uninstall(this.$data, this.session);
  scout.CompactTree.parent.prototype._remove.call(this);
};

/**
 * @override
 */
scout.CompactTree.prototype._$buildNode = function(node) {
  if (node.level === 0) {
    //TODO [jgu] sections without child nodes are not visible, never build
    // Sections (only draw if they have child nodes)
    //    if (node.childNodes.length > 0) {
    var $section = this.$container.makeDiv('section expanded')
      .data('node', node);
    $section.appendDiv('title')
      .text(node.text);

    node.$node = $section;
    //    }
  } else {
    var $parent = node.parentNode.$node;
    // Sections nodes
    var $sectionNode = $parent.makeDiv('section-node')
      .data('node', node)
      .on('mousedown', this._onNodeMouseDown.bind(this))
      .on('mouseup', this._onNodeMouseUp.bind(this));

    node.$node = $sectionNode;

  }

  return node.$node;
};

/**
 * @override
 */
scout.CompactTree.prototype._insertNodeInDOMAtPlace = function(node, index) {
  var visibleNodeBefore = this.visibleNodesFlat[index - 1];
  var n;
  if (!visibleNodeBefore) {
    node.$node.prependTo(this.$nodesContainer);
  } else if (visibleNodeBefore.level < node.level) {
    //insert after first child node (title from the level above)
    node.$node.insertAfter(visibleNodeBefore.$node.children()[0]);
  } else {
    n = visibleNodeBefore.$node;
    for (var i = 0; i < visibleNodeBefore.level - node.level; i++) {
      n = n.parent();
    }
    node.$node.insertAfter(n);
  }
};

/**
 * @override
 */
scout.CompactTree.prototype._decorateNode = function(node) {
  var formerClasses,
    $node = node.$node;
  if (!$node) {
    // This node is not yet rendered, nothing to do
    return;
  }

  if ($node.hasClass('section')) {
    $node = $node.children('title');
    formerClasses = 'title';
  } else {
    formerClasses = 'section-node';
    if ($node.isSelected()) {
      formerClasses += ' selected';
    }
  }
  $node.removeClass();
  $node.addClass(formerClasses);
  $node.addClass(node.cssClass);
  $node.text(node.text);

  scout.styles.legacyStyle(node, $node);

  if (scout.strings.hasText(node.tooltipText)) {
    $node.attr('title', node.tooltipText);
  }

  // TODO [15.1] bsh: More attributes...
  // iconId
  // tooltipText
};

/**
 * @override
 */
scout.CompactTree.prototype.selectNodes = function(nodes, notifyServer) {
  var selectedSectionNodes = [];
  nodes = scout.arrays.ensure(nodes);
  // If a section is selected, automatically change selection to first section-node
  nodes.forEach(function(node) {
    var $node = node.$node;
    if (!$node.hasClass('section-node')) {
      node = $node.children('.section-node').first().data('node');
      // Ensure the server model stays in sync with the UI
      notifyServer = true;
    }
    if (node) {
      selectedSectionNodes.push(node);
    }
  }, this);

  scout.CompactTree.parent.prototype.selectNodes.call(this, selectedSectionNodes, notifyServer);
};

/**
 * @override
 */
scout.CompactTree.prototype._renderExpansion = function(node) {
  // nop (not supported by CompactTree)
};

/**
 * @override
 */
scout.CompactTree.prototype._updateItemPath = function() {
  // nop (not supported by CompactTree)
};
