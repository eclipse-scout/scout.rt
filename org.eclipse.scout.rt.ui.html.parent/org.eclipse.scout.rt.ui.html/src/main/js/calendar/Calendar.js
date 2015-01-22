// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Calendar = function() {
  scout.Calendar.parent.call(this);

  // main elements
  this.$container;
  this.$header;
  this.$range;
  this.$modes;
  this.$grid;

  // mode
  this.DAY = 1;
  this.WEEK = 2;
  this.MONTH = 3;
  this.WORK = 4;

  // additional modes; show be stored in model
  this.showYear = true;
  this.showList = true;
};
scout.inherits(scout.Calendar, scout.ModelAdapter);

/* -- basics -------------------------------------------- */
scout.Calendar.prototype.init = function(model, session) {
  scout.Calendar.parent.prototype.init.call(this, model, session);
  this._setDisplayMode(3, scout.dates.parseJsonDate(this.selectedDate));
};

scout.Calendar.prototype._render = function($parent) {
  // basics, layout etc.
  this._$parent = $parent;
  this.$container = this._$parent.appendDiv('calendar').attr('id', this._generateId('calendar'));
  this.$container.addClass('not-implemented'); // TODO Remove once implemented
  var layout = new scout.CalendarLayout(this);
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(layout);
  this.htmlComp.pixelBasedSizing = false;

  // main elements
  this.$header = this.$container.appendDiv('calendar-header');
  this.$grid = this.$header.appendDiv('calendar-grid');

  // header contains all controls
  this.$range = this.$header.appendDiv('calendar-range');
  this.$range.appendDiv('calendar-minus-minus', '--').click(this._onClickMinus.bind(this));
  this.$range.appendDiv('calendar-minus', '-').click(this._onClickMinusMinus.bind(this));
  this.$range.appendDiv('calendar-plus', '+').click(this._onClickPlus.bind(this));
  this.$range.appendDiv('calendar-plus-plus', '++').click(this._onClickPlusPlus.bind(this));
  this.$range.appendDiv('calendar-select', 'Januar 2015');
  this.$range.appendDiv('calendar-today', 'Heute').click(this._onClickToday.bind(this));

  // ... and modes
  this.$modes = this.$header.appendDiv('calendar-modes');
  this.$modes.appendDiv('calendar-day calendar-mode', 'Tag').data('mode', this.DAY).click(this._onClickMode.bind(this));
  this.$modes.appendDiv('calendar-work calendar-mode', 'Arbeitswoche').data('mode', this.WORK).click(this._onClickMode.bind(this));
  this.$modes.appendDiv('calendar-week calendar-mode', 'Woche').data('mode', this.WEEK).click(this._onClickMode.bind(this));
  this.$modes.appendDiv('calendar-month calendar-mode', 'Monat').data('mode', this.MONTH).click(this._onClickMode.bind(this));

  this.$modes.appendDiv('calendar-year', 'Jahr').click(this._onClickYear.bind(this));
  this.$modes.appendDiv('calendar-list', 'Liste').click(this._onClickList.bind(this));

  // render the grid
  this._renderDisplayMode();
};

scout.Calendar.prototype._onClickMinus = function(event) {};
scout.Calendar.prototype._onClickMinusMinus = function(event) {};
scout.Calendar.prototype._onClickPlus = function(event) {};
scout.Calendar.prototype._onClickPlusPlus = function(event) {};
scout.Calendar.prototype._onClickToday = function(event) {};

scout.Calendar.prototype._onClickMode = function(event) {
  this._setDisplayMode($(event.target).data('mode'), scout.dates.parseJsonDate(this.selectedDate));
  this._renderDisplayMode();
};

scout.Calendar.prototype._onClickYear = function(event) {
  this.showYear = !this.showYear;
  this._renderDisplayMode();
};
scout.Calendar.prototype._onClickList = function(event) {
  this.showList = !this.showList;
  this._renderDisplayMode();
};

/* --  set and render grid -------------------------------------------- */

scout.Calendar.prototype._renderViewRange = function() {
};

