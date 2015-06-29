// FIXME NBU/AWE: inherit from Widget.js? refactor un-/installKeyStroke
scout.DesktopNavigation = function(desktop) {
  this.desktop = desktop;
  this.session = desktop.session;

  this.BREADCRUMB_SWITCH_WIDTH = 190;

  this.$navigation;
  this.$viewButtons;
  this.$container;
  this.htmlViewButtons;

  this.activeTab;
  this.outlineTab;
  this.keyStrokeAdapter = this._createKeyStrokeAdapter();
  this.viewMenuTab;
};

scout.DesktopNavigation.prototype.render = function($parent) {
  this.$navigation = $parent.appendDiv('desktop-navigation');
  this.$viewButtons = this.$navigation.appendDiv('view-buttons');
  this.htmlViewButtons = new scout.HtmlComponent(this.$viewButtons, this.session);
  this.htmlViewButtons.setLayout(new scout.ViewButtonsLayout(this.htmlViewButtons));

  this.viewMenuTab = new scout.ViewMenuTab(this._viewButtons('MENU'), this.session);
  this.viewMenuTab.render(this.$viewButtons);

  this._viewButtons('TAB').forEach(function(viewTab) {
    viewTab.render(this.$viewButtons);
  }, this);

  this._viewButtons().forEach(function(viewButton) {
    viewButton.on('propertyChange', this._onViewButtonPropertyChange.bind(this));
  }, this);

  this.$container = this.$navigation.appendDiv('navigation-container');
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
  if (event.selected !== undefined) {
    this.htmlViewButtons.revalidateLayout();
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
  this.viewMenuTab.onOutlineChanged(outline);
  this.outline.validateFocus();
};

// vertical splitter
scout.DesktopNavigation.prototype.onResize = function(event) {
  var w = event.data; // data = newSize

  this.$navigation.width(w);
  this.htmlViewButtons.revalidateLayout();
  this.desktop.$taskBar.css('left', w);
  this.desktop.$bench.css('left', w);

  //FIXME AWE bounce effect is broken
  if (w <= this.BREADCRUMB_SWITCH_WIDTH) {
    if (!this.$navigation.hasClass('navigation-breadcrumb')) {
      this.$navigation.addClass('navigation-breadcrumb');
      this.outline.setBreadcrumbEnabled(true);
    }
  } else {
    this.$navigation.removeClass('navigation-breadcrumb');
    this.outline.setBreadcrumbEnabled(false);
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
  this.viewMenuTab._onClickTab();
};
