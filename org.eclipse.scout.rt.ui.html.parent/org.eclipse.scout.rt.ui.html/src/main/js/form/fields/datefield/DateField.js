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
    focus(function() {
      this._picker.show();
    }.bind(this)).
    appendTo(this.$container);

  this._picker = new scout.DatePicker(this.session.locale.dateFormat, this.$field);

  this.addIcon();
  this.addStatus();
};

scout.DateField.prototype._onFieldBlur = function() {
  scout.DateField.parent.prototype._onFieldBlur.call(this);

  this._picker.close();
};

scout.DateField.prototype._onKeyDown = function(event) {
  var years = 0, months = 0, days = 0, diff = 0;

  if (event.which === scout.keys.ESC) {
    this._picker.close();
  }

  if (event.which === scout.keys.RIGHT && this.$field[0].selectionStart === this.$field.val().length) {
    this._picker.acceptPrediction();
  }

  if (event.which == scout.keys.PAGE_UP || event.which == scout.keys.PAGE_DOWN) {
    months = (event.which === scout.keys.PAGE_UP ? -1 : 1);
    this._picker.shiftViewDate(0, months, 0);

    event.preventDefault();
    return;
  }

  if (event.which == scout.keys.UP || event.which == scout.keys.DOWN) {
    diff = (event.which === scout.keys.UP ? -1 : 1);

    if (event.ctrlKey) {
      years = diff;
    } else if (event.shiftKey) {
      months = diff;
    } else {
      days = diff;
    }

    this._picker.shiftSelectedDate(years, months, days);
    event.preventDefault();
    return;
  }

  var that = this;
  setTimeout(function(e){
    var text = this.$field.val();
    if (text == this.$field.data('old-text')) return;

    var start = this.$field[0].selectionStart,
      end = this.$field[0].selectionEnd;

    if (text === ''){
      this._picker.set('', 0);
    } else if (!this._picker.check(text)){
      this._picker.set(text, 1);
      this.$field.data('value', '');
    }  else {
      this._picker.set(this._picker.findPredict(text), 0, text.length);
    }

    this.$field[0].setSelectionRange(start, end);
    this._picker.show();
  }.bind(this), 1);
};