scout.Calendar.prototype._setDisplayMode = function(mode, showDate) {
  showDate = showDate || new Date();

  var year = showDate.getFullYear(),
    month = showDate.getMonth(),
    date = showDate.getDate(),
    day = showDate.getDay(),
    start,
    end;

  // find start and end of displayed components based on showDate
  if (mode === this.DAY) {
    start = new Date(year, month, date);
    end = new Date(year, month, date + 1);
  } else if (mode === this.WEEK) {
    start = new Date(year, month, date - day + 1);
    end = new Date(year, month, date - day + 5);
  } else if (mode === this.MONTH) {
    start = new Date(year, month, 1);
    end = new Date(year, month + 1, 0);
  } else if (mode === this.WORK) {
    start = new Date(year, month, date - day + 1);
    end = new Date(year, month, date - day + 7);
  }

  // set range...
  this.session.send(this.id, 'setVisibleRange', {
    dateRange: {
      // TODO Calendar | Get initial dates from date selector
      from: scout.dates.toJsonDate(start),
      to: scout.dates.toJsonDate(end)
    }
  });

  // ... and mode
  this.session.send(this.id, 'setDisplayMode', {
    displayMode: mode
  });
};

scout.Calendar.prototype._renderDisplayMode = function() {
  // select mode
  $("[data-mode]", this.$modes).select(false);
  $("[data-mode='" + this.displayMode +"']", this.$modes).select(true);

  // draw grid for mode
  if (this.displayMode === this.DAY) {
    this._gridDays();
  } else if (this.displayMode ===  this.WEEK) {
    this._gridDays();
  } else if (this.displayMode === this.MONTH) {
    this._gridMonth();
  } else if (this.displayMode ===  this.WORK) {
    this._gridDays();
  }

  // select and draw year
  $(".calendar-year", this.$modes).select(this.showYear);
  this._gridYear(this.showYear);

  // select and draw list
  $(".calendar-list", this.$modes).select(this.showList);
  this._gridList(this.showList);
};

scout.Calendar.prototype._gridDays = function() {
  this.$grid.empty();
  this.$grid.appendDiv('', 'Day');
};

scout.Calendar.prototype._gridMonth = function() {
  this.$grid.empty();
  this.$grid.appendDiv('', 'Month');
};

scout.Calendar.prototype._gridYear = function() {
  this.$grid.appendDiv('', 'Year');
};

scout.Calendar.prototype._gridList = function() {
  this.$grid.appendDiv('', 'List');
};


/* -----------  Scout Stuff ---------------------


scout.Calendar.prototype._renderViewRange = function() {
>>>>>>> 22cd3e7 html ui: calender prototype
};

scout.Calendar.prototype._renderComponents = function() {
};

scout.Calendar.prototype._renderSelectedComponent = function() {
};

scout.Calendar.prototype._renderDisplayMode = function() {
};

scout.Calendar.prototype._renderDisplayCondensed = function() {
};

scout.Calendar.prototype._renderTitle = function() {
};


scout.Calendar.prototype._renderSelectedDate = function() {
};

scout.Calendar.prototype._renderLoadInProgress = function() {
};

scout.Calendar.prototype._renderStartHour = function() {
};

scout.Calendar.prototype._renderEndHour = function() {
};

scout.Calendar.prototype._renderUseOverflowCells = function() {
};

scout.Calendar.prototype._renderShowDisplayModeSelection = function() {
};

scout.Calendar.prototype._renderMarkNoonHour = function() {
};

scout.Calendar.prototype._renderMarkOutOfMonthDays = function() {
};

scout.Calendar.prototype.onModelAction = function(event) {
  if (event.type === 'calendarChanged') {
    this._onCalendarChanged(event);
  } else if (event.type === 'calendarChangedBatch') {
    this._onCalendarChangedBatch(event.batch);
  } else {
    $.log.warn('Model event not handled. Widget: scout.Calendar. Event: ' + event.type + '.');
  }
};

scout.Calendar.prototype._onCalendarChanged = function(calendarEvent) {
  // TODO Calendar | Implement --> see JsonCalendarEvent
};

scout.Calendar.prototype._onCalendarChangedBatch = function(calendarEventBatch) {
  // TODO Calendar | Implement --> see JsonCalendarEvent (calendarEventBatch is an array of CalendarEvent)
};
*/
