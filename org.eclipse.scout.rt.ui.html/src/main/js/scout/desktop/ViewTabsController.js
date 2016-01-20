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
scout.ViewTabsController.prototype.createAndRenderViewTab = function(view, position) {
  var viewId = view.id;
  //Tab is already existing.
  var viewTab = this._viewTabMap[viewId],
    newViewTab = !viewTab;
  if (newViewTab) {

    // Create the view tab.
    viewTab = scout.create('DesktopViewTab', {
      parent: view.displayParent,
      view: view,
      $bench: this._desktop.$bench,
      viewTabController: this
    });

    // Register lifecycle listener on view tab.
    viewTab.on('tabClicked', this.selectViewTab.bind(this));
    viewTab.on('remove', this._removeViewTab.bind(this, viewTab, viewId));

    var index = position;
    var parentViewTab = this.viewTab(viewTab._view.displayParent);
    if (parentViewTab) {
      index = this._viewTabs.indexOf(parentViewTab) + this._calculateExactPosition(parentViewTab, index) + 1;
    } else {
      index = this._calculateExactPosition(this._desktop, index);
    }

    // Register the view tab.
    scout.arrays.insert(this._viewTabs, viewTab, index);
    this._viewTabMap[viewId] = viewTab;

  }
  // Render the view tab.
  if (this._desktop._hasTaskBar() && !viewTab.rendered) {
    viewTab.render(this._desktop._$viewTabBar);
  }

  //when rendering desktop also add all child tabs.
  if (view.session.desktop.initialFormRendering && newViewTab) {
    view.formController.render();
  }

  return viewTab;
};

scout.ViewTabsController.prototype._calculateExactPosition = function(parent, position) {
  if (position === 0) {
    return 0; // return 1; --> BenchModeTest = grün
    // FIXME nbu: (von A.WE) das hier macht den BENCH modus vom Desktop kaputt (siehe BenchModeTest)
    // dort gibt es keine outline, die app wird mit einem einzigen form (view) gestartet
    // von diesem form aus öffnet man ein weiteres form (view), das modal zum aktuellen form sein soll
    // der aktuelle code fügt dieses neue form an position 0 ein. Das ist nicht richtig, das neue form
    // muss rechts vom aktuellen tab
  } else {
    var tabs = position || parent.views.length;
    var searchUntil = position || parent.views.length;
    for (var i = 0; i < searchUntil; i++) {
      tabs = tabs + this._calculateExactPosition(parent.views[i]);
    }
    return tabs;
  }
};

/**
 * Method invoked once the given view tab is removed from DOM.
 */
scout.ViewTabsController.prototype._removeViewTab = function(viewTab, viewId) {
  // Unregister the view tab.
  var viewTabIndexBefore = this._viewTabs.indexOf(viewTab) - 1;
  scout.arrays.remove(this._viewTabs, viewTab);
  delete this._viewTabMap[viewId];

  // Select next available view tab.
  // FIXME dwi: (activeForm) use activeForm here or when no form is active, show outline again (from A.WE)
  if (this._selectedViewTab === viewTab) {
    if (viewTabIndexBefore >= 0) {
      this.selectViewTab(this._viewTabs[viewTabIndexBefore]);
    } else {
      this._desktop.bringOutlineToFront(this._desktop.outline);
    }
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

  // set _selectedViewTab before selecting view tab. if this is not done before there is a problem when refreshing the webpage.
  // parent is not set as selected, but rendered, before child-> child is rendered into same view because parent is not deselect.
  // parent viewTab.select calls rendering of child.
  this._selectedViewTab = viewTab;
  // Select the new view tab.
  viewTab.select();

  // Invalidate layout and focus.
  this._desktop._layoutTaskBar();
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
