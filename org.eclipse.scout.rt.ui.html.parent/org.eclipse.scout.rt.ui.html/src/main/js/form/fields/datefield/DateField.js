scout.DateField = function() {
  scout.DateField.parent.call(this);
};
scout.inherits(scout.DateField, scout.ValueField);

scout.DateField.prototype._render = function($parent) {
  this.addContainer($parent, 'date-field');
  this.addLabel();
  this.addMandatoryIndicator();

  this.$field = $('<input>').
    attr('type', 'text').
    addClass('field').
    blur(this._onFieldBlur.bind(this)).
    keydown(this._onKeyDown.bind(this)).
    focus(this._onFocus.bind(this)).
    appendTo(this.$container);

  this._dateFormat = this.session.locale.dateFormat; //FIXME CGU datefield has own dateformat
  this._picker = new scout.DatePicker(this._dateFormat, this.$field);

  this.addIcon();
  this.addStatus();
};

scout.DateField.prototype._onFocus = function() {
  var displayText = this.$field.val();
  this._picker.selectDateByText(displayText);
  if (!this._$predict || this._$predict.length === 0){
    this._$predict = this._createPredictionField();
  }
};

scout.DateField.prototype._onFieldBlur = function() {
  scout.DateField.parent.prototype._onFieldBlur.call(this);

  this.$field.css('background-color', this._$predict.css('background-color'));
  this._$predict.remove();
  this._$predict = null;
  this._picker.close();
};

scout.DateField.prototype._onKeyDown = function(event) {
  var years = 0, months = 0, days = 0, diff = 0;
  var cursorPos = this.$field[0].selectionStart;
  var displayText = this.$field.val();

  if (event.which === scout.keys.ESC) {
    this._picker.close();
  } else if (event.which === scout.keys.RIGHT && cursorPos === displayText.length) {
    this._acceptPrediction();
  } else if (event.which == scout.keys.PAGE_UP || event.which == scout.keys.PAGE_DOWN) {
    months = (event.which === scout.keys.PAGE_UP ? -1 : 1);
    this._picker.shiftViewDate(0, months, 0);
    event.preventDefault();
  } else if (event.which == scout.keys.UP || event.which == scout.keys.DOWN) {
    diff = (event.which === scout.keys.UP ? -1 : 1);

    if (event.ctrlKey) {
      years = diff;
    } else if (event.shiftKey) {
      months = diff;
    } else {
      days = diff;
    }

    this._picker.shiftSelectedDate(years, months, days);
    this._$predict.val(this.$field.val());
    event.preventDefault();
  } else {
    //Use set timeout because field value is not set when keydown is fried. Keydown is used because keyup feels laggy.
    setTimeout(function(e) {
      if (!this._$predict) {
        //Return if $predict was already removed (e.g. by focus lost)
        return;
      }

      displayText = this.$field.val();
      var predictedDateText = '';
      if (this._validate(displayText)){
        predictedDateText = this._predict(displayText);
      }
      this._$predict.val(predictedDateText);
      this._picker.selectDateByText(predictedDateText);
    }.bind(this), 1);
  }
};

/**
 * Analyzes the text and checks whether all relevant parts are filled.<p>
 * If year is provided, month and day need to be provided as well.<br>
 * If no year but a month is provided day needs to be provided as well.
 */
scout.DateField.prototype._validate = function (text) {
  var dateInfo = this._dateFormat.analyze(text, true);
  var day = dateInfo.day, month = dateInfo.month, year = dateInfo.year;

  if (year) {
    return day >= 0 && day < 32 && month >= 0 && month < 13 && year > 0 && year < 9999;
  } else if (month) {
    return day >= 0 && day < 32 && month >= 0 && month < 13;
  } else if (day) {
    return day >= 0 && day < 32;
  } else {
    return false;
  }
};

scout.DateField.prototype._acceptPrediction = function() {
  var prediction = this._$predict.val();
  this.$field.val(prediction);
  this._picker.selectDateByText(prediction);
};

scout.DateField.prototype._predict = function(text) {
  var now = new Date();
  var currentYear = String(now.getFullYear());
  var dateInfo = this._dateFormat.analyze(text);
  var day = dateInfo.day,
    month = dateInfo.month,
    year = dateInfo.year;

  if (!day) {
    day = now.getDate();
  }
  if (!month) {
    month = now.getMonth() + 1;
  }

  if (year){
    if (year.length === 1 && year.substr(0, 1) === '0') {
      year += '9';
    }
    if (year.length === 1 && year.substr(0, 1) === '1') {
      year += year.substr(3, 1);
    }
    if (year.substr(0, 1) === '2') {
      year += currentYear.substr(year.length, 4 - year.length);
    }
    if (year.substr(0, 2) === '19') {
      year += '1999'.substr(year.length, 4 - year.length);
    }
  } else {
    year = currentYear;
  }

  return this._dateFormat.pattern.replace('dd', day).replace('MM', month).replace('yyyy', year);
};

scout.DateField.prototype._createPredictionField = function () {
  var $predict = this.$field.
    clone().
    addClass('predict').
    attr('disabled', 'disabled');

  //Prediction field is in the back of the original field -> make original field transparent
  var fieldBg = this.$field.css('background-color');
  this.$field.css('background-color', 'transparent');
  $predict.css('background-color', fieldBg);

  this.$field.before($predict);
  return $predict;
};
