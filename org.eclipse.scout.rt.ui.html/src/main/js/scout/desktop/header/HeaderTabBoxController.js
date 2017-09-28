/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
 * The {@link {@link scout.HeaderViewTabAreaController}} is used to link the center {@link {@link scout.SimpleTabBox}} (all forms with displayViewId='C')
 * with a {@link {@link scout.SimpleTabArea}} placed in the header.
 * It is an extension of {@link {@link scout.SimpleTabBoxController}}.
 *
 * @see scout.SimpleTabBoxController
 */
scout.HeaderTabBoxController = function() {
  scout.HeaderTabBoxController.parent.call(this);

  this.bench;
  this._viewsChangedHandler = this._onViewsChanged.bind(this);

  this.tabAreaCenter;
  this.tabAreaInHeader = false;
};
scout.inherits(scout.HeaderTabBoxController, scout.DesktopTabBoxController);

scout.HeaderTabBoxController.prototype.install = function(bench, tabArea) {
  this.bench = scout.assertParameter('bench', bench);

  var tabBoxCenter = this.bench.getTabBox('C');
  this.tabAreaCenter = tabBoxCenter.tabArea;

  scout.HeaderTabBoxController.parent.prototype.install.call(this, tabBoxCenter, tabArea);
};

scout.HeaderTabBoxController.prototype._installListeners = function() {
  scout.HeaderTabBoxController.parent.prototype._installListeners.call(this);
  this.bench.on('viewAdd', this._viewsChangedHandler);
  this.bench.on('viewRemove', this._viewsChangedHandler);
};

scout.HeaderTabBoxController.prototype._onViewsChanged = function() {
  if (this.bench.getViews().some(function(view) {
      return 'C' !== view.displayViewId;
    })) {
    // has views in other view stacks
    this._setViewTabAreaInHeader(false);
  } else {
    // has only views in center
    this._setViewTabAreaInHeader(true);
  }
};

scout.HeaderTabBoxController.prototype._setViewTabAreaInHeader = function(inHeader) {
  this.tabAreaInHeader = inHeader;
  this.tabAreaCenter.setVisible(!inHeader);
  this.tabArea.setVisible(inHeader);
};

scout.HeaderTabBoxController.prototype.getTabs = function() {
  if (this.tabAreaInHeader) {
    return this.tabArea.getTabs();
  }
  return this.tabAreaCenter.getTabs();
};
