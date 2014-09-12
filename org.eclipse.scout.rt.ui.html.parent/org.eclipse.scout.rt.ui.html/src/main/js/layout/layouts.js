// ---- Layout ----
// Static utility functions for layouting
scout.Layout = function() {
};

scout.Layout.setLayout = function($comp, layout) {
  $comp.data('layout', layout);
};

scout.Layout.getLayout = function($comp) {
  return $comp.data('layout');
};

scout.Layout.layout = function($comp)  {
  var layout = this.getLayout($comp);
  if (!layout) {
    throw 'Tried to layout component [id=' + $comp.attr('id') + ' class=' + $comp.attr('class') + '] but component has no layout';
  }
  layout.layout($comp);
};

scout.Layout.measureString = function(text) {
  var $div = $('#StringMeasurement');
  if ($div.length === 0) {
    throw 'DIV StringMeasurement does\'nt exist';
  }
  $div.html(text);
  return new scout.Dimension($div.width(), $div.height());
};

// FIXME AWE: getInsets impl. ist so falsch margin und border separat bestimmen
// schauen ob wir das überhaupt brauchen (width VS outherWidth im vergleich z Swing)
// ggf. andere Stellen refactoren an denen das hier auch gebraucht wird
scout.Layout.getInsets = function($comp) {
  var hMargin = $comp.outerWidth(true) - $comp.width();
  var vMargin = $comp.outerHeight(true) - $comp.height();
  return new scout.Insets(vMargin / 2, hMargin / 2, vMargin / 2, hMargin / 2);
};

scout.Layout.getDimension = function($comp) {
  return new scout.Dimension($comp.width(), $comp.height());
};

// Takes a GridData object and creates a LogicalGridData object which is set on the given component.
scout.Layout.setLogicalGridData = function($comp, gridData) {
  var logicalGridData = new scout.LogicalGridDataBuilder().build(gridData);
  $comp.data('logicalGridData', logicalGridData);
};

scout.Layout.getLogicalGridData = function($comp) {
  return $comp.data('logicalGridData');
};

scout.Layout.debugComponent = function($comp) {
  var attrs = '';
  if ($comp.attr('id')) {
    attrs += 'id=' + $comp.attr('id');
  }
  if ($comp.attr('class')) {
    attrs += ' class=' + $comp.attr('class');
  }
  if (attrs.length === 0) {
    attrs = $comp.html().substring(0, 30) + '...';
  }
  return 'Comp[' + attrs + ']';
};

// ---- Abstract Layout ----
// Abstract layout class with functions used by all layouts.
scout.AbstractLayout = function() {
};

// Sets the size of given component and layout component.
scout.AbstractLayout.prototype.setSize = function($comp, size) {
  $comp.
    css('width', size.width).
    css('height', size.height);
  scout.Layout.layout($comp);
};

scout.AbstractLayout.prototype.setBounds = function($comp, bounds) {
  $comp.
    css('left', bounds.x).
    css('width', bounds.width).
    css('top', bounds.y).
    css('height', bounds.height);
  scout.Layout.layout($comp);
};

// ---- Fill Layout ----
// layout a container with a single child element, so the child element will have the same size as the parent.
scout.FillLayout = function() {
  scout.FillLayout.parent.call(this);
};
scout.inherits(scout.FillLayout, scout.AbstractLayout);

// TODO AWE: (layout) daraus ein FormLayout machen?
scout.FillLayout.prototype.layout = function($parent) {
//  var $comp = $parent.children(':first');
  var $comp = $parent.children('.root-group-box');
  this.setSize($comp, scout.Layout.getDimension($parent));
};

scout.FillLayout.prototype.preferredLayoutSize = function($parent) {
  return scout.Layout.getDimension($parent);
};

// ---- Group-Box Layout ----
scout.GroupBoxLayout = function() {
  scout.GroupBoxLayout.parent.call(this);
};
scout.inherits(scout.GroupBoxLayout, scout.AbstractLayout);

