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
import {TreeNode} from '../../../index';
import $ from 'jquery';

export default class ProposalTreeNode extends TreeNode {

  constructor() {
    super();
  }

  _init(model) {
    super._init(model);
  }

  _renderText() {
    let text = this.text;
    if (this.lookupRow.active === false) {
      text += ' (' + this.session.text('InactiveState') + ')';
    }
    if (this.htmlEnabled) {
      this.$text.html(text);
    } else {
      this.$text.textOrNbsp(text);
    }
  }

  _getStyles() {
    return this.lookupRow;
  }

  _decorate() {
    // This node is not yet rendered, nothing to do
    if (!this.$node) {
      return;
    }

    super._decorate();
    this.$node.toggleClass('inactive', !this.lookupRow.active);
  }

  isBrowseLoadIncremental() {
    return this.proposalChooser.isBrowseLoadIncremental();
  }

  loadChildren() {
    if (this.isBrowseLoadIncremental()) {
      let parentKey = this.lookupRow.key;
      return this.proposalChooser.smartField.lookupByRec(parentKey);
    }
    // child nodes are already loaded -> same as parent.loadChildren
    return $.resolvedDeferred();
  }

  hasChildNodes() {
    if (this.isBrowseLoadIncremental() && !this.childrenLoaded) {
      return true; // because we don't now yet
    }
    return super.hasChildNodes();
  }
}
