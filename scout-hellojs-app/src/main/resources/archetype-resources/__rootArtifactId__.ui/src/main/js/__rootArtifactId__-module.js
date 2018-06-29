(function(${rootArtifactId}, scout, $, undefined) {
  __include("${rootArtifactId}/common/ajax.js");
  __include("${rootArtifactId}/App.js");
  __include("${rootArtifactId}/repository/Repository.js");

  __include("${rootArtifactId}/person/PersonForm.js");
  __include("${rootArtifactId}/person/PersonRepository.js");
  __include("${rootArtifactId}/person/Person.js");
  __include("${rootArtifactId}/person/PersonTablePage.js");
}(window.${rootArtifactId} = window.${rootArtifactId} || {}, scout, jQuery));
