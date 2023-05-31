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

  static MIN_SPLITTER_POSITION = 210;

  protected _relativeSplitterPosition: number;
  protected _availableHeight: number;
  protected _splitterConstraints: string;

  constructor(widget: CalendarSidebar) {
    super();

    this.widget = widget;
  }


  override layout($container: JQuery) {
    const htmlComp = HtmlComponent.get($container);
    const insets = htmlComp.insets();
    const availableSize = htmlComp.availableSize().subtract(insets);

    const yearPanel = this.widget.yearPanel;
    const splitter = this.widget.splitter;
    const resourcesPanel = this.widget.resourcesPanel;

    this._validateSplitterPosition(htmlComp, splitter, availableSize);

    // Set sizes
    splitter.setVisible(!yearPanel.$container.hasClass('hidden') && !resourcesPanel.$container.hasClass('hidden'));

    let yearPanelHeight, resourcesPanelHeight;
    let splitterHeight = splitter.htmlComp.bounds().height;

    yearPanelHeight = yearPanel.isVisible() ? availableSize.height : 0;
    resourcesPanelHeight = resourcesPanel.isVisible() ? availableSize.height : 0;

    if (splitter.isVisible()) {
      yearPanelHeight = splitter.position - splitterHeight / 2;
      resourcesPanelHeight = availableSize.height - yearPanelHeight - splitterHeight;
    }

    yearPanel.htmlComp.setBounds(new Rectangle(insets.left, insets.top, availableSize.width, yearPanelHeight));
    resourcesPanel.htmlComp.setBounds(new Rectangle(insets.left, insets.top + splitterHeight, availableSize.width, resourcesPanelHeight));
  }

  protected _validateSplitterPosition(htmlComp: HtmlComponent, splitter: Splitter, availableSize: Dimension) {
    if (splitter.position === null) {
      // Set initial splitter position
      this._silentUpdateSpliterPosition(htmlComp, splitter, availableSize.height / 2);
    } else if (availableSize.height !== this._availableHeight) {
      // Window has been resized -> preserve relative splitter position
      this._silentUpdateSpliterPosition(htmlComp, splitter, availableSize.height * this._relativeSplitterPosition);
    }

    // Validate min and max splitter position
    let maxSplitterPosition = availableSize.height - CalendarSidebarLayout.MIN_SPLITTER_POSITION;
    if (splitter.position < CalendarSidebarLayout.MIN_SPLITTER_POSITION) {
      this._silentUpdateSpliterPosition(htmlComp, splitter, CalendarSidebarLayout.MIN_SPLITTER_POSITION);
    } else if (splitter.position > maxSplitterPosition) {
      this._silentUpdateSpliterPosition(htmlComp, splitter, Math.max(maxSplitterPosition, CalendarSidebarLayout.MIN_SPLITTER_POSITION));
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
