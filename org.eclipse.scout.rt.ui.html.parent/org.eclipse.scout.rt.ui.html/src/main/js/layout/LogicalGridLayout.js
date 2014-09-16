/**
 * JavaScript port of org.eclipse.scout.rt.ui.swing.LogicalGridLayout.
 */
scout.LogicalGridLayout = function(hgap, vgap) {
  scout.LogicalGridLayout.parent.call(this);
  this.m_info;
  this.m_hgap = hgap || 0;
  this.m_vgap = vgap || 0;
};
scout.inherits(scout.LogicalGridLayout, scout.AbstractLayout);

// TODO AWE: (layout) implement resize event

scout.LogicalGridLayout.prototype.validateLayout = function($parent) {
  var visibleComps = [], visibleCons = [], i, cons,
    children = $parent.children('.form-field');
  for (i = 0; i < children.length; i++) {
    var $comp = $(children[i]);
    if ($comp.isVisible()) {
      visibleComps.push($comp);
      cons = scout.Layout.getLogicalGridData($comp);
      cons.validate();
      visibleCons.push(cons);
    }
  }
  this.m_info = new scout.LogicalGridLayoutInfo(visibleComps, visibleCons, this.m_hgap, this.m_vgap);
  $.log('(LogicalGridLayout#validateLayout) $parent=' + scout.Layout.debugComponent($parent));
};

scout.LogicalGridLayout.prototype.layout = function($parent) {
  this._verifyLayout($parent);
  var formFields = $parent.children('.form-field');
  var i, $components = [],
    $comp, gridDatas = [],
    data;
  for (i = 0; i < formFields.length; i++) {
    $comp = $(formFields[i]);
    data = $comp.data('logicalGridData');
    if (!data) {
      throw 'component [id=' + $comp.attr('id') + ' class=' + $comp.attr('class') + '] does not have a logical-grid-data. Failed to layout';
    }
    $components.push($comp);
    gridDatas.push(data);
  }

  // Calculate layout - TODO AWE: (layout) move to validateLayout()?
  var parentSize = scout.Layout.getDimension($parent),
    parentInsets = scout.Layout.getInsets($parent);
  $.log('(LogicalGridLayout#layout) size of parent ' + scout.Layout.debugComponent($parent) + '= ' + parentSize + ' insets=' + parentInsets);
  var cellBounds = this.m_info.layoutCellBounds(parentSize, parentInsets);

  // Set bounds of components
  var r1, r2, r, d, maxHeight = 0, tmpMaxHeight = 0, layout;
  for (i = 0; i < $components.length; i++) {
    $comp = $components[i];
    data = gridDatas[i];
    r1 = cellBounds[data.gridy][data.gridx];
    r2 = cellBounds[data.gridy + data.gridh - 1][data.gridx + data.gridw - 1];
    r = r1.union(r2);
    $.log('layoutContainer comp=' + scout.Layout.debugComponent($comp) + ' r=' + r);
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
  return this.getLayoutSize($parent, 1);
};

scout.LogicalGridLayout.prototype.getLayoutSize = function($parent, sizeflag) {
  this._verifyLayout($parent);
  var dim = new scout.Dimension();
  // w
  var i, w, h, useCount = 0;
  for (i = 0; i < this.m_info.cols; i++) {
    w = this.m_info.width[i][sizeflag];
    if (useCount > 0) {
      dim.width += this.m_hgap;
    }
    dim.width += w;
    useCount++;
  }
  // h
  useCount = 0;
  for (i = 0; i < this.m_info.rows; i++) {
    h = this.m_info.height[i][sizeflag];
    if (useCount > 0) {
      dim.height += this.m_vgap;
    }
    dim.height += h;
    useCount++;
  }
  if (dim.width > 0 && dim.height > 0) {
    var insets = scout.Layout.getInsets($parent);
    dim.width += insets.left + insets.right;
    dim.height += insets.top + insets.bottom;
  }
  return dim;
};
