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
import {LookupRow, TreeNode, TreeProposalChooser} from '../../../index';
import $ from 'jquery';
import ProposalTreeNodeModel from './ProposalTreeNodeModel';
import {SmartFieldLookupResult} from './SmartField';

export default class ProposalTreeNode<TValue> extends TreeNode implements ProposalTreeNodeModel<TValue> {
  declare model: ProposalTreeNodeModel<TValue>;

  lookupRow: LookupRow<TValue>;
  parentId: string;
  proposalChooser: TreeProposalChooser<TValue>;

  constructor() {
    super();
  }

  protected override _renderText() {
    if (this.htmlEnabled) {
      this.$text.html(this.text);
    } else {
      this.$text.textOrNbsp(this.text);
    }
  }

  protected override _getStyles(): LookupRow<TValue> {
    return this.lookupRow;
  }

  /** @internal */
  override _decorate() {
    // This node is not yet rendered, nothing to do
    if (!this.$node) {
      return;
    }

    super._decorate();
    this.$node.toggleClass('inactive', !this.lookupRow.active);
  }

  isBrowseLoadIncremental(): boolean {
    return this.proposalChooser.isBrowseLoadIncremental();
  }

  override loadChildren(): JQuery.Deferred<SmartFieldLookupResult<TValue>> {
    if (this.isBrowseLoadIncremental()) {
      let parentKey = this.lookupRow.key;
      return this.proposalChooser.smartField.lookupByRec(parentKey) as JQuery.Deferred<SmartFieldLookupResult<TValue>>;
    }
    // child nodes are already loaded -> same as parent.loadChildren
    return $.resolvedDeferred();
  }

  override hasChildNodes(): boolean {
    if (this.isBrowseLoadIncremental() && !this.childrenLoaded) {
      return true; // because we don't now yet
    }
    return super.hasChildNodes();
  }
}
