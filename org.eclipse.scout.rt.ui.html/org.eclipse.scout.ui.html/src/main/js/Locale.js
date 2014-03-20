//define local als singleton
locale = new function Locale () {
  //FIXME CGU check with chris
//  this.localeString = model.localeString;
//  this.decimalFormatSymbols = model.decimalFormatSymbols;

  log(this.decimalFormatSymbols);

// TODO cru:init with server

  //  init localized number pattern chars
  this._numberD = '0';
  this._numberN = '#';
  this._numberP = '.';
  this._numberM = '-';
  this._numberG = ',';
  this._numberS = ';';

  // init localized date constants
  this.dateWeekday = ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'];
  this.dateWeekdayLong = ['Sonntag', 'Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag'];
  this.dateMonth = ['Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'];
  this.dateMonthLong = ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'];
  this.dateWeekStarts = 1;
  this.dateAM = 'AM';
  this.datePM = 'PM';

  function getText() {
  }
}();

// convert number to sting using java format
// without prefix and suffix and without E and without %

locale.Number = function (pattern) {
  // format function will use these (defaults)
  this.negPrefix = locale._numberM;
  this.negSuffix = '';
  this.groupChar = locale._numberG;
  this.groupLength = 0;
  this.pointChar = locale._numberP;
  this.zeroBefore = 1;
  this.zeroAfter = 0;
  this.allAfter = 0;

  //  find prefix and suffix for negativ numbers
  var split = pattern.split(locale._numberS);
  if (split.length > 1) {
    this.negPrefix = split[1].slice(0, _find(split[1], locale._numberD + locale._numberN, 1));
    this.negSuffix = split[1].slice(_find(split[1], locale._numberD + locale._numberN, -1) + 1);
    pattern = split[0];
  }

  // find group length
  var start = _find(pattern, locale._numberG, -1),
    end = _find(pattern, locale._numberP, 1) || pattern.length;
  if (start && end)  this.groupLength = end - start - 1;
  pattern = pattern.replace(locale._numberG, '');

  // split on decimal point
  split = pattern.split(locale._numberP);

  // find digits before and after decimal point
  this.zeroBefore = _count(split[0], locale._numberD);
  this.zeroAfter = _count(split[1], locale._numberD);
  this.allAfter = this.zeroAfter + _count(split[1], locale._numberN);

  // helper function
  function _find(string, chars, dir) {
    for (var i = ((dir == 1) ? 0 : string.length - 1); i < string.length && i > -1; i += dir) {
      if (chars.indexOf(string[i]) > -1) return i;
    }
    return null;
  }

  function _count(str, separator) {
    return string.split(separator).length - 1;
  }
};

// in public use for formatting
locale.Number.prototype.format = function format (number) {
  if (number < 0) return this.negPrefix + this.format(-number) + this.negSuffix;

  // before decimal point
  var before = Math.floor(number);
  before = (before === 0) ? '' : String(before);
  before = (before.length >= this.zeroBefore) ? before : Array(this.zeroBefore - before.length + 1).join('0') + before;

  // group digits
  if (this.groupLength) {
    for (var i = before.length - this.groupLength; i > 0; i -= this.groupLength) {
      before = before.substr(0, i) + this.groupChar + before.substr(i);
    }
  }

  // after decimal point
  var after = number.toFixed(this.allAfter);
  after = after.slice(after.indexOf('.') + 1);
  for (var j = after.length - 1; j > this.zeroAfter - 1; j--) {
    if (after[j] != '0') break;
    after = after.slice(0, -1);
  }

  // put together and return
  return before + (after ? this.pointChar + after : '');
};


// convert milliseconds to string using java format
// without timezone

