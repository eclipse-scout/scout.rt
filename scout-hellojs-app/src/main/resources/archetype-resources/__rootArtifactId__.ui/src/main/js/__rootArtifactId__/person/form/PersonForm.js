#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
${rootArtifactId}.PersonForm = function() {
  ${rootArtifactId}.PersonForm.parent.call(this);

  this.firstNameField = null;
  this.lastNameField = null;
};
scout.inherits(${rootArtifactId}.PersonForm, scout.Form);

${rootArtifactId}.PersonForm.prototype._init = function(model) {
  ${rootArtifactId}.PersonForm.parent.prototype._init.call(this, model);
  this._initFields();
};

/**
 * Override this method if you have different fields.
 * Then you need to customize importData and exportData too.
 */
${rootArtifactId}.PersonForm.prototype._initFields = function() {
  this.firstNameField = this.widget('FirstNameField');
  this.lastNameField = this.widget('LastNameField');
};

${rootArtifactId}.PersonForm.prototype._jsonModel = function() {
  return scout.models.getModel('${rootArtifactId}.PersonForm');
};

${rootArtifactId}.PersonForm.prototype.exportData = function() {
  var person = this.data;
  person.setFirstName(this.firstNameField.value);
  person.setLastName(this.lastNameField.value);
  return person;
};

${rootArtifactId}.PersonForm.prototype.importData = function() {
  var person = this.data;
  this.firstNameField.setValue(person.firstName);
  this.lastNameField.setValue(person.lastName);
};

${rootArtifactId}.PersonForm.prototype._save = function(data) {
  return (data.personId ? ${rootArtifactId}.persons.store(data) : ${rootArtifactId}.persons.create(data))
    .then(this._onSaveDone.bind(this));
};

${rootArtifactId}.PersonForm.prototype._onSaveDone = function(person) {
  this.session.desktop.dataChange({
    dataType: ${rootArtifactId}.Person.EVENT_TYPE,
    data: person
  });

  return ${symbol_dollar}.resolvedPromise();
};

${rootArtifactId}.PersonForm.prototype._load = function() {
  if (this.data.personId) {
    // refresh data from server
    return ${rootArtifactId}.persons.load(this.data.personId);
  }
  return ${symbol_dollar}.resolvedPromise(this.data);
};
