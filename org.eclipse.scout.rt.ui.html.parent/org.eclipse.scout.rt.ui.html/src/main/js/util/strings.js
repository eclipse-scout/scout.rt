scout.strings = {

  nl2br: function(text) {
    if (!text) {
      return text;
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
  }

};
