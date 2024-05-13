/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Calendar, CalendarSidebar, CalendarSidebarSplitter, CalendarsPanel, Dimension, HtmlComponent, Rectangle, Splitter, YearPanel} from '../index';

export class CalendarSidebarLayout extends AbstractLayout {
  calendarSidebar: CalendarSidebar;
  yearPanel: YearPanel;
  calendarsPanel: CalendarsPanel;
  splitter: CalendarSidebarSplitter;
  /**
   * Min splitter position,
   * arbitrary number to show at least one month of the year panel
   */
  minSplitterPosition: number;
  collapsedLabelHeight: number;

  protected _relativeSplitterPosition: number;
  protected _availableHeight: number;
  protected _newSplitterPosition: number;
  protected _newRelativeSplitterPosition: number;
  protected _animateNewSplitterPosition: boolean;

  constructor(widget: CalendarSidebar) {
    super();

    this.calendarSidebar = widget;
    this.minSplitterPosition = 200;
    this.collapsedLabelHeight = 40;
    this.yearPanel = this.calendarSidebar.yearPanel;
    this.splitter = this.calendarSidebar.splitter;
    this.calendarsPanel = this.calendarSidebar.calendarsPanel;
    this._availableHeight = null;
    this._newSplitterPosition = null;
    this._newRelativeSplitterPosition = null;
    this._animateNewSplitterPosition = false;
  }

  override layout($container: JQuery) {
    let htmlComp = HtmlComponent.get($container),
      insets = htmlComp.insets(),
      availableSize = htmlComp.availableSize().subtract(insets);

    this._validateSplitterPosition(htmlComp, this.splitter, availableSize);

    // Applies new absolute position
    if (this._newSplitterPosition !== null) {
      let newPos = this._newSplitterPosition;
      this._newSplitterPosition = null;
      this._setSplitterPosition(newPos, this._animateNewSplitterPosition);
      this._validateSplitterPosition(htmlComp, this.splitter, availableSize);
    }

    // Applies new relative position
    if (this._newRelativeSplitterPosition !== null) {
      let newPos = this._availableHeight / 100 * this._newRelativeSplitterPosition;
      this._newRelativeSplitterPosition = null;
      this._setSplitterPosition(newPos, this._animateNewSplitterPosition);
      this._validateSplitterPosition(htmlComp, this.splitter, availableSize);
    }

    // Calculate new heigths for ui elements
    let yearPanelHeight = this.splitter.position - 8; // Margin
    let calendarsPanelMargin = (this.splitter.collapsed ? this.collapsedLabelHeight : 20);
    let calendarsPanelHeight = availableSize.height - yearPanelHeight - calendarsPanelMargin;

    // Makes splitter invisible when calendars panel is not displayable
    this.splitter.$container.toggleClass('hidden', !this.calendarSidebar.calendarsPanelDisplayable);

    // Set new sizes
    this.yearPanel.htmlComp.setBounds(new Rectangle(insets.left, insets.top, availableSize.width, yearPanelHeight));
    this.calendarsPanel.htmlComp.setBounds(new Rectangle(insets.left, insets.top + calendarsPanelMargin, availableSize.width, calendarsPanelHeight));

    this.calendarSidebar.yearPanel._scrollYear();
  }

  protected _validateSplitterPosition(htmlComp: HtmlComponent, splitter: Splitter, availableSize: Dimension) {
    // Window has been resized -> preserve relative splitter position
    if (availableSize.height !== this._availableHeight) {
      this._silentUpdateSpliterPosition(htmlComp, splitter, availableSize.height * this._relativeSplitterPosition);
    }

    // Validate min and max splitter position
    let maxSplitterPosition = availableSize.height - this.collapsedLabelHeight;
    if (!this.calendarSidebar.calendarsPanelDisplayable) {
      this._silentUpdateSpliterPosition(htmlComp, splitter, Math.max(availableSize.height, 0));
    } else if (splitter.position < this.minSplitterPosition) {
      this._silentUpdateSpliterPosition(htmlComp, splitter, Math.max(this.minSplitterPosition, 0));
    } else if (splitter.position > maxSplitterPosition) {
      this._silentUpdateSpliterPosition(htmlComp, splitter, Math.max(maxSplitterPosition, 0));
    }

    // Update splitter collapsed
    let splitterCollapsed = splitter.position >= maxSplitterPosition;
    this.splitter.setCollapsed(splitterCollapsed);
    (this.calendarSidebar.parent as Calendar).setShowCalendarsPanel(!splitterCollapsed);

    // Update cached values
    this._availableHeight = availableSize.height;
    this._relativeSplitterPosition = splitter.position / availableSize.height;
  }

  protected _setSplitterPosition(pos: number, animate?: boolean) {
    if (!animate) {
      this.splitter.setPosition(pos);
    } else {
      let opts: JQuery.EffectsOptions<HTMLElement> = {
        progress: () => {
          this.splitter.setPosition(this.splitter.$container.cssTop());
        }
      };
      this.splitter.$container.animate({top: pos}, opts);
    }
  }

  /**
   * Updates splitter position without triggerig re-layouting
   */
  protected _silentUpdateSpliterPosition(htmlComp: HtmlComponent, splitter: Splitter, newPosition: number) {
    htmlComp.suppressInvalidate = true;
    try {
      splitter.setPosition(newPosition);
    } finally {
      htmlComp.suppressInvalidate = false;
    }
  }

  setNewSplitterPosition(pos: number, animate?: boolean) {
    this._newSplitterPosition = pos;
    this._animateNewSplitterPosition = animate;
  }

  setNewSplitterPositionPercentage(pos: number, animate?: boolean) {
    this._newRelativeSplitterPosition = pos;
    this._animateNewSplitterPosition = animate;
  }
}
