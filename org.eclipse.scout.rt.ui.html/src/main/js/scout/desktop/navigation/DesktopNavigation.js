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
scout.DesktopNavigation = function() {
  scout.DesktopNavigation.parent.call(this);
  this.$body = null;
  this.layoutData = {};
  this.toolBoxVisible = false;
  this.viewButtonBox = null;
  this._outlinePropertyChangeHandler = this._onOutlinePropertyChange.bind(this);
  this._desktopPropertyChangeHandler = this._onDesktopPropertyChange.bind(this);
  this._viewButtonBoxPropertyChangeHandler = this._onViewButtonBoxPropertyChange.bind(this);
};
scout.inherits(scout.DesktopNavigation, scout.Widget);

scout.DesktopNavigation.DEFAULT_STYLE_WIDTH; // Configured in sizes.css
scout.DesktopNavigation.BREADCRUMB_STYLE_WIDTH; // Configured in sizes.css
scout.DesktopNavigation.MIN_WIDTH; // Configured in sizes.css

scout.DesktopNavigation.prototype._init = function(model) {
  scout.DesktopNavigation.parent.prototype._init.call(this, model);
  scout.DesktopNavigation.MIN_WIDTH = scout.styles.getSize('desktop-navigation', 'min-width', 'minWidth', 49);
  scout.DesktopNavigation.DEFAULT_STYLE_WIDTH = scout.styles.getSize('desktop-navigation', 'width', 'width', 290);
  scout.DesktopNavigation.BREADCRUMB_STYLE_WIDTH = scout.styles.getSize('desktop-navigation-breadcrumb', 'width', 'width', 240);
  this.desktop = this.parent;
  this.updateHandleVisibility();
  this._setOutline(model.outline);
  this.viewButtonBox = scout.create('ViewButtonBox', {
    parent: this,
    viewButtons: this.desktop.viewButtons,
    singleViewButton: this.singleViewButton
  });
  this.viewButtonBox.on('propertyChange', this._viewButtonBoxPropertyChangeHandler);
  this._updateSingleViewButton();
};

scout.DesktopNavigation.prototype._render = function() {
  this.$container = this.$parent.appendDiv('desktop-navigation');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.DesktopNavigationLayout(this));
  this.htmlComp.layoutData = this.layoutData;

  this.$body = this.$container.appendDiv('navigation-body')
    .on('mousedown', this._onNavigationBodyMouseDown.bind(this));
  this.htmlCompBody = scout.HtmlComponent.install(this.$body, this.session);
  this.htmlCompBody.setLayout(new scout.SingleLayout());

  this.desktop.on('propertyChange', this._desktopPropertyChangeHandler);
};

scout.DesktopNavigation.prototype._remove = function() {
  this.desktop.off('propertyChange', this._desktopPropertyChangeHandler);
  scout.DesktopNavigation.parent.prototype._remove.call(this);
};

scout.DesktopNavigation.prototype._renderProperties = function() {
  scout.DesktopNavigation.parent.prototype._renderProperties.call(this);
  this._renderViewButtonBox();
  this._renderToolBoxVisible();
  this._renderOutline();
  this._renderHandleVisible();
  this._renderSingleViewButton();
};

scout.DesktopNavigation.prototype._renderViewButtonBox = function() {
  this.viewButtonBox.render();
};

scout.DesktopNavigation.prototype._removeOutline = function() {
  if (!this.outline) {
    return;
  }
  this.outline.remove();
};

scout.DesktopNavigation.prototype._renderOutline = function() {
  if (!this.outline) {
    return;
  }
  this.outline.render(this.$body);
  this.outline.invalidateLayoutTree();
  // Layout immediate to prevent flickering when breadcrumb mode is enabled
  // but not initially while desktop gets rendered because it will be done at the end anyway
  if (this.rendered) {
    this.outline.validateLayoutTree();
    this.outline.validateFocus();
  }
};

scout.DesktopNavigation.prototype.setOutline = function(outline) {
  this.setProperty('outline', outline);
};

scout.DesktopNavigation.prototype._setOutline = function(newOutline) {
  var oldOutline = this.outline;
  if (this.outline) {
    this.outline.off('propertyChange', this._outlinePropertyChangeHandler);
  }
  if (this.rendered) {
    this._removeOutline();
  }
  this.outline = newOutline;
  if (this.outline) {
    this.outline.setIconVisible(this.singleViewButton);
    this.outline.setParent(this);
    this.outline.setBreadcrumbTogglingThreshold(scout.DesktopNavigation.BREADCRUMB_STYLE_WIDTH);
    // if both have breadcrumb-toggling enabled: make sure new outline uses same display style as old
    if (this.outline.toggleBreadcrumbStyleEnabled && oldOutline && oldOutline.toggleBreadcrumbStyleEnabled &&
      oldOutline.displayStyle) {
      this.outline.setDisplayStyle(oldOutline.displayStyle);
    }
    this.outline.inBackground = this.desktop.inBackground;
    this.outline.on('propertyChange', this._outlinePropertyChangeHandler);
    this._updateHandle();
  }
};

