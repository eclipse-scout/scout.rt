scout.SequenceBox = function() {
  scout.SequenceBox.parent.call(this);
  this._addAdapterProperties('fields');
  this.$sequenceBox;
};

scout.inherits(scout.SequenceBox, scout.FormField);

scout.SequenceBox.prototype._render = function($parent) {
  this.$container = $('<div>').
    appendTo($parent).
    attr('id', 'SequenceBox-' + this.id).
    addClass('form-field');

  this.$label = $('<label>').
    appendTo(this.$container);

  this.$status = $('<span>')
  .addClass('status')
  .appendTo(this.$container);

  this.$sequenceBox = $('<ul>').
    addClass('field').
    addClass('sequence-box').
    appendTo(this.$container);

  var i, $li, $div, $components = [], gridDatas = [];
  var builder = new scout.LogicalGridDataBuilder();
  for (i = 0; i < this.fields.length; i++) {
    $li = $('<li>').appendTo(this.$sequenceBox);
    this.fields[i].render($li);
    $components.push($li);
    gridDatas.push(builder.build(this.fields[i].gridData));
  }

  $.log('parent-width='+this.$sequenceBox.width());

  var env = new scout.SwingEnvironment();
  var layout = new scout.LogicalGridLayoutInfo(env, $components, gridDatas, 5, 5); // TODO AWE: (layout) woher kommen die 5, 5?
  var parentSize = new scout.Dimension(this.$sequenceBox.width(), this.$sequenceBox.height());
  var parentInsets = new scout.Insets(0, 0, 0, 0);
  var bounds = layout.layoutCellBounds(parentSize, parentInsets);

  var $comp, cell;
  for (i = 0; i < $components.length; i++) {
    $comp = $components[i];
    cell = bounds[0][i];
    if (cell) { // TODO AWE: (layout) bug when comp is not visible (see SequenceBoxForm#SomeFieldsHaventAValueField)
      $comp.css('left', cell.x);
      $comp.css('width', cell.width);
    }
    //$.log('cell='+cell);
  }
};
