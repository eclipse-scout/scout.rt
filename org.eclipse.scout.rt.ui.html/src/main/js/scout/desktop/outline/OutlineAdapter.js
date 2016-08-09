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
scout.OutlineAdapter = function() {
  scout.OutlineAdapter.parent.call(this);
  this._addAdapterProperties(['defaultDetailForm', 'views', 'dialogs', 'messageBoxes', 'fileChoosers']);
};
scout.inherits(scout.OutlineAdapter, scout.TreeAdapter);

scout.OutlineAdapter.prototype._init = function(model) {
  scout.OutlineAdapter.parent.prototype._init.call(this, model);
//  scout.Tree.visitNodes(model.nodes, this._initPage.bind(this));
};

scout.OutlineAdapter.prototype._initPage = function(page, parentNode) {
  if (!page.childNodes) {
    page.childNodes = [];
  }
  if (page.detailTable) {
    page.detailTable = this.session.getOrCreateModelAdapter(page.detailTable, this);
  }
  if (page.detailForm) {
    page.detailForm = this.session.getOrCreateModelAdapter(page.detailForm, this);
  }
};

scout.OutlineAdapter.prototype._onPageChanged = function(event) {
  var page;
  if (event.nodeId) {
    page = this.widget._nodeById(event.nodeId);

    page.detailFormVisible = event.detailFormVisible;
    page.detailForm = this.session.getOrCreateWidget(event.detailForm, this, this.widget);

    page.detailTableVisible = event.detailTableVisible;
    page.detailTable = this.session.getOrCreateWidget(event.detailTable, this, this.widget);
  } else {
    this.widget.defaultDetailForm = this.session.getOrCreateWidget(event.detailForm, this, this.widget);
  }

  this.widget.pageChanged(page);
};

scout.OutlineAdapter.prototype.onModelAction = function(event) {
  if (event.type === 'pageChanged') {
    this._onPageChanged(event);
  } else {
    scout.OutlineAdapter.parent.prototype.onModelAction.call(this, event);
  }
};

// FIXME [6.1] CGU detail table adapter must be destroyed, add listener on init?
