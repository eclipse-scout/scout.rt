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
    $.makeDIV('', 'Beispieltext').
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
    .data('command', 'bold');
  this.$commandFormat.appendDIV('rich-text-command rich-text-bar-strike', 'a')
    .data('command', 'strikeThrough');
  this.$commandFormat.appendDIV('rich-text-command rich-text-bar-underline', 'a')
    .data('command', 'underline');

  this.$commandMark = this.$commandBar.appendDIV('rich-text-bar-group');
  this.$commandMark.appendDIV('rich-text-command rich-text-bar-white', '&nbsp')
    .data('command', 'BackColor');
  this.$commandMark.appendDIV('rich-text-command rich-text-bar-yellow', '&nbsp')
    .data('command', 'BackColor');
  this.$commandMark.appendDIV('rich-text-command rich-text-bar-green', '&nbsp')
    .data('command', 'BackColor');

  this.$commandList = this.$commandBar.appendDIV('rich-text-bar-group');
  this.$commandList.appendDIV('rich-text-command rich-text-bar-plain')
    .data('command', 'outdent');
  this.$commandList.appendDIV('rich-text-command rich-text-bar-bullet')
    .data('command', 'insertunorderedlist');
  this.$commandList.appendDIV('rich-text-command rich-text-bar-number')
    .data('command', 'insertorderedlist');


  $('.rich-text-command', this.$commandBar).click((this._onCommandClick.bind(this)));

  this.addLabel();
  //FIXME CGU anders lösen, denn: feld soll über die ganze breite gehen
  //this.addMandatoryIndicator();
  this.addStatus();
};

scout.RichTextField.prototype._onCommandClick = function(event) {
  var command = $(event.target).data('command'),
    niceHeader = $(event.target).data('nice-header'),
    attribute = '';

  if (command === 'BackColor') {
    attribute = $(event.target).css('background-color');
  }

  document.execCommand(command, false, attribute);

  if (command === 'BackColor') {
    window.getSelection().collapse(this.$field[0].firstChild, 1);
  }
};
