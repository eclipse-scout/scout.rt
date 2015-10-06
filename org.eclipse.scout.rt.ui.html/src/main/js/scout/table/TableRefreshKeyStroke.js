scout.TableRefreshKeyStroke = function(table) {
  scout.TableRefreshKeyStroke.parent.call(this);
  this.field = table;
  this.which = [scout.keys.F5];
  this.renderingHints.offset = 0;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.field.footer ? this.field.footer._$infoLoad.find('.table-info-button') : null;
  }.bind(this);
};
scout.inherits(scout.TableRefreshKeyStroke, scout.KeyStroke);

scout.TableRefreshKeyStroke.prototype.handle = function(event) {
  this.field.reload();
};
