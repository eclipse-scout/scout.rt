// FIXME NBU/AWE: inherit from Widget.js? refactor un-/installKeyStroke
scout.DesktopNavigation = function(desktop) {
  this.desktop = desktop;
  this.session = desktop.session;

  this.$navigation;
  this.$viewButtons;
  this.$container;

  this.activeTab;
  this.outlineTab;
  this.$outlineTitle;
  this.breadcrumbSwitchWidth = 190;
  this.keyStrokeAdapter = this._createKeyStrokeAdapter();
  this.viewMenuTab;
};

scout.DesktopNavigation.prototype.render = function($parent) {
  this.$navigation = $parent.appendDiv('desktop-navigation');
  this.$viewButtons = this.$navigation.appendDiv('view-buttons');

  this.viewMenuTab = new scout.ViewMenuTab(this._viewButtons('MENU'), this.session);
  this.viewMenuTab.render(this.$viewButtons);

  this._viewButtons('TAB').forEach(function(viewTab) {
    viewTab.render(this.$viewButtons);
  }, this);

  this.$container = this.$navigation.appendDiv('navigation-container');
  this._installKeyStrokeAdapter();
};

scout.DesktopNavigation.prototype._viewButtons = function(displayStyle) {
  var viewButtons = [];
  this.desktop.viewButtons.forEach(function(viewButton) {
    if (viewButton.displayStyle === displayStyle) {
      viewButtons.push(viewButton);
    }
  });
  return viewButtons;
};

scout.DesktopNavigation.prototype._selectTab = function(tab, outline) {
  this.desktop.changeOutline(outline);
  this.session.send(this.desktop.id, 'outlineChanged', {
    outlineId: outline.id
  });
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
//  this.$outlineTitle.html(this.outline.title); // FIXME AWE (desktop) remove 1st tab
  this.viewMenuTab.onOutlineChanged(outline);
  this.outline.validateFocus();
};

// vertical splitter
scout.DesktopNavigation.prototype.onResize = function(event) {
  var w = event.data; // data = newSize

  this.$navigation.width(w);
  this.desktop.$taskBar.css('left', w);
  this.desktop.$bench.css('left', w);

  if (w <= this.breadcrumbSwitchWidth) {
    if (!this.$navigation.hasClass('navigation-breadcrumb')) {
      this.$navigation.addClass('navigation-breadcrumb');
      this.outline.setBreadcrumbEnabled(true);
    }
  } else {
    this.$navigation.removeClass('navigation-breadcrumb');
    this.outline.setBreadcrumbEnabled(false);
  }
};

/**
 * Called by OutlineViewButton.js
 */
scout.DesktopNavigation.prototype.onOutlinePropertyChange = function(event) {
  for (var propertyName in event.properties) {
    if (propertyName === 'text') {
      this.$outlineTitle.text(event.properties[propertyName]);
    }
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

/* --- INNER TYPES ---------------------------------------------------------------- */

scout.DesktopNavigation.TabAndContent = function($tab) {
  this.$tab = $tab;
  this.$content = null;
};
