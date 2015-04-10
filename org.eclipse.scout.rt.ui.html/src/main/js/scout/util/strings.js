scout.strings = {

  /**
   * @param text
   * @param encodeHtml defaults to true
   */
  nl2br: function(text, encodeHtml) {
    if (!text) {
      return text;
    }
    encodeHtml = encodeHtml !== undefined ? encodeHtml : true;
    if (encodeHtml) {
      text = scout.strings.encode(text);
    }
    return text.replace(/\n/g, '<br>');
  },

  removeAmpersand: function(text) {
    if (!text) {
      return text;
    }
    // Remove single & that are not surrounded by & or &&
    text = text.replace(/(^|[^&]|&&)&($|[^&]|&&)/g, '$1$2');
    // Replace remaining && by a single &
    text = text.replace(/&&/g, '&');
    return text;
  },

  hasText: function(text) {
    if (typeof text !== 'string' || text.length === 0) {
      return false;
    }
    return !/^\s*$/.test(text);
  },

  repeat: function(pattern, count) {
    if (pattern === undefined || pattern === null) {
      return pattern;
    }
    if (typeof count !== 'number' || count < 1) {
      return '';
    }
    var result = '';
    for (var i = 0; i < count; i++) {
      result += pattern;
    }
    return result;
  },

  padZeroLeft: function(s, padding) {
    if (s === undefined || s === null) {
      return s;
    }
    if (typeof padding !== 'number' || padding < 1 || (s + '').length >= padding) {
      return s;
    }
    var z = scout.strings.repeat('0', padding) + s;
    return z.slice(-padding);
  },

  nvl: function(s, defaultValue) {
    if (s === undefined || s === null) {
      return defaultValue;
    }
    return s;
  },

  startsWith: function(fullString, startString) {
    if (fullString === undefined || fullString === null || startString === undefined || startString === null) {
      return false;
    }
    if (startString.length === 0) {
      return true;
    }
    return (fullString.substr(0, startString.length) === startString);
  },

  endsWith: function(fullString, endString) {
    if (fullString === undefined || fullString === null || endString === undefined || endString === null) {
      return false;
    }
    if (endString.length === 0) {
      return true;
    }
    return (fullString.substr(-endString.length) === endString);
  },

  /**
   * Returns the number of occurrences of 'separator' in 'string'
   */
  count: function(string, separator) {
    if (!string || !separator) {
      return 0;
    }
    return string.split(separator).length - 1;
  },

  /**
   * Encodes the html of the given string.
   */
  encode: function(string) {
    if (!string) {
      return string;
    }
    var $div = $('<div>');
    return $div.text(string).html();
  }
};
