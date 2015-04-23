// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Planner = function() {
  scout.Planner.parent.call(this);
  this.resourceMap = [];

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
scout.inherits(scout.Planner, scout.ModelAdapter);

scout.Planner.prototype.init = function(model, session) {
  scout.Planner.parent.prototype.init.call(this, model, session);
  for (var i = 0; i < this.resources.length; i++) {
    this._initResource(this.resources[i]);
  }
  // this.selectedResources contains ids -> map to actual resources
  this.selectedResources = this._resourcesByIds(this.selectedResources);
};

scout.Planner.prototype._initResource = function(resource) {
  scout.defaultValues.applyTo(resource, 'Resource');
  scout.defaultValues.applyTo(resource.activities, 'Cell');
  this.resourceMap[resource.id] = resource;
};

scout.Planner.prototype._render = function($parent) {
  //basics, layout etc.
  this._$parent = $parent;
  this.$container = this._$parent.appendDiv('planner').attr('id', this._generateId('planner'));
  var layout = new scout.PlannerLayout(this);
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(layout);
  this.htmlComp.pixelBasedSizing = false;

  // main elements
  this.$header = this.$container.appendDiv('planner-header');
  this.$year = this.$container.appendDiv('planner-year-container').appendDiv('planner-year');
  this.$grid = this.$container.appendDiv('planner-grid');
  scout.scrollbars.install(this.$grid);
  this.$list = this.$container.appendDiv('planner-list-container').appendDiv('planner-list');

  // header contains all controls
  this.$range = this.$header.appendDiv('planner-range');
  this.$range.appendDiv('planner-minus').click(this._onClickMinus.bind(this));
  this.$range.appendDiv('planner-plus').click(this._onClickPlus.bind(this));
  this.$range.appendDiv('planner-select');

  // ... and modes
  this.$commands = this.$header.appendDiv('planner-commands');
  this.$commands.appendDiv('planner-today').click(this._onClickToday.bind(this));
  this.$commands.appendDiv('planner-separator');
  this.$commands.appendDiv('planner-mode-day planner-mode').attr('data-mode', this.DAY).click(this._onClickMode.bind(this));
  this.$commands.appendDiv('planner-mode-work planner-mode').attr('data-mode', this.WORK).click(this._onClickMode.bind(this));
  this.$commands.appendDiv('planner-mode-week planner-mode').attr('data-mode', this.WEEK).click(this._onClickMode.bind(this));
  this.$commands.appendDiv('planner-mode-month planner-mode').attr('data-mode', this.MONTH).click(this._onClickMode.bind(this));
  this.$commands.appendDiv('planner-separator');
  this.$commands.appendDiv('planner-toggle-year').click(this._onClickYear.bind(this));
  this.$commands.appendDiv('planner-toggle-list').click(this._onClickList.bind(this));


  // should be done by server?
  this.displayMode = this.MONTH;
  this._updateModel();
  this._updateScreen();
};

scout.Planner.prototype._renderProperties = function() {
  scout.Planner.parent.prototype._renderProperties.call(this);
  this._renderSelectedResources();
};

/* -- basics, events -------------------------------------------- */

