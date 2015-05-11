// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

// FIXME AWE: (calendar) check bug reported from Michael: switch month when items are still loading (async)
scout.Calendar = function() {
  scout.Calendar.parent.call(this);

  // main elements
  this.$container;
  this.$header;
  this.$range;
  this.$modes;
  this.$year;
  this.$grid;
  this.$list;
  this.$progress;

  // additional modes; should be stored in model
  this.showYear = false;
  this.showList = false;

  /**
   * The narrow view range is different from the regular view range.
   * It contains only dates that exactly match the requested dates,
   * the regular view range contains also dates from the first and
   * next month. The exact range is not sent to the server.
   */
  this._exactRange;

  this._$selectedComponent;
  this._tooltipDelay;

  this._addAdapterProperties(['components', 'menus', 'selectedComponent']);
};
scout.inherits(scout.Calendar, scout.ModelAdapter);


scout.Calendar.DisplayMode = {
  DAY: 1,
  WEEK: 2,
  MONTH: 3,
  WORK: 4 // FIXME AWE: (calendar) rename to WORKWEEK
};

/**
 * Used as a multiplier in date calculations back- and forward (in time).
 */
scout.Calendar.Direction = {
  BACKWARD: -1,
  FORWARD: 1
};

scout.Calendar.prototype._isDay = function() {
  return this.displayMode === scout.Calendar.DisplayMode.DAY;
};

scout.Calendar.prototype._isWeek = function() {
  return this.displayMode === scout.Calendar.DisplayMode.WEEK;
};

scout.Calendar.prototype._isMonth = function() {
  return this.displayMode === scout.Calendar.DisplayMode.MONTH;
};

scout.Calendar.prototype._isWork = function() {
  return this.displayMode === scout.Calendar.DisplayMode.WORK;
};

scout.Calendar.prototype.init = function(model, session) {
  scout.Calendar.parent.prototype.init.call(this, model, session);
  this._syncSelectedDate(model.selectedDate);
  this._exactRange = this._calcExactRange();
  this.viewRange = this._calcViewRange();

  // FIXME AWE: (calendar) improve this and do it client-side? currently we're playing
  // ping-pong with the UI because the client cannot determine its view-range itself
  this._sendViewRangeChanged();
};

scout.Calendar.prototype._syncSelectedDate = function(dateString) {
  this.selectedDate = scout.dates.create(dateString);
};

scout.Calendar.prototype._syncViewRange = function(viewRange) {
  this.viewRange = new scout.Range(
    scout.dates.create(viewRange.from),
    scout.dates.create(viewRange.to));
};

