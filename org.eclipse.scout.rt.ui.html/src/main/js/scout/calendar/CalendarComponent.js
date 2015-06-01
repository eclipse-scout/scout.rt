scout.CalendarComponent = function() {
  scout.CalendarComponent.parent.call(this);

  /**
   * Selected is a GUI only property (the model doesn't have it)
   */
  this._selected = false;
  this._tooltip;
  this._tooltipDelay;
  this._$parts = [];
  this.events = new scout.EventSupport();
};
scout.inherits(scout.CalendarComponent, scout.ModelAdapter);

/**
 * @override ModelAdapter.js
 */
scout.CalendarComponent.prototype._remove = function() {
  // list-components must be informed, that their source has been removed
  this.events.trigger('removed');

  // remove $parts and tooltip, because they're not children of this.$container
  if (this._tooltip) {
    this._tooltip.remove();
  }
  this._$parts.forEach(function($part) {
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
      .appendDiv('calendar-component', this.item.subject )
      .addClass(this.item.cssClass)
      .data('component', this)
      .data('partDay', partDay)
      .mousedown(this._onMousedown.bind(this))
      .mouseenter(this._onHoverIn.bind(this))
      .mouseleave(this._onHoverOut.bind(this))
      .on('contextmenu', this._onContextMenu.bind(this));

    this._$parts.push($part);

    if (!this.parent._isMonth()) {
      if (this.fullDay) {
        $part.addClass('component-task');
      } else {
        var
          fromDate = scout.dates.parseJsonDate(this.fromDate),
          toDate = scout.dates.parseJsonDate(this.toDate),
          partFrom = fromDate.getHours() + fromDate.getMinutes() / 60,
          partTo =  toDate.getHours() + toDate.getMinutes() / 60;

        // position and height depending on start and end date
        $part.addClass('component-day');
        if (this.coveredDays.length === 1) {
          this._partPosition($part, partFrom, partTo);
        } else if (scout.dates.isSameDay(partDay, fromDate)) {
          this._partPosition($part, partFrom, 24)
            .addClass('component-open-bottom');
        } else if (scout.dates.isSameDay(partDay, toDate)) {
          this._partPosition($part, 0, partFrom)
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

// FIXME CRU: tuning
scout.CalendarComponent.prototype._findDayInGrid = function(date) {
  var $day;
  $('.calendar-day', this.parent.$grid)
    .each(function () {
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
  return !this.parent._isMonth()  && !this.fullDay;
};

scout.CalendarComponent.prototype._partPosition = function($part, y1, y2) {
  var y1Top = this.parent._dayPosition(y1),
      y2Top = this.parent._dayPosition(y2);
  return $part
    .css('top', y1Top + '%')
    .css('height', y2Top - y1Top + '%');
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
  if (oldSelected != selected) {
    this.events.trigger('selected', {selected: selected});
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

/**
 * Show tooltip with delay, so user is not flooded with tooltips when filled with many items.
 * Because of the asynchronous nature of the Calendar, the component may be removed and
 * created again, while the user hovers over a component.
 */
scout.CalendarComponent.prototype._onHoverIn = function(event) {
  var $part = $(event.target);
  this._tooltipDelay = setTimeout(function() {
    this._tooltip = new scout.Tooltip({
      text: this._description(),
      $anchor: $part,
      arrowPosition: 15,
      arrowPositionUnit: '%',
      htmlEnabled: true
    });
    this._tooltip.render();
  }.bind(this), 750);
};

scout.CalendarComponent.prototype._onHoverOut = function(event) {
  clearTimeout(this._tooltipDelay);
  if (this._tooltip) {
    this._tooltip.remove();
    this._tooltip = null;
  }
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
    descParts.push({text: this.item.subject, style: 'bold'});
  }

  // time-range
  if (this.fullDay) {
    // NOP
  } else if (scout.dates.isSameDay(fromDate, toDate)) {
    range = 'von ' + this._format(fromDate, 'HH:mm') + ' bis ' + this._format(fromDate, 'HH:mm');
  } else {
    range = range = 'von ' + this._format(fromDate, 'EEEE HH:mm ') + ' bis ' + this._format(toDate, ' EEEE HH:mm');
  }

  if (scout.strings.hasText(range)) {
    descParts.push({text: range, style: 'small'});
  }

  // body
  if (scout.strings.hasText(this.item.body)) {
    descParts.push({text: this.item.body});
  }

  // build text
  descParts.forEach(function(part) {
    text += (part.style ? '<span class = "' + part.style + '">' + part.text + '</span>' : part.text) + '<br/>';
  });

  return text;
};
