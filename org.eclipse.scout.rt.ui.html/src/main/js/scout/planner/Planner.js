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

  // mode
  this.DAY = 1;
  this.WEEK = 2;
  this.MONTH = 3;
  this.WORK = 4;
  this.CALENDAR_WEEK = 5;
  this.YEAR = 6;

  // additional modes; should be stored in model
  this.showYear = false;
};
scout.inherits(scout.Planner, scout.ModelAdapter);

scout.Planner.prototype.init = function(model, session) {
  scout.Planner.parent.prototype.init.call(this, model, session);
  for (var i = 0; i < this.resources.length; i++) {
    this._initResource(this.resources[i]);
  }
  this._syncViewRange(this.viewRange);
  this._syncSelectedResources(this.selectedResources);
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
  this.$scale = this.$container.appendDiv('planner-scale');
  this.$year = this.$container.appendDiv('planner-year-container').appendDiv('planner-year');
  this.$grid = this.$container.appendDiv('planner-grid');
  scout.scrollbars.install(this.$grid);

  // header contains all controls
  this.$range = this.$header.appendDiv('planner-range');
  this.$range.appendDiv('planner-minus').click(this._onClickMinus.bind(this));
  this.$range.appendDiv('planner-plus').click(this._onClickPlus.bind(this));
  this.$range.appendDiv('planner-select');

  // ... and modes
  this.$commands = this.$header.appendDiv('planner-commands');
  this.$commands.appendDiv('planner-today').click(this._onClickToday.bind(this));
  this.$commands.appendDiv('planner-separator');
  this.$commands.appendDiv('planner-mode-day planner-mode').attr('data-mode', this.DAY).click(this._onClickDisplayMode.bind(this));
  this.$commands.appendDiv('planner-mode-work planner-mode').attr('data-mode', this.WORK).click(this._onClickDisplayMode.bind(this));
  this.$commands.appendDiv('planner-mode-week planner-mode').attr('data-mode', this.WEEK).click(this._onClickDisplayMode.bind(this));
  this.$commands.appendDiv('planner-mode-month planner-mode').attr('data-mode', this.MONTH).click(this._onClickDisplayMode.bind(this));
  this.$commands.appendDiv('planner-mode-year planner-mode').attr('data-mode', this.YEAR).click(this._onClickDisplayMode.bind(this));
  this.$commands.appendDiv('planner-separator');
  this.$commands.appendDiv('planner-toggle-year').click(this._onClickYear.bind(this));

  // should be done by server?
  this.displayMode = this.MONTH;
  this._updateModel();
  this._updateScreen();
};

//TODO CGU/CRU vermutlich nicht nötig, da in updateScreen gemacht
//scout.Planner.prototype._renderProperties = function() {
//  scout.Planner.parent.prototype._renderProperties.call(this);

  //this._renderSelectedResources();
//  this._renderDisplayMode();
//};

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

scout.Planner.prototype._onClickDisplayMode = function(event) {
  var displayMode = $(event.target).data('mode');
  this.setDisplayMode(displayMode);
};

scout.Planner.prototype._onClickYear = function(event) {
  // set flag
  this.showYear = !this.showYear;

  // update screen
  this._updateScreen();
};

/* --  set display mode and range ------------------------------------- */

scout.Planner.prototype._updateModel = function() {};

