scout.RichTextField = function() {
  scout.RichTextField.parent.call(this);
};
scout.inherits(scout.RichTextField, scout.ValueField);

scout.RichTextField.prototype._renderProperties = function() {
  scout.RichTextField.parent.prototype._renderProperties.call(this);
};

scout.RichTextField.prototype._render = function($parent) {
  this.addContainer($parent, 'rich-text-field');

  // create editable div
  this.addField(
    $.makeDIV('field', 'Beispieltext').
      attr('contentEditable', 'true').
      blur(this._onFieldBlur.bind(this)).
      // return should not create div
      keydown(function(e) {
        if (e.keyCode === 13) {
          document.execCommand('insertHTML', false, '<br><br>');
          return false;
        }
      }));
  
  // command bar
  this.$commandBar = this.$container.appendDIV('rich-text-bar');

  this.$commandFormat = this.$commandBar.appendDIV('rich-text-bar-group');

  this.$commandFormat.appendDIV('rich-text-command rich-text-bar-bold', 'a')
    .data('command', 'bold').data('attribute', '');
  this.$commandFormat.appendDIV('rich-text-command rich-text-bar-underline', 'a')
    .data('command', 'underline').data('attribute', '');
  this.$commandFormat.appendDIV('rich-text-command rich-text-bar-strike', 'a')
    .data('command', 'strikeThrough').data('attribute', '');

  this.$commandMark = this.$commandBar.appendDIV('rich-text-bar-group');
  this.$commandMark.appendDIV('rich-text-command rich-text-bar-white', '&nbsp')
    .data('command', 'BackColor').data('attribute', 'white');
  this.$commandMark.appendDIV('rich-text-command rich-text-bar-yellow', '&nbsp')
    .data('command', 'BackColor').data('attribute', 'yellow');
  this.$commandMark.appendDIV('rich-text-command rich-text-bar-green', '&nbsp')
    .data('command', 'BackColor').data('attribute', 'green');

  this.$commandList = this.$commandBar.appendDIV('rich-text-bar-group');
  this.$commandList.appendDIV('rich-text-command rich-text-bar-plain', 'ohne')
    .data('command', 'outdent').data('attribute', '');
  this.$commandList.appendDIV('rich-text-command rich-text-bar-bullet', 'bullet')
    .data('command', 'insertunorderedlist').data('attribute', '');
  this.$commandList.appendDIV('rich-text-command rich-text-bar-number', 'num')
    .data('command', 'insertorderedlist').data('attribute', 'green');

  this.$commandList = this.$commandBar.appendDIV('rich-text-bar-group');
  this.$commandList.appendDIV('rich-text-command rich-text-bar-line', 'line')
    .data('command', 'inserthorizontalrule').data('attribute', '');

  this.$commandList.appendDIV('rich-text-command rich-text-bar-line', 'numline')
    .data('command', 'inserthorizontalrule').data('attribute', '');

  $('.rich-text-command', this.$commandBar).click((this._onCommandClick.bind(this)));

  // anders lösen, denn: feld soll über die ganze breite gehen
  //this.addLabel();
  //this.addMandatoryIndicator();
  //this.addStatus();
};

scout.RichTextField.prototype._onCommandClick = function(event) {
  var command = $(event.target).data('command'),
    attribute = $(event.target).data('attribute');
  document.execCommand (command, false, attribute);
  // in some cases set cursor at the end of selection
  // todo
};