scout.Calendar.prototype._render = function($parent) {
  $.log.debug('(Calendar#_render)');

  var w, $w, d, $d,
    layout = new scout.CalendarLayout(this);

  this._$parent = $parent;
  this.$container = this._$parent.appendDiv('calendar').attr('id', this._generateId('calendar'));

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(layout);
  this.htmlComp.pixelBasedSizing = false;

  // main elements
  this.$header = this.$container.appendDiv('calendar-header');
  this.$year = this.$container.appendDiv('calendar-year-container').appendDiv('calendar-year');
  this.$grid = this.$container.appendDiv('calendar-grid');
  this.$list = this.$container.appendDiv('calendar-list-container').appendDiv('calendar-list');

  // header contains all controls
  this.$progress = this.$header.appendDiv('calendar-progress').text('Lade Kalender-Einträge...'); // FIXME AWE: (calendar) i18n
  this.$range = this.$header.appendDiv('calendar-range');
  this.$range.appendDiv('calendar-previous').click(this._onClickPrevious.bind(this));
  this.$range.appendDiv('calendar-next').click(this._onClickNext.bind(this));
  this.$range.appendDiv('calendar-select');

  // ... and modes
  this.$commands = this.$header.appendDiv('calendar-commands');
  this.$commands.appendDiv('calendar-today').click(this._onClickToday.bind(this));
  this.$commands.appendDiv('calendar-separator');
  this.$commands.appendDiv('calendar-mode-day calendar-mode').attr('data-mode', scout.Calendar.DisplayMode.DAY).click(this._onClickDisplayMode.bind(this));
  this.$commands.appendDiv('calendar-mode-work calendar-mode').attr('data-mode', scout.Calendar.DisplayMode.WORK).click(this._onClickDisplayMode.bind(this));
  this.$commands.appendDiv('calendar-mode-week calendar-mode').attr('data-mode', scout.Calendar.DisplayMode.WEEK).click(this._onClickDisplayMode.bind(this));
  this.$commands.appendDiv('calendar-mode-month calendar-mode').attr('data-mode', scout.Calendar.DisplayMode.MONTH).click(this._onClickDisplayMode.bind(this));
  this.$commands.appendDiv('calendar-separator');
  this.$commands.appendDiv('calendar-toggle-year').click(this._onClickYear.bind(this));
  this.$commands.appendDiv('calendar-toggle-list').click(this._onClickList.bind(this));

  // append the main grid
  for (w = 0; w < 7; w++) {
    $w = this.$grid.appendDiv();
    if (w === 0) {
      $w.addClass('calendar-week-header');
    } else {
      $w.addClass('calendar-week');
    }

    for (d = 0; d < 8; d++) {
      $d = $w.appendDiv();
      if (w === 0 && d === 0) {
        $d.addClass('calendar-week-name');
      } else if (w === 0 && d > 0) {
        $d.addClass('calendar-day-name');
      } else if (w > 0 && d === 0) {
        $d.addClass('calendar-week-name');
      } else if (w > 0 && d > 0) {
        // FIXME AWE: (calendar) we must also select the clicked day and update the model
        $d.addClass('calendar-day')
          .data('day', d)
          .data('week', w)
          .on('contextmenu', this._onDayContextMenu.bind(this));
      }
    }
  }

  // click event on all day and children elements
  $('.calendar-day', this.$grid).click(this._onClickDay.bind(this));
  this._updateScreen();
};

scout.Calendar.prototype._renderProperties = function() {
  this._renderComponents();
  this._renderLoadInProgress();
  this._renderMenus();
  this._renderDisplayMode();
  this._renderSelectedDate();
  this._renderViewRange();
};

scout.Calendar.prototype._renderComponents = function() {

  $.log.debug('(Calendar#_renderComponents) impl.');

  var i, j, c, itemId, $component, d, $day,
    fromDate, toDate,
    countTask = 5;

  // remove all existing items
  this._removeComponents();

  // main loop
  for (i = 0; i < this.components.length; i++) {
    c = this.components[i];
    fromDate = scout.dates.parseJsonDate(c.fromDate);
    toDate = scout.dates.parseJsonDate(c.toDate);

    // loop covered days
    for (j = 0; j < c.coveredDays.length; j++) {
      d = scout.dates.parseJsonDate(c.coveredDays[j]);

      // day in shown month?
      $day = this._findDay(d);

      // if not: continue
      if ($day === undefined) {
        continue;
      }

      // draw component
      if (!c.rendered) {
        c.render($day);
      }

      // adapt design if mode not month
      if (!this._isMonth()) {
        if (c.fullDay) {
          // task
          $component
            .addClass('component-task')
            .css('top', 'calc(' + this._dayPosition(-1) + '% + ' + countTask + 'px)');
          countTask += 25;

        } else {
          var fromHours = fromDate.getHours(),
            fromMinutes = fromDate.getMinutes(),
            toHours = toDate.getHours(),
            toMinutes = toDate.getMinutes();

          // appointment
          $component
            .addClass('component-day')
            .unbind('mouseenter mouseleave')
            .mouseenter(this._onComponentDayHoverIn.bind(this))
            .mouseleave(this._onComponentDayHoverOut.bind(this));

          // position and height depending on start and end date
          if (c.coveredDays.length === 1) {
            $component.css('top', this._dayPosition(fromHours + fromMinutes / 60) + '%')
              .css('height', this._dayPosition(toHours + toMinutes / 60) - this._dayPosition(fromHours + fromMinutes / 60) + '%');
          } else if (scout.dates.isSameDay(d, fromDate)) {
            $component.css('top', this._dayPosition(fromHours + fromMinutes / 60) + '%')
              .css('height', this._dayPosition(24) - this._dayPosition(fromHours + fromMinutes / 60) + '%')
              .addClass('component-open-bottom');
          } else if (scout.dates.isSameDay(d, toDate)) {
            $component.css('top', this._dayPosition(0) + '%')
              .css('height', this._dayPosition(fromHours + fromMinutes / 60) - this._dayPosition(0) + '%')
              .addClass('component-open-top');
          } else {
            $component.css('top', this._dayPosition(1) + '%')
              .css('height', this._dayPosition(24) - this._dayPosition(1) + '%')
              .addClass('component-open-top')
              .addClass('component-open-bottom');
          }
        }
      }
    }
  }

  this._arrangeComponents();
};

