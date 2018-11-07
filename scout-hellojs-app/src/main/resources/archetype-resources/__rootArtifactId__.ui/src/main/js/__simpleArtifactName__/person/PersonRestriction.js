#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
${simpleArtifactName}.PersonRestriction = function() {
  this.resourceType = 'PersonRestriction';
  this.firstName = null;
  this.lastName = null;
};

${simpleArtifactName}.PersonRestriction.prototype.init = function(model) {
  ${symbol_dollar}.extend(this, model);
};

${simpleArtifactName}.PersonRestriction.prototype.setFirstName = function(firstName) {
  this.firstName = firstName;
};

${simpleArtifactName}.PersonRestriction.prototype.setLastName = function(lastName) {
  this.lastName = lastName;
};
