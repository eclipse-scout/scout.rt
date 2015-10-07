scout.TableCopyKeyStroke = function(table) {
  scout.TableCopyKeyStroke.parent.call(this);
  this.field = table;
  this.which = [scout.keys.C];
  this.ctrl = true;
  this.renderingHints.render = false;
};
scout.inherits(scout.TableCopyKeyStroke, scout.KeyStroke);

scout.TableCopyKeyStroke.prototype.handle = function(event) {
  this.field.exportToClipboard();
};
