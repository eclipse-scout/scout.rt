// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Calendar = function() {
  scout.Calendar.parent.call(this);

  // main elements (reduce?)
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

  // dates
  this.selected = new Date();
  this.show = new Date();
  this.start = new Date();
  this.end = new Date();

  // additional modes; should be stored in model
  this.showYear = false;
  this.showList = false;
};
scout.inherits(scout.Calendar, scout.ModelAdapter);

/* -- basics -------------------------------------------- */
scout.Calendar.prototype.init = function(model, session) {
  scout.Calendar.parent.prototype.init.call(this, model, session);
};

scout.Calendar.prototype._render = function($parent) {
  // basics, layout etc.
  this._$parent = $parent;
  this.$container = this._$parent.appendDiv('calendar').attr('id', this._generateId('calendar'));
  var layout = new scout.CalendarLayout(this);
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(layout);
  this.htmlComp.pixelBasedSizing = false;

  // main elements
  this.$header = this.$container.appendDiv('calendar-header');
  this.$year = this.$container.appendDiv('calendar-year');
  this.$grid = this.$container.appendDiv('calendar-grid');
  this.$list = this.$container.appendDiv('calendar-list');

  // header contains all controls
  this.$range = this.$header.appendDiv('calendar-range');
  this.$range.appendDiv('calendar-minus').click(this._onClickMinus.bind(this));
  this.$range.appendDiv('calendar-plus').click(this._onClickPlus.bind(this));
  this.$range.appendDiv('calendar-select', 'Januar 2015');

  // ... and modes
  this.$commands = this.$header.appendDiv('calendar-commands');
  this.$commands.appendDiv('calendar-today').click(this._onClickToday.bind(this));
  this.$commands.appendDiv('calendar-separator');
  this.$commands.appendDiv('calendar-mode-day calendar-mode').attr('data-mode', this.DAY).click(this._onClickMode.bind(this));
  this.$commands.appendDiv('calendar-mode-work calendar-mode').attr('data-mode', this.WORK).click(this._onClickMode.bind(this));
  this.$commands.appendDiv('calendar-mode-week calendar-mode').attr('data-mode', this.WEEK).click(this._onClickMode.bind(this));
  this.$commands.appendDiv('calendar-mode-month calendar-mode').attr('data-mode', this.MONTH).click(this._onClickMode.bind(this));
  this.$commands.appendDiv('calendar-separator');
  this.$commands.appendDiv('calendar-toggle-year').click(this._onClickYear.bind(this));
  this.$commands.appendDiv('calendar-toggle-list').click(this._onClickList.bind(this));

  // render the dates
  for (var w = 0; w < 7; w++) {
    var $w = this.$grid.appendDiv();

    if (w===0) {
      $w.addClass('calendar-week-header');
    } else {
      $w.addClass('calendar-week');
    }

    for (var d = 0; d < 8; d++) {
      var $d = $w.appendDiv();

      if (w === 0 && d === 0) {
        $d.addClass('calendar-week-name');
      } else if (w === 0 && d > 0) {
        $d.addClass('calendar-day-name');
      } else if (w > 0 && d === 0) {
        $d.addClass('calendar-week-name');
      } else if (w > 0 && d > 0) {
        $d.addClass('calendar-day')
          .data('day', d).data('week', w)
          .click(this._onClickDay.bind(this));
      }
    }
  }

  // show grid
  this._renderDisplayMode();
};

scout.Calendar.prototype._onClickMinus = function(event) {};

scout.Calendar.prototype._onClickPlus = function(event) {};

scout.Calendar.prototype._onClickToday = function(event) {
};

scout.Calendar.prototype._onClickMode = function(event) {
  this._setDisplayMode($(event.target).data('mode'), scout.dates.parseJsonDate(this.selectedDate));
};

scout.Calendar.prototype._onClickYear = function(event) {
  this.showYear = !this.showYear;
  this._renderDisplayMode();
};
scout.Calendar.prototype._onClickList = function(event) {
  this.showList = !this.showList;
  this._renderDisplayMode();
};

