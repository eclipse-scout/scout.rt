// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.ActivityMap = function() {
  scout.ActivityMap.parent.call(this);

  // main elements
  this.$container;
  this.$header;
  this.$range;
  this.$modes;
  this.$year;
  this.$grid;
  this.$list;

  // mode
  this.DAY = 1;
  this.WEEK = 2;
  this.MONTH = 3;
  this.WORK = 4;

  // additional modes; should be stored in model
  this.showYear = false;
  this.showList = false;

  // adapter
  //this._addAdapterProperties(['days', 'selectedDays']);
};
scout.inherits(scout.ActivityMap, scout.ModelAdapter);

scout.ActivityMap.prototype.init = function(model, session) {
  scout.ActivityMap.parent.prototype.init.call(this, model, session);
};

scout.ActivityMap.prototype._render = function($parent) {
  //basics, layout etc.
  this._$parent = $parent;
  this.$container = this._$parent.appendDiv('activity-map').attr('id', this._generateId('activity-map'));
  var layout = new scout.ActivityMapLayout(this);
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(layout);
  this.htmlComp.pixelBasedSizing = false;

  // main elements
  this.$header = this.$container.appendDiv('activity-map-header');
  this.$year = this.$container.appendDiv('activity-map-year-container').appendDiv('activity-map-year');
  this.$grid = this.$container.appendDiv('activity-map-grid');
  this.$list = this.$container.appendDiv('activity-map-list-container').appendDiv('activity-map-list');

  // header contains all controls
  this.$range = this.$header.appendDiv('activity-map-range');
  this.$range.appendDiv('activity-map-minus').click(this._onClickMinus.bind(this));
  this.$range.appendDiv('activity-map-plus').click(this._onClickPlus.bind(this));
  this.$range.appendDiv('activity-map-select');

  // ... and modes
  this.$commands = this.$header.appendDiv('activity-map-commands');
  this.$commands.appendDiv('activity-map-today').click(this._onClickToday.bind(this));
  this.$commands.appendDiv('activity-map-separator');
  this.$commands.appendDiv('activity-map-mode-day activity-map-mode').attr('data-mode', this.DAY).click(this._onClickMode.bind(this));
  this.$commands.appendDiv('activity-map-mode-work activity-map-mode').attr('data-mode', this.WORK).click(this._onClickMode.bind(this));
  this.$commands.appendDiv('activity-map-mode-week activity-map-mode').attr('data-mode', this.WEEK).click(this._onClickMode.bind(this));
  this.$commands.appendDiv('activity-map-mode-month activity-map-mode').attr('data-mode', this.MONTH).click(this._onClickMode.bind(this));
  this.$commands.appendDiv('activity-map-separator');
  this.$commands.appendDiv('activity-map-toggle-year').click(this._onClickYear.bind(this));
  this.$commands.appendDiv('activity-map-toggle-list').click(this._onClickList.bind(this));

  // should be done by server?
  this.displayMode = this.MONTH;
  this._updateModel();
  this._updateScreen();
};

/* -- basics, events -------------------------------------------- */

scout.ActivityMap.prototype._onClickMinus = function(event) {
  var year = this.selected.getFullYear(),
    month = this.selected.getMonth(),
    date = this.selected.getDate(),
    day = this.selected.getDay();

  // find new selected date
  if (this.displayMode === this.DAY) {
    this.selected = new Date(year, month, date - 1);
  } else if (this.displayMode === this.WEEK || this.displayMode === this.WORK) {
    this.selected = new Date(year, month, date - 7);
  } else if (this.displayMode === this.MONTH) {
    this.selected = new Date(year, month - 1, date);
  }

  // update calendar
  this._updateModel();
  this._updateScreen();
};

scout.ActivityMap.prototype._onClickPlus = function(event) {
  var year = this.selected.getFullYear(),
    month = this.selected.getMonth(),
    date = this.selected.getDate(),
    day = this.selected.getDay();

  // find new selected date
  if (this.displayMode === this.DAY) {
    this.selected = new Date(year, month, date + 1);
  } else if (this.displayMode === this.WEEK || this.displayMode === this.WORK) {
    this.selected = new Date(year, month, date + 7);
  } else if (this.displayMode === this.MONTH) {
    this.selected = new Date(year, month + 1, date);
  }

  // update calendar
  this._updateModel();
  this._updateScreen();
};

