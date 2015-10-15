scout.DatePickerLayout = function(datePicker) {
  this._datePicker = datePicker;
};
scout.inherits(scout.DatePickerLayout, scout.AbstractLayout);

scout.DatePickerLayout.COLUMNS = 7;
scout.DatePickerLayout.ROWS = 6; // rows excl. weekday row
scout.DatePickerLayout.MIN_DAY_SIZE = 28;

scout.DatePickerLayout.prototype.layout = function($container) {
  var
    // DOM elements
    $header = $container.find('.date-picker-header'),
    $scrollable = $container.find('.date-picker-scrollable'),
    $month = $container.find('.date-picker-month'),
    $firstDay = $container.find('.date-picker-day').first(),
    // Calculate dimensions
    htmlContainer = scout.HtmlComponent.get($container),
    containerSize = htmlContainer.getSize().subtract(htmlContainer.getInsets()),
    // Header height is also used as height for weekdays (defined by CSS)
    headerHeight = scout.graphics.getSize($header, true).height + 1, // + 1 for separator-line
    scrollableInsets = scout.graphics.getInsets($scrollable),
    scrollableSize = scout.graphics.getSize($scrollable).subtract(scrollableInsets),
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
    dayTextSize = scout.graphics.getSize($firstDay),
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
    .height(monthHeight)
    .css('margin-left', monthMarginLeftRight);

  // month: only set width, height is given by the popup-size
  $month.width(monthWidth);

  // layout weekdays and days
  $container.find('.date-picker-weekday, .date-picker-day').each(function() {
    var $element = $(this);
    if ($element.hasClass('date-picker-day')) {
      // days
      $element
        .css('margin', dayMargin)
        .css('padding-top', dayPaddingTop)
        .cssWidth(cssDaySize)
        .cssHeight(cssDaySize);
    } else {
      // weekdays: only set width, the rest is defined by CSS
      $element.cssWidth(daySize);
    }
  });
};
