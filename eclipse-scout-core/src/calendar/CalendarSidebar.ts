/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {CalendarSidebarLayout, CalendarsPanel, HtmlComponent, InitModelOf, scout, Splitter, Widget, YearPanel} from '../index';

export class CalendarSidebar extends Widget {
  yearPanel: YearPanel;
  splitter: Splitter;
  calendarsPanel: CalendarsPanel;

  protected _showYearPanel: boolean;
  protected _showCalendarsPanel: boolean;

  constructor() {
    super();

    this.yearPanel = null;
    this.splitter = null;
    this.calendarsPanel = null;
  }

  override init(model: InitModelOf<this>) {
    super.init(model);

    this.yearPanel = scout.create(YearPanel, {
      parent: this
    });
    this.splitter = scout.create(Splitter, {
      parent: this,
      splitHorizontal: false,
      cssClass: 'line'
    });
    this.calendarsPanel = scout.create(CalendarsPanel, {
      parent: this,
      checkable: true
    });

    this.splitter.on('positionChange', this._onSplitterPositionChange.bind(this));

  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('calendar-sidebar');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new CalendarSidebarLayout(this));
    this.htmlComp.invalidateLayoutTree(false);
    this.yearPanel.render();
    this.splitter.render();
    this.calendarsPanel.render();
  }

  _onSplitterPositionChange(event) {
    this.invalidateLayoutTree(false);
  }

  startShowYearPanel(show: boolean) {
    if (show === this._showYearPanel) {
      return;
    }
    this._showYearPanel = show;
    this._layoutSizes(show, this._showCalendarsPanel, 100, 0);
  }

  startShowCalendarsPanel(show: boolean) {
    if (show === this._showCalendarsPanel) {
      return;
    }
    this._showCalendarsPanel = show;
    this._layoutSizes(show, this._showYearPanel, 0, 100);
  }

  protected _layoutSizes(elmentVisible: boolean, otherElementVisible: boolean, fullExpandedPos: number, notExpandedPos: number) {
    let layout = this.htmlComp.layout as CalendarSidebarLayout;
    if (elmentVisible && !otherElementVisible) {
      // Nothing is visible before, sidebar extends horizontally, no vertical animation
      layout.setNewSplitterPositionPercentage(fullExpandedPos, false);
    } else if (elmentVisible && otherElementVisible) {
      // Other panel is already visible, animate beside it
      layout.setNewSplitterPositionPercentage(50, true);
    } else if (!elmentVisible && otherElementVisible) {
      // Hide this panel with animation
      layout.setNewSplitterPositionPercentage(notExpandedPos, true);
    } else if (!elmentVisible && !otherElementVisible) {
      // Nothing is expanded, sidebar will colapse, no vertical animation
    }
    this.invalidateLayoutTree(false);
  }
}