scout.Calendar.prototype._renderLoadInProgress = function() {
  $.log.debug('(Calendar#_renderLoadInProgress) impl.');
  this.$progress.setVisible(this.loadInProgress);
  if (this.loadInProgress) {
    scout.status.animateStatusMessage(this.$progress, 'Lade Kalender-Einträge...');
  }
};

scout.Calendar.prototype._renderViewRange = function() {
  $.log.debug('(Calendar#_renderViewRange) impl.');
};

scout.Calendar.prototype._renderDisplayMode = function() {
  $.log.debug('(Calendar#_renderDisplayMode) impl.');
};

scout.Calendar.prototype._renderSelectedDate = function() {
  $.log.debug('(Calendar#_renderSelectedDate) impl.');
};

scout.Calendar.prototype._renderMenus = function() {
  // FIXME AWE: (calendar) here we should update the menu-bar (see Table.js)
  $.log.debug('(Calendar#_renderMenus) impl.');
};

/* -- basics, events -------------------------------------------- */

scout.Calendar.prototype._onClickPrevious = function(event) {
  this._navigateDate(scout.Calendar.Direction.BACKWARD);
};

scout.Calendar.prototype._onClickNext = function(event) {
  this._navigateDate(scout.Calendar.Direction.FORWARD);
};

scout.Calendar.prototype._dateParts = function(date, modulo) {
  var parts = {
    year: date.getFullYear(),
    month: date.getMonth(),
    date: date.getDate(),
    day: date.getDay()
  };
  if (modulo) {
    parts.day = (date.getDay() + 6) % 7;
  }
  return parts;
};

scout.Calendar.prototype._navigateDate = function(direction) {
  this.selectedDate = this._calcSelectedDate(direction);
  this._updateModel();
};

// FIXME AWE: (calendar) discuss with C.GU - should this really be calculated in the UI?
scout.Calendar.prototype._calcSelectedDate = function(direction) {
  var p = this._dateParts(this.selectedDate),
    dayOperand = direction,
    weekOperand = direction * 7,
    monthOperand = direction;

  if (this._isDay()) {
    return new Date(p.year, p.month, p.date + dayOperand);
  } else if (this._isWeek() || this._isWork()) {
    return new Date(p.year, p.month, p.date + weekOperand);
  } else if (this._isMonth()) {
    return new Date(p.year, p.month + monthOperand, p.date);
  }
};

scout.Calendar.prototype._updateModel = function() {
  this._exactRange = this._calcExactRange();
  var newViewRange = this._calcViewRange();
  this._resetComponentMap(newViewRange);
  this.viewRange = newViewRange;
  this._sendModelChanged();
  this._updateScreen();
};

scout.Calendar.prototype._resetComponentMap = function(newViewRange) {
  var oldViewRange = this.viewRange;
  if (!newViewRange.equals(oldViewRange)) {
    $.log.debug('viewRange has changed to ' + newViewRange + '. Reset $componentMap cache');
    this._removeComponents();
    this._$componentMap = {};
  }
};

scout.Calendar.prototype._removeComponents = function() {
  $('.calendar-component', this.$grid).remove();
};

/**
 * Calculates exact date range of displayed components based on selected-date.
 */
