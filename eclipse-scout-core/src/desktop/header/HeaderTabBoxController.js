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
import {DesktopTabBoxController, scout} from '../../index';

/**
 * The {@link {@link scout.HeaderViewTabAreaController}} is used to link the center {@link {@link SimpleTabBox}} (all forms with displayViewId='C')
 * with a {@link {@link SimpleTabArea}} placed in the header.
 * It is an extension of {@link {@link SimpleTabBoxController}}.
 *
 * @see SimpleTabBoxController
 */
export default class HeaderTabBoxController extends DesktopTabBoxController {
  constructor() {
    super();

    this.bench = null;
    this._viewsChangedHandler = this._onViewsChanged.bind(this);

    this.tabAreaCenter = null;
    this.tabAreaInHeader = false;
  }

  install(bench, tabArea) {
    this.bench = scout.assertParameter('bench', bench);

    let tabBoxCenter = this.bench.getTabBox('C');
    this.tabAreaCenter = tabBoxCenter.tabArea;

    super.install(tabBoxCenter, tabArea);
  }

  _installListeners() {
    super._installListeners();
    this.bench.on('viewAdd', this._viewsChangedHandler);
    this.bench.on('viewRemove', this._viewsChangedHandler);
  }

  _onViewsChanged() {
    if (this.bench.getViews().some(view => 'C' !== view.displayViewId)) {
      // has views in other view stacks
      this._setViewTabAreaInHeader(false);
    } else {
      // has only views in center
      this._setViewTabAreaInHeader(true);
    }
  }

  _setViewTabAreaInHeader(inHeader) {
    this.tabAreaInHeader = inHeader;
    this.tabAreaCenter.setVisible(!inHeader);
    this.tabArea.setVisible(inHeader);
    let desktop = this.tabArea.session.desktop;
    if (desktop.rendered) {
      desktop.$container.toggleClass('view-tab-area-in-bench', !inHeader);
    }
  }

  getTabs() {
    if (this.tabAreaInHeader) {
      return this.tabArea.getTabs();
    }
    return this.tabAreaCenter.getTabs();
  }

  _onViewTabSelect(view) {
    super._onViewTabSelect(view);
    this.tabArea.updateFirstTabSelected();
  }
}
