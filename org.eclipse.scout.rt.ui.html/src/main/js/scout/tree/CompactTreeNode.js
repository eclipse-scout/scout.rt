/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/

/**
 * @class
 */
scout.CompactTreeNode = function(tree) {
  scout.CompactTreeNode.parent.call(this, tree);
};
scout.inherits(scout.CompactTreeNode, scout.TreeNode);

/**
 * @override
 */
scout.CompactTreeNode.prototype.render = function() {
  var tree = this.getTree();

  if (this.isSection()) {
    var $section = tree.$container
      .makeDiv('section expanded')
      .data('node', this);
    $section
      .appendDiv('title')
      .text(this.text);

    this.$node = $section;
  } else {
    var $parent = this.parentNode.$node;
    // Sections nodes
    var $sectionNode = $parent.makeDiv('section-node')
      .data('node', this)
      .on('mousedown', tree._onNodeMouseDown.bind(tree))
      .on('mouseup', tree._onNodeMouseUp.bind(tree));

    this.$node = $sectionNode;
  }

  return this.$node;
};

/**
 * @override
 */
scout.CompactTreeNode.prototype._decorate = function() {
  // This node is not yet rendered, nothing to do
  if (!this.$node) {
    return;
  }

  var formerClasses,
    $node = this.$node;

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
  $node.addClass(this.cssClass);
  $node.text(this.text);

  scout.styles.legacyStyle(this, $node);

  if (scout.strings.hasText(this.tooltipText)) {
    $node.attr('title', this.tooltipText);
  }
};

scout.CompactTreeNode.prototype.isSection = function() {
  return this.level === 0;
};

scout.CompactTreeNode.prototype._updateIconWidth = function() {
  // NOP
};
