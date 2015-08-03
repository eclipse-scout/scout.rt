/**
 * Controller to interact with 'view tab bar'.
 */
scout.ViewTabsController = function(desktop) {
  this._desktop = desktop;
  this._selectedViewTab;
  this._viewTabs = [];
  this._viewTabMap = {}; // [key=viewId, value=DesktopViewTab instance]
};

/**
 * Creates and renders a view tab for the given view.
 */
scout.ViewTabsController.prototype.createAndRenderViewTab = function(view) {
  // Create the view tab.
  var viewTab = new scout.DesktopViewTab(view, this._desktop.$bench, this._desktop.session);
  var viewId = view.id;

  // Register lifecycle listener on view tab.
  viewTab.on('tabClicked', this.selectViewTab.bind(this));
  viewTab.on('remove', this._removeViewTab.bind(this, viewTab, viewId));

  // Register the view tab.
  this._viewTabs.push(viewTab);
  this._viewTabMap[viewId] = viewTab;

  // Render the view tab.
  if (this._desktop._hasTaskBar()) {
    viewTab.render(this._desktop._$viewTabBar);
  }

  return viewTab;
};

/**
 * Method invoked once the given view tab is removed from DOM.
 */
scout.ViewTabsController.prototype._removeViewTab = function(viewTab, viewId) {
  // Unregister the view tab.
  scout.arrays.remove(this._viewTabs, viewTab);
  delete this._viewTabMap[viewId];

  // Select next available view tab.
  // FIXME DWI: (activeForm) use activeForm here or when no form is active, show outline again (from A.WE)
  if (this._selectedViewTab === viewTab) {
    this._selectLastViewTab();
  }

  this._desktop._layoutTaskBar();
};

/**
 * Selects the given view tab and attaches its associated view.
 */
scout.ViewTabsController.prototype.selectViewTab = function(viewTab) {
  if (this._selectedViewTab === viewTab) {
    return;
  }

  // Hide outline content.
  this._desktop._sendNavigationToBack();
  this._desktop._detachOutlineContent();

  // Deselect the current selected tab.
  this.deselectViewTab();

  // Select the new view tab.
  viewTab.select();
  this._selectedViewTab = viewTab;

  // Invalidate layout and focus.
  this._desktop._layoutTaskBar();
  scout.focusManager.validateFocus(this._desktop.session.uiSessionId);
};

/**
 * Deselects the currently selected view tab.
 */
scout.ViewTabsController.prototype.deselectViewTab = function() {
  if (!this._selectedViewTab) {
    return;
  }

  this._selectedViewTab.deselect();
  this._selectedViewTab = null;
};

/**
 * Returns the view tab associated with the given view.
 */
scout.ViewTabsController.prototype.viewTab = function(view) {
  return this._viewTabMap[view.id];
};

/**
 * Returns the all view tabs.
 */
scout.ViewTabsController.prototype.viewTabs = function() {
  return this._viewTabs;
};

/**
 * Returns the selected view tab.
 */
scout.ViewTabsController.prototype.selectedViewTab = function() {
  return this._selectedViewTab;
};

/**
 * Returns the number of view tabs.
 */
scout.ViewTabsController.prototype.viewTabCount = function() {
  return this._viewTabs.length;
};

/**
 * Selects the last view tab.
 */
scout.ViewTabsController.prototype._selectLastViewTab = function() {
  if (this._viewTabs.length > 0) {
    this.selectViewTab(this._viewTabs[this._viewTabs.length - 1]);
  } else {
    this.deselectViewTab();

    this._desktop._attachOutlineContent();
    this._desktop._bringNavigationToFront();
  }
};
