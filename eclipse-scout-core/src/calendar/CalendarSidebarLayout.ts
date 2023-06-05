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

  protected _newSplitterPosition: number;
  protected _animateNewSplitterPosition: boolean;

  constructor(widget: CalendarSidebar) {
    super();

    this.widget = widget;
    this._splitter = widget.splitter;
    this._newSplitterPosition = null;
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

    if (this._newSplitterPosition !== null) {
      let newPos = this._newSplitterPosition;
      this._newSplitterPosition = null;
      this._setSplitterPosition(newPos, this._animateNewSplitterPosition);
    }
  }

  setNewSplitterPosition(pos: number, animate?: boolean) {
    this._newSplitterPosition = pos;
    this._animateNewSplitterPosition = animate;
  }

  setNewSplitterPositionPercentage(pos: number, animate?: boolean) {
    this.setNewSplitterPosition(this._availableHeight / 100 * pos, animate);
  }

  protected _setSplitterPosition(pos: number, animate?: boolean) {
    if (!animate) {
      this._splitter.setPosition(pos);
    } else {
      let opts: JQuery.EffectsOptions<HTMLElement> = {
        progress: () => {
          this._splitter.setPosition(this._splitter.$container.cssTop());
        }
      };
      this._splitter.$container.animate({top: pos}, opts);
    }
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
