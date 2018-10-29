(function(${simpleArtifactName}, scout, $, undefined) {
  __include("${simpleArtifactName}/common/ajax.js");
  __include("${simpleArtifactName}/App.js");
  __include("${simpleArtifactName}/repository/Repository.js");

  __include("${simpleArtifactName}/desktop/Desktop.js");

  __include("${simpleArtifactName}/person/PersonForm.js");
  __include("${simpleArtifactName}/person/PersonRepository.js");
  __include("${simpleArtifactName}/person/Person.js");
  __include("${simpleArtifactName}/person/PersonTablePage.js");
}(window.${simpleArtifactName} = window.${simpleArtifactName} || {}, scout, jQuery));
