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
scout.CalendarComponent = function() {
  scout.CalendarComponent.parent.call(this);

  /**
   * Selected is a GUI only property (the model doesn't have it)
   */
  this._selected = false;
  this._$parts = [];
  this.events = new scout.EventSupport();
};
scout.inherits(scout.CalendarComponent, scout.ModelAdapter);

/**
 * Since we cannot configure any key-strokes on a calendar-component we must
 * return null here, so no key-stroke will be installed in Widget.js.
 * @override Widget.js
 */
scout.CalendarComponent.prototype._createKeyStrokeContext = function() {
  return null;
};

/**
 * @override ModelAdapter.js
 */
scout.CalendarComponent.prototype._remove = function() {
  // remove $parts because they're not children of this.$container
  var tooltipSupport = this.parent._tooltipSupport;
  this._$parts.forEach(function($part) {
    tooltipSupport.uninstall($part);
    $part.remove();
  });
  scout.CalendarComponent.parent.prototype._remove.call(this);
};

scout.CalendarComponent.prototype._render = function($parent) {
  $.log.debug('(CalendarComponent#_render)');
  var i, partDay, $day, $part;

  for (i = 0; i < this.coveredDays.length; i++) {
    // check if day is in visible view range
    partDay = scout.dates.parseJsonDate(this.coveredDays[i]);
    $day = this._findDayInGrid(partDay);
    if ($day === undefined) {
      continue;
    }

    $part = $day
      .appendDiv('calendar-component', this.item.subject)
      .addClass(this.item.cssClass)
      .data('component', this)
      .data('partDay', partDay)
      .data('tooltipText', this._description.bind(this))
      .mousedown(this._onMousedown.bind(this))
      .on('contextmenu', this._onContextMenu.bind(this));
    this.parent._tooltipSupport.install($part);
    this._$parts.push($part);

    if (!this.parent._isMonth()) {
      if (this.fullDay) {
        var count = $('.component-task', $day).length;
        this._arrangeTask(1 + 26 * count);
        $part.addClass('component-task');
      } else {
        var
          fromDate = scout.dates.parseJsonDate(this.fromDate),
          toDate = scout.dates.parseJsonDate(this.toDate),
          partFrom = this._getHours(this.fromDate),
          partTo = this._getHours(this.toDate);

        // position and height depending on start and end date
        $part.addClass('component-day');
        if (this.coveredDays.length === 1) {
          this._partPosition($part, partFrom, partTo);
        } else if (scout.dates.isSameDay(partDay, fromDate)) {
          this._partPosition($part, partFrom, 24)
            .addClass('component-open-bottom');
        } else if (scout.dates.isSameDay(partDay, toDate)) {
          this._partPosition($part, 0, partTo)
            .addClass('component-open-top');
        } else {
          this._partPosition($part, 1, 24)
            .addClass('component-open-top')
            .addClass('component-open-bottom');
        }
      }
    }
  }
};

scout.CalendarComponent.prototype._getHours = function(date){
  var d = scout.dates.parseJsonDate(date);
  return d.getHours() + d.getMinutes() / 60;
};

// FIXME awe: tuning
scout.CalendarComponent.prototype._findDayInGrid = function(date) {
  var $day;
  $('.calendar-day', this.parent.$grid)
    .each(function() {
      if (scout.dates.isSameDay($(this).data('date'), date)) {
        $day = $(this);
        return;
      }
    });
  return $day;
};

scout.CalendarComponent.prototype._isTask = function() {
  return !this.parent._isMonth() && this.fullDay;
};

scout.CalendarComponent.prototype._arrangeTask = function(taskOffset) {
  this._$parts.forEach(function($part) {
    $part.css('top', 'calc(85% + ' + taskOffset + 'px)');
  });
};

scout.CalendarComponent.prototype._isDayPart = function() {
  return !this.parent._isMonth() && !this.fullDay;
};