scout.Calendar.prototype._calcExactRange = function() {
  var from, to,
    p = this._dateParts(this.selectedDate, true);

  if (this._isDay()) {
    from = new Date(p.year, p.month, p.date);
    to = new Date(p.year, p.month, p.date + 1);
  } else if (this._isWeek()) {
    from = new Date(p.year, p.month, p.date - p.day);
    to = new Date(p.year, p.month, p.date - p.day + 6);
  } else if (this._isMonth()) {
    from = new Date(p.year, p.month, 1);
    to = new Date(p.year, p.month + 1, 0);
  } else if (this._isWork()) {
    from = new Date(p.year, p.month, p.date - p.day);
    to = new Date(p.year, p.month, p.date - p.day + 4);
  }

  return new scout.Range(from, to);
};

/**
 * Calculates the view-range, which is what the user sees in the UI.
 * The view-range is wider than the exact-range in the monthly mode,
 * as it contains also dates from the previous and next month.
 */
scout.Calendar.prototype._calcViewRange = function() {
  if (this._isMonth()) {
    var viewFrom = _calcViewFromDate(this._exactRange.from),
      viewTo = _calcViewToDate(viewFrom);
    return new scout.Range(viewFrom, viewTo);
  } else {
    return this._exactRange;
  }

  // FIXME AWE: (calendar) do date calculations in java with a proper Calendar impl.?
  // --> use dates.js here
  function _calcViewFromDate(fromDate) {
    var i, tmpDate = new Date(fromDate.valueOf());
    for (i = 0; i < 42; i++){
      tmpDate.setDate(tmpDate.getDate() - 1);
      if ((tmpDate.getDay() === 0) && tmpDate.getMonth() !== fromDate.getMonth()) {
        return tmpDate;
      }
    }
    throw new Error('failed to calc viewFrom date');
  }

  function _calcViewToDate(fromDate) {
    var i, tmpDate = new Date(fromDate.valueOf());
    for (i = 0; i < 42; i++) {
      tmpDate.setDate(tmpDate.getDate() + 1);
    }
    return tmpDate;
  }
};

scout.Calendar.prototype._onClickToday = function(event) {
  this.selectedDate = new Date();
  this._updateModel();
};

scout.Calendar.prototype._onClickDisplayMode = function(event) {
  this.displayMode = $(event.target).data('mode');
  if (this._isWork()) {
    // change date if selectedDate is on a weekend
    var p = this._dateParts(this.selectedDate, true);
    if (p.day > 4) {
      this.selectedDate = new Date(p.year, p.month, p.date - p.day + 4);
    }
  }
  this._updateModel();
};

scout.Calendar.prototype._onClickYear = function(event) {
  this.showYear = !this.showYear;
  this._updateScreen();
};
scout.Calendar.prototype._onClickList = function(event) {
  this.showList = !this.showList;
  this._updateScreen();
//  if (this.showList) {
//    this.$list.empty();
//    this._renderComponentPanel();
//  }
};

scout.Calendar.prototype._onClickDay = function(event) {
  var $clicked = $(event.currentTarget);

  // select clicked day
  $('.selected', this.$grid).select(false);
  $clicked.select(true);
  this.selectedDate = $clicked.data('date');

  // change selected day in year picker
  this.colorYear();

  // if day list shown, redraw it
  if (this.showList) {
    this.$list.empty();
    this._renderComponentPanel();
  }

};

/* --  set display mode and range ------------------------------------- */

scout.Calendar.prototype._sendModelChanged = function() {
  var data = {
    viewRange: this._jsonViewRange(),
    selectedDate: scout.dates.toJsonDate(this.selectedDate),
    displayMode: this.displayMode
  };
  this.session.send(this.id, 'modelChanged', data);
};

scout.Calendar.prototype._sendViewRangeChanged = function() {
  this.session.send(this.id, 'viewRangeChanged', {
    viewRange: this._jsonViewRange()});
};

scout.Calendar.prototype._sendSelectionChanged = function() {
  this.session.send(this.id, 'selectionChanged', {
    date: scout.dates.toJsonDate(this.selectedDate),
    componentId: this.selectedComponent.id});
};

scout.Calendar.prototype._jsonViewRange = function() {
  return {
    from: scout.dates.toJsonDate(this.viewRange.from),
    to: scout.dates.toJsonDate(this.viewRange.to)
  };
};

