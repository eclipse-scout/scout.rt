#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
${simpleArtifactName}.Person = function() {
  this.resourceType = 'Person';
  this.personId = null;
  this.firstName = null;
  this.lastName = null;
};

${simpleArtifactName}.Person.EVENT_TYPE = 'person';

${simpleArtifactName}.Person.prototype.init = function(model) {
  ${symbol_dollar}.extend(this, model);
};

${simpleArtifactName}.Person.prototype.setFirstName = function(firstName) {
  this.firstName = firstName;
};

${simpleArtifactName}.Person.prototype.setLastName = function(lastName) {
  this.lastName = lastName;
};

${simpleArtifactName}.Person.prototype.setPersonId = function(id) {
  this.personId = id;
};
