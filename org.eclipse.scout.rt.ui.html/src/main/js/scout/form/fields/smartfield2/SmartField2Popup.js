scout.SmartField2Popup = function() {
  scout.SmartField2Popup.parent.call(this);
  this.animateRemoval = true;
};
scout.inherits(scout.SmartField2Popup, scout.Popup);

scout.SmartField2Popup.prototype._init = function(model) {
  scout.SmartField2Popup.parent.prototype._init.call(this, model);
  this.proposalChooser = this._createProposalChooser(model);
};

scout.SmartField2Popup.prototype._createProposalChooser = function(model) {
  return new scout.TableProposalChooser2(this, this._onLookupRowSelected.bind(this));
};

scout.SmartField2Popup.prototype._smartField = function() {
  return this.parent;
};

scout.SmartField2Popup.prototype._smartFieldBounds = function() {
  return scout.graphics.offsetBounds(this.parent.$field);
};

/**
 * @override
 */
scout.SmartField2Popup.prototype._createLayout = function() {
  if (this._smartField().variant === scout.SmartField2.Variant.DROPDOWN) {
    return new scout.DropdownPopupLayout(this, this.proposalChooser);
  } else {
    return new scout.SmartField2PopupLayout(this, this.proposalChooser);
  }
};

scout.SmartField2Popup.prototype._render = function($parent) {
  scout.SmartField2Popup.parent.prototype._render.call(this, $parent);
  this.$container.addClass('dropdown-popup');
  this.proposalChooser.render(this.$container);
};

scout.SmartField2Popup.prototype.setLookupRows = function(lookupRows) {
  this.proposalChooser.setLookupRows(lookupRows);
};

scout.SmartField2Popup.prototype.getSelectedLookupRow = function() {
  return this.proposalChooser.getSelectedLookupRow();
};

/**
 * Delegates the key event to the proposal chooser.
 */
scout.SmartField2Popup.prototype.delegateKeyEvent = function(event) {
  event.originalEvent.smartFieldEvent = true;
  this.proposalChooser.delegateKeyEvent(event);
};

scout.SmartField2Popup.prototype._onLookupRowSelected = function(lookupRow) {
  this.trigger('select', {
    lookupRow: lookupRow
  });
};