scout.CalendarComponent.prototype._getHourRange = function(day){
  var hourRange = new scout.Range(this._getHours(this.fromDate), this._getHours(this.toDate));
  var dateRange = new scout.Range(new Date(this.fromDate), new Date(this.toDate));

  if (scout.dates.isSameDay(day, dateRange.from) && scout.dates.isSameDay(day, dateRange.to)) {
    return new scout.Range(hourRange.from, hourRange.to);
  } else if (scout.dates.isSameDay(day, dateRange.from)) {
    return new scout.Range(hourRange.from, 24);
  } else if (scout.dates.isSameDay(day, dateRange.to)) {
    return new scout.Range(0, hourRange.to);
  } else {
    return new scout.Range(0, 24);
  }
  return undefined;
};

scout.CalendarComponent.prototype.getPartDayPosition = function(day){
  return this._getDisplayDayPosition(this._getHourRange(day));
};

scout.CalendarComponent.prototype._getDisplayDayPosition = function(range){
  var preferredRange = new scout.Range(this.parent._dayPosition(range.from), this.parent._dayPosition(range.to));
  var minRangeSize = 2.5;
  if(preferredRange.size() < minRangeSize){
    return new scout.Range(preferredRange.from, preferredRange.from + minRangeSize);
  }
  return preferredRange;
};

scout.CalendarComponent.prototype._partPosition = function($part, y1, y2) {
  var range = new scout.Range(y1, y2);
  var r = this._getDisplayDayPosition(range);

  return $part
    .css('top', r.from + '%')
    .css('height', r.to - r.from  + '%');
};

scout.CalendarComponent.prototype._renderProperties = function() {
  this._renderSelected();
};

scout.CalendarComponent.prototype._renderSelected = function() {
  // the rendered check is required because the selected component may be
  // off-screen. in that case it is not rendered.
  if (this.rendered) {
    var selected = this._selected;
    this._$parts.forEach(function($part) {
      $part.toggleClass('comp-selected', selected);
    });
  }
};

scout.CalendarComponent.prototype.setSelected = function(selected) {
  var oldSelected = this._selected;
  this._selected = selected;
  if (oldSelected !== selected) {
    this.events.trigger('selected', {
      selected: selected
    });
    this._renderSelected();
  }
};

scout.CalendarComponent.prototype._onMousedown = function(event) {
  var $part = $(event.delegateTarget);
  this.parent._selectedComponentChanged(this, $part.data('partDay'));
};

scout.CalendarComponent.prototype._onContextMenu = function(event) {
  this.parent._showContextMenu(event, 'Calendar.CalendarComponent');
};

scout.CalendarComponent.prototype._format = function(date, pattern) {
  return scout.dates.format(date, this.session.locale, pattern);
};

scout.CalendarComponent.prototype._description = function() {
  var descParts = [],
    range = null,
    text = '',
    fromDate = scout.dates.parseJsonDate(this.fromDate),
    toDate = scout.dates.parseJsonDate(this.toDate);

  // subject
  if (scout.strings.hasText(this.item.subject)) {
    descParts.push({
      text: scout.strings.encode(this.item.subject),
      cssClass: 'calendar-component-title'
    });
  }

  // time-range
  if (this.fullDay) {
    // NOP
  } else if (scout.dates.isSameDay(fromDate, toDate)) {
    range = this.session.text('ui.FromXToY', this._format(fromDate, 'HH:mm'), this._format(toDate, 'HH:mm'));
  } else {
    range = this.session.text('ui.FromXToY', this._format(fromDate, 'EEEE HH:mm '), this._format(toDate, 'EEEE HH:mm'));
  }

  if (scout.strings.hasText(range)) {
    descParts.push({
      text: range,
      cssClass: 'calendar-component-intro'
    });
  }

  // body
  if (scout.strings.hasText(this.item.body)) {
    descParts.push({
      text: this.item.body
    });
  }

  // build text
  descParts.forEach(function(part) {
    text += (part.cssClass ? '<span class="' + part.cssClass + '">' + part.text + '</span>' : part.text) + '<br/>';
  });

  return text;
};
