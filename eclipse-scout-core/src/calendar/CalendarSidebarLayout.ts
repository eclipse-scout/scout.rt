/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Calendar, CalendarSidebar, CalendarSidebarSplitter, Dimension, HtmlComponent, Rectangle, ResourcePanel, Splitter, YearPanel} from '../index';

export class CalendarSidebarLayout extends AbstractLayout {
  calendarSidebar: CalendarSidebar;
  yearPanel: YearPanel;
  resourcePanel: ResourcePanel;
  splitter: CalendarSidebarSplitter;
  /**
   * Min splitter position,
   * arbitrary number to show at least one month of the year panel
   */
  minSplitterPosition: number;

  protected _relativeSplitterPosition: number;
  protected _availableHeight: number;
  protected _newSplitterPosition: number;
  protected _newRelativeSplitterPosition: number;
  protected _animateNewSplitterPosition: boolean;

  constructor(widget: CalendarSidebar) {
    super();

    this.calendarSidebar = widget;
    this.minSplitterPosition = 200;
    this.yearPanel = this.calendarSidebar.yearPanel;
    this.splitter = this.calendarSidebar.splitter;
    this.resourcePanel = this.calendarSidebar.resoucePanel;
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
    let resourcePanelMargin = (this.splitter.collapsed ? this._calculateCollapsedLabelHeight() : 0) + 20;
    let resourcePanelHeight = availableSize.height - yearPanelHeight - resourcePanelMargin;

    // Makes splitter invisible when resource panel is not displayable
    this.splitter.$container.toggleClass('hidden', !this.calendarSidebar.resourcePanelDisplayable);

    // Set new sizes
    this.yearPanel.htmlComp.setBounds(new Rectangle(insets.left, insets.top, availableSize.width, yearPanelHeight));
    this.resourcePanel.htmlComp.setBounds(new Rectangle(insets.left, insets.top + resourcePanelMargin, availableSize.width, resourcePanelHeight));

    this.calendarSidebar.yearPanel._scrollYear();
  }

  protected _validateSplitterPosition(htmlComp: HtmlComponent, splitter: Splitter, availableSize: Dimension) {
    let maxSplitterPosition = availableSize.height - this._calculateCollapsedLabelHeight();

    // Window has been resized -> preserve relative splitter position
    if (availableSize.height !== this._availableHeight) {
      this._silentUpdateSpliterPosition(htmlComp, splitter, this._relativeSplitterPosition * maxSplitterPosition);
    }

    // Validate min and max splitter position
    if (!this.calendarSidebar.resourcePanelDisplayable) {
      this._silentUpdateSpliterPosition(htmlComp, splitter, Math.max(availableSize.height, 0));
    } else if (splitter.position < this.minSplitterPosition) {
      this._silentUpdateSpliterPosition(htmlComp, splitter, Math.max(this.minSplitterPosition, 0));
    } else if (splitter.position > maxSplitterPosition) {
      this._silentUpdateSpliterPosition(htmlComp, splitter, Math.max(maxSplitterPosition, 0));
    }

    // Update splitter collapsed
    let splitterCollapsed = splitter.position >= maxSplitterPosition;
    this.splitter.setCollapsed(splitterCollapsed);
    (this.calendarSidebar.parent as Calendar).setShowResourcePanel(!splitterCollapsed);

    // Update cached values
    this._availableHeight = availableSize.height;
    this._relativeSplitterPosition = splitter.position / maxSplitterPosition;
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

  protected _calculateCollapsedLabelHeight(): number {
    if (!this.splitter.rendered) {
      return 40; // arbitrary number
    }
    let labelHeight = this.splitter.$collapsedLabel.outerHeight();
    let splitterHeight = this.splitter.$splitterBox.outerHeight();
    return labelHeight + splitterHeight;
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