scout.Planner.prototype._updateScreen = function() {
  if (this.rendered) {
    this._removeAllResources();
  }
  this._renderResources();
  this._renderSelectedResources();

  // select mode
  $('.planner-mode', this.$commands).select(false);
  $("[data-mode='" + this.displayMode +"']", this.$modes).select(true);

  // testdata - mode = year
//  if (this.displayMode === this.DAY) {
//    this.viewRange.from = scout.dates.parseJsonDate("2015-04-01 00:00:00.000");
//    this.viewRange.to = scout.dates.parseJsonDate("2015-04-01 00:00:00.000");
//  } else if (this.displayMode ===  this.WORK) {
//    this.viewRange.from = scout.dates.parseJsonDate("2015-03-30 00:00:00.000");
//    this.viewRange.to = scout.dates.parseJsonDate("2015-04-03 00:00:00.000");
//  } else if  (this.displayMode ===  this.WEEK) {
//    this.viewRange.from = scout.dates.parseJsonDate("2015-03-30 00:00:00.000");
//    this.viewRange.to = scout.dates.parseJsonDate("2015-04-05 00:00:00.000");
//  } else if (this.displayMode === this.MONTH) {
//    this.viewRange.from = scout.dates.parseJsonDate("2015-07-01 00:00:00.000");
//    this.viewRange.to = scout.dates.parseJsonDate("2016-02-01 00:00:00.000");
//  } else if (this.displayMode === this.YEAR) {
//    this.viewRange.from = scout.dates.parseJsonDate("2015-04-01 00:00:00.000");
//    this.viewRange.to = scout.dates.parseJsonDate("2016-03-01 00:00:00.000");
//  }

  // update
  this._layoutRange();
  this._layoutScale();

  // if year shown and changed, redraw year
  if (this.showYear) {
    this.$year.empty();
    this.drawYear();
  }

  // color year
  this.colorYear();
};

scout.Planner.prototype._layoutRange = function() {
  var text,
    toText = ' bis ';

  // find range text
  if (scout.dates.isSameDay(this.viewRange.from, this.viewRange.to)) {
    text = this._dateFormat(this.viewRange.from, 'd. MMMM yyyy');
  } else if (this.viewRange.from.getMonth() == this.viewRange.to.getMonth()) {
    text = this._dateFormat(this.viewRange.from, 'd.') + toText + this._dateFormat(this.viewRange.to, 'd. MMMM yyyy');
  } else if (this.viewRange.from.getFullYear() === this.viewRange.to.getFullYear()) {
    if (this.displayMode === this.MONTH || this.displayMode === this.YEAR) {
      text = this._dateFormat(this.viewRange.from, 'MMMM yyyy') + toText + this._dateFormat(this.viewRange.to, 'MMMM yyyy');
    } else {
      text = this._dateFormat(this.viewRange.from, 'd.  MMMM') + toText + this._dateFormat(this.viewRange.to, 'd. MMMM yyyy');
    }
  } else {
    if (this.displayMode === this.MONTH || this.displayMode === this.YEAR) {
      text = this._dateFormat(this.viewRange.from, 'MMMM yyyy') + toText + this._dateFormat(this.viewRange.to, 'MMMM yyyy');
    } else {
      text = this._dateFormat(this.viewRange.from, 'd.  MMMM yyyy') +toText + this._dateFormat(this.viewRange.to, 'd. MMMM yyyy');
    }
  }

  // set text
  $('.planner-select', this.$range).text(text);
};
scout.Planner.prototype._layoutScale  = function() {
  var $timelineLarge,
    $timelineSmall,
    loop,
    $divLarge;

  // empty scale
  this.$scale.empty();

  // append main elements
  this.$scale.appendDiv('planner-scale-title', "Titel");
  $timelineLarge = this.$scale.appendDiv('timeline-large');
  $timelineSmall = this.$scale.appendDiv('timeline-small');

  // fill timeline large depending on mode
  // TODO: depending on screen size: smaller or large representation
  if (this.displayMode === this.DAY) {
    loop = new Date(this.startDate.valueOf());

    // from start to end
    while (loop < this.endDate) {
      if ((loop.getMinutes() === 0) || (loop.valueOf() == this.startDate.valueOf())) {
        $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'HH')).data('count', 0);
      }

      $timelineSmall.appendDiv('scale-item', this._dateFormat(loop, ':mm'));
      loop.setMinutes(loop.getMinutes() + 30);
      $divLarge.data('count', $divLarge.data('count') + 1);
    }
  } else if ((this.displayMode === this.WORK) || (this.displayMode === this.WEEK)) {
    loop = new Date(this.startDate.valueOf());

    // from start to end
    while (loop < this.endDate) {
      if ((loop.getHours() === 0) || (loop.valueOf() == this.startDate.valueOf())) {
        if ((loop.getMonth() === 0) || (loop.valueOf() == this.startDate.valueOf())) {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'd. MMMM yyyy')).data('count', 0);
        } else if (loop.getDate() === 1) {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'd. MMM')).data('count', 0);
        } else {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'd.')).data('count', 0);
        }
      }

      $timelineSmall.appendDiv('scale-item', this._dateFormat(loop, 'HH:mm'));
      loop.setHours(loop.getHours() + 6);
      $divLarge.data('count', $divLarge.data('count') + 1);
    }

  } else if (this.displayMode === this.MONTH) {
    loop = new Date(this.startDate.valueOf());

    // from start to end
    while (loop < this.endDate) {
      if ((loop.getDate() < 8 ) || (loop.valueOf() == this.startDate.valueOf())) {
        if ((loop.getMonth() === 0) || (loop.valueOf() == this.startDate.valueOf())) {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM yyyy')).data('count', 0);
        } else {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM')).data('count', 0);
        }
      }

      $timelineSmall.appendDiv('scale-item', scout.dates.weekInYear(loop));
      loop.setDate(loop.getDate() + 7);
      $divLarge.data('count', $divLarge.data('count') + 1);
    }

  } else if (this.displayMode === this.YEAR) {
    loop = new Date(this.startDate.valueOf());

    // from start to end
    while (loop < this.endDate) {
      if ((loop.getMonth() === 0) || (loop.valueOf() == this.startDate.valueOf())) {
        $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'yyyy')).data('count', 0);
      }

      $timelineSmall.appendDiv('scale-item', this._dateFormat(loop, 'MMMM'));
      loop.setMonth(loop.getMonth() + 1);
      $divLarge.data('count', $divLarge.data('count') + 1);
    }
  }

  // set sizes
  var w = 100 / $timelineSmall.children().length;
  $timelineLarge.children().each(function () {
    $(this).css('width', $(this).data('count') * w + '%');
  });
  $timelineSmall.children().css('width', w + '%');

};

