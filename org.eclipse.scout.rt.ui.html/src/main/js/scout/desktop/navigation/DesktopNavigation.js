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
// FIXME nbu/AWE: inherit from Widget.js? refactor un-/installKeyStroke
scout.DesktopNavigation = function() {
  this.$navigation;
  this.$viewButtons;
  this.$container;
  this.htmlViewButtons;
  this.viewMenuTab;
};
scout.inherits(scout.DesktopNavigation, scout.Widget);

scout.DesktopNavigation.DEFAULT_STYLE_WIDTH = 290; // Same value as in sizes.css
scout.DesktopNavigation.BREADCRUMB_STYLE_WIDTH = 240; // Same value as in sizes.css // FIXME awe: make dynamic (min. breadcrumb width)
scout.DesktopNavigation.MIN_SPLITTER_SIZE = 49; // not 50px because last pixel is the border (would not look good)

scout.DesktopNavigation.prototype._init = function(model) {
  scout.DesktopNavigation.parent.prototype._init.call(this, model);
  this.desktop = this.parent;
};

scout.DesktopNavigation.prototype._render = function($parent) {
  // TODO CGU rename $navigation to $container
  this.$navigation = $parent.appendDiv('desktop-navigation');
  this.htmlComp = new scout.HtmlComponent(this.$navigation, this.session);
  this.htmlComp.setLayout(new scout.DesktopNavigationLayout(this));

  this.$viewButtons = this.$navigation.appendDiv('view-buttons');
  this.htmlViewButtons = new scout.HtmlComponent(this.$viewButtons, this.session);
  this.htmlViewButtons.setLayout(new scout.ViewButtonsLayout(this.htmlViewButtons));
  this.viewMenuTab = new scout.ViewMenuTab(this._viewButtons('MENU'), this.session);
  this.viewMenuTab.render(this.$viewButtons);

  var i, viewTab,
    viewTabs = this._viewButtons('TAB');
  for (i = 0; i < viewTabs.length; i++) {
    viewTab = viewTabs[i];
    viewTab.render(this.$viewButtons);
    if (i === viewTabs.length - 1) {
      viewTab.last();
    }
  }

  this.$container = this.$navigation.appendDiv('navigation-container')
    .on('mousedown', this._onNavigationMousedown.bind(this));
  this.htmlCompBody = new scout.HtmlComponent(this.$container, this.session);
  this.htmlCompBody.setLayout(new scout.SingleLayout());
};

scout.DesktopNavigation.prototype._viewButtons = function(displayStyle) {
  var viewButtons = [];
  this.desktop.viewButtons.forEach(function(viewButton) {
    if (displayStyle === undefined ||
      displayStyle === viewButton.displayStyle) {
      viewButtons.push(viewButton);
    }
  });
  return viewButtons;
};

scout.DesktopNavigation.prototype._getNumSelectedTabs = function() {
  var numSelected = 0;
  if (this.viewMenuTab.selected) {
    numSelected++;
  }
  this._viewButtons('TAB').forEach(function(viewTab) {
    if (viewTab.selected) {
      numSelected++;
    }
  });
  return numSelected;
};

scout.DesktopNavigation.prototype._onNavigationMousedown = function(event) {
  if (this.outline.inBackground) {
    this.desktop.bringOutlineToFront(this.outline);
  }
};

scout.DesktopNavigation.prototype.onOutlineChanged = function(outline, bringToFront) {
  if (this.outline === outline) {
    return;
  }
  if (this.outline) {
    this.outline.remove();

    // Make sure new outline uses same display style as old
    if (outline.autoToggleBreadcrumbStyle) {
      var breadcrumbEnabled = this.outline.breadcrumbEnabled;
      outline.setBreadcrumbEnabled(breadcrumbEnabled);
    }
  }

  this.outline = outline;
  this.outline.setBreadcrumbTogglingThreshold(scout.DesktopNavigation.BREADCRUMB_STYLE_WIDTH);
  this.outline.render(this.$container);
  this.outline.invalidateLayoutTree();
  this.outline.handleOutlineContent(bringToFront);
  this._updateViewButtons(outline);
  this.outline.validateFocus();
  // Layout immediate to prevent flickering when breadcrumb mode is enabled
  // but not initially while desktop gets rendered because it will be done at the end anyway
  if (this.desktop.rendered) {
    this.outline.validateLayoutTree();
  }
};

/**
 * This method updates the state of the view-menu-tab and the selected state of outline-view-buttons.
 * This method must also work in offline mode.
 */
scout.DesktopNavigation.prototype._updateViewButtons = function(outline) {
  this.viewMenuTab.onOutlineChanged(outline);
  this._viewButtons('TAB').forEach(function(viewTab) {
    if (viewTab instanceof scout.OutlineViewButton) {
      viewTab.onOutlineChanged(outline);
    }
  });
};

scout.DesktopNavigation.prototype.doViewMenuAction = function(event) {
  this.viewMenuTab.togglePopup(event);
};

scout.DesktopNavigation.prototype.sendToBack = function() {
  this.viewMenuTab.sendToBack();
  this.outline.sendToBack();
};

scout.DesktopNavigation.prototype.bringToFront = function() {
  this.viewMenuTab.bringToFront();
  this.outline.bringToFront();
};
