scout.CheckBoxField = function() {
  scout.CheckBoxField.parent.call(this);
  this.$checkBox;
  this.$checkBoxLabel;
};
scout.inherits(scout.CheckBoxField, scout.ValueField);

/**
 * @override ModelAdapter
 */
scout.CheckBoxField.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.CheckBoxField.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  keyStrokeContext.registerKeyStroke(new scout.CheckBoxToggleKeyStroke(this));
};

scout.CheckBoxField.prototype._render = function($parent) {
  this.addContainer($parent, 'check-box-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addField($('<div>'));

  this.$checkBox = $.makeDiv('check-box')
    .appendTo(this.$field)
    .on('mousedown', this._onMouseDown.bind(this));

  this.$checkBoxLabel = $.makeDiv('label')
    .appendTo(this.$field)
    .on('mousedown', this._onMouseDown.bind(this));

  scout.tooltips.install(this.$checkBoxLabel, {
    parent: this,
    tooltipText: function($label) {
      if ($label.isContentTruncated()) {
        return $label.text();
      }
    }
  });

  this.addStatus();
};

scout.CheckBoxField.prototype.acceptInput = function(whileTyping, forceSend) {
  //nop;
};

scout.CheckBoxField.prototype._renderDisplayText = function(displayText) {
  //nop;
};

scout.CheckBoxField.prototype._onMouseDown = function() {
  this._toggleChecked();
};

scout.CheckBoxField.prototype._toggleChecked = function() {
  if (!this.enabled) {
    return;
  }
  this.$checkBox.toggleClass('checked');
  var uiChecked = this.$checkBox.hasClass('checked');
  this._send('clicked', {checked: uiChecked});
};

/**
 * @override
 */
scout.CheckBoxField.prototype._renderEnabled = function(enabled) {
  scout.CheckBoxField.parent.prototype._renderEnabled.call(this);
  if (this.enabled) {
    this.$checkBox.attr('tabindex', '0');
  } else {
    this.$checkBox.removeAttr('tabindex');
  }
  this.$checkBox.setEnabled(this.enabled);
};

scout.CheckBoxField.prototype._renderProperties = function() {
  scout.CheckBoxField.parent.prototype._renderProperties.call(this);
  this._renderValue(this.value);
};

scout.CheckBoxField.prototype._renderValue = function(value) {
  this.$checkBox.toggleClass('checked', value);
};

/**
 * @override
 */
scout.CheckBoxField.prototype._renderLabel = function(label) {
  if (this.$checkBoxLabel) {
    this.$checkBoxLabel.text(label || '');
  }
};
