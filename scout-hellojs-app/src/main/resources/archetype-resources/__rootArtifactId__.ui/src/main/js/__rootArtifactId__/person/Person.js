#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
${rootArtifactId}.Person = function() {
  this.resourceType = 'Person';
  this.personId = null;
  this.firstName = null;
  this.lastName = null;
};

${rootArtifactId}.Person.EVENT_TYPE = 'person';

${rootArtifactId}.Person.prototype.init = function(model) {
  ${symbol_dollar}.extend(this, model);
};

${rootArtifactId}.Person.prototype.setFirstName = function(firstName) {
  this.firstName = firstName;
};

${rootArtifactId}.Person.prototype.setLastName = function(lastName) {
  this.lastName = lastName;
};

${rootArtifactId}.Person.prototype.setPersonId = function(id) {
  this.personId = id;
};
