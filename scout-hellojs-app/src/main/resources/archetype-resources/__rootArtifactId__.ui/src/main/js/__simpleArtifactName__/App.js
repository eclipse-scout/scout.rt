#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
${simpleArtifactName}.App = function() {
  ${simpleArtifactName}.App.parent.call(this);
  this.apiUrl = '../api/';
  this.appPrefix = '${simpleArtifactName}.';
};
scout.inherits(${simpleArtifactName}.App, scout.App);

${simpleArtifactName}.App.prototype._createSession = function(options) {
  var session = ${simpleArtifactName}.App.parent.prototype._createSession.call(this, options);
  session.on('localeSwitch', this._onLocaleSwitch.bind(this));
  return session;
};

${simpleArtifactName}.App.prototype._loadLocale = function() {
  var localeTag = scout.webstorage.getItem(sessionStorage, "locale");
  if (scout.strings.hasText(localeTag)) {
    var locale = scout.locales.get(localeTag);
    if (locale) {
      return locale;
    }
    ${symbol_dollar}.log.warn('Unsupported languageTag in settings found: ' + localeTag + '. Using navigator locale.');
  }

  // Use the default locale
  return scout.locales.getNavigatorLocale();
};

${simpleArtifactName}.App.prototype._onLocaleSwitch = function(event) {
  scout.webstorage.setItem(sessionStorage, "locale", event.locale.languageTag);
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
};
