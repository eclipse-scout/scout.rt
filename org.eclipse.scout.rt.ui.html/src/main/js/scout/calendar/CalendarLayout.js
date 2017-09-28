/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.CalendarLayout = function(calendar) {
  scout.CalendarLayout.parent.call(this);
  this.calendar = calendar;
  this.stacked = false;
  this.compacted = false;
};
scout.inherits(scout.CalendarLayout, scout.AbstractLayout);

scout.CalendarLayout.prototype.layout = function($container) {
  var height = 0, headerHeight = 0,
    $yearContainer = this.calendar._yearPanel.$container,
    $grid = this.calendar.$grid,
    $header = this.calendar.$header;

  height += $container.cssMarginTop() + $container.cssMarginBottom();
  $container.css('height', 'calc(100% - ' + height + 'px)');

  this.undoCompact();
  this.undoStack();
  if ($header[0].scrollWidth > $container.width()) {
    this.stack();
  }
  if ($header[0].scrollWidth > $container.width()) {
    this.compact();
  }

  headerHeight = $header.outerHeight(true);
  $yearContainer.css('height', 'calc(100% - ' + (headerHeight + $yearContainer.cssMarginY()) + 'px)');
  $grid.css('height', 'calc(100% - ' + (headerHeight + $grid.cssMarginY()) + 'px)');

  this.calendar.layoutSize();
  this.calendar.layoutYearPanel();
};

scout.CalendarLayout.prototype.compact = function() {
  if (this.compacted) {
    return;
  }
  var $headerRow2 = this.calendar.$headerRow2;
  this.calendar.$title.appendTo(this.calendar.$headerRow2);
  $headerRow2.show();
  this.compacted = true;
};

scout.CalendarLayout.prototype.undoCompact = function() {
  if (!this.compacted) {
    return;
  }
  var $headerRow2 = this.calendar.$headerRow2;
  this.calendar.$title.insertBefore(this.calendar.$commands);
  $headerRow2.hide();
  this.compacted = false;
};

scout.CalendarLayout.prototype.stack = function() {
  if (this.stacked) {
    return;
  }
  this.calendar.$commands.children('.calendar-mode').hide();
  this.calendar.modesMenu.setVisible(true);
  this.stacked = true;
};

scout.CalendarLayout.prototype.undoStack = function() {
  if (!this.stacked) {
    return;
  }
  this.calendar.$commands.children('.calendar-mode').show();
  this.calendar.modesMenu.setVisible(false);
  this.stacked = false;
};
