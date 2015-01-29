scout.TableLayout = function(table) {
  scout.TableLayout.parent.call(this);
  this.table = table;
};
scout.inherits(scout.TableLayout, scout.AbstractLayout);

scout.TableLayout.prototype.layout = function($container) {
  var menuBar = this.table.menuBar,
    footer = this.table.footer,
    header = this.table.header,
    $data = this.table.$data,
    height = 0;

  if (menuBar.$container.isVisible()){
    height += scout.graphics.getSize(menuBar.$container).height;
  }
  if (footer) {
    height += scout.graphics.getSize(footer.$container).height;
    height += scout.graphics.getSize(footer.$controlContainer).height;
  }
  if (header) {
    height += scout.graphics.getSize(header.$container).height;
  }
  height += $data.cssMarginTop() + $data.cssMarginBottom();
  $data.css('height', 'calc(100% - '+ height + 'px)');

  scout.scrollbars.update(this.table.$data);
};

scout.TableLayout.prototype.preferredLayoutSize = function($comp) {
  return scout.graphics.getSize($comp);
};
