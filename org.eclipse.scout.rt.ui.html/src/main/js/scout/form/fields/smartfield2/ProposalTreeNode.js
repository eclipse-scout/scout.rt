/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.ProposalTreeNode = function() {
  scout.ProposalTreeNode.parent.call(this);
};
scout.inherits(scout.ProposalTreeNode, scout.TreeNode);

scout.ProposalTreeNode.prototype.isBrowseLoadIncremental = function() {
  return this.proposalChooser.isBrowseLoadIncremental();
};

scout.ProposalTreeNode.prototype.loadChildren = function() {
  if (this.isBrowseLoadIncremental()) {
    var parentKey = this.lookupRow.key;
    console.log('browseLoadIncremental=true parentKey=', parentKey);
    return this.proposalChooser._smartField().lookupByParentKey(parentKey);
  }
  // child nodes are already loaded -> same as parent.loadChildren
  console.log('browseLoadIncremental=false');
  return $.resolvedDeferred();
};

scout.ProposalTreeNode.prototype.hasChildNodes = function() {
  if (this.isBrowseLoadIncremental() && !this.childrenLoaded) {
    return true; // because we don't now yet
  }
  return scout.ProposalTreeNode.parent.prototype.hasChildNodes.call(this);
};
