scout.FilterFieldsGroupBox = function() {
  scout.FilterFieldsGroupBox.parent.call(this);
  this.gridColumnCount = 1;
  this.cssClass = 'filter-fields';
};
scout.inherits(scout.FilterFieldsGroupBox, scout.GroupBox);

scout.FilterFieldsGroupBox.prototype._init = function(model) {
  scout.FilterFieldsGroupBox.parent.prototype._init.call(this, model);
  this.filter.addFilterFields(this);
};

/**
 * @override GroupBox.js
 */
scout.FilterFieldsGroupBox.prototype._renderProperties = function($parent) {
  scout.FilterFieldsGroupBox.parent.prototype._renderProperties.call(this, $parent);
  this.filter.modifyFilterFields();
};

// TODO [7.0] awe: (filter) es braucht wahrscheinlich auch eine range-validierung? z.B. from muss kleiner sein als to
// Prüfen ob wir eine sequence-box dafür verwenden wollen und dafür eine client-seitige validierung impl., diese
// geschieht heute auf dem UI server. Evtl. wäre auch ein from/to validator für beliebige felder sinnvoll (auch
// ausserhalb einer sequence-box)
scout.FilterFieldsGroupBox.prototype.addFilterField = function(objectType, text) {
  var field = scout.create(objectType, {
    parent: this,
    label: this.session.text(text),
    statusVisible: false,
    labelWidthInPixel: 50,
    maxLength: 100,
    updateDisplayTextOnModify: true
  });
  this.addField0(field);
  return field;
};

// TODO [7.0] awe, cgu: (addField): see to-do in TileContainerBox.js
// Added '0' to the name to avoid temporarily to avoid naming conflict with FormField#addField
scout.FilterFieldsGroupBox.prototype.addField0 = function(field) {
  this.fields.push(field);
  this._prepareFields();
};
