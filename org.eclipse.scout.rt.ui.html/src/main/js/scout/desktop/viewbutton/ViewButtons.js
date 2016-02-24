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
scout.ViewButtons = function() {
  scout.ViewButtons.parent.call(this);
  this.viewMenuTab;
};
scout.inherits(scout.ViewButtons, scout.Widget);

scout.ViewButtons.prototype._render = function($parent) {
  var viewTabs;

  this.desktop = this.session.desktop,
  this.$container = $parent.appendDiv('view-buttons');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.ViewButtonsLayout(this));
  this.viewMenuTab = new scout.ViewMenuTab(this._viewButtons('MENU'), this.session);
  this.viewMenuTab.render(this.$container);

  viewTabs = this._viewButtons('TAB');
  this._viewButtons('TAB').forEach(function(viewTab, i) {
    viewTab.render(this.$container);
    if (i === viewTabs.length - 1) {
      viewTab.last();
    }
  }, this);
};

scout.ViewButtons.prototype._viewButtons = function(displayStyle) {
  var viewButtons = [];
  this.desktop.viewButtons.forEach(function(viewButton) {
    if (displayStyle === undefined ||
      displayStyle === viewButton.displayStyle) {
      viewButtons.push(viewButton);
    }
  });
  return viewButtons;
};

/**
 * This method updates the state of the view-menu-tab and the selected state of outline-view-buttons.
 * This method must also work in offline mode.
 */
scout.ViewButtons.prototype.onOutlineChanged = function(outline) {
  this.viewMenuTab.onOutlineChanged(outline);
  this._viewButtons('TAB').forEach(function(viewTab) {
    if (viewTab instanceof scout.OutlineViewButton) {
      viewTab.onOutlineChanged(outline);
    }
  });
};


scout.ViewButtons.prototype.doViewMenuAction = function(event) {
  this.viewMenuTab.togglePopup(event);
};
