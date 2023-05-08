/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, CalendarSidebar, HtmlComponent, Rectangle} from '../index';

export class CalendarSidebarLayout extends AbstractLayout {
  widget: CalendarSidebar;
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

    let splitterConstraints = JSON.stringify({
      availableSize: availableSize,
      splitterVisible: splitter.isVisible()
    });
    if (splitterConstraints !== this._splitterConstraints) {
      // If container size has changed, ensure the splitter position is valid (e.g. when restoring a maximized window)
      let splitterPosition = splitter.position;
      // Set initial splitter position (depending on the container width, therefore must be done during the layout)
      if (splitterPosition === null) {
        splitterPosition = availableSize.height / 2;
      }
      // Set positions without triggering this layout again
      htmlComp.suppressInvalidate = true;
      try {
        splitter.setPosition(splitterPosition);
      } finally {
        htmlComp.suppressInvalidate = false;
      }
      this._splitterConstraints = splitterConstraints;
    }

    // Set sizes
    let splitterHeight = splitter.htmlComp.bounds().height;

    let yearPanelHeight = splitter.position - splitterHeight / 2;
    yearPanel.htmlComp.setBounds(new Rectangle(insets.left, insets.top, availableSize.width, insets.top + yearPanelHeight));

    let resourcesPanelHeight = availableSize.height - yearPanelHeight - splitterHeight / 2;
    resourcesPanel.htmlComp.setBounds(new Rectangle(insets.left, insets.top + splitterHeight, availableSize.width, insets.top + resourcesPanelHeight));
  }
}
