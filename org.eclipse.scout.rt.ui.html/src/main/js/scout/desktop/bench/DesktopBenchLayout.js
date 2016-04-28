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

scout.DesktopBenchLayout = function(bench) {
  scout.DesktopBenchLayout.parent.call(this);
  this.bench = bench;
  this.containerSize;
};
scout.inherits(scout.DesktopBenchLayout, scout.AbstractLayout);

scout.DesktopBenchLayout.prototype.layout = function($container) {
  var
    htmlContainer = this.bench.htmlComp,
    containerSize = htmlContainer.getAvailableSize(),
    components = this.bench.getComponents(),
    layoutBySplitterPosition;

  if (!components) {
    return;
  }
  containerSize = containerSize.subtract(htmlContainer.getInsets());

  layoutBySplitterPosition = components.filter(function(comp) {
    return comp instanceof scout.Splitter;
  }).map(function(splitter) {
    return $.isNumeric(splitter.getPosition()) && $.isNumeric(splitter.getRatio());
  }).reduce(function(b1, b2, index) {
    if (index === 0) {
      return b2;
    }
    return b1 && b2;
  }, false);

  if (!layoutBySplitterPosition) {
    this._layoutInitial(components, containerSize);
  } else if (containerSize.equals(this.containerSize)) {
    this._layoutBySplitterPosition(components, containerSize);
  } else {
    this._layoutByRatio(components, containerSize);
  }

  this.containerSize = containerSize;

};

scout.DesktopBenchLayout.prototype._layoutByRatio = function(components, containerSize) {
  // set positions from ratio
  components.forEach(function(comp) {
    if (comp instanceof scout.Splitter) {
      comp.setPosition(Math.floor(comp.getRatio() * containerSize.width));
    }
  });
  this._layoutBySplitterPosition(components, containerSize);
};

scout.DesktopBenchLayout.prototype._layoutBySplitterPosition = function(components, containerSize) {
  var x = 0;
  components.forEach(function(comp, index) {
    if (!(comp instanceof scout.Splitter)) {
      var bounds = new scout.Rectangle(x, 0, 0, containerSize.height);
      if ((components.length - 1) > index) {
        bounds.width = components[index + 1].position - x;
        x = x + bounds.width;
      } else {
        bounds.width = containerSize.width - x;
      }
      comp.htmlComp.setBounds(bounds);
    }
  });
};

scout.DesktopBenchLayout.prototype._layoutInitial = function(components, containerSize) {
  var columnWidth = containerSize.width;
  var columnCount = components.filter(function(comp) {
    return comp instanceof scout.BenchColumn;
  }).length;
  if (columnCount > 0) {
    columnWidth = containerSize.width / columnCount;
  }
  var x = 0;
  components.forEach(function(comp, index) {
    if (comp instanceof scout.Splitter) {
      comp.setPosition(x, true);
    } else {
      var bounds = new scout.Rectangle(x, 0, columnWidth, containerSize.height);
      comp.htmlComp.setBounds(bounds);
      x = x + columnWidth;
    }
  });
};
