scout.MnemonicKeyStroke = function(keyStroke, field) {
  scout.MnemonicKeyStroke.parent.call(this);
  this.keyStroke = keyStroke;
  this.drawHint = true;
  this.alt = true;
  this.shift = true;
  this._field = field;
  this.initKeyStrokeParts();
};
scout.inherits(scout.MnemonicKeyStroke, scout.KeyStroke);
/**
 * @Override scout.KeyStroke
 */
scout.MnemonicKeyStroke.prototype.handle = function(event) {
  this._field.$label.trigger('click');
};
/**
 * @Override scout.KeyStroke
 */
scout.MnemonicKeyStroke.prototype._drawKeyBox = function($container) {
  var offset = this._findMnemonicPosition();
  if(offset){
    var keyText = this._$containerForKeyBox().html().match(new RegExp('(' + scout.codesToKeys[this.keyStrokeKeyPart] + ')', 'i'))[0];
    this._$containerForKeyBox().prependDiv('key-box ', keyText).css('left', '' + offset.left + 'px').css('top', ''+offset.top+'px').css('position', 'fixed').css('z-index', '100');
  }
};

scout.MnemonicKeyStroke.prototype._findMnemonicPosition = function(){
  //Find letter position of Mnemonic letter to align keyBox
  var $elem = this._$containerForKeyBox();
  var text = $elem.html();
  var newText = text.replace(new RegExp('(' + scout.codesToKeys[this.keyStrokeKeyPart] + ')', 'i'), '<span class="keyStrokePos">$1</span>');
  $elem.html(newText);
  var offset = $(".keyStrokePos", $elem).offset();
  $elem.html(text);
  offset.top = offset.top-6;
  offset.left =offset.left-8;

  return offset;
};

scout.MnemonicKeyStroke.prototype._$containerForKeyBox = function(){
  return this._field.$label;
};

/**
 * @Override scout.KeyStroke
 */
scout.MnemonicKeyStroke.prototype.removeKeyBox = function($container){
  $('.key-box', this._$containerForKeyBox()).remove();
  $('.key-box-additional', this._$containerForKeyBox()).remove();
};
