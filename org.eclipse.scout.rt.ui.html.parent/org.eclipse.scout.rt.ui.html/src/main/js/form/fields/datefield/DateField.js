scout.DateField = function() {
  scout.DateField.parent.call(this);
};
scout.inherits(scout.DateField, scout.ValueField);

scout.DateField.prototype._render = function($parent) {
  this.addContainer($parent, 'date-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addField(scout.fields.new$TextField()
    .focus(this._onFieldFocus.bind(this))
    .blur(this._onFieldBlur.bind(this))
    .keydown(this._onKeyDown.bind(this))
    .click(this._onClick.bind(this)));
  this._dateFormat = this.session.locale.dateFormat; // FIXME CGU datefield has own dateformat
  this._picker = new scout.DatePicker(this._dateFormat, this);
  this.addIcon();
  this.addStatus();
};

scout.DateField.prototype._onFieldFocus = function() {
  if (!this._$predict || this._$predict.length === 0) {
    this._$predict = this._createPredictionField();
  }
};

scout.DateField.prototype._onFieldBlur = function() {
  this._acceptPrediction();

  //Only update model if date is valid (according to ui)
  if (!this.errorStatusUi) {
    this._updateDisplayText(this.$field.val(), false);
  }

  this._$predict.remove();
  this._$predict = null;
  this._picker.close();
};

scout.DateField.prototype._onClick = function() {
  this.openPicker();
};

scout.DateField.prototype._onIconClick = function(event) {
  scout.DateField.parent.prototype._onIconClick.call(this, event);
  this.openPicker();
};

/**
 * Opens picker and selects date
 */
scout.DateField.prototype.openPicker = function() {
  this._updateSelection(this.$field.val());
};

/**
 * Called by datepicker when a date has been selected
 */
scout.DateField.prototype.onDateSelected = function(date) {
  var text = this._dateFormat.format(date);
  this._renderDisplayText(text);
};

/**
 * @Override
 */
scout.DateField.prototype._renderDisplayText = function(text) {
  this.$field.val(text);

  //Make sure there is no invisible and wrong prediction
  if (this._$predict) {
    this._$predict.val('');
  }
};

/**
 * @Override
 */
scout.DateField.prototype._renderErrorStatus = function(errorStatus) {
  scout.DateField.parent.prototype._renderErrorStatus.call(this, errorStatus);
  if (this._$predict) {
    this._$predict.toggleClass('has-error', !! errorStatus);
  }
};

scout.DateField.prototype._onKeyDown = function(event) {
  var years = 0,
    months = 0,
    days = 0,
    diff = 0,
    cursorPos = this.$field[0].selectionStart,
    displayText = this.$field.val(),
    prediction = this._$predict.val();

  if (event.which === scout.keys.TAB ||
    event.which === scout.keys.SHIFT) {
    return;
  }
  if (event.which === scout.keys.ENTER) {
    //Update model and close picker
    this._updateDisplayText(this.$field.val(), false);
    this._picker.close();
    return;
  }
  if (event.which === scout.keys.ESC) {
    this._picker.close();
    return;
  }

  if (event.which === scout.keys.RIGHT && cursorPos === displayText.length) {
    //Move cursor one right and apply next char of the prediction
    if (prediction) {
      this.$field.val(prediction.substring(0, displayText.length + 1));
    }
    return;
  }

  if (event.which === scout.keys.PAGE_UP || event.which === scout.keys.PAGE_DOWN) {
    months = (event.which === scout.keys.PAGE_UP ? -1 : 1);
    this._picker.shiftSelectedDate(0, months, 0);
    return;
  }

  if (event.which === scout.keys.UP || event.which === scout.keys.DOWN) {
    diff = (event.which === scout.keys.UP ? -1 : 1);

    if (event.ctrlKey) {
      years = diff;
    } else if (event.shiftKey) {
      months = diff;
    } else {
      days = diff;
    }

    this._picker.shiftSelectedDate(years, months, days);
    return false;
  }

  //Use set timeout because field value is not set when keydown is fried. Keydown is used because keyup feels laggy.
  setTimeout(function(e) {
    if (!this._$predict) {
      //Return if $predict was already removed (e.g. by focus lost)
      return;
    }

    displayText = this.$field.val();
    var predictedDateText = '';
    var valid = this.validateDisplayText(displayText);
    if (displayText && valid) {
      predictedDateText = this._predict(displayText);
    }
    this._$predict.val(predictedDateText);
    this._updateSelection(predictedDateText);
  }.bind(this), 1);
};

scout.DateField.prototype._updateSelection = function(displayText) {
  var date = this._dateFormat.parse(displayText);
  this._picker.selectDate(date);
};

/**
 * Analyzes the text and checks whether all relevant parts are filled.<p>
 * If year is provided, month and day need to be provided as well.<br>
 * If no year but a month is provided day needs to be provided as well.
 * @return errorStatus
 */
scout.DateField.prototype._validateDisplayText = function(text) {
  if (!text) {
    return;
  }

  //FIXME CGU what if text is 12. Juli 2014 -> wrong? actually depends on pattern... check with cru prototype
  //FIXME CGU optimize validation -> 1a.12.2003 currently is valid because parseInt strips 'a' maybe better use regexp. Also 10....02.2014 is currently valid
  var dateInfo = this._dateFormat.analyze(text, true);
  var day = dateInfo.day,
    month = dateInfo.month,
    year = dateInfo.year;
  var valid = false;

  if (year) {
    valid = day >= 0 && day < 32 && month >= 0 && month < 13 && year > 0 && year < 9999;
  } else if (month) {
    valid = day >= 0 && day < 32 && month >= 0 && month < 13;
  } else if (day) {
    valid = day >= 0 && day < 32;
  }
  if (!valid) {
    return {
      message: this.session.text('InvalidDateFormat')
    };
  }
};

/*
 * @return true if valid, false if not
 */
scout.DateField.prototype.validateDisplayText = function(text) {
  this.errorStatusUi = this._validateDisplayText(text);
  this._renderErrorStatus(this.errorStatusUi);
  return !this.errorStatusUi;
};

scout.DateField.prototype._acceptPrediction = function() {
  var prediction = this._$predict.val();
  if (!prediction) {
    return;
  }

  this.$field.val(prediction);
  this._updateSelection(prediction);
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

  if (year) {
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

scout.DateField.prototype._createPredictionField = function() {
  var $predict = this.$field
    .clone()
    .addClass('predict')
    .attr('tabIndex', '-1');

  $predict.val('');
  this.$field.before($predict);
  return $predict;
};
