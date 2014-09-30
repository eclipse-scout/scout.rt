scout.DateField = function() {
  scout.DateField.parent.call(this);
};
scout.inherits(scout.DateField, scout.ValueField);

scout.DateField.prototype._render = function($parent) {
  this.addContainer($parent, 'DateField');
  this.$container.addClass('date-field'); // TODO AWE: refactor addContainer to accept options-object
  this.addLabel();
  this.addMandatoryIndicator();

  this.$field = $('<input>').
    attr('type', 'text').
    addClass('field').
    blur(this._onFieldBlur.bind(this)).
    appendTo(this.$container);

  this.addIcon();
  this.addStatus();

  this.$field.focus(function() {
    this._showPicker();
  }.bind(this));
};

scout.DateField.prototype._showPicker = function(show) {
  var that = this;
  var dateFormat = this.session.locale.dateFormat;
  var field = this.$field;

  var pattern = dateFormat.pattern;
  var sep = pattern.replace('dd', '').replace('MM', '').replace('yyyy', '')[0];

  function analyze(text){
    var t_pattern = pattern.split(sep);
    var t_text = text.split(sep);

    var d = t_text[t_pattern.indexOf('dd')];
    var m = t_text[t_pattern.indexOf('MM')];
    var y = t_text[t_pattern.indexOf('yyyy')];

    return [d, m, y];
  }

  function format(date){
    var d = date.getDate(),
      m = date.getMonth() + 1,
      y = date.getFullYear();

    return pattern.replace('dd', (d <= 9 ? '0' + d : d)).replace('MM', (m <= 9 ? '0' + m : m)).replace('yyyy', y);
  }

  function parse(text) {
    var a = analyze(text);
    var d = parseInt(a[0]),
      m = parseInt(a[1]),
      y = parseInt(a[2]);

    return new Date((y < 100 ? y + 2000 : y), m - 1, d);
  }

  function check(text) {
    var a = analyze(text);
    var d = a[0], m = a[1], y = a[2];
    var pd = parseInt(d), pm = parseInt(m), py = parseInt(y);

    if (y) {
      return pd >= 0 && pd < 32 && pm >= 0 && pm < 13 && py > 0 && py < 9999;
    } else if (m) {
      return pd >= 0 && pd < 32 && pm >= 0 && pm < 13;
    } else if (d) {
      return pd >= 0 && pd < 32;
    } else {
      return false;
    }
  }

  function find_predict(text){
    var a = analyze(text);
    var d = a[0], m = a[1], y = a[2];

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
  }

  function set(text, error, cursor){
    cursor = (cursor ? cursor : text.length);
    predict.val((error ? '' : text));
    field.val(text.substr(0, cursor));
    field.data('value', (error ? '' : text));
    field.data('old-text', text.substr(0, cursor));
    field.data('error', error);
    field.trigger('change');
    $('body').trigger('update');
  }

  var weekdays = dateFormat.symbols.weekdaysShortOrdered;
  var months = dateFormat.symbols.months;

  var select = (field.data('value') ? parse(field.data('value')) : null);
  var today = new Date();
  var show = new Date(show ? show : (select ? select : today));
  var start = new Date(show);

  var predict = this.$container.find(".predict");
  if (predict.length == 0){
    predict = field.clone().addClass('predict').attr('disabled', 'disabled');//$('<input disabled id="predict">');
    var fieldBg = field.css('background-color');
    field.css('background-color', 'transparent');
    predict.css('background-color', fieldBg);
    field.before(predict);
  }

  $("#date_box").remove();
  var box = $('<div id="date_box"></div>');
  field.after(box);

  var header_text = months[show.getMonth()] + ' ' + show.getFullYear()
  box.append('<div class="date_header"><div class="date_left_y" data-jump="-12"></div><div class="date_left_m" data-jump="-1"></div><div class="date_month">' + header_text + '</div><div class="date_right_y" data-jump="12"></div><div class="date_right_m" data-jump="1"></div></div>');

  $('.date_left_y, .date_left_m, .date_right_m, .date_right_y', box).mousedown(function(){
    var j = $(this).data('jump');
    that._showPicker(show.setMonth(show.getMonth() + j));
    return false;
  });

  for(var i in weekdays){
    box.append('<div class="date_weekday">' + weekdays[i] + "</div>");
  }

  for(var offset = 0; offset < 42; offset++){
    start.setDate(start.getDate() - 1);
    var diff = new Date(start.getYear(), show.getMonth(), 0).getDate() - start.getDate();
    if ((start.getDay() == 0) && (start.getMonth() != show.getMonth()) && (diff > 1)){
      break;
    }
  }

  for(var i = 0; i < 42; i++){
    start.setDate(start.getDate() + 1);

    if ((start.getDay() == 6) || (start.getDay() == 0)) {
      var cl = (start.getMonth() != show.getMonth() ? ' date_out_weekend' : ' date_weekend')
    }
    else {
      var cl = (start.getMonth() != show.getMonth() ? ' date_out' : '')
    }

    if (start.setHours(0, 0, 0, 0) == today.setHours(0, 0, 0, 0)){
      cl += ' date_now';
    }

    if (select && (start.setHours(0, 0, 0, 0) == select.setHours(0, 0, 0, 0))){
      cl += ' date_select';
    }

    var d = start.getDate();
    date = $('<div class="date_day ' + cl + '" data-date="' + format(start) + '">' + (d <= 9 ? '0' + d : d) + '</div>')
    box.append(date);
  }

  $('.date_day', box).mousedown(function(){
    set($(this).data('date'), 0 );
  });

  field.off();

  this.$field.focus(function() {
    this._showPicker();
  }.bind(this));

  field.keydown(function(e){
    if (e.which == 13) {
      var tabindex = $(this).attr('tabindex');
            tabindex++;
            $('[tabindex=' + tabindex + ']').focus();
    }

    if (e.which == 27) {
      field.blur();
    }

    if (e.which == 39 && field[0].selectionStart == field.val().length) {
      var p = predict.val();
      var f = field.val();
      set(p, 0, f.length + 1);
      that._showPicker();
    }

    if (e.which == 33 || e.which == 34) {
      var diff = (e.which == '33' ? -1 : 1);
      show = new Date(show.getFullYear(), show.getMonth() - diff, show.getDate());
      that._showPicker(show);
      e.preventDefault();
      return false;
    }

    if (e.which == 38 || e.which == 40) {
      var diff = (e.which == '38' ? -1 : 1);

      var change = (diff ? [0, 0, diff]: null);
      change = (diff && e.shiftKey ? [0, diff, 0] : change);
      change = (diff && e.ctrlKey ? [diff, 0, 0] : change);

      var d = (select ? new Date(select.getFullYear() + change[0], select.getMonth() + change[1], select.getDate()  + change[2]) : new Date());
      var text = format(d);
      set(text, 0);
      that._showPicker();
      return false;
    }

    setTimeout(function(e){
      var text = field.val();
      if (text == field.data('old-text')) return;

      var start = field[0].selectionStart,
        end = field[0].selectionEnd;

      if (text == ''){
        set('', 0);
      } else if (!check(text)){
        set(text, 1);
        field.data('value', '');
      }  else {
        set(find_predict(text), 0, text.length);
      }

      field[0].setSelectionRange(start, end)
      that._showPicker();
    }, 1)
  });

  box[0].addEventListener("mousewheel", function(e) {
      var e = e || window.event,
        w = e.wheelDelta ? e.wheelDelta / 10 : -e.detail * 3;
      var diff = (w >= 0 ? -1 : 1);
      show = new Date(show.getFullYear(), show.getMonth() + diff, show.getDate());
      that._showPicker(show);
      e.preventDefault();
  }, false);

  field.blur(function(event){
//    var p = parse(field.data('value'));
//    if (p != 'Invalid Date') {
//      set(format(p), 0);
//    } else if (field.val() == '') {
//      set('', 0);
//    } else {
//      field.data('error', 1);
//    }

    field.css('background-color', predict.css('background-color'));
    predict.remove();
    box.remove();
  });
};