scout.ActivityMap.prototype._onClickToday = function(event) {
  // new selected date
  this.selected = new Date();

  // update calendar
  this._updateModel();
  this._updateScreen();
};

scout.ActivityMap.prototype._onClickMode = function(event) {
  // set new mode
  this.displayMode = $(event.target).data('mode');

  // update calendar
  this._updateModel();
  this._updateScreen();
};

scout.ActivityMap.prototype._onClickYear = function(event) {
  // set flag
  this.showYear = !this.showYear;

  // update screen
  this._updateScreen();
};
scout.ActivityMap.prototype._onClickList = function(event) {
  // set flag
  this.showList = !this.showList;

  // update screen
  this._updateScreen();
};

scout.ActivityMap.prototype._onClickDay = function(event) {
  var $clicked = $(event.currentTarget);

  // select clicked day
  $('.selected', this.$grid).select(false);
  $clicked.select(true);
  this.selected = $clicked.data('date');

  // change selected day in year picker
  this.colorYear();

  // if day list shown, redraw it
  if (this.showList) {
    this.$list.empty();
    this.drawList();
  }

};

/* --  set display mode and range ------------------------------------- */

scout.ActivityMap.prototype._updateModel = function() {
};

scout.ActivityMap.prototype._updateScreen = function() {
  this._renderActivityRows();
};



scout.ActivityMap.prototype._renderActivityRows = function() {
  var i, $row;
  for (i = 0; i < this.rows.length; i++) {
    $row = this._build$ActivityRow(this.rows[i], this.$data);
    $row.appendTo(this.$data);
  }
};

scout.ActivityMap.prototype._build$ActivityRow = function(row) {
  var i, $cell,
    $row = $.makeDiv('activity-row');
  $row.appendSpan().text('resourceId: ' + row.resourceId);
  for (i = 0; i < row.cells.length; i++) {
    $cell = this._build$ActivityCell(row.cells[i]);
    $cell.appendTo($row);
  }
  return $row;
};

scout.ActivityMap.prototype._build$ActivityCell = function(cell) {
  var i,
    $cell = $.makeDiv('activity-cell');
  $cell.text('minorValue: ' + cell.minorValue + ', majorValue: ' + cell.majorValue);
  return $cell;
};


/* -- year, draw and color ---------------------------------------- */

scout.ActivityMap.prototype.drawYear = function() {
  // init vars
  var year = this.start.getFullYear(),
    first, $month, day, $day;

  // set title
  var $title = this.$year.appendDiv('year-title').data('year', year);

  // append 3 years
  $title.appendDiv('year-title-item', year - 1)
    .data('year-diff', -1)
    .click(this._onYearClick.bind(this));

  $title.appendDiv('year-title-item selected', year);

  $title.appendDiv('year-title-item', year + 1)
    .data('year-diff', +1)
    .click(this._onYearClick.bind(this));

  // add months and days
  for (var month = 0; month < 12; month++) {
    first = new Date(year, month, 1);
    $month = this.$year.appendDiv('year-month').attr('data-title', this._dateFormat(first, 'MMMM'));
    for (var d = 1; d <= 31; d++) {
      day = new Date(year, month, d);

      // stop if day is already out of range
      if (day.getMonth() !== month) {
        break;
      }

      // add div per day
      $day = $month.appendDiv('year-day', d).data('date', day);

      // first day has margin depending on weekday
      if (d === 1) {
        $day.css('margin-left', ((day.getDay() + 6) % 7) * $day.outerWidth());
      }
    }
  }

  // bind events for days divs
  $('.year-day', this.$year)
   .click(this._onYearDayClick.bind(this))
   .hover(this._onYearHoverIn.bind(this), this._onYearHoverOut.bind(this));
};

scout.ActivityMap.prototype.colorYear = function() {
  // color is only needed if visible
  if (!this.showYear) {
    return;
  }

  // remove color information
  $('.year-day.year-range, .year-day.year-range-day', this.$year).removeClass('year-range year-range-day');

  // loop all days and colorize based on range and selected
  var that = this,
    $day, date;

  $('.year-day', this.$year).each( function (){
    $day = $(this);
    date = $day.data('date');

    if (that.displayMode !== that.DAY && date >= that.start && date <= that.end) {
      $day.addClass('year-range');
    }

    if (scout.dates.isSameDay(date, that.selected)) {
      $day.addClass('year-range-day');
    }
  });
};

