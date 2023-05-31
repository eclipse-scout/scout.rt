/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, CalendarSidebar, Dimension, HtmlComponent, Rectangle, Splitter} from '../index';

export class CalendarSidebarLayout extends AbstractLayout {
  widget: CalendarSidebar;

  protected _splitter: Splitter;

  protected _relativeSplitterPosition: number;
  protected _availableHeight: number;
  protected _splitterConstraints: string;

  constructor(widget: CalendarSidebar) {
    super();

    this.widget = widget;
    this._splitter = widget.splitter;
  }


  override layout($container: JQuery) {
    let htmlComp = HtmlComponent.get($container),
      insets = htmlComp.insets(),
      availableSize = htmlComp.availableSize().subtract(insets),
      yearPanelHeight, resourcesPanelHeight, splitterHeight;

    this._validateSplitterPosition(htmlComp, this._splitter, availableSize);

    splitterHeight = this._splitter.htmlComp.bounds().height;

    yearPanelHeight = this._splitter.position - splitterHeight / 2;
    resourcesPanelHeight = availableSize.height - yearPanelHeight - splitterHeight;

    this.widget.yearPanel.htmlComp.setBounds(new Rectangle(insets.left, insets.top, availableSize.width, yearPanelHeight));
    this.widget.resourcesPanel.htmlComp.setBounds(new Rectangle(insets.left, insets.top + splitterHeight, availableSize.width, resourcesPanelHeight));

    // Makes splitter invisible when its at top or bottom
    if (this._splitter.position !== 0 && this._splitter.position !== this._availableHeight) {
      this._splitter.$container.removeClass('invisible');
    } else {
      this._splitter.$container.addClass('invisible');
    }

    this.setAnimatedSplitterPosition();
  }

  setAnimatedSplitterPosition() {
    if (!this.widget.invalidPanelSizes) {
      return;
    }
    if (this.widget.showYearPanel && !this.widget.showResourcesPanel && this._splitter.position === 0) {
      // No vertical animation on fade in of calendar sidebar (year panel)
      this._splitter.setPosition(this._availableHeight);
    } else if (this.widget.showResourcesPanel && !this.widget.showYearPanel && this._splitter.position === this._availableHeight) {
      // No vertical animation on fade in of calendar sidebar (resources panel)
      this._splitter.setPosition(0);
    } else if (!this.widget.showYearPanel && !this.widget.showResourcesPanel) {
      // No vertical animation on fade out of calendar sidebar
    } else {
      let opts: JQuery.EffectsOptions<HTMLElement> = {
        progress: () => {
          this._splitter.setPosition(this._splitter.$container.cssTop());
        }
      };
      let pos = this.widget.showYearPanel && this.widget.showResourcesPanel
        ? this._availableHeight / 2
        : this.widget.showYearPanel ? this._availableHeight : 0;
      this._splitter.$container.animate({
        top: pos
      }, opts);
    }
    this.widget.invalidPanelSizes = false;
  }

  protected _validateSplitterPosition(htmlComp: HtmlComponent, splitter: Splitter, availableSize: Dimension) {
    // Window has been resized -> preserve relative splitter position
    if (availableSize.height !== this._availableHeight) {
      this._silentUpdateSpliterPosition(htmlComp, splitter, availableSize.height * this._relativeSplitterPosition);
    }

    // Validate min and max splitter position
    let maxSplitterPosition = availableSize.height;
    if (splitter.position < 0) {
      this._silentUpdateSpliterPosition(htmlComp, splitter, 0);
    } else if (splitter.position > maxSplitterPosition) {
      this._silentUpdateSpliterPosition(htmlComp, splitter, Math.max(maxSplitterPosition, 0));
    }

    // Update cached values
    this._availableHeight = availableSize.height;
    this._relativeSplitterPosition = splitter.position / availableSize.height;
  }

  protected _silentUpdateSpliterPosition(htmlComp: HtmlComponent, splitter: Splitter, newPosition: number) {
    htmlComp.suppressInvalidate = true;
    try {
      splitter.setPosition(newPosition);
    } finally {
      htmlComp.suppressInvalidate = false;
    }
  }
}
