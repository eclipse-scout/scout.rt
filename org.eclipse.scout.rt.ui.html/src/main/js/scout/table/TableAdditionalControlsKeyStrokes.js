scout.TableAdditionalControlsKeyStrokes = function(field, control) {
  scout.TableAdditionalControlsKeyStrokes.parent.call(this);
  this.drawHint = true;
  this._field = field;
  this.keyStroke = 'ESC';
  this.initKeyStrokeParts();
};
scout.inherits(scout.TableAdditionalControlsKeyStrokes, scout.KeyStroke);
/**
 * @Override scout.KeyStroke
 */
scout.TableAdditionalControlsKeyStrokes.prototype.handle = function(event) {
  if (event.which === scout.keys.ESC) {
    //close actual controlwindow
    if (this._field) {
      this._field.toggle();
    }
    return;
  }
};
/**
 * @Override scout.KeyStroke
 */
scout.TableAdditionalControlsKeyStrokes.prototype._drawKeyBox = function($container, drawedKeys) {
  if (event.which === scout.keys.ESC) {
    //close actual controlwindow
    var $openControl = $('.control.selected', this._field.$container);
    if ($openControl) {
      $openControl.trigger('click');
    }
    return;
  }
};
