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
 * The {@link {@link scout.HeaderViewTabAreaController}} is used to link the center {@link {@link scout.ViewArea}} (all forms with displayViewId='C')
 * with a {@link {@link scout.ViewTabArea}} placed in the header.
 * It is an extension of {@link {@link scout.ViewTabAreaController}}.
 *
 * @see scout.ViewTabAreaController
 */
scout.HeaderViewTabBoxController = function(bench, viewTabArea) {
  this.bench = bench;

  // event listeners
  this._viewsChanged = this._onViewsChanged.bind(this);

  scout.HeaderViewTabBoxController.parent.call(this, bench.getViewArea('C'), viewTabArea);

  this.viewTabAreaCenter = bench.getViewArea('C').viewTabArea;
  this.viewTabAreaInHeader;

};
scout.inherits(scout.HeaderViewTabBoxController, scout.ViewTabAreaController);

scout.HeaderViewTabBoxController.prototype._installListeners = function() {
  scout.HeaderViewTabBoxController.parent.prototype._installListeners.call(this);
  this.bench.on('viewAdded', this._viewsChanged);
  this.bench.on('viewRemoved', this._viewsChanged);
};

scout.HeaderViewTabBoxController.prototype._onViewsChanged = function() {
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

scout.HeaderViewTabBoxController.prototype._setViewTabAreaInHeader = function(inHeader) {
  if (this.viewTabAreaInHeader === inHeader) {
    return;
  }
  this.viewTabAreaInHeader = inHeader;
  this.viewTabAreaCenter.setVisible(!inHeader);
  this.viewTabArea.setVisible(inHeader);
};
