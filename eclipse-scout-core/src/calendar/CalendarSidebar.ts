/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {CalendarSidebarLayout, CalendarSidebarModel, CalendarSidebarSplitter, CalendarsPanel, Event, HtmlComponent, InitModelOf, scout, SplitterPositionChangeEvent, Widget, YearPanel} from '../index';

export class CalendarSidebar extends Widget implements CalendarSidebarModel {
  declare model: CalendarSidebarModel;

  yearPanel: YearPanel;
  splitter: CalendarSidebarSplitter;
  calendarsPanel: CalendarsPanel;

  calendarsPanelDisplayable: boolean;
  /**
   * Indicates, if the layout process is animated or not
   */
  protected _animationEnabled: boolean;

  constructor() {
    super();

    this.yearPanel = null;
    this.splitter = null;
    this.calendarsPanel = null;
    this.calendarsPanelDisplayable = false;
    this._animationEnabled = false;
  }

  override init(model: InitModelOf<this>) {
    super.init(model);

    this.yearPanel = scout.create(YearPanel, {
      parent: this
    });
    this.splitter = scout.create(CalendarSidebarSplitter, {
      parent: this,
      splitHorizontal: false,
      collapsedLabel: this.session.text('ui.AvailableCalendars')
    });
    this.calendarsPanel = scout.create(CalendarsPanel, {
      parent: this,
      checkable: true
    });

    this.splitter.on('positionChange', this._onSplitterPositionChange.bind(this));
    this.splitter.on('splitterClick', this._onSplitterClick.bind(this));
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('calendar-sidebar');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new CalendarSidebarLayout(this));
    this.yearPanel.render();
    this.splitter.render();
    this.calendarsPanel.render();
  }

  protected _onSplitterPositionChange(event: SplitterPositionChangeEvent) {
    this.invalidateLayoutTree(false);
  }

  protected _onSplitterClick(event: Event<CalendarSidebarSplitter>) {
    this.setCalendarsPanelExpanded(this.splitter.collapsed, true);
  }

  setCalendarsPanelExpanded(expanded: boolean, animate = false) {
    if (this.splitter.collapsed !== undefined && this.splitter.collapsed !== expanded) {
      return;
    }
    // 62 is the golden ratio
    (this.htmlComp.layout as CalendarSidebarLayout).setNewSplitterPositionPercentage(expanded ? 62 : 100, animate);
    this.invalidateLayoutTree(false);
  }

  setCalendarsPanelDisplayable(displayable: boolean) {
    this.setProperty('calendarsPanelDisplayable', displayable);
  }

  protected _setCalendarsPanelDisplayable(displayable: boolean) {
    this._setProperty('calendarsPanelDisplayable', displayable);
    this.invalidateLayoutTree(false);
  }
}
