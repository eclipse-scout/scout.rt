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

scout.MnemonicKeyStroke.prototype.handle = function(event) {
  this._field.$label.trigger('click');
};

scout.MnemonicKeyStroke.prototype.addHintOnLabel = function() {
  var $elem = this._field.$label;
  var text = $elem.html();
  var newText = text.replace(this.keyStroke, '<span class="keyStrokePos">'+this.keyStroke+'</span>');
  $elem.html(newText);
  var offset = $(".keyStrokePos", $elem).offset();
  $elem.html(text);

  if(offset){
    var topPos = offset.top-6;
    var left = offset.left-8;
    $elem.prependDiv('key-box ', this.keyStroke).css('left', '' + left + 'px').css('top', ''+topPos+'px').css('position', 'fixed').css('z-index', '100');
  }
};

scout.MnemonicKeyStroke.prototype.removeHintOnLabel = function() {
  $('.key-box', this._field.$container).remove();
};