scout.Calendar.prototype._updateScreen = function() {
  $.log.info('(Calendar#_updateScreen)');

  // select mode
  $('.calendar-mode', this.$commands).select(false);
  $("[data-mode='" + this.displayMode +"']", this.$modes).select(true);

  // remove selected day
  $('.selected', this.$grid).select(false);

  // layout grid
  this.layoutLabel();
  this.layoutSize();
// XXX
//  this._renderComponents();
  this.layoutAxis();

  // if year shown and changed, redraw year
  if (this.selectedDate.getFullYear() !== $('.year-title', this.$year).data('year') && this.showYear) {
    this.$year.empty();
    this.drawYear();
  }

  // if list shown and changed, redraw year
  if (this.showList) {
    this.$list.empty();
    this._renderComponentPanel();
  }

  this.colorYear();
};

scout.Calendar.prototype.layoutSize = function() {
  // reset animation sizes
  $('div', this.$container).removeData(['new-width', 'new-height']);

  // init vars
  var $selected = $('.selected', this.$grid),
    headerH = $('.calendar-week-header', this.$grid).height(),
    gridH = this.$grid.height(),
    gridW = this.$container.width();

  // show or hide year
  $('.calendar-toggle-year', this.$modes).select(this.showYear);
  if (this.showYear) {
    this.$year.parent().data('new-width', 270);
    gridW -= 270;
  } else {
    this.$year.parent().data('new-width', 0);
  }

  // show or hide work list
  $(".calendar-toggle-list", this.$modes).select(this.showList);
  if (this.showList) {
    this.$list.parent().data('new-width', 270);
    gridW -= 270;
  } else {
    this.$list.parent().data('new-width', 0);
  }

  // basic grid width
  this.$grid.data('new-width', gridW);

  // layout week
  if (this._isDay() || this._isWeek() || this._isWork()) {
    $('.calendar-week', this.$grid).data('new-height', 0);
    $selected.parent().data('new-height', gridH - headerH);
  } else {
    $('.calendar-week', this.$grid).data('new-height', parseInt((gridH - headerH) / 6, 10));
  }

  // layout days
  if (this._isDay()) {
    $('.calendar-day-name, .calendar-day', this.$grid)
      .data('new-width', 0);
    $('.calendar-day-name:nth-child(' + ($selected.index() + 1) + '), .calendar-day:nth-child(' + ($selected.index() + 1) +')', this.$grid)
      .data('new-width', gridW - headerH);
  } else if (this._isWork()) {
    $('.calendar-day-name, .calendar-day', this.$grid)
      .data('new-width', 0);
    $('.calendar-day-name:nth-child(-n+6), .calendar-day:nth-child(-n+6)', this.$grid)
      .data('new-width', parseInt((gridW - headerH) / 5, 10));
  } else if (this._isMonth() || this._isWeek()) {
    $('.calendar-day-name, .calendar-day', this.$grid)
      .data('new-width', parseInt((gridW - headerH) / 7, 10));
  }

  // set day-name (based on width of shown column)
  var width = this.$container.width(),
    weekdays;

  if (this._isDay()) {
    width /= 1;
  } else if (this._isWork()) {
    width /= 5;
  } else if  (this._isWeek()) {
    width /= 7;
  } else if (this._isMonth()) {
    width /= 7;
  }

  if (width > 100) {
      weekdays = this.session.locale.dateFormat.symbols.weekdaysOrdered;
  } else {
      weekdays = this.session.locale.dateFormat.symbols.weekdaysShortOrdered;
  }

  $('.calendar-day-name', this.$grid).each(function (index) {
    $(this).text(weekdays[index]);
  });

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
  var text, $dates,
    $selected = $('.selected', this.$grid),
    exFrom = this._exactRange.from,
    exTo = this._exactRange.to;

  // set range text
  if (this._isDay()) {
    text = this._format(exFrom, 'd. MMMM yyyy');
  } else if (this._isWork() || this._isWeek()) {
    if (exFrom.getMonth() === exTo.getMonth()) {
      text = this._format(exFrom, 'd.') + ' bis ' + this._format(exTo, 'd. MMMM yyyy');
    } else if (exFrom.getFullYear() === exTo.getFullYear()) {
      text = this._format(exFrom, 'd. MMMM') + ' bis ' + this._format(exTo, 'd. MMMM yyyy');
    } else {
      text = this._format(exFrom, 'd. MMMM yyyy') + ' bis ' + this._format(exTo, 'd. MMMM yyyy');
    }
  } else if (this._isMonth() ) {
    text = this._format(exFrom, 'MMMM yyyy');
  }
  $('.calendar-select', this.$range).text(text);

  // prepare to set all day date and mark selected one
  $dates = $('.calendar-day', this.$grid);

  var w, d, cssClass,
    fromDate = this.viewRange.from,
    date = new Date(fromDate.valueOf());

  // loop all days and set value and class
  for (w = 0; w < 6; w++) {
    for (d = 0; d < 7; d++) {
      cssClass = '';
      if ((date.getDay() === 6) || (date.getDay() === 0)) {
        cssClass = date.getMonth() !== fromDate.getMonth() ? ' weekend-out' : ' weekend';
      }
      else {
        cssClass = date.getMonth() !== fromDate.getMonth() ? ' out' : '';
      }
      if (scout.dates.isSameDay(date, new Date())) {
        cssClass += ' now';
      }
      if (scout.dates.isSameDay(date, this.selectedDate)) {
        cssClass += ' selected';
      }
      text = this._format(date, 'dd');
      $dates.eq(w * 7 + d)
        .removeClass('weekend-out weekend out selected now')
        .addClass(cssClass)
        .attr('data-day-name', text)
        .data('date', new Date(date.valueOf()));
      date.setDate(date.getDate() + 1);
    }
  }
};

