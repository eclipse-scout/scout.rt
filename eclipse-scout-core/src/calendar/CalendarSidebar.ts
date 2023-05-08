/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {CalendarSidebarLayout, HtmlComponent, InitModelOf, ResourcesPanel, scout, Splitter, Widget, YearPanel} from '../index';

export class CalendarSidebar extends Widget {
  yearPanel: YearPanel;
  splitter: Splitter;
  resourcesPanel: ResourcesPanel;

  constructor() {
    super();

    this.yearPanel = null;
    this.splitter = null;
    this.resourcesPanel = null;
  }

  override init(model: InitModelOf<this>) {
    super.init(model);

    this.yearPanel = scout.create(YearPanel, {
      parent: this
    });
    this.splitter = scout.create(Splitter, {
      parent: this,
      $anchor: this.$container,
      splitHorizontal: false,
      cssClass: 'line'
    });
    this.resourcesPanel = scout.create(ResourcesPanel, {
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
    this.resourcesPanel.render();
  }

  _onSplitterPositionChange(event) {
    this.invalidateLayoutTree(false);
  }
}
