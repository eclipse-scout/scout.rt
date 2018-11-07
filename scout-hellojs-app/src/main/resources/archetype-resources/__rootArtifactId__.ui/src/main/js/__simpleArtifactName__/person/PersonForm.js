#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
${simpleArtifactName}.PersonForm = function() {
  ${simpleArtifactName}.PersonForm.parent.call(this);

  this.firstNameField = null;
  this.lastNameField = null;
  this.salaryField = null;
  this.externalField = null;
};
scout.inherits(${simpleArtifactName}.PersonForm, scout.Form);

${simpleArtifactName}.PersonForm.prototype._jsonModel = function() {
  return scout.models.getModel('${simpleArtifactName}.PersonForm');
};

${simpleArtifactName}.PersonForm.prototype._init = function(model) {
  ${simpleArtifactName}.PersonForm.parent.prototype._init.call(this, model);
  this._initFields();
};

/**
 * Override this method if you have different fields.
 * Then you need to customize importData and exportData too.
 */
${simpleArtifactName}.PersonForm.prototype._initFields = function() {
  this.firstNameField = this.widget('FirstNameField');
  this.lastNameField = this.widget('LastNameField');
  this.salaryField = this.widget('SalaryField');
  this.externalField = this.widget('ExternalField');
};

${simpleArtifactName}.PersonForm.prototype.exportData = function() {
  var person = this.data;
  person.setFirstName(this.firstNameField.value);
  person.setLastName(this.lastNameField.value);
  person.setSalary(this.salaryField.value);
  person.setExternal(this.externalField.value);
  return person;
};

${simpleArtifactName}.PersonForm.prototype.importData = function() {
  var person = this.data;
  this.firstNameField.setValue(person.firstName);
  this.lastNameField.setValue(person.lastName);
  this.salaryField.setValue(person.salary);
  this.externalField.setValue(person.external);
};

${simpleArtifactName}.PersonForm.prototype._save = function(data) {
  return (data.personId ? ${simpleArtifactName}.persons.store(data) : ${simpleArtifactName}.persons.create(data))
    .then(this._onSaveDone.bind(this));
};

${simpleArtifactName}.PersonForm.prototype._onSaveDone = function(person) {
  this.session.desktop.dataChange({
    dataType: ${simpleArtifactName}.Person.EVENT_TYPE,
    data: person
  });

  return ${symbol_dollar}.resolvedPromise();
};

${simpleArtifactName}.PersonForm.prototype._load = function() {
  if (this.data.personId) {
    // refresh data from server
    return ${simpleArtifactName}.persons.load(this.data.personId);
  }
  return ${symbol_dollar}.resolvedPromise(this.data);
};
