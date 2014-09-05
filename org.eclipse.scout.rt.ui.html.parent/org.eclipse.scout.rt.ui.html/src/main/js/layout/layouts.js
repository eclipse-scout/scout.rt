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
  $div.html(text);
  return new scout.Dimension($div.width(), $div.height());
};

scout.Layout.getDimension = function($comp) {
  return new scout.Dimension($comp.width(), $comp.height());
};

// Takes a GridData object and creates a LogicalGridData object which is set on the given component.
scout.Layout.setLogicalGridData = function($comp, gridData) {
  var logicalGridData = new scout.LogicalGridDataBuilder().build(gridData);
  $comp.data('logicalGridData', logicalGridData);
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
  var $body = $parent.find('.group-box-body').first();
  this.setSize($body, new scout.Dimension(parentSize.width, bodyHeight));
};

scout.GroupBoxLayout.prototype.preferredLayoutSize = function($parent) {
  return new scout.Dimension(550, 300); // TODO impl.
};

// ---- Logical Grid Layout ----
scout.LogicalGridLayout = function() {
  scout.LogicalGridLayout.parent.call(this);
  this.info;
};
scout.inherits(scout.LogicalGridLayout, scout.AbstractLayout);

scout.LogicalGridLayout.prototype.layout = function($parent) {
  var $formFields = $parent.children('.form-field');
  var i, $components = [],
    $comp, gridDatas = [],
    data;
  for (i = 0; i < $formFields.length; i++) {
    $comp = $($formFields[i]);
    data = $comp.data('logicalGridData');
    if (!data) {
      throw 'component [id=' + $comp.attr('id') + ' class=' + $comp.attr('class') + '] does not have a logical-grid-data. Failed to layout';
    }
    $components.push($comp);
    gridDatas.push(data);
  }

  // Calculate layout - TODO: move to validateLayout()?
  var env = new scout.SwingEnvironment(); // TODO: rename HtmlEnvironment, make more static?
  this.info = new scout.LogicalGridLayoutInfo(env, $components, gridDatas, 5, 5);
  var parentSize = scout.Layout.getDimension($parent);
  console.log('(LogicalGridLayout#layout) size of parent ' + $parent + '= ' + parentSize);
  var parentInsets = new scout.Insets(0, 0, 0, 0);
  var cellBounds = this.info.layoutCellBounds(parentSize, parentInsets);

  // Set bounds of components
  var r1, r2, r, d, maxHeight = 0, tmpMaxHeight = 0, layout;
  for (i = 0; i < $components.length; i++) {
    $comp = $components[i];
    data = gridDatas[i];
    if (!$comp.is(':visible')) {
      continue; // skip invisible components
    }
    r1 = cellBounds[data.gridy][data.gridx];
    r2 = cellBounds[data.gridy + data.gridh - 1][data.gridx + data.gridw - 1];
    r = r1.union(r2);
    if (data.topInset > 0) {
      r.y += data.topInset;
      r.height -= data.topInset;
    }
    if (data.fillHorizontal && data.fillVertical) {
      // ok
    } else {
      d = $comp.data('layout').preferredLayoutSize($comp);
      if (!data.fillHorizontal) {
        if (d.width < r.width) {
          var delta = r.width - d.width;
          r.width = d.width;
          if (data.horizontalAlignment == 0) {
            // Do ceil the result as other layout managers of Java also handle floating calculation results that way.
            // This is important if being used in conjunction with another layout manager.
            // E.g. the editable checkbox in inline table cell is a JCheckBox and rendered by LogicalGridLayout,
            // whereas the default boolean representation in a table cell is simply an image on a label positioned by
            // default layout manager. If switching in between of edit and non-edit mode, the widget would bounce otherwise.
            r.x += Math.ceil(delta / 2.0);
          } else if (data.horizontalAlignment > 0) {
            r.x += delta;
          }
        }
      }
      if (!data.fillVertical) {
        if (d.height < r.height) {
          var delta = r.height - d.height;
          if (data.heightHint == 0) {
            r.height = d.height;
          } else {
            r.height = data.heightHint;
          }
          if (data.verticalAlignment == 0) {
            // Do ceil the result as other layout managers of Java also handle floating calculation results that way.
            // This is important if being used in conjunction with another layout manager.
            // E.g. the editable checkbox in inline table cell is a JCheckBox and rendered by LogicalGridLayout,
            // whereas the default boolean representation in a table cell is simply an image on a label positioned by
            // default layout manager. If switching in between of edit and non-edit mode, the widget would bounce otherwise.
            r.y += Math.ceil(delta / 2.0);
          } else if (data.verticalAlignment > 0) {
            r.y += delta;
          }
        }
      }
    }
    this.setBounds($comp, r);

    tmpMaxHeight = r.y + r.height - 1;
    if (tmpMaxHeight > maxHeight) {
      maxHeight = tmpMaxHeight;
    }
  }

  // After all components have been laid out in the container (with absolute positioning) we must
  // resize the parent-container to the correct heigth, since absolute positioned elements are
  // outside of the HTML flow.
  // $parent.css('height', maxHeight + 'px'); // FIXME AWE: check this - war vorher $body - evtl. entfernen
};

scout.LogicalGridLayout.prototype.preferredLayoutSize = function($parent) {
  return new scout.Dimension(400, 300); // TODO impl.
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

// ---- Button Layout ----
// layout which does nothing
scout.ButtonLayout = function() {
  scout.ButtonLayout.parent.call(this);
}
scout.inherits(scout.ButtonLayout, scout.AbstractLayout);

scout.ButtonLayout.prototype.layout = function($parent) {
  // button has no children - nothing to do here
};

scout.ButtonLayout.prototype.preferredLayoutSize = function($parent) {
  var hMargin = $parent.outerWidth(true) - $parent.width();
  var vMargin = $parent.outerHeight(true) - $parent.height();
  var textSize = scout.Layout.measureString($parent.html());
  return new scout.Dimension(textSize.width + hMargin, textSize.height + vMargin);
};
