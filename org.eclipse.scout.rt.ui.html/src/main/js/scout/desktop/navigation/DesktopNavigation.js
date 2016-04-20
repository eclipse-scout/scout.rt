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
scout.DesktopNavigation = function() {
  scout.DesktopNavigation.parent.call(this);
  this.$container;
  this.$body;
  this.viewButtons;
  this._outlinePropertyChangeHandler = this._onOutlinePropertyChange.bind(this);
};
scout.inherits(scout.DesktopNavigation, scout.Widget);

scout.DesktopNavigation.DEFAULT_STYLE_WIDTH; // Configured in sizes.css
scout.DesktopNavigation.BREADCRUMB_STYLE_WIDTH; // Configured in sizes.css
scout.DesktopNavigation.MIN_SPLITTER_SIZE = 49; // not 50px because last pixel is the border (would not look good)

scout.DesktopNavigation.prototype._init = function(model) {
  scout.DesktopNavigation.parent.prototype._init.call(this, model);
  scout.DesktopNavigation.DEFAULT_STYLE_WIDTH = $.pxToNumber(scout.styles.get('desktop-navigation', 'width').width);
  scout.DesktopNavigation.BREADCRUMB_STYLE_WIDTH = $.pxToNumber(scout.styles.get('desktop-navigation-breadcrumb', 'width').width);
  this.desktop = this.parent;
  this.layoutData = model.layoutData || {};
  this.toolBarVisible = scout.nvl(model.toolBarVisible, false);
  this.setOutline(model.outline);
};

scout.DesktopNavigation.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('desktop-navigation');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.DesktopNavigationLayout(this));
  this.htmlComp.layoutData = this.layoutData;
  this.viewButtons = scout.create('ViewButtons', {
    parent: this
  });
  this.viewButtons.render(this.$container);

  this.$body = this.$container.appendDiv('navigation-body')
    .on('mousedown', this._onNavigationBodyMousedown.bind(this));
  this.htmlCompBody = new scout.HtmlComponent(this.$body, this.session);
  this.htmlCompBody.setLayout(new scout.SingleLayout());
  this._renderToolBarVisible();
  this._renderOutline();
  this._renderHandle();
};

scout.DesktopNavigation.prototype._renderOutline = function() {
  if (!this.outline) {
    return;
  }
  this.outline.setParent(this);
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
  if (this.outline === outline) {
    return;
  }
  if (this.outline) {
    if (this.rendered) {
      this.outline.remove();
    }

    // Make sure new outline uses same display style as old
    if (outline.autoToggleBreadcrumbStyle) {
      var displayStyle = this.outline.displayStyle;
      outline.setDisplayStyle(displayStyle);
    }
    this.outline.off('propertyChange', this._outlinePropertyChangeHandler);
  }

  this.outline = outline;
  if (this.outline) {
    this.outline.setBreadcrumbTogglingThreshold(scout.DesktopNavigation.BREADCRUMB_STYLE_WIDTH);
    this.outline.inBackground = this.desktop.inBackground;
    this.outline.on('propertyChange', this._outlinePropertyChangeHandler);
    this._updateHandle();
    if (this.rendered) {
      this._renderOutline();
    }
  }
};

scout.DesktopNavigation.prototype.sendToBack = function() {
  this.viewButtons.sendToBack();
  if (this.outline) {
    this.outline.sendToBack();
  }
};

scout.DesktopNavigation.prototype.bringToFront = function() {
  this.viewButtons.bringToFront();
  if (this.outline) {
    this.outline.bringToFront();
  }
};

scout.DesktopNavigation.prototype.setToolBarVisible = function(toolBarVisible) {
  this.toolBarVisible = toolBarVisible;
  if (this.rendered) {
    this._renderToolBarVisible();
  }
};

scout.DesktopNavigation.prototype._updateHandle = function() {
  if (this.handle) {
    this.handle.setRightVisible(this.desktop.outlineDisplayStyle() === scout.Tree.DisplayStyle.BREADCRUMB);
  }
};

scout.DesktopNavigation.prototype._renderToolBarVisible = function() {
  if (this.toolBarVisible) {
    this._renderToolBar();
  } else {
    this._removeToolBar();
  }
};

scout.DesktopHeader.prototype._renderLogo = function() {
  if (!this.logo) {
    this.logo = scout.create('DesktopLogo', {
      parent: this,
      url: this.logoUrl
    });
    this.logo.render(this.$container);
  } else {
    this.logo.setUrl(this.logoUrl);
  }
};

scout.DesktopNavigation.prototype._renderToolBar = function() {
  if (this.toolBar) {
    return;
  }
  this.toolBar = scout.create('DesktopToolBox', {
    parent: this,
    menus: this.desktop.menus
  });
  this.toolBar.render(this.$container);
};

scout.DesktopNavigation.prototype._removeToolBar = function() {
  if (!this.toolBar) {
    return;
  }
  this.toolBar.remove();
  this.toolBar = null;
};

scout.DesktopNavigation.prototype._renderHandle = function() {
  if (this.handle || this.desktop.displayStyle === scout.Desktop.DisplayStyle.COMPACT) {
    return;
  }
  this.handle = scout.create('DesktopNavigationHandle', {
    parent: this
  });
  this.handle.render(this.$container);
  this.handle.$container.addClass('navigation-open');
  this.handle.on('action', this._onHandleAction.bind(this));
  this._updateHandle();
};

scout.DesktopNavigation.prototype._onNavigationBodyMousedown = function(event) {
  this.desktop.bringOutlineToFront();
};

scout.DesktopNavigation.prototype._onOutlinePropertyChange = function(event) {
  if (event.changedProperties.indexOf('displayStyle') !== -1) {
    this._updateHandle();
  }
};

scout.DesktopNavigation.prototype._onHandleAction = function(event) {
  if (event.left) {
    this.desktop.shrinkNavigation();
  } else {
    this.desktop.enlargeNavigation();
  }
};
