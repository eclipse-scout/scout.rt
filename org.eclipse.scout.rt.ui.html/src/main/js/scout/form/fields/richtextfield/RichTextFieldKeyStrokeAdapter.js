scout.RichTextFieldKeyStrokeAdapter = function(richTextField) {
  scout.RichTextFieldKeyStrokeAdapter.parent.call(this, richTextField);

  // Prevent enter to bubble up and execute form or groupbox enter key.
  this.registerKeyStroke(new scout.RichTextFieldEnterKeyStroke());
  this.registerKeyStroke(new scout.RichTextFieldBackspaceKeyStroke());
};

scout.inherits(scout.RichTextFieldKeyStrokeAdapter, scout.FormFieldKeyStrokeAdapter);

scout.RichTextFieldEnterKeyStroke = function() {
  scout.RichTextFieldEnterKeyStroke.parent.call(this);
  this.keyStroke = 'ENTER';
  this.drawHint = false;
  this.initKeyStrokeParts();
};
scout.inherits(scout.RichTextFieldEnterKeyStroke, scout.KeyStroke);

scout.RichTextFieldEnterKeyStroke.prototype.handle = function(event) {};

scout.RichTextFieldBackspaceKeyStroke = function() {
  scout.RichTextFieldBackspaceKeyStroke.parent.call(this);
  this.keyStroke = 'Backspace';
  this.drawHint = false;
  this.initKeyStrokeParts();
};
scout.inherits(scout.RichTextFieldBackspaceKeyStroke, scout.KeyStroke);

scout.RichTextFieldBackspaceKeyStroke.prototype.handle = function(event) {};
