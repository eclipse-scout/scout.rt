/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {strings, styles, TreeNode} from '../index';

export default class CompactTreeNode extends TreeNode {
  declare childNodes: CompactTreeNode[];
  declare parentNode: CompactTreeNode;

  override render($parent: JQuery, paddingLeft: number) {
    let tree = this.getTree();

    if (this.isSection()) {
      let $section = tree.$container
        .makeDiv('section expanded')
        .data('node', this);
      $section
        .appendDiv('title')
        .text(this.text);

      this.$node = $section;
    } else {
      let $parent = this.parentNode.$node;
      // Sections nodes
      this.$node = $parent.makeDiv('section-node')
        .data('node', this)
        .on('mousedown', tree._onNodeMouseDown.bind(tree))
        .on('mouseup', tree._onNodeMouseUp.bind(tree));
    }
  }

  /** @internal */
  override _decorate() {
    // This node is not yet rendered, nothing to do
    if (!this.$node) {
      return;
    }

    let formerClasses,
      $node: JQuery<HTMLElement> = this.$node;

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

    styles.legacyStyle(this, $node);

    if (strings.hasText(this.tooltipText)) {
      $node.attr('title', this.tooltipText);
    }
  }

  isSection(): boolean {
    return this.level === 0;
  }
}
