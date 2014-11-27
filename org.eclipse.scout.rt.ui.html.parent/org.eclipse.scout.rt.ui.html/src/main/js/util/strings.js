scout.strings = {

  nl2br: function(text) {
    if (!text) {
      return text;
    }
    return text.replace(/\n/g,'<br>');
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

  /**
   * Fastest algorithm (source: http://stackoverflow.com/a/5450113)
   */
  repeat: function (pattern, count) {
    if (pattern === undefined || pattern === null) {
      return pattern;
    }
    if (typeof count !== 'number' || count < 1) {
      return '';
    }
    var result = '';
    while (count > 1) {
        if (count & 1) {
          result += pattern;
        }
        count >>= 1;
        pattern += pattern;
    }
    return result + pattern;
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
  }

};
