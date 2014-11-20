scout.texts = {
  'textMap': {
      'back':'Zurück',
      'home':'Home',
      'loadOptions':'Lade Optionen...',
      'noOptions':'Keine Übereinstimmung',
      'oneOption':'1 Option',
      'options':'{0} Optionen',
      'invalidDateFormat':'Das Datum ist in einem ungültigen Format',
      'resetColumns':'Spalten zurücksetzen',
      'filterBy':'Filtern nach ...',
      'searchFor':'Suchen nach ...',
      'tableRowCount0': 'Keine Zeile',
      'tableRowCount1': 'Eine Zeile',
      'tableRowCount': '{0} Zeilen',
      'numRowsSelected': '{0} selektiert',
      'selectAll': 'Alle selektieren',
      'selectNone': 'Keine selektieren',
      'numRowsFiltered': '{0} gefiltert',
      'numRowsFilteredBy': '{0} gefiltert durch {1}',
      'removeFilter': 'Filter entfernen',
      'numRowsLoaded': '{0} geladen',
      'reloadData': 'Daten neu laden',
      'showEveryDate': 'jedes Datum anzeigen',
      'groupedByWeekday': 'gruppiert nach Wochentag',
      'groupedByMonth': 'gruppiert nach Monat',
      'groupedByYear': 'gruppiert nach Jahr',
      'count': 'Anzahl'
  }
};

/**
 * Returns a localized text for the given text key using the current locale.
 * @param textKey
 * @param [optional1, optional2, ...] used to replace placeholders in the text-string {0}
 * TODO AWE: (framework) load texts from server on client start-up, remove hardcoded texts
 */
//FIXME CGU this should be moved to session -> locale may be different for each session
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
