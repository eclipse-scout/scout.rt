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
scout.DesktopBench = function() {
  scout.DesktopBench.parent.call(this);
  this._desktopOutlineChangedHandler = this._onDesktopOutlineChanged.bind(this);
  this._outlineNodesSelectedHandler = this._onOutlineNodesSelected.bind(this);
  this._outlinePageChangedHandler = this._onOutlinePageChanged.bind(this);
  this._outlinePropertyChangeHandler = this._onOutlinePropertyChange.bind(this);
};
scout.inherits(scout.DesktopBench, scout.Widget);

scout.DesktopBench.prototype._init = function(model) {
  scout.DesktopBench.parent.prototype._init.call(this, model);
  this.desktop = this.session.desktop;
  this.outlineContentVisible = scout.nvl(model.outlineContentVisible, true);
};

scout.DesktopBench.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.DesktopBench.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  // Bound to desktop
  this.desktopKeyStrokeContext = new scout.KeyStrokeContext();
  this.desktopKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.desktopKeyStrokeContext.$bindTarget = this.desktop.$container;
  this.desktopKeyStrokeContext.$scopeTarget = this.$container;
  this.desktopKeyStrokeContext.registerKeyStroke(this.desktop.keyStrokes);
};

scout.DesktopBench.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('desktop-bench');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.DesktopBenchLayout(this));
  this.htmlComp.pixelBasedSizing = false;
  this.setOutline(this.desktop.outline); //TODO CGU maybe better create destroy(), call setOutline in init and attach outline listener in init/destroy
  this._renderOrAttachOutlineContent();

  this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
  this.desktop.on('outlineChanged', this._desktopOutlineChangedHandler);
};

scout.DesktopBench.prototype._remove = function() {
  this.desktop.off('outlineChanged', this._desktopOutlineChangedHandler);
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
  scout.DesktopBench.parent.prototype._remove.call(this);
};

scout.DesktopBench.prototype._renderOrAttachOutlineContent = function() {
  if (!this.outlineContent || this.outline.inBackground) {
    return;
  }
  if (!this.outlineContent.rendered) {
    this._renderOutlineContent();
  } else if (!this.outlineContent.attached) {
    this.outlineContent.attach();
  }
};

scout.DesktopBench.prototype._renderOutlineContent = function() {
  if (!this.outlineContent || this.outline.inBackground) {
    return;
  }

  if (this.outlineContent instanceof scout.Table) {
    this.outlineContent.menuBar.top();
    this.outlineContent.menuBar.large();
  }
  this.outlineContent.render(this.$container);
  this.outlineContent.htmlComp.validateRoot = true;
  this.outlineContent.invalidateLayoutTree(false);

  // Layout immediate to prevent 'laggy' form visualization,
  // but not initially while desktop gets rendered because it will be done at the end anyway
  if (this.desktop.rendered) {
    this.outlineContent.validateLayoutTree();

    // Request focus on first element in outline content
    this.session.focusManager.validateFocus();
  }
  if (this.outlineContent instanceof scout.Table) {
    this.outlineContent.restoreScrollPosition();
  }
};

scout.DesktopBench.prototype._removeOutlineContent = function() {
  if (!this.outlineContent) {
    return;
  }
  if (this.outlineContent instanceof scout.Table) {
    this.outlineContent.storeScrollPosition();
  }
  this.outlineContent.remove();
};

scout.DesktopBench.prototype.setOutline = function(outline) {
  if (this.outline) {
    this.outline.off('nodesSelected', this._outlineNodesSelectedHandler);
    this.outline.off('pageChanged', this._outlinePageChangedHandler);
    this.outline.off('propertyChange', this._outlinePropertyChangeHandler);
  }
  this.outline = outline;
  if (this.outline) {
    this.outline.on('nodesSelected', this._outlineNodesSelectedHandler);
    this.outline.on('pageChanged', this._outlinePageChangedHandler);
    this.outline.on('propertyChange', this._outlinePropertyChangeHandler);
  }
  this.updateOutlineContent();
};

scout.DesktopBench.prototype.setOutlineContent = function(content) {
  if (this.outlineContent === content) {
    return;
  }
  if (this.rendered) {
    this._removeOutlineContent();
  }
  this.outlineContent = content;
  if (this.rendered) {
    this._renderOrAttachOutlineContent();
  }
};

scout.DesktopBench.prototype.setOutlineContentVisible = function(visible) {
  this.outlineContentVisible = visible;
  this.updateOutlineContent();
};

scout.DesktopBench.prototype.bringToFront = function() {
  this._renderOrAttachOutlineContent();
};

scout.DesktopBench.prototype.sendToBack = function() {
  if (this.outlineContent) {
    this.outlineContent.detach();
  }
};

scout.DesktopBench.prototype._showDefaultDetailForm = function() {
  this.setOutlineContent(this.outline.defaultDetailForm, true);
};

scout.DesktopBench.prototype._showOutlineOverview = function() {
  this.setOutlineContent(this.outline.outlineOverview, true);
};

scout.DesktopBench.prototype._showDetailContentForPage = function(node) {
  if (!node) {
    throw new Error('called _showDetailContentForPage without node');
  }

  var content;
  if (node.detailForm && node.detailFormVisible && node.detailFormVisibleByUi) {
    content = node.detailForm;
  } else if (node.detailTable && node.detailTableVisible) {
    content = node.detailTable;
  }

  this.setOutlineContent(content);
};

scout.DesktopBench.prototype.updateOutlineContent = function() {
  if (!this.outlineContentVisible) {
    return;
  }
  var selectedPages = this.outline.selectedNodes;
  if (selectedPages.length === 0) {
    if (this.outline.defaultDetailForm) {
      this._showDefaultDetailForm();
    } else if (this.outline.outlineOverview) {
      this._showOutlineOverview();
    }
  } else {
    // Outline does not support multi selection -> [0]
    var selectedPage = selectedPages[0];
    this._showDetailContentForPage(selectedPage);
  }
};

scout.DesktopBench.prototype.updateOutlineContentDebounced = function() {
  clearTimeout(this._updateOutlineContentTimeout);
  this._updateOutlineContentTimeout = setTimeout(function() {
    this.updateOutlineContent();
  }.bind(this), 300);
};

scout.DesktopBench.prototype._onDesktopOutlineChanged = function(event) {
  this.setOutline(this.desktop.outline);
};

scout.DesktopBench.prototype._onOutlineNodesSelected = function(event) {
  if (event.debounce) {
    this.updateOutlineContentDebounced();
  } else {
    this.updateOutlineContent();
  }
};

scout.DesktopBench.prototype._onOutlinePageChanged = function(event) {
  var selectedPage = this.outline.selectedNodes[0];
  if (!event.page && !selectedPage || event.page === selectedPage) {
    this.updateOutlineContent();
  }
};

scout.DesktopBench.prototype._onOutlinePropertyChange = function(event) {
  if (event.changedProperties.indexOf('defaultDetailForm') !== -1) {
    this.updateOutlineContent();
  }
};
