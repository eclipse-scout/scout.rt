scout.FilterFieldsGroupBox = function() {
  scout.FilterFieldsGroupBox.parent.call(this);
};
scout.inherits(scout.FilterFieldsGroupBox, scout.GroupBox);

scout.FilterFieldsGroupBox.prototype._init = function(model) {
  scout.FilterFieldsGroupBox.parent.prototype._init.call(this, model);
  this.filter = model.filter;
  this.filter.addFilterFields(this);
  this.cssClass = 'filter-fields';
};

/**
 * @override GroupBox.js
 */
scout.FilterFieldsGroupBox.prototype._render = function($parent) {
  scout.FilterFieldsGroupBox.parent.prototype._render.call(this, $parent);
  this.filter.modifyFilterFields();
};

// FIXME AWE: (filter) es braucht wahrscheinlich auch eine range-validierung? z.B. from muss kleiner sein als to
// Prüfen ob wir eine sequence-box dafür verwenden wollen und dafür eine client-seitige validierung impl., diese
// geschieht heute auf dem UI server. Evtl. wäre auch ein from/to validator für beliebige felder sinnvoll (auch
// ausserhalb einer sequence-box)
scout.FilterFieldsGroupBox.prototype.addFilterField = function(objectType, text, gridY) {
  var field = scout.create(objectType, {
    parent: this,
    label: this.session.text(text),
    statusVisible: false,
    labelWidthInPixel: 50,
    maxLength: 100,
    gridData: {
      y: gridY
    }
  });
  this.addField0(field);
  return field;
};

// FIXME AWE: see fix-me in TileContainerBox.js
// Added '0' to the name to avoid temporarily to avoid naming conflict with FormField#addField
scout.FilterFieldsGroupBox.prototype.addField0 = function(field) {
  this.fields.push(field);
};
