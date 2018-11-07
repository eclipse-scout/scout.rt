${simpleArtifactName}.PersonSearchForm = function() {
  ${simpleArtifactName}.PersonSearchForm.parent.call(this);
};
scout.inherits(${simpleArtifactName}.PersonSearchForm, scout.Form);

${simpleArtifactName}.PersonSearchForm.prototype._init = function(model) {
  ${simpleArtifactName}.PersonSearchForm.parent.prototype._init.call(this, model);
  this._initListeners();
};

${simpleArtifactName}.PersonSearchForm.prototype._jsonModel = function() {
  return scout.models.getModel('${simpleArtifactName}.PersonSearchForm');
};

${simpleArtifactName}.PersonSearchForm.prototype._initListeners = function() {
  var parentTable = this.parent.table;
  this.widget('SearchButton').on('action', parentTable.reload.bind(parentTable));
};

${simpleArtifactName}.PersonSearchForm.prototype.exportData = function() {
  return {
    firstName: this.widget('FirstNameField').value,
    lastName: this.widget('LastNameField').value
  };
};
