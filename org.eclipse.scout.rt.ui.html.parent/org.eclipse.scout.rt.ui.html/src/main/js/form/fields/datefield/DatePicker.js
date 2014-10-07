scout.DatePicker = function(dateFormat, $field) {
  this._dateFormat = dateFormat;
  this._$field = $field;
  this.selectedDate = null;
  this.viewDate = null;
  this.$box = null;
};

scout.DatePicker.prototype.selectDate = function(date) {
  this.show(null, date);
};

scout.DatePicker.prototype.show = function(viewDate, selectedDate) {
  this.selectedDate = selectedDate;
  this.viewDate = viewDate;
  if (!this.viewDate) {
    if (this.selectedDate) {
      this.viewDate = this.selectedDate;
    }
    else {
      this.viewDate = new Date();
    }
  }

  if (this.$box) {
    this.$box.remove();
  }
  this.$box = this._createDateBox();
  this.$box.find('.date-box-day').mousedown(this._onDayMouseDown.bind(this));
  this.$box.find('.date-box-left-y, .date-box-left-m, .date-box-right-m, .date-box-right-y').mousedown(this._onNavigationMouseDown.bind(this));
  this.$box[0].addEventListener("mousewheel", this._onMouseWheel.bind(this), false);
};

scout.DatePicker.prototype._onNavigationMouseDown = function(event) {
  var $target = $(event.currentTarget);
  var diff = $target.data('shift');
  this.shiftViewDate(0, diff, 0);

  //Prevent closing the box
  event.preventDefault();
};

scout.DatePicker.prototype._onDayMouseDown = function(event) {
  //FIXME CGU click would be better but comes after field.blur -> ignore blur when clicking inside box and close box here
  var $target = $(event.currentTarget);
  var date = $target.data('date');
  this._onDateSelected(date);
};

scout.DatePicker.prototype._onMouseWheel = function(event) {
  event = event || window.event;
  var wheelData = event.wheelDelta ? event.wheelDelta / 10 : -event.detail * 3;
  var diff = (wheelData >= 0 ? -1 : 1);

  this.shiftViewDate(0, diff, 0);
  event.preventDefault();
};

scout.DatePicker.prototype.close = function() {
  this.$box.remove();
};

scout.DatePicker.prototype.shiftViewDate = function(years, months, days) {
  var date = this.viewDate;

  date = scout.dates.shift(date, years, months, days);
  this.show(date);
};

scout.DatePicker.prototype.shiftSelectedDate = function(years, months, days) {
  var date = this.selectedDate;
  if (!date) {
    date = new Date();
  }
  date = scout.dates.shift(date, years, months, days);

  var text = this._dateFormat.format(date);
  this._onDateSelected(text);
  this.selectDate(date);
};

scout.DatePicker.prototype._onDateSelected = function (text){
  this._$field.val(text);
};

scout.DatePicker.prototype._createDateBox = function () {
  var cl, i, now = new Date();
  var day, dayInMonth, $day;
  var weekdays = this._dateFormat.symbols.weekdaysShortOrdered;
  var months = this._dateFormat.symbols.months;
  var start = new Date(this.viewDate);

  var $box = $.makeDIV('date-box').
    cssLeft(this._$field.position().left).
    cssTop(this._$field.innerBottom());

  // Create header
  var headerText = months[this.viewDate.getMonth()] + ' ' + this.viewDate.getFullYear();
  var headerHtml =
    '<div class="date-box-header">' +
    '  <div class="date-box-left-y" data-shift="-12"></div>' +
    '  <div class="date-box-left-m" data-shift="-1"></div>' +
    '  <div class="date-box-month">' + headerText + '</div>' +
    '  <div class="date-box-right-y" data-shift="12"></div>' +
    '  <div class="date-box-right-m" data-shift="1"></div>' +
    '</div>';
  $box.append(headerHtml);

  // Create weekday header
  for (i in weekdays){
    $box.appendDIV('date-box-weekday', weekdays[i]);
  }

  // Find start date (-1)
  for (var offset = 0; offset < 42; offset++){
    start.setDate(start.getDate() - 1);
    var diff = new Date(start.getYear(), this.viewDate.getMonth(), 0).getDate() - start.getDate();
    if ((start.getDay() === 0) && (start.getMonth() !== this.viewDate.getMonth()) && (diff > 1)){
      break;
    }
  }

  // Create days
  for (i = 0; i < 42; i++){
    start.setDate(start.getDate() + 1);

    if ((start.getDay() === 6) || (start.getDay() === 0)) {
      cl = (start.getMonth() != this.viewDate.getMonth() ? ' date-box-out-weekend' : ' date-box-weekend');
    }
    else {
      cl = (start.getMonth() != this.viewDate.getMonth() ? ' date-box-out' : '');
    }

    if (start.setHours(0, 0, 0, 0) === now.setHours(0, 0, 0, 0)){
      cl += ' date-box-now';
    }

    if (this.selectedDate && (start.setHours(0, 0, 0, 0) === this.selectedDate.setHours(0, 0, 0, 0))){
      cl += ' date-box-select';
    }

    dayInMonth = start.getDate();
    day = (dayInMonth <= 9 ? '0' + dayInMonth : dayInMonth);
    $day = $box.
      appendDIV('date-box-day ' + cl, day).
      data('date', this._dateFormat.format(start));
  }

  this._$field.after($box);
  return $box;
};
