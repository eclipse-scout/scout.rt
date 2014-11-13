scout.RichTextField = function() {
  scout.RichTextField.parent.call(this);
};
scout.inherits(scout.RichTextField, scout.ValueField);

scout.RichTextField.prototype._renderProperties = function() {
  scout.RichTextField.parent.prototype._renderProperties.call(this);
};

scout.RichTextField.prototype._render = function($parent) {
  this.addContainer($parent, 'rich-text-field');

  // create editabel div
  this.$field = $.makeDIV('field', 'Beispieltext');
  this.$field
    .blur(this._onFieldBlur.bind(this))
    .attr('contentEditable', 'true')
    .appendTo(this.$container);

  // return should not create div
  this.$field.keydown(function(e) {
    if (e.keyCode === 13) {
      document.execCommand('insertHTML', false, '<br><br>');
      return false;
    }
  });

  // command bar
  this.$commandBar = this.$container.appendDIV('rich-text-bar');

  this.$commandFormat = this.$commandBar.appendDIV('rich-text-bar-group');
  this.$commandFormat.appendDIV('rich-text-command rich-text-bar-bold', 'a').data('command', 'bold');
  this.$commandFormat.appendDIV('rich-text-command rich-text-bar-underline', 'a').data('command', 'underline');
  this.$commandFormat.appendDIV('rich-text-command rich-text-bar-strike', 'a').data('command', 'strikeThrough');

  this.$commandMark = this.$commandBar.appendDIV('rich-text-bar-group');
  this.$commandMark.appendDIV('rich-text-command rich-text-bar-white', '&nbsp');
  this.$commandMark.appendDIV('rich-text-command rich-text-bar-yellow', '&nbsp');
  this.$commandMark.appendDIV('rich-text-command rich-text-bar-green', '&nbsp');

  this.$commandList = this.$commandBar.appendDIV('rich-text-bar-group');
  this.$commandList.appendDIV('rich-text-command rich-text-bar-plain', 'ohne');
  this.$commandList.appendDIV('rich-text-command rich-text-bar-bullet', 'bullet');
  this.$commandList.appendDIV('rich-text-command rich-text-bar-number', 'num');

  // anders lösen, denn: feld soll über die ganze breite gehen
  //this.addLabel();
  //this.addMandatoryIndicator();
  //this.addStatus();
};

