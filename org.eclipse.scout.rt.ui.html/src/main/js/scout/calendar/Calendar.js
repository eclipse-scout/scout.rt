// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

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

  // mode
  this.DAY = 1;
  this.WEEK = 2;
  this.MONTH = 3;
  this.WORK = 4;

  // dates
  this.selected = new Date();
  this.start = new Date();
  this.end = new Date();

  // additional modes; should be stored in model
  this.showYear = false;
  this.showList = false;
  this._addAdapterProperties(['components', 'selectedComponent']);
};
scout.inherits(scout.Calendar, scout.ModelAdapter);


/* -- basics, create divs ------------------------------------------------ */
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
  this.$year = this.$container.appendDiv('calendar-year-container').appendDiv('calendar-year');
  this.$grid = this.$container.appendDiv('calendar-grid');
  this.$list = this.$container.appendDiv('calendar-list-container').appendDiv('calendar-list');

  // header contains all controls
  this.$range = this.$header.appendDiv('calendar-range');
  this.$range.appendDiv('calendar-minus').click(this._onClickMinus.bind(this));
  this.$range.appendDiv('calendar-plus').click(this._onClickPlus.bind(this));
  this.$range.appendDiv('calendar-select');

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

  // append the main grid
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
          .data('day', d).data('week', w);
      }
    }
  }

  //click event on all day and children elements
  $('.calendar-day', this.$grid).click(this._onClickDay.bind(this));

  // should be done by server?
  this.displayMode = this.MONTH;
  this._updateModel();
  this._updateScreen();
};

/* -- basics, events -------------------------------------------- */

