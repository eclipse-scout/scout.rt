/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Tree} from '../index';
import {CompactTreeRightKeyStroke} from '../index';
import {HtmlComponent} from '../index';
import {Range} from '../index';
import {CompactTreeUpKeyStroke} from '../index';
import {CompactTreeDownKeyStroke} from '../index';
import {MenuItemsOrder} from '../index';
import {CompactTreeLeftKeyStroke} from '../index';
import {TreeLayout} from '../index';
import {arrays} from '../index';
import {scout} from '../index';

export default class CompactTree extends Tree {

constructor() {
  super();
  this.$nodesContainer;
  this._scrolldirections = 'y';
}


/**
 * @override Tree.js
 */
_initTreeKeyStrokeContext() {
  this.keyStrokeContext.registerKeyStroke([
    new CompactTreeUpKeyStroke(this),
    new CompactTreeDownKeyStroke(this),
    new CompactTreeLeftKeyStroke(this),
    new CompactTreeRightKeyStroke(this)
  ]);
}

_createTreeNode(nodeModel) {
  nodeModel = scout.nvl(nodeModel, {});
  nodeModel.parent = this;
  return scout.create('CompactTreeNode', nodeModel);
}

_render() {
  this.$container = this.$parent.appendDiv('compact-tree');

  var layout = new TreeLayout(this);
  this.htmlComp = HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(layout);

  this.$data = this.$container.appendDiv('tree-data');
  this._installScrollbars({
    borderless: true
  });
  this.menuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new MenuItemsOrder(this.session, 'Tree')
  });
  this.menuBar.render();

  this.$nodesContainer = this.$data.appendDiv('nodes');
  this._updateNodeDimensions();
  this._renderViewport();
  this.invalidateLayoutTree();
}

/**
 * @override
 */
_calculateCurrentViewRange() {
  this.viewRangeSize = this.visibleNodesFlat.length;
  return new Range(0, Math.max(this.visibleNodesFlat.length, 0));
}

/**
 * @override
 */
calculateViewRangeSize() {
  return this.visibleNodesFlat.length;
}

/**
 * @override
 */
_insertNodeInDOMAtPlace(node, index) {
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
}

/**
 * @override
 */
selectNodes(nodes, debounceSend) {
  var selectedSectionNodes = [];
  nodes = arrays.ensure(nodes);
  nodes.forEach(function(node) {
    // If a section is selected, automatically change selection to first section-node
    if (node.isSection()) {
      if (node.childNodes.length > 0) {
        selectedSectionNodes.push(node.childNodes[0]);
      }
    } else {
      selectedSectionNodes.push(node);
    }
  }, this);

  super.selectNodes( selectedSectionNodes);
}

/**
 * @override
 */
_renderExpansion(node) {
  // nop (not supported by CompactTree)
}

/**
 * @override
 */
_updateItemPath() {
  // nop (not supported by CompactTree)
}
}
