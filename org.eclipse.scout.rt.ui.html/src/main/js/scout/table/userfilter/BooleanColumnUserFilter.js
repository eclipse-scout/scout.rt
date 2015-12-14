scout.BooleanColumnUserFilter = function() {
  scout.BooleanColumnUserFilter.parent.call(this);

  this.freeText;
};
scout.inherits(scout.BooleanColumnUserFilter, scout.ColumnUserFilter);

/**
 * @override ColumnUserFilter.js
 */
scout.BooleanColumnUserFilter.prototype.createAddFilterEventData = function() {
  var data = scout.BooleanColumnUserFilter.parent.prototype.createAddFilterEventData.call(this);
  data.freeText = this.freeText;
  return data;
};

/**
 * @implements ColumnUserFilter.js
 */
scout.BooleanColumnUserFilter.prototype.fieldsFilterActive = function() {
  return scout.strings.hasText(this.freeText);
};

/**
* @implements ColumnUserFilter.js
*/
scout.BooleanColumnUserFilter.prototype.acceptByFields = function(key, normKey) {
   // FIXME AWE (filter) use cellTextForTextFilter
   return normKey.toLowerCase().indexOf(this.freeText.toLowerCase()) > -1;
};

/**
 * @implements ColumnUserFilter.js
 */
scout.BooleanColumnUserFilter.prototype.updateFilterFields = function(event) {
  $.log.debug('(TextColumnUserFilter#updateFilterFields) text=' + event.text);
  this.freeText = event.text.trim();
};

/**
 * @implements ColumnUserFilter.js
 */
scout.BooleanColumnUserFilter.prototype._useTextInsteadOfNormValue = function(value) {
  // null is valid, if for text columns. We do not want to store -empty-
  return value === null ? false : true;
};