/*jshint sub:true*/
locale.Date = function (pattern) {
  this.formatFunc = [];
  this.pattern = pattern;

  patterLibrary = {};

  patterLibrary['year'] =
    [
    {term : 'yyyy',
     func: function (date) { return String(date.getFullYear()); }},
    {term : 'yy',
     func: function (date) { return String(date.getFullYear()).slice(2); }}
     ];

  patterLibrary['month'] =
    [
    {term : 'MMMM',
     func: function (date) { return locale.dateMonthLong[date.getMonth()]; }},
    {term : 'MMM',
     func: function (date) { return locale.dateMonth[date.getMonth()]; }},
    {term : 'MM',
     func: function (date) {  return _padding(date.getMonth() + 1); }},
    {term : 'M',
     func: function (date) {  return date.getMonth() + 1; }}
     ];

  patterLibrary['week in year'] =
    [
    {term : 'ww',
     func: function (date) { return _padding(_weekInYear(date)); }},
    {term : 'w',
     func: function (date) { return _weekInYear(date); }}
     ];

  patterLibrary['week in month'] =
    [
    {term : 'WW',
     func: function (date) { return _padding(_weekInMonth(date)); }},
    {term : 'W',
     func: function (date) { return _weekInMonth(date); }}
     ];

  patterLibrary['day in month'] =
    [
    {term : 'dd',
     func: function (date) { return _padding(date.getDate()); }},
    {term : 'd',
     func: function (date) { return date.getDate(); }}
     ];

  patterLibrary['weekday'] =
    [
    {term : 'EE',
     func: function (date) { return locale.dateWeekdayLong[date.getDay()]; }},
    {term : 'E',
     func: function (date) { return locale.dateWeekday[date.getDay()]; }}
     ];

  patterLibrary['hour: 0 - 23'] =
    [
    {term : 'HH',
     func: function (date) { return _padding(date.getHours()); }},
    {term : 'H',
     func: function (date) { return date.getHours(); }}
     ];

  patterLibrary['hour: 1 - 12'] =
    [
    {term : 'KK',
     func: function (date) { return _padding((date.getHours() + 11) % 12 + 1); }},
    {term : 'K',
     func: function (date) { return (date.getHours() + 11) % 12 + 1; }}
     ];

  patterLibrary['am/pm marker'] =
    [
    {term : 'a',
     func: function (date) { return (date.getHours() < 12) ? locale.dateAM : locale.datePM; }}
     ];

  patterLibrary['minutes'] =
    [
    {term : 'mm',
     func: function (date) { return _padding(date.getMinutes()); }},
    {term : 'm',
     func: function (date) { return date.getMinutes(); }}
     ];

  patterLibrary['seconds'] =
    [
    {term : 'ss',
     func: function (date) { return _padding(date.getSeconds()); }},
    {term : 's',
     func: function (date) { return date.getSeconds(); }}
     ];

  patterLibrary['milliseconds'] =
    [
    {term : 'SSS',
     func: function (date) { return ('000' + date.getMilliseconds()).slice(-3); }},
     {term : 'S',
       func: function (date) { return date.getMilliseconds(); }}
     ];


  for (var l in patterLibrary) {
    for (var p = 0; p < patterLibrary[l].length; p += 1) {
      var test = patterLibrary[l][p];
      if (pattern.indexOf(test.term) > -1) {
        var closure = function (term, func) {
          return function (string, date) {
            return string.replace(term, func(date)) ;
          };
        }(test.term, test.func);
        this.formatFunc.push(closure);
        break;
      }
    }
  }

  function _padding (number)  {
    return (number <= 9 ? '0' + number : number);
  }

  function _weekInYear(date) {
    var onejan = new Date(date.getFullYear(), 0, 1);
    return Math.ceil((((date - onejan) / 86400000) + onejan.getDay() + 1) / 7);
  }

  function _weekInMonth(date) {
      var onemon = new Date(date.getFullYear(), date.getMonth(), 1);
      return Math.ceil((((date - onemon) / 86400000) + onemon.getDay() + 1) / 7);
  }
};

locale.Date.prototype.format = function format (time) {
  var ret = this.pattern,
    date = new Date(time);

  for (f = this.formatFunc.length - 1; f >= 0; f-- ) {
    ret = this.formatFunc[f](ret, date);
  }

  return ret;
};

// http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html
