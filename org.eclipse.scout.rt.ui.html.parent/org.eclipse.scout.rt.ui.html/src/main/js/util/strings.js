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
  }

};