scout.Planner.prototype._onClickMinus = function(event) {
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

scout.Planner.prototype._onClickPlus = function(event) {
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

scout.Planner.prototype._onClickToday = function(event) {
  // new selected date
  this.selected = new Date();

  // update calendar
  this._updateModel();
  this._updateScreen();
};

scout.Planner.prototype._onClickMode = function(event) {
  // set new mode
  this.displayMode = $(event.target).data('mode');

  // update calendar
  this._updateModel();
  this._updateScreen();
};

scout.Planner.prototype._onClickYear = function(event) {
  // set flag
  this.showYear = !this.showYear;

  // update screen
  this._updateScreen();
};
scout.Planner.prototype._onClickList = function(event) {
  // set flag
  this.showList = !this.showList;

  // update screen
  this._updateScreen();
};

scout.Planner.prototype._onClickDay = function(event) {
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

scout.Planner.prototype._updateModel = function() {
};

scout.Planner.prototype._updateScreen = function() {
  this._renderResources();
};



scout.Planner.prototype._renderResources = function() {
  var i, $resource, resource;
  for (i = 0; i < this.resources.length; i++) {
    resource = this.resources[i];
    $resource = this._build$Resource(resource, this.$grid);
    $resource.data('resource', resource)
      .on('mousedown', this._onResourceMousedown.bind(this))
      .appendTo(this.$grid);
    resource.$resource = $resource;
  }
};

scout.Planner.prototype._build$Resource = function(resource) {
  var i, $activity,
    $resource = $.makeDiv('resource');
  $resource.appendSpan('resource-cell').text(resource.resourceCell.text);
  for (i = 0; i < resource.activities.length; i++) {
    $activity = this._build$Activity(resource.activities[i]);
    $activity.appendTo($resource);
  }
  return $resource;
};

scout.Planner.prototype._build$Activity = function(activity) {
  var i,
    $activity = $.makeDiv('activity'),
    level = Math.min(activity.level * 100, 100),
    levelColor = scout.helpers.modelToCssColor(activity.levelColor);

    if (!levelColor) {
      levelColor = '#80c1d0';
    }

    $activity.text(activity.text);
    $activity.css('background-color', 'transparent');
    $activity.css('background-image', 'linear-gradient(to top, ' + levelColor + ' 0%, ' + levelColor + ' ' + level + '%, transparent ' + level + '%, transparent 100% )');

  return $activity;
};


/* -- year, draw and color ---------------------------------------- */

scout.Planner.prototype.drawYear = function() {
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

scout.Planner.prototype.colorYear = function() {
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

scout.Planner.prototype._onYearClick = function(event) {
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


scout.Planner.prototype._onYearDayClick = function(event) {
  // new selected day
  this.selected = $('.year-hover-day', this.$year).data('date');

  // update calendar
  this._updateModel();
  this._updateScreen();
};


scout.Planner.prototype._onYearHoverIn = function(event) {
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

scout.Planner.prototype._onYearHoverOut = function(event) {
  // remove all hover effects
  $('.year-day.year-hover, .year-day.year-hover-day', this.$year).removeClass('year-hover year-hover-day');
};


scout.Planner.prototype._renderDays = function() {
};

scout.Planner.prototype._renderWorkDayCount = function() {
};

scout.Planner.prototype._renderWorkDaysOnly = function() {
};

scout.Planner.prototype._renderFirstHourOfDay = function() {
};

scout.Planner.prototype._renderLastHourOfDay = function() {
};

scout.Planner.prototype._renderIntradayInterval = function() {
};

scout.Planner.prototype._renderPlanningMode = function() {
};

scout.Planner.prototype._renderResourceIds = function() {
};

scout.Planner.prototype._renderSelectedBeginTime = function() {
};

scout.Planner.prototype._renderSelectedEndTime = function() {
};

scout.Planner.prototype._syncSelectedResources = function(selectedResources) {
  this.selectedResources = this._resourcesByIds(selectedResources);
};

scout.Planner.prototype._renderSelectedResources = function(newIds, oldSelectedResources) {
  if (oldSelectedResources) {
    oldSelectedResources.forEach(function(resource) {
      resource.$resource.select(false);
    });
  }

  this.selectedResources.forEach(function(resource) {
    resource.$resource.select(true);
  });
};

scout.Planner.prototype._renderSelectedActivityCell = function() {
};

scout.Planner.prototype._renderTimeScale = function() {
};

scout.Planner.prototype._renderDrawSections = function() {
};

scout.Planner.prototype._resourcesByIds = function(ids) {
  return ids.map(this._resourceById.bind(this));
};

scout.Planner.prototype._resourceById = function(id) {
  return this.resourceMap[id];
};

scout.Planner.prototype._sendSetSelection = function(resourceIds, beginTime, endTime) {
  this.session.send(this.id, 'setSelection', {
    resourceIds: resourceIds,
    beginTime: beginTime,
    endTime: endTime
  });
};

scout.Planner.prototype._onResourceMousedown = function(event) {
  var $resource = $(event.delegateTarget),
    resource = $resource.data('resource');

  this.selectResource(resource);
};

scout.Planner.prototype.selectResource = function(resource) {
  var oldSelection = this.selectedResources;
  this.selectedResources = [resource];
  // FIXME CRU set begin and end time
  this._sendSetSelection([resource.id]);
  this._renderSelectedResources('', oldSelection);
};


scout.Planner.prototype._onResourcesInserted = function(resources) {

};

scout.Planner.prototype._onResourcesDeleted = function(resourceIds) {

};

scout.Planner.prototype._onAllResourcesDeleted = function() {

};

scout.Planner.prototype._onResourcesUpdated = function(resources) {

};

scout.Planner.prototype._onResourceOrderChanged = function(resourceIds) {

};

scout.Planner.prototype.onModelAction = function(event) {
  if (event.type === 'resourcesInserted') {
    this._onResourcesInserted(event.resources);
  } else if (event.type === 'resourcesDeleted') {
    this._onResourcesDeleted(event.resourceIds);
  } else if (event.type === 'allResourcesDeleted') {
    this._onAllResourcesDeleted();
  } else if (event.type === 'resourceOrderChanged') {
    this._onResourceOrderChanged(event.resourceIds);
  } else if (event.type === 'resourcesUpdated') {
    this._onResourcesUpdated(event.resources);
  } else {
    $.log.warn('Model event not handled. Widget: scout.Planner. Event: ' + event.type + '.');
  }
};