/* --  render essources, activities --------------------------------- */

scout.Planner.prototype._removeAllResources = function() {
  this.resources.forEach(function(resource) {
    resource.$resource.remove();
  });
};

scout.Planner.prototype._renderResources = function(resources) {
  var i, $resource, resource;

  resources = resources || this.resources;
  for (i = 0; i < resources.length; i++) {
    resource = resources[i];
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
    level = 100 - Math.min(activity.level * 100, 100),
    levelColor = scout.helpers.modelToCssColor(activity.levelColor);

  $activity.text(activity.text)
    .data('activity', activity)
    .on('click', this._onActivityClick.bind(this));
  if (activity.cssClass) {
    $activity.addClass(activity.cssClass);
  }
  if (levelColor) {
    $activity.css('background-color', levelColor);
  }
  // the background-color represents the fill level and not the image. This makes it easier to change the color using a css class
  $activity.css('background-image', 'linear-gradient(to bottom, #fff 0%, #fff ' + level + '%, transparent ' + level + '%, transparent 100% )');

  activity.$activity = $activity;
  return $activity;
};

/* -- year, draw and color ---------------------------------------- */

scout.Planner.prototype.drawYear = function() {
  // init vars
  var year = this.viewRange.from.getFullYear(),
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

  $('.year-day', this.$year).each(function() {
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
  $('.year-day', this.$year).each(function() {
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

scout.Planner.prototype._onResourceMousedown = function(event) {
  var $resource = $(event.delegateTarget),
  resource = $resource.data('resource');

  this.selectResource(resource);
};

scout.Planner.prototype._onActivityClick = function(event) {
  var $activity = $(event.delegateTarget),
    activity = $activity.data('activity');

  $.l(activity);
};


/* -- helper ---------------------------------------------------- */

scout.Planner.prototype._dateFormat = function(date, pattern) {
  var d = new Date(date.valueOf()),
    dateFormat = new scout.DateFormat(this.session.locale, pattern);

  return dateFormat.format(d);
};

/* -----------  Scout  -------------------------------*/

scout.Planner.prototype._renderViewRange = function() {
  //FIXME CGU/CRU always redraw whole screen? what if several properties change?
  this._updateScreen();
};

scout.Planner.prototype._renderWorkDayCount = function() {};

scout.Planner.prototype._renderWorkDaysOnly = function() {};

scout.Planner.prototype._renderFirstHourOfDay = function() {};

scout.Planner.prototype._renderLastHourOfDay = function() {};

scout.Planner.prototype._renderIntradayInterval = function() {};

scout.Planner.prototype._renderDisplayMode = function() {};

scout.Planner.prototype._renderSelectedBeginTime = function() {};

scout.Planner.prototype._renderSelectedEndTime = function() {};

scout.Planner.prototype._syncViewRange = function(viewRange) {
  this.viewRange = {
    from: scout.dates.create(viewRange.from),
    to: scout.dates.create(viewRange.to)
  };
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

scout.Planner.prototype._renderSelectedActivityCell = function() {};

scout.Planner.prototype._renderDrawSections = function() {};

scout.Planner.prototype._resourcesByIds = function(ids) {
  return ids.map(this._resourceById.bind(this));
};

scout.Planner.prototype._resourceById = function(id) {
  return this.resourceMap[id];
};

scout.Planner.prototype.setDisplayMode = function(displayMode) {
  this.displayMode = displayMode;
  this._sendSetDisplayMode(displayMode);

  //FIXME CGU/CRU currently only triggered by server, because end time is not known here
//  this._updateScreen();
};

scout.Planner.prototype.selectResource = function(resource) {
  var oldSelection = this.selectedResources;
  this.selectedResources = [resource];
  // FIXME CRU set begin and end time
  this._sendSetSelection([resource.id]);
  this._renderSelectedResources('', oldSelection);
};

scout.Planner.prototype._insertResources = function(resources) {
  // Update model
  resources.forEach(function(resource) {
    this._initResource(resource);
    // Always insert new rows at the end, if the order is wrong a rowOrderChange event will follow
    this.resources.push(resource);
  }.bind(this));

  // Update HTML
  if (this.rendered) {
    this._renderResources(resources);
    this.htmlComp.invalidateTree();
  }
};

scout.Planner.prototype._deleteResources = function(resources) {
  resources.forEach(function(resource) {
    // Update model
    scout.arrays.remove(this.resources, resource);
    delete this.resourcesMap[resource.id];

    // Update HTML
    if (this.rendered) {
      resource.$resource.remove();
      delete resource.$resource;
    }
  }.bind(this));
};

scout.Planner.prototype._deleteAllResources = function() {
  // Update HTML
  if (this.rendered) {
    this._removeAllResources();
    this.htmlComp.invalidateTree();
  }

  // Update model
  this.resources = [];
  this.resourcesMap = {};
};

scout.Planner.prototype._sendSetDisplayMode = function(displayMode) {
  this.session.send(this.id, 'setDisplayMode', {
    displayMode: displayMode
  });
};

scout.Planner.prototype._sendSetSelection = function(resourceIds, beginTime, endTime) {
  this.session.send(this.id, 'setSelection', {
    resourceIds: resourceIds,
    beginTime: beginTime,
    endTime: endTime
  });
};

scout.Planner.prototype._onResourcesInserted = function(resources) {
  this._insertResources(resources);
};

scout.Planner.prototype._onResourcesDeleted = function(resourceIds) {
  var resources = this._resourcesByIds(resourceIds);
  this._deleteResources(resources);
};

scout.Planner.prototype._onAllResourcesDeleted = function() {
  this._deleteAllResources();
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