/* -- year, events ---------------------------------------- */

scout.ActivityMap.prototype._onYearClick = function(event) {
  // prepare calculation
  var diff = $(event.target).data('year-diff'),
    year = this.selected.getFullYear(),
    month = this.selected.getMonth(),
    date = this.selected.getDate();

  // find new selected date
  this.selected = new Date(year + diff, month, date);

  // update calendar
  this._updateModel();
  this._updateScreen();
};


scout.ActivityMap.prototype._onYearDayClick = function(event) {
  // new selected day
  this.selected = $('.year-hover-day', this.$year).data('date');

  // update calendar
  this._updateModel();
  this._updateScreen();
};


scout.ActivityMap.prototype._onYearHoverIn = function(event) {
  // init vars
  var $day = $(event.target),
    date1 = $day.data('date'),
    year = date1.getFullYear(),
    month = date1.getMonth(),
    date = date1.getDate(),
    day = (date1.getDay() + 6) % 7,
    that = this,
    startHover,
    endHover,
    $day2, date2;

  // find hover based on mode
  if (this.displayMode === this.DAY) {
    startHover = new Date(year, month, date);
    endHover = new Date(year, month, date);
  } else if (this.displayMode === this.WEEK) {
    startHover = new Date(year, month, date - day);
    endHover = new Date(year, month, date - day + 6);
  } else if (this.displayMode === this.MONTH) {
    startHover = new Date(year, month, 1);
    endHover = new Date(year, month + 1, 0);
  } else if (this.displayMode === this.WORK) {
    startHover = new Date(year, month, date - day);
    endHover = new Date(year, month, date - day + 4);

    // in case of work week: selected date has to be opart of range
    if (date1 > endHover) {
      date1 = endHover;
    }
  }

  // loop days and colorize based on hover start and hover end
  $('.year-day', this.$year).each( function (){
    $day2 = $(this);
    date2 = $day2.data('date');

    if (date2 >= startHover && date2 <= endHover) {
      $day2.addClass('year-hover');
    } else {
      $day2.removeClass('year-hover');
    }

    if (scout.dates.isSameDay(date1, date2)) {
      $day2.addClass('year-hover-day');
    }

  });
};

scout.ActivityMap.prototype._onYearHoverOut = function(event) {
  // remove all hover effects
  $('.year-day.year-hover, .year-day.year-hover-day', this.$year).removeClass('year-hover year-hover-day');
};



/*
scout.ActivityMap.prototype._renderDays = function() {
};

scout.ActivityMap.prototype._renderWorkDayCount = function() {
};

scout.ActivityMap.prototype._renderWorkDaysOnly = function() {
};

scout.ActivityMap.prototype._renderFirstHourOfDay = function() {
};

scout.ActivityMap.prototype._renderLastHourOfDay = function() {
};

scout.ActivityMap.prototype._renderIntradayInterval = function() {
};

scout.ActivityMap.prototype._renderPlanningMode = function() {
};

scout.ActivityMap.prototype._renderResourceIds = function() {
};

scout.ActivityMap.prototype._renderSelectedBeginTime = function() {
};

scout.ActivityMap.prototype._renderSelectedEndTime = function() {
};

scout.ActivityMap.prototype._renderSelectedResourceIds = function() {
};

scout.ActivityMap.prototype._renderSelectedActivityCell = function() {
};

scout.ActivityMap.prototype._renderTimeScale = function() {
};

scout.ActivityMap.prototype._renderDrawSections = function() {
};

scout.ActivityMap.prototype.onModelAction = function(event) {
  if (event.type === 'activityMapChanged') {
    this._onActivityMapChanged(event);
  } else {
    $.log.warn('Model event not handled. Widget: scout.Calendar. Event: ' + event.type + '.');
  }
};

scout.ActivityMap.prototype._onActivityMapChanged = function(event) {
  // TODO ActivityMap | Implement --> see JsonActivityMapEvent
};
*/