scout.GroupBoxLayout.prototype.layout = function($parent) {
  var parentSize = scout.Layout.getDimension($parent);
  var titleHeight = 28; // TODO: dynamisch ermitteln / visibility / existenz pruefen
  var bodyHeight = parentSize.height - titleHeight;
  this.setSize(this._getBody($parent), new scout.Dimension(parentSize.width, bodyHeight));
};

scout.GroupBoxLayout.prototype.preferredLayoutSize = function($parent) {
  var $body = this._getBody($parent);
  var layout = scout.Layout.getLayout($body);
  var bodySize = layout.preferredLayoutSize($body);
  var size = new scout.Dimension(bodySize.width, bodySize.height);
  size.height += 28;
  // TODO AWE: (layout) add insets
  return size;
};

scout.GroupBoxLayout.prototype._getBody = function($parent) {
  return $parent.find('.group-box-body').first();
};


// ---- Form-Field Layout ----
// Layout for a form-field with label, status-label and a field
scout.FormFieldLayout = function() {
  scout.FormFieldLayout.parent.call(this);
};
scout.inherits(scout.FormFieldLayout, scout.AbstractLayout);

scout.FormFieldLayout.prototype.layout = function($parent) {
  var parentSize = scout.Layout.getDimension($parent);
  var widthDiff = 0;
  var $label = $parent.children('label');
  if ($label.is(':visible')) {
    $label.css('width', '130px');
    widthDiff += 130;
  }
  var $status = $parent.children('.status');
  if ($status.is(':visible')) {
    $status.css('width', '10px');
    widthDiff += 10;
  }
  $parent.children('.field').
    css('width', (parentSize.width - widthDiff) + 'px');
};

scout.FormFieldLayout.prototype.preferredLayoutSize = function($parent) {
  var width = 0,
    height = 23,
    $label = $parent.children('label'),
    $status = $parent.children('.status'),
    $field = $parent.children('.field');
  if ($label.is(':visible')) {
    width += 130;
  }
  if ($status.is(':visible')) {
    width += 10;
  }
  if ($field.is(':visible')) {
    var prefSize, layout = scout.Layout.getLayout($field);
    if (layout) {
      prefSize = layout.preferredLayoutSize($field);
    } else {
      prefSize = scout.Layout.getDimension($field);
    }
    width += prefSize.width;
    height += prefSize.height;
  }
  return new scout.Dimension(width, height);
};

// TODO: ueberlegen ob wir hier wirklich "Layout" meinen oder ob wir einfach nur eine prefSize func brauchen

// ---- Text-Field Layout ----
// used to calculate the preferred size of a HTML text-field. Note that this is not the same as the "auto" size of the HTML element.
// Browsers typically render a text-field larger than the minimum size to display the whole text.

scout.TextFieldLayout = function() {
  scout.TextFieldLayout.parent.call(this);
};
scout.inherits(scout.TextFieldLayout, scout.AbstractLayout);

scout.TextFieldLayout.prototype.preferredLayoutSize = function($parent) {
   return scout.Layout.measureString($parent.val());
};

// ---- Button Field Layout ----
// layout for fields with a button
scout.ButtonFieldLayout = function() {
  scout.ButtonFieldLayout.parent.call(this);
};
scout.inherits(scout.ButtonFieldLayout, scout.AbstractLayout);

scout.ButtonFieldLayout.prototype.layout = function($parent) {
  // button has no children - nothing to do here
};

scout.ButtonFieldLayout.prototype.preferredLayoutSize = function($parent) {
  var $button = $parent.find('button');
  var hMargin = $button.outerWidth(true) - $button.width();
  var vMargin = $button.outerHeight(true) - $button.height();
  var textSize = scout.Layout.measureString($button.html());
  return new scout.Dimension(textSize.width + hMargin, textSize.height + vMargin);
};
