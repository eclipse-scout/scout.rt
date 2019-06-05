#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
scout.objectFactories = $.extend(scout.objectFactories, {
  'Desktop': function() {
    return new ${simpleArtifactName}.Desktop();
  }
});
