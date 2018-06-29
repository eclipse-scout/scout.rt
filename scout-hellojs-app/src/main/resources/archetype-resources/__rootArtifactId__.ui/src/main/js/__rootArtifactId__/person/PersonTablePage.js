#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
${rootArtifactId}.PersonTablePage = function() {
  ${rootArtifactId}.PersonTablePage.parent.call(this);

  this._dataChangeListener = null;
};
scout.inherits(${rootArtifactId}.PersonTablePage, scout.PageWithTable);

${rootArtifactId}.PersonTablePage.prototype._jsonModel = function() {
  return scout.models.getModel('${rootArtifactId}.PersonTablePage');
};

${rootArtifactId}.PersonTablePage.prototype._init = function(model) {
  var m = ${symbol_dollar}.extend({}, this._jsonModel(), model);
  ${rootArtifactId}.PersonTablePage.parent.prototype._init.call(this, m);
  this._initListeners();
};

/**
 * Override this method if you want to customize the menu entries.
 */
${rootArtifactId}.PersonTablePage.prototype._initListeners = function() {
  this._dataChangeListener = this._onDataChange.bind(this);
  this.session.desktop.on('dataChange', this._dataChangeListener);

  var editPersonMenu = this.detailTable.widget('EditPersonMenu');
  editPersonMenu.on('action', this._onEditPersonMenuAction.bind(this));

  var deletePersonMenu = this.detailTable.widget('DeletePersonMenu');
  deletePersonMenu.on('action', this._onDeletePersonMenuAction.bind(this));

  var createPersonMenu = this.detailTable.widget('CreatePersonMenu');
  createPersonMenu.on('action', this._onCreatePersonMenuAction.bind(this));
};

${rootArtifactId}.PersonTablePage.prototype._destroy = function() {
  this.session.desktop.off('dataChange', this._dataChangeListener);
  ${rootArtifactId}.PersonTablePage.parent.prototype._destroy.call(this);
};

${rootArtifactId}.PersonTablePage.prototype._onDataChange = function(event) {
  if (event.dataType === ${rootArtifactId}.Person.EVENT_TYPE) {
    this.reloadPage();
  }
};

${rootArtifactId}.PersonTablePage.prototype._loadTableData = function() {
  return ${rootArtifactId}.persons.list();
};

${rootArtifactId}.PersonTablePage.prototype._transformTableDataToTableRows = function(tableData) {
  return tableData
    .map(function(person) {
      return {
        person: person,
        cells: [
          person.personId,
          person.firstName,
          person.lastName
        ]
      };
    });
};

${rootArtifactId}.PersonTablePage.prototype._getSelectedPerson = function() {
  var selection = this.detailTable.selectedRow();
  if (selection) {
    return selection.person;
  }
  return null;
};

${rootArtifactId}.PersonTablePage.prototype._createPersonForm = function() {
  var outline = this.getOutline();
  var personForm = scout.create('${rootArtifactId}.PersonForm', {
    parent: outline
  });
  return personForm;
};

${rootArtifactId}.PersonTablePage.prototype._onEditPersonMenuAction = function(event) {
  var personForm = this._createPersonForm();
  personForm.setData(this._getSelectedPerson());
  personForm.open();
};

${rootArtifactId}.PersonTablePage.prototype._onDeletePersonMenuAction = function(event) {
  scout.MessageBoxes.openYesNo(this.session.desktop, this.session.text('DeleteConfirmationTextNoItemList'))
    .then(function(button) {
      if (button === scout.MessageBox.Buttons.YES) {
        ${rootArtifactId}.persons.remove(this._getSelectedPerson().personId)
          .then(this._onPersonDeleted.bind(this));
      }
    }.bind(this));
};

${rootArtifactId}.PersonTablePage.prototype._onPersonDeleted = function() {
  this.session.desktop.dataChange({
    dataType: ${rootArtifactId}.Person.EVENT_TYPE
  });
};

${rootArtifactId}.PersonTablePage.prototype._onCreatePersonMenuAction = function(event) {
  var personForm = this._createPersonForm();
  var emptyPerson = scout.create('${rootArtifactId}.Person', {}, {
    ensureUniqueId: false
  });
  personForm.setData(emptyPerson);
  personForm.open();
};
