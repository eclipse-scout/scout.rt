/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, Calendar} from '../index';

export default class CalendarLayout extends AbstractLayout {
  calendar: Calendar;
  stacked: boolean;
  compacted: boolean;
  compactWidth: number;

  constructor(calendar: Calendar) {
    super();
    this.calendar = calendar;
    this.stacked = false;
    this.compacted = false;
    this.compactWidth = 550;
  }

  override layout($container: JQuery) {
    let height = 0,
      headerHeight = 0,
      // @ts-ignore
      $yearContainer = this.calendar._yearPanel.$container,
      $grids = this.calendar.$grids,
      $listContainer = this.calendar.$listContainer,
      $header = this.calendar.$header;

    height += $container.cssMarginTop() + $container.cssMarginBottom();
    $container.css('height', 'calc(100% - ' + height + 'px)');

    this.undoCompact();
    this.undoStack();
    if ($header[0].scrollWidth > $container.width()) {
      this.stack();
    }
    if ($container.width() < this.compactWidth || $header[0].scrollWidth > $container.width()) {
      // Title may take a lot of space, make it always compact for small devices so it won't toggle when changing display mode or view range
      this.compact();
    }

    headerHeight = $header.outerHeight(true);
    $yearContainer.css('height', 'calc(100% - ' + (headerHeight + $yearContainer.cssMarginY()) + 'px)');
    $grids.css('height', 'calc(100% - ' + (headerHeight + $grids.cssMarginY()) + 'px)');
    $listContainer.css('height', 'calc(100% - ' + (headerHeight + $listContainer.cssMarginY()) + 'px)');

    this.calendar.layoutSize();
    this.calendar.layoutYearPanel();
  }

  compact() {
    if (this.compacted) {
      return;
    }
    let $headerRow2 = this.calendar.$headerRow2;
    this.calendar.$title.appendTo(this.calendar.$headerRow2);
    $headerRow2.show();
    this.compacted = true;
  }

  undoCompact() {
    if (!this.compacted) {
      return;
    }
    let $headerRow2 = this.calendar.$headerRow2;
    this.calendar.$title.insertBefore(this.calendar.$commands);
    $headerRow2.hide();
    this.compacted = false;
  }

  stack() {
    if (this.stacked) {
      return;
    }
    this.calendar.$commands.children('.calendar-mode').hide();
    this.calendar.modesMenu.setVisible(true);
    this.stacked = true;
  }

  undoStack() {
    if (!this.stacked) {
      return;
    }
    this.calendar.$commands.children('.calendar-mode').show();
    this.calendar.modesMenu.setVisible(false);
    this.stacked = false;
  }
}
