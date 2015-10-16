scout.DatePickerLayout = function(datePicker) {
  this._datePicker = datePicker;
  this._cache = {
    monthWidth: 0,
    daySize: 0,
    cssDaySize: 0,
    dayMargin: 0,
    dayPaddingTop: 0
  };
};
scout.inherits(scout.DatePickerLayout, scout.AbstractLayout);

scout.DatePickerLayout.COLUMNS = 7;
scout.DatePickerLayout.ROWS = 6; // rows excl. weekday row
scout.DatePickerLayout.MIN_DAY_SIZE = 26;
scout.DatePickerLayout.SIDE_RATIO = 1.3918;

// FIXME AWE: seitenverhältnis vom date-picker verbessern (nicht quadratisch, sondern leicht rechteckig)
// FIXME AWE: animation verbessern (links und rechts abdecken, damit DIVs die reinsliden nicht sichtbar sind)
// FIXME AWE: kreise um days sehen noch nicht gut aus

scout.DatePickerLayout.prototype.layout = function($container) {
  var
    // DOM elements
    $header = $container.find('.date-picker-header'),
    $scrollable = $container.find('.date-picker-scrollable'),
    $month = $container.find('.date-picker-month'),
    $firstDay = $month.find('.date-picker-day').first(),
    // Calculate dimensions
    htmlContainer = scout.HtmlComponent.get($container),
    containerSize = htmlContainer.getSize().subtract(htmlContainer.getInsets()),
    // Header height is also used as height for weekdays (defined by CSS)
    headerHeight = scout.graphics.getSize($header, true).height + 1, // + 1 for separator-line
    scrollableInsets = scout.graphics.getInsets($scrollable),
    scrollableSize = containerSize.subtract(scrollableInsets),
    // picker and all day-elements must be quadratic,
    // otherwise round selection would be an ellipse
    monthHeight = containerSize.height - headerHeight,
    dayWidth = Math.floor(scrollableSize.width / scout.DatePickerLayout.COLUMNS),
    dayHeight = Math.floor((monthHeight - headerHeight) / scout.DatePickerLayout.ROWS),
    daySize = Math.max(scout.DatePickerLayout.MIN_DAY_SIZE, Math.min(dayWidth, dayHeight)),
    monthWidth = daySize * scout.DatePickerLayout.COLUMNS,
    monthMarginLeftRight = Math.max(0, Math.floor((scrollableSize.width - monthWidth) / 2)),
    // measure first day in calendar, so we know how we must set
    // paddings and margins for each day
    dayTextSize = this._measureDaySize($month),
    dayMargin = Math.max(0, (daySize - scout.DatePickerLayout.MIN_DAY_SIZE) / 2),
    cssDaySize = daySize - 2 * dayMargin,
    dayPaddingTop = Math.max(0, (cssDaySize - dayTextSize.height) / 2);

  // we set padding instead of width, because background-color and bottom-border
  // should always use 100% of the popup width
  // we must add the horiz. padding from the scrollable to the header, so the
  // header is aligned
  $header
    .css('padding-left', monthMarginLeftRight + scrollableInsets.left + dayMargin)
    .css('padding-right', monthMarginLeftRight + scrollableInsets.right + dayMargin);

  // only set left margin to center the scrollable
  $scrollable
    .cssWidth(monthWidth + scrollableInsets.horizontal())
    .cssHeight(monthHeight + scrollableInsets.vertical())
    .css('margin-left', monthMarginLeftRight);

  // store results in cache (so the can be access during animation, without recalculating the whole layout)
  this._cache.monthWidth = monthWidth;
  this._cache.daySize = daySize;
  this._cache.cssDaySize = cssDaySize;
  this._cache.dayMargin = dayMargin;
  this._cache.dayPaddingTop = dayPaddingTop;

  this._layoutMonth($month);
};

/**
 * Adds a temporary day DIV (without width, height, padding or margin) to the $month element,
 * measures the size and removes the DIV immediately after measurement.
 */
scout.DatePickerLayout.prototype._measureDaySize = function($month) {
  var $tmpDay = $.makeDiv('date-picker-day')
    .text('30') // because the string 30 is wider than 11
    .appendTo($month),
    size = scout.graphics.getSize($tmpDay);
  $tmpDay.remove();
  return size;
};

/**
 * This functions is used to layout a month separately from the rest of the date-picker container
 * it is used to layout the month box during the animation.
 */
scout.DatePickerLayout.prototype._layoutMonth = function($month) {
  var cache = this._cache;

  // month: only set width, height is given by the popup-size
  $month.width(cache.monthWidth);

  // layout weekdays and days
  $month.find('.date-picker-weekday, .date-picker-day').each(function() {
    var $element = $(this),
      dayInMonth = $element.data('dayInMonth');

    if ($element.hasClass('date-picker-day')) {
      // days
      $element
        .css('margin', cache.dayMargin)
        .css('padding-top', cache.dayPaddingTop)
        .cssWidth(cache.cssDaySize)
        .cssHeight(cache.cssDaySize);
      // helps to center days between 10 and 19 nicer (especially when website is zoomed > 100%)
      if (dayInMonth > 9 && dayInMonth < 20) {
        $element.css('padding-right', 2);
      }
    } else {
      // weekdays: only set width, the rest is defined by CSS
      $element.cssWidth(cache.daySize);
    }
  });
};