scout.Calendar.prototype.layoutAxis = function() {
  var $e, $selected = $('.selected', this.$grid);

  // remove old axis
  $('.calendar-week-axis, .calendar-week-task', this.$grid).remove();

  // set weekname or day schedule
  if (this._isMonth()) {
    $('.calendar-week-name').each(function (index) {
      if (index > 0) {
        $e = $(this);
        $e.text('KW ' + scout.dates.weekInYear($e.next().data('date')));
      }
    });
  } else {
    $('.calendar-week-name').text('');
    $selected.parent().appendDiv('calendar-week-axis').attr('data-axis-name', '08:00').css('top', this._dayPosition(8) + '%');
    $selected.parent().appendDiv('calendar-week-axis').attr('data-axis-name', '12:00').css('top', this._dayPosition(12) + '%');
    $selected.parent().appendDiv('calendar-week-axis').attr('data-axis-name', '13:00').css('top', this._dayPosition(13) + '%');
    $selected.parent().appendDiv('calendar-week-axis').attr('data-axis-name', '17:00').css('top', this._dayPosition(17) + '%');
    $selected.parent().appendDiv('calendar-week-task').attr('data-axis-name', 'Tasks').css('top', this._dayPosition(-1) + '%');
  }
};

/* -- year, draw and color ---------------------------------------- */

