// FIXME NBU/AWE: inherit from Widget.js? refactor un-/installKeyStroke
scout.DesktopNavigation = function(desktop) {
  this.desktop = desktop;
  this.session = desktop.session;

  this.$navigation;
  this.$viewButtons;
  this.$container;
  this.htmlViewButtons;

  this.activeTab;
  this.outlineTab;
  this.keyStrokeAdapter = this._createKeyStrokeAdapter();
  this.viewMenuTab;
  this._breadcrumb = false;

};

scout.DesktopNavigation.BREADCRUMB_SWITCH_WIDTH = 190; // FIXME AWE: make dynamic (min. breadcrumb width)
scout.DesktopNavigation.MIN_SPLITTER_SIZE = 50;

scout.DesktopNavigation.prototype.render = function($parent) {
  this.$navigation = $parent.appendDiv('desktop-navigation');
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

  this._viewButtons().forEach(function(viewButton) {
    viewButton.on('propertyChange', this._onViewButtonPropertyChange.bind(this));
  }, this);

  this.$container = this.$navigation.appendDiv('navigation-container')
    .on('mousedown', this._onNavigationMousedown.bind(this));
  this._installKeyStrokeAdapter();
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

scout.DesktopNavigation.prototype._onViewButtonPropertyChange = function(event) {
  if (scout.helpers.isOneOf(event.changedProperties, 'selected')) {
    this.htmlViewButtons.revalidateLayout();
  }
};

scout.DesktopNavigation.prototype._onNavigationMousedown = function(event) {
  if (this.outline.inBackground) {
    this.desktop.bringOutlineToFront(this.outline);
  }
};

scout.DesktopNavigation.prototype.onOutlineChanged = function(outline) {
  if (this.outline === outline) {
    return;
  }
  if (this.outline) {
    this.outline.remove();
  }
  this.outline = outline;
  this.outline.render(this.$container);
  this.outline.htmlComp.validateLayout();
  this.outline.pixelBasedSizing = true;
  this._updateViewButtons(outline);
  this.outline.validateFocus();
};

scout.DesktopNavigation.prototype._updateViewButtons = function(outline) {
  this.viewMenuTab.onOutlineChanged(outline);
  this._viewButtons('TAB').forEach(function(viewTab) {
    if (viewTab instanceof scout.OutlineViewButton) {
      viewTab.onOutlineChanged(outline);
    }
  });
  this.htmlViewButtons.revalidateLayout();
};

// vertical splitter
scout.DesktopNavigation.prototype.onResize = function(event) {
  var newWidth = Math.max(event.data, scout.DesktopNavigation.MIN_SPLITTER_SIZE); // data = newSize, ensure newSize is not negative
  this.$navigation.width(newWidth);
  this.htmlViewButtons.revalidateLayout();
  this.desktop.navigationWidthUpdated(newWidth);
  this._setBreadcrumbEnabled(newWidth <= scout.DesktopNavigation.BREADCRUMB_SWITCH_WIDTH);
};

scout.DesktopNavigation.prototype._setBreadcrumbEnabled = function(enabled) {
  var oldBreadcrumbEnabled = this._breadcrumbEnabled;
  if (oldBreadcrumbEnabled !== enabled) {
    this._breadcrumbEnabled = enabled;
    this.$navigation.toggleClass('navigation-breadcrumb', enabled);
    this.outline.setBreadcrumbEnabled(enabled);
    this.viewMenuTab.setBreadcrumbEnabled(enabled);
  }
};

scout.DesktopNavigation.prototype._createKeyStrokeAdapter = function() {
  return new scout.DesktopNavigationKeyStrokeAdapter(this);
};

scout.DesktopNavigation.prototype._installKeyStrokeAdapter = function() {
  if (this.keyStrokeAdapter && !scout.keyStrokeManager.isAdapterInstalled(this.keyStrokeAdapter)) {
    scout.keyStrokeManager.installAdapter(this.desktop.$container, this.keyStrokeAdapter);
  }
};

scout.DesktopNavigation.prototype._uninstallKeyStrokeAdapter = function() {
  if (this.keyStrokeAdapter && scout.keyStrokeManager.isAdapterInstalled(this.keyStrokeAdapter)) {
    scout.keyStrokeManager.uninstallAdapter(this.keyStrokeAdapter);
  }
};

scout.DesktopNavigation.prototype.doViewMenuAction = function() {
  if (this.viewMenuTab.popup === undefined || !this.viewMenuTab.popup.rendered) {
    this.viewMenuTab._onMousedownTab();
  } else {
    this.viewMenuTab.popup.close();
  }
};

scout.DesktopNavigation.prototype.sendToBack = function() {
  this.viewMenuTab.sendToBack();
  this.outline.sendToBack();
};

scout.DesktopNavigation.prototype.bringToFront = function() {
  this.viewMenuTab.bringToFront();
  this.outline.bringToFront();
};
