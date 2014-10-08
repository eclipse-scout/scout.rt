scout.texts = {
  'textMap': {
      'loadOptions':'Lade Optionen...',
      'noOptions':'Keine Übereinstimmung',
      'oneOption':'1 Option',
      'options':'{0} Optionen'
  }
};

/**
 * Returns a localized text for the given text key using the current locale.
 * @param textKey
 * @param [optional1, optional2, ...] used to replace placeholders in the text-string {0}
 * TODO AWE: (framework) load texts from server on client start-up, remove hardcoded texts
 */
scout.texts.get = function(textKey) {
  if (scout.texts.textMap.hasOwnProperty(textKey)) {
    var len = arguments.length,
        text = scout.texts.textMap[textKey];
    if (len === 1) {
      return text;
    } else {
      var i, placeholder;
      for (i = 1; i < len; i++) {
        placeholder = '{' + (i - 1) + '}';
        text = text.replace(placeholder, arguments[i]);
      }
      return text;
    }
  } else {
    return '[undefined text: ' + textKey + ']';
  }
};
