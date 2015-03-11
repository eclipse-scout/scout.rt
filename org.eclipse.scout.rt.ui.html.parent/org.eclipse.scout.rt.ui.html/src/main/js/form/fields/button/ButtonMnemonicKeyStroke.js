scout.ButtonMnemonicKeyStroke = function(keyStroke, field) {
  scout.ButtonMnemonicKeyStroke.parent.call(this, keyStroke, field);
  this.initKeyStrokeParts();
};
scout.inherits(scout.ButtonMnemonicKeyStroke, scout.MnemonicKeyStroke);

/**
 * @Override scout.MnemonicKeyStroke
 */
scout.ButtonMnemonicKeyStroke.prototype.handle = function(event) {
  this._field.$field.trigger('click');
};


scout.ButtonMnemonicKeyStroke.prototype.addHintOnLabel = function() {
  var $elem = this._field.$field;
  var text = $elem.html();
  var newText = text.replace(this.keyStroke, '<span class="keyStrokePos">'+this.keyStroke+'</span>');
  $elem.html(newText);
  var offset = $(".keyStrokePos", $elem).offset();
  $elem.html(text);

  if(offset){
    var topPos = offset.top-6;
    var left = offset.left-8;
    $elem.prependDiv('key-box ', this.keyStroke).css('left', '' + left + 'px').css('top', ''+topPos+'px').css('position', 'fixed');
  }
};
