scout.DatePicker = function(dateFormat, $field) {
  this._dateFormat = dateFormat;
  this._$field = $field;
  this.$box = null;
};

scout.DatePicker.prototype.show = function(show) {
  var that = this;
  var cl;

  function parse(text) {
    var a = that.analyze(text);
    var d = parseInt(a[0], 10),
      m = parseInt(a[1], 10),
      y = parseInt(a[2], 10);

    return new Date((y < 100 ? y + 2000 : y), m - 1, d);
  }

  var weekdays = this._dateFormat.symbols.weekdaysShortOrdered;
  var months = this._dateFormat.symbols.months;

  this.selectedDate = (this._$field.data('value') ? parse(this._$field.data('value')) : null);
  var today = new Date();
  this.viewDate = new Date(show ? show : (this.selectedDate ? this.selectedDate : today));
  var start = new Date(this.viewDate);

  if (!this._$predict || this._$predict.length === 0){
    this._$predict = this._$field.clone().addClass('predict').attr('disabled', 'disabled');//$('<input disabled id="predict">');
    var fieldBg = this._$field.css('background-color');
    this._$field.css('background-color', 'transparent');
    this._$predict.css('background-color', fieldBg);
    this._$field.before(this._$predict);
  }

  if (this.$box) {
    this.$box.remove();
  }
  this.$box = $('<div id="date_box"></div>');
  this.$box.cssLeft(this._$field.position().left);
  this.$box.cssTop(this._$field.innerBottom());
  this._$field.after(this.$box);

  var header_text = months[this.viewDate.getMonth()] + ' ' + this.viewDate.getFullYear();
  this.$box.append('<div class="date_header"><div class="date_left_y" data-jump="-12"></div><div class="date_left_m" data-jump="-1"></div><div class="date_month">' + header_text + '</div><div class="date_right_y" data-jump="12"></div><div class="date_right_m" data-jump="1"></div></div>');

  $('.date_left_y, .date_left_m, .date_right_m, .date_right_y', this.$box).mousedown(function(){
    var j = $(this).data('jump');
    that.show(that.viewDate.setMonth(that.viewDate.getMonth() + j));
    return false;
  });

  for(var i in weekdays){
    this.$box.append('<div class="date_weekday">' + weekdays[i] + "</div>");
  }

  for(var offset = 0; offset < 42; offset++){
    start.setDate(start.getDate() - 1);
    var diff = new Date(start.getYear(), this.viewDate.getMonth(), 0).getDate() - start.getDate();
    if ((start.getDay() === 0) && (start.getMonth() !== this.viewDate.getMonth()) && (diff > 1)){
      break;
    }
  }

  for(var i = 0; i < 42; i++){
    start.setDate(start.getDate() + 1);

    if ((start.getDay() === 6) || (start.getDay() === 0)) {
      cl = (start.getMonth() != this.viewDate.getMonth() ? ' date_out_weekend' : ' date_weekend');
    }
    else {
      cl = (start.getMonth() != this.viewDate.getMonth() ? ' date_out' : '');
    }

    if (start.setHours(0, 0, 0, 0) == today.setHours(0, 0, 0, 0)){
      cl += ' date_now';
    }

    if (this.selectedDate && (start.setHours(0, 0, 0, 0) === this.selectedDate.setHours(0, 0, 0, 0))){
      cl += ' date_select';
    }

    var d = start.getDate();
    var date = $('<div class="date_day ' + cl + '" data-date="' + this.format(start) + '">' + (d <= 9 ? '0' + d : d) + '</div>');
    this.$box.append(date);
  }

  $('.date_day', this.$box).mousedown(function(){
    that.set($(this).data('date'), 0 );
  });

  this.$box[0].addEventListener("mousewheel", function(e) {
      var e = e || window.event,
        w = e.wheelDelta ? e.wheelDelta / 10 : -e.detail * 3;
      var diff = (w >= 0 ? -1 : 1);
      var show = new Date(that.viewDate.getFullYear(), that.viewDate.getMonth() + diff, that.selectedDate.getDate());
      that.show(show);
      e.preventDefault();
  }, false);

};

scout.DatePicker.prototype.close = function() {
//  var p = parse(field.data('value'));
//  if (p != 'Invalid Date') {
//    set(format(p), 0);
//  } else if (field.val() == '') {
//    set('', 0);
//  } else {
//    field.data('error', 1);
//  }

  if (this._$predict) {
    this._$field.css('background-color', this._$predict.css('background-color'));
    this._$predict.remove();
    this._$predict = null;
  }
  this.$box.remove();
};

scout.DatePicker.prototype.acceptPrediction = function() {
  var p = this._$predict.val();
  var f = this._$field.val();
  this.set(p, 0, f.length + 1);
  this.show();
};

scout.DatePicker.prototype.findPredict = function(text) {
  var today = new Date();
  var a = this.analyze(text);
  var d = a[0], m = a[1], y = a[2];
  var pattern = this._dateFormat.pattern;

  d = (d ? d : today.getDate());
  m = (m ? m : today.getMonth() + 1);

  var year = String(today.getFullYear());
  if (y){
    y = (y.length == 1 && y.substr(0, 1) == '0' ? y + '9' : y);
    y = (y.length == 1 && y.substr(0, 1) == '1' ? y + year.substr(3, 1) : y);
    y = (y.substr(0, 1) == '2' ? y + year.substr(y.length, 4 - y.length) : y);
    y = (y.substr(0, 2) == '19'? y + '1999'.substr(y.length, 4 - y.length) : y);
  }
  else {
    y = year;
  }

  return pattern.replace('dd', d).replace('MM', m).replace('yyyy', y);
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

  var text = this.format(date);
  this.set(text, 0);
  this.show(date);
};

scout.DatePicker.prototype.format = function(date) {
  var d = date.getDate(),
    m = date.getMonth() + 1,
    y = date.getFullYear();

  return this._dateFormat.pattern.replace('dd', (d <= 9 ? '0' + d : d)).replace('MM', (m <= 9 ? '0' + m : m)).replace('yyyy', y);
};

scout.DatePicker.prototype.set = function (text, error, cursor){
  cursor = (cursor ? cursor : text.length);
  this._$predict.val((error ? '' : text));
  this._$field.val(text.substr(0, cursor));
  this._$field.data('value', (error ? '' : text));
  this._$field.data('old-text', text.substr(0, cursor));
  this._$field.data('error', error);
  this._$field.trigger('change');
  $('body').trigger('update');
};

scout.DatePicker.prototype.check = function (text) {
  var a = this.analyze(text);
  var d = a[0], m = a[1], y = a[2];
  var pd = parseInt(d, 10), pm = parseInt(m, 10), py = parseInt(y, 10);

  if (y) {
    return pd >= 0 && pd < 32 && pm >= 0 && pm < 13 && py > 0 && py < 9999;
  } else if (m) {
    return pd >= 0 && pd < 32 && pm >= 0 && pm < 13;
  } else if (d) {
    return pd >= 0 && pd < 32;
  } else {
    return false;
  }
};

scout.DatePicker.prototype.analyze = function (text) {
  var sep = this._dateFormat.pattern.replace('dd', '').replace('MM', '').replace('yyyy', '')[0];
  var t_pattern = this._dateFormat.pattern.split(sep);
  var t_text = text.split(sep);

  var d = t_text[t_pattern.indexOf('dd')];
  var m = t_text[t_pattern.indexOf('MM')];
  var y = t_text[t_pattern.indexOf('yyyy')];

  return [d, m, y];
};
