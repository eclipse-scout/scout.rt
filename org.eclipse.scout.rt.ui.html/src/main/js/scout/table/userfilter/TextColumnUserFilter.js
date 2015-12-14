scout.TextColumnUserFilter = function() {
  scout.TextColumnUserFilter.parent.call(this);

  this.freeText;
  this.freeTextField;
};
scout.inherits(scout.TextColumnUserFilter, scout.ColumnUserFilter);

/**
 * @override ColumnUserFilter.js
 */
scout.TextColumnUserFilter.prototype.createAddFilterEventData = function() {
  var data = scout.TextColumnUserFilter.parent.prototype.createAddFilterEventData.call(this);
  data.freeText = this.freeText;
  return data;
};

/**
 * @override ColumnUserFilter.js
 */
scout.TextColumnUserFilter.prototype.fieldsFilterActive = function() {
  return scout.strings.hasText(this.freeText);
};

/**
* @override ColumnUserFilter.js
*/
scout.TextColumnUserFilter.prototype.acceptByFields = function(key, normKey) {
   // FIXME AWE (filter) use cellTextForTextFilter
   return normKey.toLowerCase().indexOf(this.freeText.toLowerCase()) > -1;
};

/**
 * @implements ColumnUserFilter.js
 */
scout.TextColumnUserFilter.prototype._useTextInsteadOfNormValue = function(value) {
  // null is valid, if for text columns. We do not want to store -empty-
  return value === null ? false : true;
};

/**
 * @implements ColumnUserFilter.js
 */
scout.TextColumnUserFilter.prototype.filterFieldsTitle = function() {
  return this.session.text('ui.FreeText');
};

/**
 * @override ColumnUserFilter.js
 */
scout.TextColumnUserFilter.prototype.addFilterFields = function(groupBox) {
  this.freeTextField = scout.create('StringField', {
    parent: groupBox,
    labelVisible: false,
    statusVisible: false,
    maxLength: 100,
    displayText: this.freeText
  });
  this.freeTextField.on('displayTextChanged', this._onDisplayTextChanged.bind(this));
  groupBox.addField0(this.freeTextField);
};

scout.TextColumnUserFilter.prototype._onDisplayTextChanged = function(event) {
  this.freeText = event.displayText.trim();
  $.log.debug('(TextColumnUserFilter#_onDisplayTextChanged) freeText=' + this.freeText);
  this.triggerFilterFieldsChanged(event);
};

/**
 * @override ColumnUserFilter.js
 */
scout.TextColumnUserFilter.prototype.modifyFilterFields = function() {
  this.freeTextField.$mandatory.remove();
  this.freeTextField.$mandatory = null;
};