scout.Calendar.prototype._onClickDay = function(event) {
  $('.selected', this.$grid).select(false);
  $(event.target).select(true);
  this.selected = $(event.target).data('date');
};


/* --  set and render grid -------------------------------------------- */

scout.Calendar.prototype._setDisplayMode = function(mode) {
  var year = this.show.getFullYear(),
    month = this.show.getMonth(),
    date = this.show.getDate(),
    day = this.show.getDay();

  // find start and end of displayed components based on showDate
  if (mode === this.DAY) {
    this.start = new Date(year, month, date);
    this.end = new Date(year, month, date + 1);
  } else if (mode === this.WEEK) {
    this.start = new Date(year, month, date - day + 1);
    this.end = new Date(year, month, date - day + 7);
  } else if (mode === this.MONTH) {
    this.start = new Date(year, month, 1);
    this.end = new Date(year, month + 1, 0);
  } else if (mode === this.WORK) {
    this.start = new Date(year, month, date - day + 1);
    this.end = new Date(year, month, date - day + 5);
  }

  // set range...
  this.session.send(this.id, 'setVisibleRange', {
    dateRange: {
      // TODO Calendar | Get initial dates from date selector
      from: scout.dates.toJsonDate(this.start),
      to: scout.dates.toJsonDate(this.end)
    }
  });

  // ... and mode
  this.session.send(this.id, 'setDisplayMode', {
    displayMode: mode
  });
};

scout.Calendar.prototype._renderDisplayMode = function() {
  // select mode
  $('.calendar-mode', this.$commands).select(false);
  $("[data-mode='" + this.displayMode +"']", this.$modes).select(true);

  // layout
  this.layoutSize();
  this.layoutLabel();
  this.layoutComponents();
};

scout.Calendar.prototype.layoutSize = function() {
  // reset animation sizes
  $('div', this.$container).removeData(['new-width', 'new-height']);

  // layout grid
  var $selected = $('.selected', this.$grid),
    headerH = $('.calendar-week-header', this.$grid).height(),
    gridH = this.$grid.height(),
    gridW = this.$container.width();

  // select and draw year
  $(".calendar-toggle-year", this.$modes).select(this.showYear);
  if (this.showYear) {
    this.$year.data('new-width', 200);
    gridW -= 200;
  } else {
    this.$year.data('new-width', 0);
  }

  // select and draw list
  $(".calendar-toggle-list", this.$modes).select(this.showList);
  if (this.showList) {
    this.$list.data('new-width', 200);
    gridW -= 200;
  } else {
    this.$list.data('new-width', 0);
  }

  // basic grid width
  this.$grid.data('new-width', gridW);

  // layout week
  if (this.displayMode === this.DAY || this.displayMode ===  this.WEEK || this.displayMode ===  this.WORK) {
    $('.calendar-week', this.$grid).data('new-height', 0);
    $selected.parent().data('new-height', gridH - headerH);
  } else {
    $('.calendar-week', this.$grid).data('new-height', parseInt((gridH - headerH) / 6, 10));
  }

  // layout days
  if (this.displayMode === this.DAY) {
    $('.calendar-day-name, .calendar-day', this.$grid)
      .data('new-width', 0);
    $('.calendar-day-name:nth-child(' + ($selected.index() + 1) + '), .calendar-day:nth-child(' + ($selected.index() + 1) +')', this.$grid)
      .data('new-width', gridW - headerH);
  } else if (this.displayMode ===  this.WORK) {
    $('.calendar-day-name, .calendar-day', this.$grid)
      .data('new-width', 0);
    $('.calendar-day-name:nth-child(-n+6), .calendar-day:nth-child(-n+6)', this.$grid)
      .data('new-width', parseInt((gridW - headerH) / 5, 10));
  } else if (this.displayMode === this.MONTH || this.displayMode ===  this.WEEK) {
    $('.calendar-day-name, .calendar-day', this.$grid)
      .data('new-width', parseInt((gridW - headerH) / 7, 10));
  }

  // animate old to new sizes
  $('div', this.$container).each(function() {
    var $e = $(this),
      w = $e.data('new-width'),
      h = $e.data('new-height');

    if (w !== undefined && w !== $e.width()) {
      $e.animateAVCSD('width', w);
    }

    if (h !== undefined && h!== $e.height()) {
      $e.animateAVCSD('height', h);
    }
  });
};

