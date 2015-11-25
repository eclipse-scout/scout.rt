/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.PlannerHeader = function() {
  scout.PlannerHeader.parent.call(this);

  this.availableDisplayModes = [];
  this._addEventSupport();
};
scout.inherits(scout.PlannerHeader, scout.Widget);

scout.PlannerHeader.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('planner-header');
  this.$range = this.$container.appendDiv('planner-range');
  this.$range.appendDiv('planner-previous').on('click', this._onPreviousClick.bind(this));
  this.$range.appendDiv('planner-today', this.session.text('ui.CalendarToday')).on('click', this._onTodayClick.bind(this));
  this.$range.appendDiv('planner-next').on('click', this._onNextClick.bind(this));
  this.$range.appendDiv('planner-select');
  this.$commands = this.$container.appendDiv('planner-commands');
  this._renderAvailableDisplayModes();
  this._renderDisplayMode();
  this._renderVisible();
};

scout.PlannerHeader.prototype.setAvailableDisplayModes = function(displayModes) {
  this.availableDisplayModes = displayModes;

  if (this.rendered) {
    this._renderAvailableDisplayModes();
  }
};

scout.PlannerHeader.prototype._renderAvailableDisplayModes = function() {
  var displayMode = scout.Planner.DisplayMode;
  this.$commands.empty();

  this.$commands.appendDiv('planner-today').on('click', this._onTodayClick.bind(this));

  if (this.availableDisplayModes.length > 1) {
    if (this.availableDisplayModes.indexOf(displayMode.DAY) > -1) {
      this.$commands.appendDiv('planner-mode', this.session.text('ui.CalendarDay'))
        .attr('data-mode', displayMode.DAY)
        .on('click', this._onDisplayModeClick.bind(this));
    }
    if (this.availableDisplayModes.indexOf(displayMode.WORK_WEEK) > -1) {
      this.$commands.appendDiv('planner-mode', this.session.text('ui.CalendarWorkWeek'))
        .attr('data-mode', displayMode.WORK_WEEK)
        .on('click', this._onDisplayModeClick.bind(this));
    }
    if (this.availableDisplayModes.indexOf(displayMode.WEEK) > -1) {
      this.$commands.appendDiv('planner-mode', this.session.text('ui.CalendarWeek'))
        .attr('data-mode', displayMode.WEEK)
        .on('click', this._onDisplayModeClick.bind(this));
    }
    if (this.availableDisplayModes.indexOf(displayMode.MONTH) > -1) {
      this.$commands.appendDiv('planner-mode', this.session.text('ui.CalendarMonth'))
        .attr('data-mode', displayMode.MONTH)
        .on('click', this._onDisplayModeClick.bind(this));
    }
    if (this.availableDisplayModes.indexOf(displayMode.CALENDAR_WEEK) > -1) {
      this.$commands.appendDiv('planner-mode', this.session.text('ui.CalendarCalendarWeek'))
        .attr('data-mode', displayMode.CALENDAR_WEEK)
        .on('click', this._onDisplayModeClick.bind(this));
    }
    if (this.availableDisplayModes.indexOf(displayMode.YEAR) > -1) {
      this.$commands.appendDiv('planner-mode', this.session.text('ui.CalendarYear'))
        .attr('data-mode', displayMode.YEAR)
        .on('click', this._onDisplayModeClick.bind(this));
    }
  }

  this.$commands.children('.planner-mode').last().addClass('last'); // draw right border
  this.$commands.appendDiv('planner-toggle-year').on('click', this._onYearClick.bind(this));
};

scout.PlannerHeader.prototype.setDisplayMode = function(displayMode) {
  this.displayMode = displayMode;

  if (this.rendered) {
    this._renderDisplayMode();
  }
};

scout.PlannerHeader.prototype._renderDisplayMode = function() {
  $('.planner-mode', this.$commands).select(false);
  $('[data-mode="' + this.displayMode + '"]', this.$commands).select(true);
};

scout.PlannerHeader.prototype.setVisible = function(visible) {
  this.visible = visible;

  if (this.rendered) {
    this._renderVisible();
  }
};

scout.PlannerHeader.prototype._renderVisible = function() {
  this.$container.setVisible(this.visible);
};

scout.PlannerHeader.prototype._onTodayClick = function(event) {
  this.trigger('todayClick');
};

scout.PlannerHeader.prototype._onNextClick = function(event) {
  this.trigger('nextClick');
};

scout.PlannerHeader.prototype._onPreviousClick = function(event) {
  this.trigger('previousClick');
};

scout.PlannerHeader.prototype._onYearClick = function(event) {
  this.trigger('yearClick');
};

scout.PlannerHeader.prototype._onDisplayModeClick = function(event) {
  var displayMode = $(event.target).data('mode');
  this.setDisplayMode(displayMode);
  this.trigger('displayModeClick', {
    displayMode: this.displayMode
  });
};
