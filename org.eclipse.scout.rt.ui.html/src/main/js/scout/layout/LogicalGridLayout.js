/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * JavaScript port of org.eclipse.scout.rt.ui.swing.LogicalGridLayout.
 */
scout.LogicalGridLayout = function(hgap, vgap) {
  scout.LogicalGridLayout.parent.call(this);
  this.validityBasedOnParentSize = new scout.Dimension();
  this.valid = false;
  this.m_info;
  this.m_hgap = hgap || 0;
  this.m_vgap = vgap || 0;
};
scout.inherits(scout.LogicalGridLayout, scout.AbstractLayout);

scout.LogicalGridLayout.prototype._verifyLayout = function($parent) {
  var htmlParent = scout.HtmlComponent.get($parent),
    parentSize = htmlParent.getSize();

  if (!this.valid || !this.validityBasedOnParentSize.equals(parentSize)) {
    this.validityBasedOnParentSize = parentSize;
    this.validateLayout($parent);
    this.valid = true;
  }
};

/**
 * @override
 */
scout.LogicalGridLayout.prototype.invalidate = function() {
  this.valid = false;
};

scout.LogicalGridLayout.prototype.validateLayout = function($parent) {
  var visibleComps = [], visibleCons = [], i, cons,
    $children = $parent.children('.form-field');

  for (i = 0; i < $children.length; i++) {
    var $comp = $children.eq(i);
    var htmlComp = scout.HtmlComponent.get($comp);
    if ($comp.isVisible()) {
      visibleComps.push($comp);
      cons = htmlComp.layoutData;
      cons.validate();
      visibleCons.push(cons);
    }
  }
  this.m_info = new scout.LogicalGridLayoutInfo(visibleComps, visibleCons, this.m_hgap, this.m_vgap);
  $.log.trace('(LogicalGridLayout#validateLayout) $parent=' + scout.HtmlComponent.get($parent).debug());
};

scout.LogicalGridLayout.prototype.layout = function($parent) {
  this._verifyLayout($parent);
  var htmlParent = scout.HtmlComponent.get($parent),
    parentSize = htmlParent.getAvailableSize(),
    parentInsets = htmlParent.getInsets();
  $.log.trace('(LogicalGridLayout#layout) parent ' + htmlParent.debug() + ' size=' + parentSize + ' insets=' + parentInsets);
  var cellBounds = this.m_info.layoutCellBounds(parentSize, parentInsets);

  // Set bounds of components
  var r1, r2, r, d, $comp, i, htmlComp, data, delta, margins;
  for (i = 0; i < this.m_info.$components.length; i++) {
    $comp = this.m_info.$components[i];
    htmlComp = scout.HtmlComponent.get($comp);
    data = this.m_info.gridDatas[i];
    r1 = cellBounds[data.gridy][data.gridx];
    r2 = cellBounds[data.gridy + data.gridh - 1][data.gridx + data.gridw - 1];
    r = r1.union(r2);
    if (data.topInset > 0) {
      r.y += data.topInset;
      r.height -= data.topInset;
    }
    margins = htmlComp.getMargins();
    r.width -= margins.horizontal();
    r.height -= margins.vertical();
    if (data.fillHorizontal && data.fillVertical) {
      // ok
    } else {
      d = this.m_info.compSize[i];
      if (!data.fillHorizontal && d.width < r.width) {
        delta = r.width - d.width;
        r.width = d.width;
        if (data.horizontalAlignment === 0) {
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
      if (!data.fillVertical && d.height < r.height) {
        delta = r.height - d.height;
        if (data.heightHint === 0) {
          r.height = d.height;
        } else {
          r.height = data.heightHint;
        }
        if (data.verticalAlignment === 0) {
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
    $.log.trace('(LogicalGridLayout#layout) comp=' + htmlComp.debug() + ' bounds=' + r);
    htmlComp.setBounds(r);
  }
};

scout.LogicalGridLayout.prototype.preferredLayoutSize = function($parent) {
  return this.getLayoutSize($parent, scout.LayoutConstants.PREF);
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
    var insets = scout.HtmlComponent.get($parent).getInsets();
    dim.width += insets.horizontal();
    dim.height += insets.vertical();
  }
  return dim;
};
