/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {DesktopBench, DesktopTabArea, DesktopTabBoxController, scout, SimpleTab, SimpleTabArea} from '../../index';
import {SimpleTabAreaTabSelectEvent} from '../../tabbox/SimpleTabAreaEventMap';

/**
 * The {@link {@link HeaderTabBoxController}} is used to link the center {@link {@link SimpleTabBox}} (all forms with displayViewId='C')
 * with a {@link {@link SimpleTabArea}} placed in the header.
 *
 * @see SimpleTabBoxController
 */
export default class HeaderTabBoxController extends DesktopTabBoxController {
  declare tabArea: DesktopTabArea;

  bench: DesktopBench;
  tabAreaCenter: SimpleTabArea;
  tabAreaInHeader: boolean;
  protected _viewsChangedHandler: () => void;

  constructor() {
    super();

    this.bench = null;
    this._viewsChangedHandler = this._onViewsChanged.bind(this);

    this.tabAreaCenter = null;
    this.tabAreaInHeader = false;
  }

  // @ts-ignore
  override install(bench: DesktopBench, tabArea?: DesktopTabArea) {
    this.bench = scout.assertParameter('bench', bench);

    let tabBoxCenter = this.bench.getTabBox('C');
    this.tabAreaCenter = tabBoxCenter.tabArea;

    super.install(tabBoxCenter, tabArea);
  }

  protected override _installListeners() {
    super._installListeners();
    this.bench.on('viewAdd', this._viewsChangedHandler);
    this.bench.on('viewRemove', this._viewsChangedHandler);
  }

  protected _onViewsChanged() {
    if (this.bench.getViews().some(view => 'C' !== view.displayViewId)) {
      // has views in other view stacks
      this._setViewTabAreaInHeader(false);
    } else {
      // has only views in center
      this._setViewTabAreaInHeader(true);
    }
  }

  protected _setViewTabAreaInHeader(inHeader: boolean) {
    this.tabAreaInHeader = inHeader;
    this.tabAreaCenter.setVisible(!inHeader);
    this.tabArea.setVisible(inHeader);
    let desktop = this.tabArea.session.desktop;
    if (desktop.rendered) {
      desktop.$container.toggleClass('view-tab-area-in-bench', !inHeader);
    }
  }

  override getTabs(): SimpleTab[] {
    if (this.tabAreaInHeader) {
      return this.tabArea.getTabs();
    }
    return this.tabAreaCenter.getTabs();
  }

  protected override _onViewTabSelect(view: SimpleTabAreaTabSelectEvent) {
    super._onViewTabSelect(view);
    this.tabArea.updateFirstTabSelected();
  }
}
