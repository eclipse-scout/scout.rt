#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
${symbol_dollar}(document).ready(function() {
  var app = new ${simpleArtifactName}.App();
  app.init({
    bootstrap: {
      modelsUrl: 'res/${simpleArtifactName}-all-macro.json',
      textsUrl: 'res/texts.json',
      localesUrl: 'res/locales.json'
    }
  });
});
