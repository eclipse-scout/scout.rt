scout.PlannerHeader = function(session) {
  scout.PlannerHeader.parent.call(this);

  this.availableDisplayModes = [];
  this.session = session;
  this._addEventSupport();
};
scout.inherits(scout.PlannerHeader, scout.Widget);

scout.PlannerHeader.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('planner-header');
  this.$range = this.$container.appendDiv('planner-range');
  this.$range.appendDiv('planner-previous').click(this._onPreviousClick.bind(this));
  this.$range.appendDiv('planner-today', this.session.text('CalendarToday')).click(this._onTodayClick.bind(this));
  this.$range.appendDiv('planner-next').click(this._onNextClick.bind(this));
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
  var DISPLAY_MODE = scout.Planner.DisplayMode;
  this.$commands.empty();

  this.$commands.appendDiv('planner-today').click(this._onTodayClick.bind(this));
  if (this.availableDisplayModes.indexOf(DISPLAY_MODE.DAY) > -1) {
    this.$commands.appendDiv('planner-mode-day planner-mode', this.session.text('CalendarDay')).attr('data-mode', DISPLAY_MODE.DAY).click(this._onDisplayModeClick.bind(this));
  }
  if (this.availableDisplayModes.indexOf(DISPLAY_MODE.WORK) > -1) {
    this.$commands.appendDiv('planner-mode-work planner-mode', this.session.text('CalendarWork')).attr('data-mode', DISPLAY_MODE.WORK).click(this._onDisplayModeClick.bind(this));
  }
  if (this.availableDisplayModes.indexOf(DISPLAY_MODE.WEEK) > -1) {
    this.$commands.appendDiv('planner-mode-week planner-mode', this.session.text('CalendarWeek')).attr('data-mode', DISPLAY_MODE.WEEK).click(this._onDisplayModeClick.bind(this));
  }
  if (this.availableDisplayModes.indexOf(DISPLAY_MODE.MONTH) > -1) {
    this.$commands.appendDiv('planner-mode-month planner-mode', this.session.text('CalendarMonth')).attr('data-mode', DISPLAY_MODE.MONTH).click(this._onDisplayModeClick.bind(this));
  }
  if (this.availableDisplayModes.indexOf(DISPLAY_MODE.CALENDAR_WEEK) > -1) {
    this.$commands.appendDiv('planner-mode-cw planner-mode', this.session.text('CalendarCalendarWeek')).attr('data-mode', DISPLAY_MODE.CALENDAR_WEEK).click(this._onDisplayModeClick.bind(this));
  }
  if (this.availableDisplayModes.indexOf(DISPLAY_MODE.YEAR) > -1) {
    this.$commands.appendDiv('planner-mode-year planner-mode last', this.session.text('CalendarYear')).attr('data-mode', DISPLAY_MODE.YEAR).click(this._onDisplayModeClick.bind(this));
  }
  this.$commands.appendDiv('planner-toggle-year').click(this._onYearClick.bind(this));
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