scout.Calendar.prototype.layoutLabel = function() {
  // init vars
  var text,
    $selected = $('.selected', this.$grid);

  // set range text
  if (this.displayMode === this.DAY) {
    text = this._dateFormat(this.start, 'd. MMMM yyyy');
  } else if (this.displayMode ===  this.WORK || this.displayMode ===  this.WEEK) {
    if (this.start.getMonth() === this.end.getMonth()) {
      text = this._dateFormat(this.start, 'd.') + ' bis ' + this._dateFormat(this.end, 'd. MMMM yyyy');
    } else if (this.start.getFullYear() === this.end.getFullYear()) {
      text = this._dateFormat(this.start, 'd. MMMM') + ' bis ' + this._dateFormat(this.end, 'd. MMMM yyyy');
    } else {
      text = this._dateFormat(this.start, 'd. MMMM') + ' bis ' + this._dateFormat(this.end, 'd. MMMM yyyy');
    }
  } else if (this.displayMode === this.MONTH ) {
    text = this._dateFormat(this.start, 'MMMM yyyy');
  }

  $('.calendar-select', this.$range).text(text);

  // set dayname (based on width of shown column)
  var $days = $('.calendar-day-name', this.$grid),
    weekdays;

  if ($days.eq($selected.index() - 1).data('new-width') > 100) {
      weekdays = this.session.locale.dateFormat.symbols.weekdaysOrdered;
  } else {
      weekdays = this.session.locale.dateFormat.symbols.weekdaysShortOrdered;
  }

  $days.each(function (index) {
    $(this).text(weekdays[index]);
  });

  // set day date and mark selected one
  var $dates = $('.calendar-day', this.$grid),
    firstDate = new Date(this.start.valueOf());

  for (var offset = 0; offset < 42; offset++){
    firstDate.setDate(firstDate.getDate() - 1);
    if ((firstDate.getDay() === 0) && firstDate.getMonth() !== this.start.getMonth()){
      break;
    }
  }

  for (var w = 0; w < 6; w++) {
    for (var d = 0; d < 7; d++) {
      var cl = '';
      firstDate.setDate(firstDate.getDate() + 1);

      if ((firstDate.getDay() === 6) || (firstDate.getDay() === 0)) {
        cl = firstDate.getMonth() !== this.start.getMonth() ? ' weekend-out' : ' weekend';
      }
      else {
        cl = firstDate.getMonth() !== this.start.getMonth() ? ' out' : '';
      }

      if (scout.dates.isSameDay(firstDate, new Date())){
        cl += ' now';
      }

      if (scout.dates.isSameDay(firstDate, this.selected)){
        cl += ' selected';
      }

      text = this._dateFormat(firstDate, 'dd');
      $dates.eq(w * 7 + d)
        .addClass(cl)
        .attr('data-day-name', text)
        .data('date', new Date(firstDate.valueOf()));
    }
  }

  // set weekname or day schedule
  $('.calendar-week-axis, .calendar-week-task', this.$grid).remove();

  if (this.displayMode === this.MONTH ) {
    $('.calendar-week-name').each(function (index) {
      if (index > 0) {
        var $e = $(this);
        $e.text('KW ' + scout.dates.weekInYear($e.next().data('date')));
      }
    });
  } else {
    $('.calendar-week-name').text('');
    $selected.parent().appendDiv('calendar-week-axis').attr('data-axis-name', '08:00').css('top', this._dayPosition(8));
    $selected.parent().appendDiv('calendar-week-axis').attr('data-axis-name', '12:00').css('top', this._dayPosition(12));
    $selected.parent().appendDiv('calendar-week-axis').attr('data-axis-name', '13:00').css('top', this._dayPosition(13));
    $selected.parent().appendDiv('calendar-week-axis').attr('data-axis-name', '17:00').css('top', this._dayPosition(17));
    $selected.parent().appendDiv('calendar-week-task').attr('data-axis-name', 'Tasks').css('top', this._dayPosition(-1));
  }

};

