import {App as App_1, locales, strings, webstorage, scout} from '@eclipse-scout/core';
import * as $ from 'jquery';

export default class App extends App_1 {

  constructor() {
    super();
    this.apiUrl = '../api/';
    this.appPrefix = '${simpleArtifactName}.';
  }


  _createSession(options) {
    var session = super._createSession(options);
    session.on('localeSwitch', this._onLocaleSwitch.bind(this));
    return session;
  }

  _loadLocale() {
    var localeTag = webstorage.getItem(sessionStorage, "locale");
    if (strings.hasText(localeTag)) {
      var locale = locales.get(localeTag);
      if (locale) {
        return locale;
      }
      $.log.warn('Unsupported languageTag in settings found: ' + localeTag + '. Using navigator locale.');
    }

    // Use the default locale
    return locales.getNavigatorLocale();
  }

  _onLocaleSwitch(event) {
    webstorage.setItem(sessionStorage, "locale", event.locale.languageTag);
    var session = event.source;
    var busyIndicator = scout.create('BusyIndicator', {
      parent: session.desktop,
      cancellable: false,
      showTimeout: 0,
      details: session.text('LanguageSwitchLoading')
    });
    busyIndicator.render();
    setTimeout(function() {
      window.location.reload();
    }, 100);
  }
}
