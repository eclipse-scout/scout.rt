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

scout.BenchColumnLayout = function(benchColumn) {
  scout.BenchColumnLayout.parent.call(this);
  this.benchColumn = benchColumn;
  this.containerSize;
};
scout.inherits(scout.BenchColumnLayout, scout.AbstractLayout);

scout.BenchColumnLayout.prototype.layout = function($container) {
  var htmlContainer = this.benchColumn.htmlComp,
    containerSize = htmlContainer.getAvailableSize(),
    components = this.benchColumn.getComponents(),
    layoutBySplitterPosition;

  if (!components) {
    return;
  }
  containerSize = containerSize.subtract(htmlContainer.getInsets());
  layoutBySplitterPosition = components.filter(function(comp) {
    return comp instanceof scout.Splitter;
  }).map(function(splitter) {
    return $.isNumeric(splitter.position);
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

scout.BenchColumnLayout.prototype._layoutByRatio = function(components, containerSize) {
  // set positions from ratio
  components.forEach(function(comp) {
    if (comp instanceof scout.Splitter) {
      comp.setPosition(Math.floor(comp.getRatio() * containerSize.height));
    }
  });
  this._layoutBySplitterPosition(components, containerSize);
};

scout.BenchColumnLayout.prototype._layoutBySplitterPosition = function(components, containerSize) {
  var y = 0;
  components.forEach(function(comp, index) {
    if (comp instanceof scout.SimpleTabBox) {
      var bounds = new scout.Rectangle(0, y, containerSize.width, 0);
      if ((components.length - 1) > index) {
        bounds.height = components[index + 1].position - y;
        y = y + bounds.height;
      } else {
        bounds.height = containerSize.height - y;
      }
      comp.htmlComp.setBounds(bounds);
    }
  });
};

scout.BenchColumnLayout.prototype._layoutInitial = function(components, containerSize) {
  var rowHeight = containerSize.height;
  var rowCount = components.filter(function(comp) {
    return comp instanceof scout.SimpleTabBox;
  }).length;
  if (rowCount > 0) {
    rowHeight = containerSize.height / rowCount;
  }
  var y = 0;
  components.forEach(function(comp, index) {
    if (comp instanceof scout.Splitter) {
      comp.setPosition(y, true);
    } else {
      var bounds = new scout.Rectangle(0, y, containerSize.width, rowHeight);
      comp.htmlComp.setBounds(bounds);
      y = y + rowHeight;
    }
  });
};
