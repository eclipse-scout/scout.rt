/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DesktopBench, DesktopTab, DesktopTabArea, DesktopTabBoxController, Form, scout, SimpleTabAreaTabSelectEvent, SimpleTabBox} from '../../index';

/**
 * The {@link HeaderTabBoxController} is used to link the center {@link SimpleTabBox} (all forms with displayViewId='C')
 * with a {@link SimpleTabArea} placed in the header.
 *
 * @see SimpleTabBoxController
 */
export class HeaderTabBoxController extends DesktopTabBoxController {
  declare tabArea: DesktopTabArea;

  bench: DesktopBench;
  tabAreaCenter: DesktopTabArea;
  tabAreaInHeader: boolean;
  protected _viewsChangedHandler: () => void;

  constructor() {
    super();

    this.bench = null;
    this._viewsChangedHandler = this._onViewsChanged.bind(this);

    this.tabAreaCenter = null;
    this.tabAreaInHeader = false;
  }

  // @ts-expect-error
  override install(bench: DesktopBench, tabArea?: DesktopTabArea) {
    this.bench = scout.assertParameter('bench', bench);

    let tabBoxCenter = this.bench.getTabBox('C') as SimpleTabBox<Form>;
    this.tabAreaCenter = tabBoxCenter.tabArea as DesktopTabArea;

    super.install(tabBoxCenter, tabArea);
  }

  protected override _installListeners() {
    super._installListeners();
    this.bench.on('viewAdd', this._viewsChangedHandler);
    this.bench.on('viewRemove', this._viewsChangedHandler);
  }

  /** @internal */
  _onViewsChanged() {
    // Allow the tab area in the header as long as the center stack is the top left.
    // This is the case when no other view is placed above or on the left of C.
    //   ____                                 ____
    // / Tab \                              / Tab \
    // +------+------+------+               +-------------+------+
    // | (NW) | (N)  |  NE  |               |             |  NE  |
    // +------+------+------+               |      C      +------+
    // |  (W) |  C   |  E   |      -->      |             |  E   |
    // +------+------+------+               +-------------+------+
    // | (SW) |  S   |  SE  |               |      S      |  SE  |
    // +------+------+------+               +-------------+------+
    //
    if (this.bench.getViews().some(view => scout.isOneOf(view.displayViewId, 'N', 'W', 'NW', 'SW'))) {
      // has views in N, W, NW, SW
      this._setViewTabAreaInHeader(false);
    } else {
      // has only views in center or on the right or below
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

  override getTabs(): DesktopTab[] {
    if (this.tabAreaInHeader) {
      return this.tabArea.getTabs();
    }
    return this.tabAreaCenter.getTabs();
  }

  protected override _onViewTabSelect(event: SimpleTabAreaTabSelectEvent<Form>) {
    super._onViewTabSelect(event);
    this.tabArea.updateFirstTabSelected();
  }
}