scout.DesktopNavigation.prototype._updateSingleViewButton = function() {
  if (this.desktop.displayStyle === scout.Desktop.DisplayStyle.COMPACT) {
    // There is not enough space to move the title up due to the toolbar -> Never switch to that mode in compact mode
    this.setSingleViewButton(false);
    return;
  }

  var menuCount = this.viewButtonBox.menuButtons.length,
    tabCount = this.viewButtonBox.tabButtons.length;
  if ((menuCount + tabCount) > 1) {
    if (menuCount > 0) {
      tabCount++;
    }
    this.setSingleViewButton(tabCount < 2);
  } else {
    this.setSingleViewButton(false);
  }
};

scout.DesktopNavigation.prototype.setSingleViewButton = function(singleViewButton) {
  this.setProperty('singleViewButton', singleViewButton);
  if (this.outline) {
    this.outline.setIconVisible(this.singleViewButton);
  }
  this.viewButtonBox.setMenuTabVisible(!singleViewButton);
};

scout.DesktopNavigation.prototype._renderSingleViewButton = function() {
  this.$container.toggleClass('single-view-button', this.singleViewButton);
  this.invalidateLayoutTree();
};

scout.DesktopNavigation.prototype.sendToBack = function() {
  if (this.viewButtonBox) {
    this.viewButtonBox.sendToBack();
  }
  if (this.outline) {
    this.outline.sendToBack();
  }
};

scout.DesktopNavigation.prototype.bringToFront = function() {
  if (this.viewButtonBox) {
    this.viewButtonBox.bringToFront();
  }
  if (this.outline) {
    this.outline.bringToFront();
  }
};

scout.DesktopNavigation.prototype.setToolBoxVisible = function(toolBoxVisible) {
  this.setProperty('toolBoxVisible', toolBoxVisible);
};

scout.DesktopNavigation.prototype.setHandleVisible = function(visible) {
  this.setProperty('handleVisible', visible);
};

scout.DesktopNavigation.prototype._updateHandle = function() {
  if (this.handle) {
    this.handle.setRightVisible(this.outline && this.outline.toggleBreadcrumbStyleEnabled &&
      this.desktop.outlineDisplayStyle() === scout.Tree.DisplayStyle.BREADCRUMB);
  }
};

scout.DesktopNavigation.prototype.updateHandleVisibility = function() {
  // Don't show handle if desktop says handle must not be visible
  this.setHandleVisible(this.desktop.navigationHandleVisible);
};

scout.DesktopNavigation.prototype._renderToolBoxVisible = function() {
  if (this.toolBoxVisible) {
    this._renderToolBox();
  } else {
    this._removeToolBox();
  }
};

scout.DesktopNavigation.prototype._renderToolBox = function() {
  if (this.toolBox) {
    return;
  }
  this.toolBox = scout.create('DesktopToolBox', {
    parent: this,
    menus: this.desktop.menus
  });
  this.toolBox.render();
};

scout.DesktopNavigation.prototype._removeToolBox = function() {
  if (!this.toolBox) {
    return;
  }
  this.toolBox.destroy();
  this.toolBox = null;
};

scout.DesktopNavigation.prototype._renderHandleVisible = function() {
  if (this.handleVisible) {
    this._renderHandle();
  } else {
    this._removeHandle();
  }
};

scout.DesktopNavigation.prototype._createHandle = function() {
  return scout.create('DesktopNavigationHandle', {
    parent: this,
    rightVisible: false,
    horizontalAlignment: scout.CollapseHandle.HorizontalAlignment.RIGHT
  });
};

scout.DesktopNavigation.prototype._renderHandle = function() {
  if (this.handle) {
    return;
  }
  this.handle = this._createHandle();
  this.handle.render();
  this.handle.addCssClass('navigation-open');
  this.handle.on('action', this._onHandleAction.bind(this));
  this._updateHandle();
};

scout.DesktopNavigation.prototype._removeHandle = function() {
  if (!this.handle) {
    return;
  }
  this.handle.destroy();
  this.handle = null;
};

scout.DesktopNavigation.prototype._onNavigationBodyMouseDown = function(event) {
  this.desktop.bringOutlineToFront();
};

scout.DesktopNavigation.prototype._onViewButtonBoxPropertyChange = function(event) {
  if (event.propertyName === 'menuButtons' || event.propertyName === 'tabButtons') {
    this._updateSingleViewButton();
  }
};

scout.DesktopNavigation.prototype._onOutlinePropertyChange = function(event) {
  if (event.propertyName === 'displayStyle') {
    this._updateHandle();
  }
};

scout.DesktopNavigation.prototype._onDesktopPropertyChange = function(event) {
  if (event.propertyName === 'navigationHandleVisible') {
    this.updateHandleVisibility();
  }
};

scout.DesktopNavigation.prototype._onHandleAction = function(event) {
  if (event.left) {
    this.desktop.shrinkNavigation();
  } else {
    this.desktop.enlargeNavigation();
  }
};
