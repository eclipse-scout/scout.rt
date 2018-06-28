#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
${symbol_dollar}(document).ready(function() {
  var app = new ${rootArtifactId}.App();
  app.init({
    bootstrap: {
      modelsUrl: 'res/${rootArtifactId}-all-macro.json',
      textsUrl: 'res/texts.json',
      localesUrl: 'res/locales.json'
    }
  });
});