scout.Calendar.prototype.drawYear = function() {
  var first, month, $month, d, day, $day,
    year = this.viewRange.from.getFullYear(),
    $title = this.$year.appendDiv('year-title').data('year', year);

  // append 3 years
  $title.appendDiv('year-title-item', year - 1)
    .data('year-diff', -1)
    .click(this._onYearClick.bind(this));

  $title.appendDiv('year-title-item selected', year);

  $title.appendDiv('year-title-item', year + 1)
    .data('year-diff', +1)
    .click(this._onYearClick.bind(this));

  // add months and days
  for (month = 0; month < 12; month++) {
    first = new Date(year, month, 1);
    $month = this.$year.appendDiv('year-month').attr('data-title', this._format(first, 'MMMM'));
    for (d = 1; d <= 31; d++) {
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

scout.Calendar.prototype.colorYear = function() {
  // color is only needed if visible
  if (!this.showYear) {
    return;
  }

  // remove color information
  $('.year-day.year-range, .year-day.year-range-day', this.$year).removeClass('year-range year-range-day');

  // loop all days and colorize based on range and selected
  var that = this,
    $day, date;

  $('.year-day', this.$year).each(function () {
    $day = $(this);
    date = $day.data('date');

    if (!that._isDay() && date >= that._exactRange.from && date <= that._exactRange.to) {
      $day.addClass('year-range');
    }

    if (scout.dates.isSameDay(date, that.selectedDate)) {
      $day.addClass('year-range-day');
    }
  });
};

/* -- year, events ---------------------------------------- */

scout.Calendar.prototype._onYearClick = function(event) {
  var diff = $(event.target).data('year-diff'),
    year = this.selectedDate.getFullYear(),
    month = this.selectedDate.getMonth(),
    date = this.selectedDate.getDate();
  this.selectedDate = new Date(year + diff, month, date);
  this._updateModel();
};

scout.Calendar.prototype._onYearDayClick = function(event) {
  this.selectedDate = $('.year-hover-day', this.$year).data('date');
  this._updateModel();
};

scout.Calendar.prototype._onYearHoverIn = function(event) {
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
  if (this._isDay()) {
    startHover = new Date(year, month, date);
    endHover = new Date(year, month, date);
  } else if (this._isWeek()) {
    startHover = new Date(year, month, date - day);
    endHover = new Date(year, month, date - day + 6);
  } else if (this._isMonth()) {
    startHover = new Date(year, month, 1);
    endHover = new Date(year, month + 1, 0);
  } else if (this._isWork()) {
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

// remove all hover effects
scout.Calendar.prototype._onYearHoverOut = function(event) {
  $('.year-day.year-hover, .year-day.year-hover-day', this.$year).removeClass('year-hover year-hover-day');
};

/**
 * Renders the panel on the left, showing all components of the selected date.
 */
scout.Calendar.prototype._renderComponentPanel = function() {
  var $c, i,
    $selected = $('.selected', this.$grid),
    $components = $selected.children('.calendar-component:not(.clone)');

  // sort based on screen position
  $components.sort(this._sortTop);

  // set title to selected day
  this.$list.appendDiv('list-title', this._format($selected.data('date'), 'd. MMMM yyyy'));

  // show all components of selected day, add links and set full text
  for (i = 0; i < $components.length; i++) {
    $c = $components.eq(i);
    $c.clone()
      .appendTo(this.$list)
      .css('width', '')
      .css('height', '')
      .css('top', '')
      .css('left', '')
      .html($c.data('component')._description())
      .appendDiv('component-link', 'öffnen');
  }
};

/* -- components, events-------------------------------------------- */

scout.Calendar.prototype._selectedComponentChanged = function(component) {
  if (this.selectedComponent) {
    this.selectedComponent.setSelected(false);
  }
  component.setSelected(true);
  this.selectedComponent = component;
  this._sendSelectionChanged();
};

scout.Calendar.prototype._onDayContextMenu = function(event) {
  this._showContextMenu(event, 'Calendar.EmptySpace');
};

scout.Calendar.prototype._onComponentDayHoverIn = function(event) {
  var $comp = $(event.currentTarget),
    oldHeight = $comp.outerHeight(),
    newHeight;

  // set to new values
  $comp
    .data('old-height', $comp[0].style.height)
    .data('old-html', $comp.html())
    .html($comp.data('component')._description())
    .css('height', 'auto')
    .appendDiv('component-link', 'öffnen');


  // animate, if usefull
  newHeight = $comp.height();
  if (oldHeight < newHeight) {
    $comp.css('height', oldHeight + 'px');
    $comp.animateAVCSD('height', newHeight + 'px');
  } else {
    $comp.css('height', oldHeight + 'px');
  }
};


// restore element
// FIXME CRU: sometime fails?
scout.Calendar.prototype._onComponentDayHoverOut = function(event) {
  var $comp = $(event.currentTarget);
  $comp
    .html($comp.data('old-html'))
    .animateAVCSD('height', $comp.data('old-height'));
};

/* -- components, arrangement------------------------------------ */

scout.Calendar.prototype._arrangeComponents = function() {
  var k, $day, $children,
    $days = $('.calendar-day', this.grid);

  for (k = 0; k < $days.length; k++) {
    $day = $days.eq(k);
    $children = $day.children('.calendar-component:not(.component-task)');

    if (this._isMonth() && $children.length > 2) {
      $day.addClass('many-items');
    } else if (!this._isMonth() && $children.length > 1) {
      // sort based on screen position
      $children.sort(this._sortTop);

      // logical placement
      this._arrangeComponentInitialX($children);
      this._arrangeComponentInitialW($children);
      this._arrangeComponentFindPlacement($children);

      // screen placement
      this._arrangeComponentSetPlacement($children);
    }
  }
};

scout.Calendar.prototype._arrangeComponentInitialX = function($children) {
  var i, j, $child, $test, stackX;
  for (i = 0; i < $children.length; i++) {
    $child = $children.eq(i);
    stackX = 0;
    for (j = 0; j < i; j++) {
      $test = $children.eq(j);
      if (this._intersect($child, $test)) {
        stackX =  $test.data('stackX') + 1;
      }
    }
    $child.data('stackX', stackX);
  }
};

scout.Calendar.prototype._arrangeComponentInitialW = function($children) {
  var i, stackX, stackMaxX = 0;
  for (i = 0; i < $children.length; i++) {
    stackX = $children.eq(i).data('stackX');
    if (stackMaxX < stackX) {
      stackMaxX = stackX;
    }
  }
  $children.data('stackW', stackMaxX + 1);
};

scout.Calendar.prototype._arrangeComponentFindPlacement = function($children) {
  // FIXME CRU: placement may be improved, test cases needed
  // 1: change x if column on the left side free
  // 2: change w if place on the right side not used
  // -> then find new w (just use _arrangeComponentInitialW)
};

scout.Calendar.prototype._arrangeComponentSetPlacement = function($children) {
  var i, $child, stackX, stackW;

  // loop and place based on data
  for (i = 0; i < $children.length; i++) {
    $child = $children.eq(i);
    stackX = $child.data('stackX');
    stackW = $child.data('stackW');

    // make last element smaller
    if (stackX < stackW - 1) {
      $child
        .css('width', 'calc(' + (100 / stackW) + '% - 7px)')
        .css('left', 'calc(' + (stackX * 100 / stackW) + '% +  7px)');
    } else {
      $child
        .css('width', 'calc(' + (100 / stackW) + '% - 14px)')
        .css('left', 'calc(' + (stackX * 100 / stackW) + '% +  7px)');
    }
  }
};

/* -- helper ---------------------------------------------------- */

scout.Calendar.prototype._dayPosition = function(hour) {
  if (hour < 0) {
    return 85;
  } else if (hour < 8) {
    return parseInt(hour / 8 * 10 + 5, 10);
  } else if (hour < 12) {
    return parseInt((hour - 8) / 4 * 25 + 15, 10);
  } else if (hour < 13) {
    return parseInt((hour - 12) / 1 * 5 + 40, 10);
  } else if (hour < 17) {
    return parseInt((hour - 13 ) / 4 * 25 + 45, 10);
  } else if (hour <= 24) {
    return parseInt((hour - 17) / 7 * 10 + 70, 10);
  }
};

scout.Calendar.prototype._hourToNumber = function(hour) {
  var splits = hour.split(':');
  return parseFloat(splits[0]) + parseFloat(splits[1]) / 60;
};

scout.Calendar.prototype._findDay = function (date) {
  // FIXME CRU: tuning
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

scout.Calendar.prototype._intersect = function ($e1, $e2) {
  var comp1 = $e1.data('component'),
    comp2 = $e2.data('component'),
    top1 = this._hourToNumber(this._format(scout.dates.parseJsonDate(comp1.fromDate), 'HH:mm')),
    bottom1 = this._hourToNumber(this._format(scout.dates.parseJsonDate(comp1.toDate), 'HH:mm')),
    top2 = this._hourToNumber(this._format(scout.dates.parseJsonDate(comp2.fromDate), 'HH:mm')),
    bottom2 = this._hourToNumber(this._format(scout.dates.parseJsonDate(comp2.toDate), 'HH:mm'));
  return (top1 >= top2 && top1 <= bottom2) || (bottom1 >= top2 && bottom1 <= bottom2);
};

scout.Calendar.prototype._sortTop = function (a, b) {
  return parseInt($(a).offset().top, 10) >  parseInt($(b).offset().top, 10);
};

scout.Calendar.prototype._format = function(date, pattern) {
  return scout.dates.format(date, this.session.locale, pattern);
};
