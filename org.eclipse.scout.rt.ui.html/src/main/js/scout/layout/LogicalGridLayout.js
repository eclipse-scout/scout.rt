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
  this.cssClass = 'logical-grid-layout';
  this.validityBasedOnContainerSize = new scout.Dimension();
  this.valid = false;
  this.m_info;
  this.m_hgap = hgap || 0;
  this.m_vgap = vgap || 0;
};
scout.inherits(scout.LogicalGridLayout, scout.AbstractLayout);

scout.LogicalGridLayout.prototype._verifyLayout = function($container, options) {
  var containerSize;
  if (options) {
    // XXX Correct?
    containerSize = new scout.Dimension(options.widthHint, options.heightHint);
  } else {
    containerSize = scout.HtmlComponent.get($container).getSize();
  }

  if (!this.valid || !this.validityBasedOnContainerSize.equals(containerSize)) {
    this.validityBasedOnContainerSize = containerSize;
    this.validateLayout($container, options);
    this.valid = true;
  }
};

/**
 * @override
 */
scout.LogicalGridLayout.prototype.invalidate = function() {
  this.valid = false;
};

scout.LogicalGridLayout.prototype.validateLayout = function($container, options) {
  var visibleComps = [],
    visibleCons = [];
  $container.children().each(function (idx, elem) {
    var $comp = $(elem);
    var htmlComp = scout.HtmlComponent.optGet($comp);
    if (htmlComp && $comp.isVisible()) {
      visibleComps.push($comp);
      var cons = htmlComp.layoutData;
      cons.validate();
      visibleCons.push(cons);
    }
  });
  this.m_info = new scout.LogicalGridLayoutInfo(visibleComps, visibleCons, this.m_hgap, this.m_vgap, options);
  $.log.trace('(LogicalGridLayout#validateLayout) $container=' + scout.HtmlComponent.get($container).debug());
};

scout.LogicalGridLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    containerSize = htmlContainer.getAvailableSize(),
    containerInsets = htmlContainer.getInsets();

  // XXX STRANGE PATTERN!
  this._verifyLayout($container, {
    widthHint: containerSize.width - containerInsets.horizontal(),
    heightHint: containerSize.height - containerInsets.vertical()
  });
  $.log.trace('(LogicalGridLayout#layout) container ' + htmlContainer.debug() + ' size=' + containerSize + ' insets=' + containerInsets);
  var cellBounds = this.m_info.layoutCellBounds(containerSize, containerInsets);

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
    console.log($comp.data('widget').objectType + '[' + $comp.data('widget').id + ']' +
        '\n  r orig : ' + r1.union(r2) +
        '\n  r      : ' + r +
        '\n  hints  : width=' + (containerSize.width - containerInsets.horizontal()) + ', height=' + (containerSize.height - containerInsets.vertical()));
    htmlComp.setBounds(r);
  }
};

scout.LogicalGridLayout.prototype.preferredLayoutSize = function($container, options) {
  return this.getLayoutSize($container, scout.LayoutConstants.PREF, options);
};

scout.LogicalGridLayout.prototype.getLayoutSize = function($container, sizeflag, options) {
  // XXX STRANGE PATTERN!
  var htmlContainer = scout.HtmlComponent.get($container),
    containerInsets = htmlContainer.getInsets();
  var innerOptions = {};
  if (options.widthHint) {
    innerOptions.widthHint = options.widthHint - containerInsets.horizontal();
  }
  if (options.heightHint) {
    innerOptions.heightHint = options.heightHint - containerInsets.vertical();
  }
  this._verifyLayout($container, innerOptions);

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
  // insets
//  var insets = scout.HtmlComponent.get($container).getInsets();
//  dim.width += insets.horizontal();
//  dim.height += insets.vertical();
  dim.width += containerInsets.horizontal();
  dim.height += containerInsets.vertical();
  return dim;
};
