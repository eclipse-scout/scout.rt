/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CalendarSidebarSplitterEventMap, CalendarSidebarSplitterModel, InitModelOf, Splitter, SplitterMoveEndEvent, SplitterMoveStartEvent} from '../index';

export class CalendarSidebarSplitter extends Splitter implements CalendarSidebarSplitterModel {
  declare model: CalendarSidebarSplitterModel;
  declare eventMap: CalendarSidebarSplitterEventMap;
  declare self: CalendarSidebarSplitter;

  collapsed: boolean;
  collapsedLabel: string;

  $splitterBox: JQuery;
  $collapsedLabel: JQuery;
  $collapseIcon: JQuery;
  $collapseBorderLeft: JQuery;
  $collapseBorderRight: JQuery;

  protected _splitterMoveStartPosition: number;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this.on('moveStart', this._onSplitterMoveStart.bind(this));
    this.on('moveEnd', this._onSplitterMoveEnd.bind(this));
  }

  protected override _render() {
    super._render();

    this.$container.addClass('calendar-sidebar-splitter');
    this.$splitterBox = this.$container.appendDiv('splitter-box');
    this.$collapsedLabel = this.$container.appendDiv('collapsed-label');

    this.$collapseBorderLeft = this.$splitterBox.appendDiv('group-collapse-border');
    this.$collapseIcon = this.$splitterBox.appendDiv('group-collapse-icon');
    this.$collapseBorderRight = this.$splitterBox.appendDiv('group-collapse-border');
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderCollapsed();
    this._renderCollapsedLabel();
  }

  setCollapsed(collapsed: boolean) {
    this.setProperty('collapsed', collapsed);
  }

  protected _renderCollapsed() {
    this.$container.toggleClass('collapsed', this.collapsed);
  }

  setCollapsedLabel(label: string) {
    this.setProperty('collapsedLabel', label);
  }

  protected _renderCollapsedLabel() {
    this.$collapsedLabel.text(this.collapsedLabel);
  }

  protected _onSplitterMoveStart(event: SplitterMoveStartEvent) {
    this._splitterMoveStartPosition = event.position;
  }

  protected _onSplitterMoveEnd(event: SplitterMoveEndEvent) {
    // No previous location available
    if (!this._splitterMoveStartPosition) {
      return;
    }

    let endPos = event.position;
    // When the difference is not too big, trigger a click event
    if (Math.abs(this._splitterMoveStartPosition - endPos) < 2) {
      this.trigger('splitterClick');
    }
    this._splitterMoveStartPosition = null;
  }
}
