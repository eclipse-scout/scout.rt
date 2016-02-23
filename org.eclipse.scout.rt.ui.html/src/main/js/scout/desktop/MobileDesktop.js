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
scout.MobileDesktop = function() {
  scout.MobileDesktop.parent.call(this);
  this.benchVisible = false;
  this._currentDetailForm;
};
scout.inherits(scout.MobileDesktop, scout.Desktop);

scout.MobileDesktop.prototype._init = function(model) {
  scout.MobileDesktop.parent.prototype._init.call(this, model);
  this.viewTabsController = new scout.MobileViewTabsController(this);
};

/**
 * @override
 */
scout.MobileDesktop.prototype._render = function($parent) {
  this.$container = $parent;
  this.$container.addClass('desktop');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.DesktopLayout(this));
  this.navigation = scout.create('DesktopNavigation', {
    parent: this
  });
  this.navigation.render($parent);
  this.setOutline(this.outline, true);

  $parent.window().on('resize', this.onResize.bind(this));
};

/**
 * @override
 */
scout.MobileDesktop.prototype._hasHeader = function() {
  return false;
};

/**
 * @override
 */
scout.MobileDesktop.prototype.setOutline = function(outline, bringToFront) {
  scout.MobileDesktop.parent.prototype.setOutline.call(this, outline, bringToFront);
  this.outline.$container.addClass('mobile');
};

/**
 * @override
 */
scout.MobileDesktop.prototype.setOutlineContent = function(content, bringToFront) {
  var prefSize, $node,
    selectedNode = this.outline.selectedNodes[0];

  bringToFront = scout.nvl(bringToFront, true);
  this.outline.menuBar.hiddenByUi = false;
  if (this._currentDetailForm) {
    this._currentDetailForm.remove();
    this._currentDetailForm = null;
  }

  if (!selectedNode) {
    return;
  }

  $node = selectedNode.$node;
  if (content && content instanceof scout.Form) {
    content.render($node);
    content.htmlComp.pixelBasedSizing = true;
    prefSize = content.htmlComp.getPreferredSize();
    content.$container.height(prefSize.height);
    content.$container.width($node.width());
    content.htmlComp.validateLayout();
    this._currentDetailForm = content;
    this.outline.menuBar.hiddenByUi = true;
    this.outline.menuBar.updateVisibility();
  } else {
    // Temporary set menubar to invisible and await response to recompute visibility again
    // This is necessary because when moving the menubar to the selected node, the menubar probably has shows the wrong menus.
    // On client side we do not know which menus belong to which page.
    // The other solution would be to never show outline menus, instead show the menus of the table resp. show the table itself.
    var oldHiddenByUi = this.outline.menuBar.hiddenByUi;
    this.outline.menuBar.hiddenByUi = true;
    this.outline.menuBar.updateVisibility();
    waitForServer(this.session, function() {
      this.outline.menuBar.hiddenByUi = oldHiddenByUi;
      this.outline.menuBar.updateVisibility();
    }.bind(this));
  }

  // Move menubar to the selected node
  this.outline.menuBar.$container.appendTo($node);

  function waitForServer(session, func) {
    if (session.areRequestsPending() || session.areEventsQueued()) {
      session.listen().done(func);
    } else {
      func();
    }
  }
};

scout.MobileViewTabsController = function(desktop) {
  scout.MobileViewTabsController.parent.call(this, desktop);
};
scout.inherits(scout.MobileViewTabsController, scout.ViewTabsController);

scout.MobileViewTabsController.prototype.createAndRenderViewTab = function(view, position) {
  // Make sure bench is visible
  this._desktop.setBenchVisible(true);

  return scout.MobileViewTabsController.parent.prototype.createAndRenderViewTab.call(this, view, position);
};

scout.MobileViewTabsController.prototype._removeViewTab = function(viewTab, viewId) {
  scout.MobileViewTabsController.parent.prototype._removeViewTab.call(this, viewTab, viewId);
  if (this._viewTabs.length === 0) {
    // Hide bench if no view forms are open -> show navigation
    this._desktop.setBenchVisible(false);
  }
};