scout.Calendar.prototype.layoutComponents = function() {
  $('.calendar-component', this.$grid).remove();

  var countTask = 5;

  for (var i = 0; i < this.components.length; i++) {
    var c = this.components[i],
      $day,
      $component,
      fromDate,
      toDate;

    if (typeof c === 'object') {
      $.l(c, c.fromDate, c.toDate, c.fullDay, c.cell.text, c.cell.tooltipText);

      fromDate = scout.dates.parseJsonDate(c.fromDate);
      toDate = scout.dates.parseJsonDate(c.toDate);

      for (var j = 0; j < c.coveredDays.length; j++) {
        // var d = scout.dates.parseJsonDate(c.coveredDays[j]);

        $day = this._findDay(fromDate);
        $component = $day.appendDiv('calendar-component', c.cell.text)
          .hover(this._onMouseenter.bind(this), this._onMouseleave.bind(this));

        if (this.displayMode !== this.MONTH) {
          $component.addClass('component-day');

          if (c.fullDay) {
            $component.css('position', 'absolute');
            $component.css('top', 'calc(' + this._dayPosition(-1)  + ')' + countTask + 'px)');
            countTask += 5;

          } else {
            // scout.dates.isSameDay(

            $component.css('position', 'absolute');
            $component.css('top', '20%');
            $component.css('height', '40%');

           // for mouse over!
            $component.attr('data-from', '08:45');
            $component.attr('data-to', '16:00');
          }
        }
      }
    }
  }
};

scout.Calendar.prototype._onMouseenter = function (date) {
  var $e = $(event.target),
    $w = $e.parent().parent();

  $w.appendDiv('calendar-week-axis-over').attr('data-axis-name', $e.attr('data-from')).css('top', $e.css('top'));

  $w.appendDiv('calendar-week-axis-over').attr('data-axis-name', $e.attr('data-to')).css('top', parseInt($e.css('top'), 10) + parseInt($e.css('height'), 10));
};

scout.Calendar.prototype._onMouseleave = function (date) {
  $('.calendar-week-axis-over', this.$grid).remove();
};


/* -- helper -------------------------------------------- */

scout.Calendar.prototype._dateFormat = function(date, pattern) {
  var d = new Date(date.valueOf());
  var dateFormat = new scout.DateFormat(this.session.locale, pattern);
  return dateFormat.format(d);
};

scout.Calendar.prototype._dayPosition = function(hour) {
  if (hour < 0) {
    return '85%';
  } else if (hour < 8) {
    return parseInt(hour / 8 * 10 + 5, 10) + '%';
  } else if (hour < 12) {
    return parseInt((hour - 8) / 4 * 25 + 15, 10) + '%';
  } else if (hour < 13) {
    return parseInt((hour - 12) / 1 * 5 + 40, 10) + '%';
  } else if (hour < 17) {
    return parseInt((hour - 13 ) / 4 * 25 + 45, 10) + '%';
  } else if (hour < 24) {
    return parseInt((hour - 17) / 7 * 10 + 70, 10) + '%';
  }
};

scout.Calendar.prototype._findDay = function (date) {
  var $day;

  $('.calendar-day', this.grid)
    .each(function () {
        if (scout.dates.isSameDay($(this).data('date'), date)) {
          $day = $(this);
          return;
        }
    });

  return $day;
};



/* -----------  Scout Stuff ---------------------*/

scout.Calendar.prototype._renderComponents = function() {
  //this.layoutComponents();
};

scout.Calendar.prototype._renderLoadInProgress = function() {
};


scout.Calendar.prototype._renderViewRange = function() {
};


/*
scout.Calendar.prototype._renderViewRange = function() {
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