scout.Calendar.prototype._onClickMinus = function(event) {
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

scout.Calendar.prototype._onClickPlus = function(event) {
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

scout.Calendar.prototype._onClickToday = function(event) {
  // new selected date
  this.selected = new Date();

  // update calendar
  this._updateModel();
  this._updateScreen();
};

scout.Calendar.prototype._onClickMode = function(event) {
  // set new mode
  this.displayMode = $(event.target).data('mode');

  // update calendar
  this._updateModel();
  this._updateScreen();
};

scout.Calendar.prototype._onClickYear = function(event) {
  // set flag
  this.showYear = !this.showYear;

  // update screen
  this._updateScreen();
};
scout.Calendar.prototype._onClickList = function(event) {
  // set flag
  this.showList = !this.showList;

  // update screen
  this._updateScreen();
};

scout.Calendar.prototype._onClickDay = function(event) {
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

scout.Calendar.prototype._updateModel = function() {
  // find base values for later calbulation
  var year = this.selected.getFullYear(),
    month = this.selected.getMonth(),
    date = this.selected.getDate(),
    day = (this.selected.getDay() + 6) % 7;

  // find start and end of displayed components based on selected date
  if (this.displayMode === this.DAY) {
    this.start = new Date(year, month, date);
    this.end = new Date(year, month, date + 1);
  } else if (this.displayMode === this.WEEK) {
    this.start = new Date(year, month, date - day);
    this.end = new Date(year, month, date - day + 6);
  } else if (this.displayMode === this.MONTH) {
    this.start = new Date(year, month, 1);
    this.end = new Date(year, month + 1, 0);
  } else if (this.displayMode === this.WORK) {
    this.start = new Date(year, month, date - day);
    this.end = new Date(year, month, date - day + 4);
  }

  // change selected day if workweek and selected day on weekend
  if (this.displayMode === this.WORK && day > 4) {
    this.selected = new Date(year, month, date - day + 4);
  }

  // set range...
  this.session.send(this.id, 'setVisibleRange', {
    dateRange: {
      from: scout.dates.toJsonDate(this.start),
      to: scout.dates.toJsonDate(this.end)
    }
  });

  // ... and mode on server
  this.session.send(this.id, 'setDisplayMode', {
    displayMode: this.displayMode
  });
};

scout.Calendar.prototype._updateScreen = function() {
  // select mode
  $('.calendar-mode', this.$commands).select(false);
  $("[data-mode='" + this.displayMode +"']", this.$modes).select(true);

  // remove selected day
  $('.selected', this.$grid).select(false);

  // layout grid
  this.layoutLabel();
  this.layoutSize();
  this.layoutComponents();
  this.layoutAxis();

  // if year shown and changed, redraw year
  if (this.selected.getFullYear() !== $('.year-title', this.$year).data('year') && this.showYear) {
    this.$year.empty();
    this.drawYear();
  }

  // if list shown and changed, redraw year
  if (this.showList) {
    this.$list.empty();
    this.drawList();
  }


  // color year
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
  $(".calendar-toggle-year", this.$modes).select(this.showYear);
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

  // set dayname (based on width of shown column)
  var width = this.$container.width(),
    weekdays;

  if (this.displayMode === this.DAY) {
    width /= 1;
  } else if (this.displayMode ===  this.WORK) {
    width /= 5;
  } else if  (this.displayMode ===  this.WEEK) {
    width /= 7;
  } else if (this.displayMode === this.MONTH) {
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

  // prepare to set all day date and mark selected one
  var $dates = $('.calendar-day', this.$grid),
    firstDate = new Date(this.start.valueOf());

  // find first visible date
  for (var offset = 0; offset < 42; offset++){
    firstDate.setDate(firstDate.getDate() - 1);
    if ((firstDate.getDay() === 0) && firstDate.getMonth() !== this.start.getMonth()){
      break;
    }
  }

  // loop all days and set value and class
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
        .removeClass('weekend-out weekend out selected now')
        .addClass(cl)
        .attr('data-day-name', text)
        .data('date', new Date(firstDate.valueOf()));
    }
  }
};

scout.Calendar.prototype.layoutAxis = function() {
  var $selected = $('.selected', this.$grid);

  // remove old axis
  $('.calendar-week-axis, .calendar-week-task', this.$grid).remove();

  // set weekname or day schedule
  if (this.displayMode === this.MONTH ) {
    $('.calendar-week-name').each(function (index) {
      if (index > 0) {
        var $e = $(this);
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

scout.Calendar.prototype._onYearClick = function(event) {
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


scout.Calendar.prototype._onYearDayClick = function(event) {
  // new selected day
  this.selected = $('.year-hover-day', this.$year).data('date');

  // update calendar
  this._updateModel();
  this._updateScreen();
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

scout.Calendar.prototype._onYearHoverOut = function(event) {
  // remove all hover effects
  $('.year-day.year-hover, .year-day.year-hover-day', this.$year).removeClass('year-hover year-hover-day');
};


/* -- list, draw ---------------------------------------------------- */
scout.Calendar.prototype.drawList = function() {
  var $selected = $('.selected', this.$grid),
    $components = $selected.children('.calendar-component:not(.clone)'),
    $c;

  // sort based on screen position
  $components.sort(this._sortTop);

  // set title to selected day
  this.$list.appendDiv('list-title', this._dateFormat($selected.data('date'), 'd. MMMM yyyy'));

  // show all components of selcted day, add links and set full text
  for (var i = 0; i < $components.length; i++) {
    $c = $components.eq(i);

    $c.clone()
      .appendTo(this.$list)
      .css('width', '')
      .css('height', '')
      .css('top', '')
      .css('left', '')
      .html(this._fullHtml($c.data('component')))
      .html(this._fullHtml($c.data('component')))
      .appendDiv('component-link', 'öffnen');
  }

};

/* -- components, draw---------------------------------------------- */

scout.Calendar.prototype.layoutComponents = function() {
  var countTask = 5,
    $day,
    c,
    $component,
    fromDate,
    toDate;

  // remove all exisiting items
  $('.calendar-component', this.$grid).remove();

  // main loop
  for (var i = 0; i < this.components.length; i++) {
    c = this.components[i];
    fromDate = scout.dates.parseJsonDate(c.fromDate);
    toDate = scout.dates.parseJsonDate(c.toDate);

    // loop covered days
    for (var j = 0; j < c.coveredDays.length; j++) {
      var d = scout.dates.parseJsonDate(c.coveredDays[j]);

      // day in shown month?
      $day = this._findDay(d);

      // if not: continue
      if ($day === undefined) {
        continue;
      }

      // draw component
      $component = $day.appendDiv('calendar-component')
        .html('<b>' + c.item.subject + '</b>')
        .css('background-color', $.ColorOpacity(c.item.color, 0.3))
        .css('border-color', '#' + c.item.color)
        .data('component', c)
        .mouseenter(this._onComponentHoverIn.bind(this));

      // adapt design if mode not month
      if (this.displayMode !== this.MONTH) {
        if (c.fullDay) {
          // task
          $component
            .addClass('component-task')
            .css('top', 'calc(' + this._dayPosition(-1)  + '% + ' + countTask + 'px)');
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
            $component.css('top',  this._dayPosition(fromHours + fromMinutes / 60) + '%')
              .css('height', this._dayPosition(toHours + toMinutes / 60) - this._dayPosition(fromHours + fromMinutes / 60) + '%');
          } else if (scout.dates.isSameDay(d, fromDate)) {
            $component.css('top',  this._dayPosition(fromHours + fromMinutes / 60) + '%')
              .css('height', this._dayPosition(24) - this._dayPosition(fromHours + fromMinutes / 60) + '%')
              .addClass('component-open-bottom');
          } else if (scout.dates.isSameDay(d, toDate)) {
            $component.css('top',  this._dayPosition(0) + '%')
              .css('height', this._dayPosition(fromHours + fromMinutes / 60) - this._dayPosition(0) + '%')
              .addClass('component-open-top');
          } else {
            $component.css('top',  this._dayPosition(1) + '%')
              .css('height', this._dayPosition(24) - this._dayPosition(1) + '%')
              .addClass('component-open-top')
              .addClass('component-open-bottom');
          }
        }
      }
    }
  }

  // find nice arragmenent :)
  this._arrangeComponent();
};

/* -- components, events-------------------------------------------- */

scout.Calendar.prototype._onComponentHoverIn = function (event) {
  var $comp = $(event.currentTarget),
    $clone = $comp.clone(),
    component = $comp.data('component'),
    $day = $comp.parent();

  // should not be possible, but in any case...
  $('.clone', this.$grid).remove();

  // build the perfect clone
  $clone.addClass('clone')
    .css('position', 'fixed')
    .css('top', $comp.offset().top + 'px')
    .css('left', $comp.offset().left + 'px')
    .css('height', $comp.outerHeight() + 'px')
    .css('width', $comp.outerWidth() + 'px')
    .css('margin', 0)
    .html(this._fullHtml(component))
    .css('z-index', 2)
    .data('component', component)
    .mouseleave(this._onComponentHoverOut.bind(this))
    .insertAfter($comp)
    .animateAVCSD('height', '100px');

  // add element to open component in new tab
  $clone.appendDiv('component-link', 'öffnen');

};


scout.Calendar.prototype._onComponentDayHoverIn = function (event) {
  var $comp = $(event.currentTarget),
    component = $comp.data('component'),
    oldHeight = $comp.outerHeight(),
    newHeight;

  // set to new values
  $comp
    .data('old-height', $comp[0].style.height)
    .data('old-html', $comp.html())
    .html(this._fullHtml(component))
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


scout.Calendar.prototype._onComponentHoverOut = function (event) {
  var $element = $(event.currentTarget);

  // close element
  $element.animateAVCSD('height', 0, $.removeThis);
};

scout.Calendar.prototype._onComponentDayHoverOut = function (event) {
  var $comp = $(event.currentTarget);

  // restore element
  // TODO cru: sometime fails?
  $comp
    .html($comp.data('old-html'))
    .animateAVCSD('height', $comp.data('old-height'));
};

/* -- components, arrangement------------------------------------ */

scout.Calendar.prototype._arrangeComponent = function () {
  var $days = $('.calendar-day', this.grid),
    $day,
    $children;

  for (var k = 0; k < $days.length; k++) {
    $day = $days.eq(k);
    $children = $day.children('.calendar-component:not(.component-task)');

    if (this.displayMode === this.MONTH && $children.length > 2) {
      $day.addClass('many-items');
    } else if (this.displayMode !== this.MONTH && $children.length > 1) {
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
  var $child,
    $test,
    stackX;

  for (var i = 0; i < $children.length; i++) {
    $child = $children.eq(i);
    stackX = 0;

    for (var j = 0; j < i; j++) {
      $test = $children.eq(j);

      if (this._intersect($child, $test)) {
        stackX =  $test.data('stackX') + 1;
      }
    }

    $child.data('stackX', stackX);
  }
};

scout.Calendar.prototype._arrangeComponentInitialW = function($children) {
  var stackMaxX = 0,
    stackX;

  for (var i = 0; i < $children.length; i++) {
    stackX = $children.eq(i).data('stackX');

    if (stackMaxX < stackX) {
      stackMaxX = stackX;
    }
  }

  $children.data('stackW', stackMaxX + 1);
};


scout.Calendar.prototype._arrangeComponentFindPlacement = function($children) {
  // TODO cru: placement may be improved, test cases needed
  // 1: change x if column on the left side free
  // 2: change w if place on the right side not used
  // -> then find new w (just use _arrangeComponentInitialW)
};

scout.Calendar.prototype._arrangeComponentSetPlacement = function($children) {
  var $child,
    stackX,
    stackW;

  // loop and place based on data
  for (var i = 0; i < $children.length; i++) {
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

scout.Calendar.prototype._dateFormat = function(date, pattern) {
  var d = new Date(date.valueOf()),
    dateFormat = new scout.DateFormat(this.session.locale, pattern);

  return dateFormat.format(d);
};

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
  // TODO: tuning
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
  var top1 = this._hourToNumber(this._dateFormat(scout.dates.parseJsonDate($e1.data('component').fromDate), 'HH:mm')),
    bottom1 = this._hourToNumber(this._dateFormat(scout.dates.parseJsonDate($e1.data('component').toDate), 'HH:mm')),
    top2 = this._hourToNumber(this._dateFormat(scout.dates.parseJsonDate($e2.data('component').fromDate), 'HH:mm')),
    bottom2 = this._hourToNumber(this._dateFormat(scout.dates.parseJsonDate($e2.data('component').toDate), 'HH:mm'));

  return (top1 >= top2 && top1 <= bottom2) || (bottom1 >= top2 && bottom1 <= bottom2);
};

scout.Calendar.prototype._sortTop = function (a, b) {
  return parseInt($(a).offset().top, 10) >  parseInt($(b).offset().top, 10);
};

scout.Calendar.prototype._fullHtml = function (component) {
  var range,
    fromDate = scout.dates.parseJsonDate(component.fromDate),
    toDate = scout.dates.parseJsonDate(component.toDate);

  // find time range
  if (component.fullDay) {
    range = '';
  } else if (scout.dates.isSameDay(fromDate, toDate)) {
    range = 'von ' + this._dateFormat(fromDate, 'HH:mm') + ' bis ' + this._dateFormat(fromDate, 'HH:mm') + '<br>';
  } else {
    range = range = 'von ' + this._dateFormat(fromDate, 'EEEE HH:mm ') + ' bis ' + this._dateFormat(toDate, ' EEEE HH:mm') + '<br>';
  }

  // compose text
  return '<b>' + component.item.subject + '</b><br>' + range + component.item.body;
};


/* -----------  Scout  -------------------------------*/

scout.Calendar.prototype._renderComponents = function() {
//  this.layoutComponents();
};

scout.Calendar.prototype._renderLoadInProgress = function() {
};

scout.Calendar.prototype._renderViewRange = function() {
};

scout.Calendar.prototype._renderDisplayMode = function() {
};

/*

scout.Calendar.prototype._renderSelectedComponent = function() {
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
};

scout.Calendar.prototype._onCalendarChangedBatch = function(calendarEventBatch) {
};
*/
