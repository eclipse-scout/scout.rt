import Scout from '../Scout';
import DateFormat from '../text/DateFormat';
import DecimalFormat from '../text/DecimalFormat';

export default class Locale {

  constructor(model) {
    model = Scout.nvl(model, DEFAULT_LOCALE_MODEL);

    this.languageTag = model.languageTag;
    var tags = Locale.splitLanguageTag(this.languageTag);
    this.language = tags[0];
    this.country = tags[1];
    this.displayLanguage = model.displayLanguage;
    this.displayCountry = model.displayCountry;

    this.decimalFormatPatternDefault = model.decimalFormatPatternDefault;
    this.decimalFormatSymbols = model.decimalFormatSymbols;

    if (this.decimalFormatPatternDefault && this.decimalFormatSymbols) {
      this.decimalFormat = new DecimalFormat(model);
    }

    this.dateFormatPatternDefault = model.dateFormatPatternDefault;
    this.dateFormatSymbols = model.dateFormatSymbols;
    this.timeFormatPatternDefault = model.timeFormatPatternDefault;

    if (this.dateFormatPatternDefault && this.dateFormatSymbols) {
      this.dateFormat = new DateFormat(model);
    }
  }

  static ensure(locale) {
    if (!locale) {
      return locale;
    }
    if (locale instanceof Locale) {
      return locale;
    }
    return new Locale(locale);
  };

  static splitLanguageTag(languageTag) {
    if (!languageTag) {
      return [];
    }
    return languageTag.split('-');
  }

  static getDefault() {
    return DEFAULT_LOCALE;
  }
}

const DEFAULT_LOCALE_MODEL = Object.freeze({
  languageTag: 'en-US',
  decimalFormatPatternDefault: '#,##0.###',
  dateFormatPatternDefault: 'MM/dd/yyyy',
  timeFormatPatternDefault: 'h:mm a',
  decimalFormatSymbols: {
    decimalSeparator: '.',
    groupingSeparator: ',',
    minusSign: '-'
  },
  dateFormatSymbols: {
    months: [
      'January',
      'February',
      'March',
      'April',
      'May',
      'June',
      'July',
      'August',
      'September',
      'October',
      'November',
      'December'
    ],
    monthsShort: [
      'Jan',
      'Feb',
      'Mar',
      'Apr',
      'May',
      'Jun',
      'Jul',
      'Aug',
      'Sep',
      'Oct',
      'Nov',
      'Dec'
    ],
    weekdays: [
      'Sunday',
      'Monday',
      'Tuesday',
      'Wednesday',
      'Thursday',
      'Friday',
      'Saturday'
    ],
    weekdaysShort: [
      'Sun',
      'Mon',
      'Tue',
      'Wed',
      'Thu',
      'Fri',
      'Sat'
    ],
    am: 'AM',
    pm: 'PM'
  }
});
const DEFAULT_LOCALE = Object.freeze(new Locale());
