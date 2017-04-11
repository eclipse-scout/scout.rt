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
scout.FlexboxLayout = function(direction) {
  scout.FlexboxLayout.parent.call(this);
  this.layoutDatasToReset = [];
  if (direction === scout.FlexboxLayout.Direction.ROW) {
    this.preferredLayoutSize = this.preferredLayoutSizeRow;
    this._getDimensionValue = this._getWidth;
    this._layoutFromLayoutData = this._layoutFromLayoutDataRow;
  } else {
    this.preferredLayoutSize = this.preferredLayoutSizeColumn;
    this._getDimensionValue = this._getHeight;
    this._layoutFromLayoutData = this._layoutFromLayoutDataColumn;
  }
};
scout.inherits(scout.FlexboxLayout, scout.AbstractLayout);

// constants
scout.FlexboxLayout.Direction = {
  COLUMN: 0,
  ROW: 1
};

scout.FlexboxLayout.prototype.invalidate = function() { //
  //  console.log('invalidate bench layout');
};

// layout functions
scout.FlexboxLayout.prototype.layout = function($container) {
  var children = this._getChildren($container),
    htmlContainer = scout.HtmlComponent.get($container),
    containerSize = htmlContainer.getAvailableSize(),
    splitterWithDelta;

  containerSize = containerSize.subtract(htmlContainer.getInsets());

  splitterWithDelta = children.filter(function(c) {
    return c.layoutData.diff;
  })[0];

  if (splitterWithDelta) {
    this._layoutDelta(children, splitterWithDelta, containerSize);
    return;
  } else {
    this._layoutComponents(children, containerSize);
    return;
  }
};

scout.FlexboxLayout.prototype._getChildren = function($container) {
  var children = [];
  $container.children().each(function() {
    var htmlChild = scout.HtmlComponent.optGet($(this));
    if(htmlChild){
      children.push(htmlChild);
    }
  });
  children = children.sort(function(a, b) {
    return (a.layoutData.order || 0) - (b.layoutData.order || 0);
  });
  return children;
};

scout.FlexboxLayout.prototype.reset = function() {
  this.layoutDatasToReset.forEach(function(ld) {
    ld.sizePx = 0;
    ld.initialPx = 0;
    ld.diff = null;
  });
  this.layoutDatasToReset = [];
};

scout.FlexboxLayout.prototype._layoutDelta = function(children, deltaComp, containerSize) {
  this.ensureInitialValues(children, containerSize);
  var delta = deltaComp.layoutData.diff,
    componentsBefore = children.slice(0, children.indexOf(deltaComp)).reverse(),
    componentsAfter = children.slice(children.indexOf(deltaComp) + 1),
    deltaDiffPrev,
    deltaDiffNext;

  // calculate if the delta can be applied to the previous and following columns
  deltaDiffPrev = _distributeDelta(componentsBefore, delta, false);
  deltaDiffNext = -_distributeDelta(componentsAfter, -delta, false);
  // compute the max delta could be applied

  delta = Math.sign(delta) * (Math.min(Math.abs(delta - deltaDiffPrev), Math.abs(delta - deltaDiffNext)));

  if (delta !== 0) {
    // apply the delta to the previous and following columns
    _distributeDelta(componentsBefore, delta, true);
    _distributeDelta(componentsAfter, -delta, true);
  }

  this._layoutFromLayoutData(children, containerSize);

  /*private functions*/
  function _distributeDelta(components, delta, applyDelta) {
    return components.reduce(function(diff, c) {
      if (diff !== 0) {
        diff = c.layoutData.acceptDelta(diff, applyDelta);
      }
      return diff;
    }, delta);
  }
};

scout.FlexboxLayout.prototype._layoutComponents = function(children, containerSize) {
  var delta = this.ensureInitialValues(children, containerSize);
  if (delta < 0) {
    this._adjust(children, delta, function(ld) {
      return ld.shrink;
    });
  } else if (delta > 0) {
    this._adjust(children, delta, function(ld) {
      return ld.grow;
    });
  }
  this._layoutFromLayoutData(children, containerSize);
};

scout.FlexboxLayout.prototype._adjust = function(children, delta, getWeightFunction) {
  var weightSum,
    deltaFactor,
    layoutDatas = children.map(function(c) {
      return c.layoutData;
    }).filter(function(ld) {
      // resizable
      return ld.acceptDelta(Math.sign(delta)) === 0;
    });

  if (layoutDatas.length < 1) {
    return;
  }

  weightSum = layoutDatas.reduce(function(sum, ld) {
    return sum + getWeightFunction(ld);
  }, 0);

  // delta factor
  deltaFactor = delta / weightSum;
  delta = layoutDatas.reduce(function(delta, ld) {
    return ld.acceptDelta(deltaFactor * getWeightFunction(ld), true);
  }, delta);
  if (Math.abs(delta) > 0.2) {
    this._adjust(children, delta, getWeightFunction);
  }

};

scout.FlexboxLayout.prototype._getPreferredSize = function(htmlComp) {
  var prefSize;
  prefSize = htmlComp.getPreferredSize({
      useCssSize: true
    })
    //    .add(htmlComp.getInsets())
    .add(htmlComp.getMargins());
  return prefSize;
};

scout.FlexboxLayout.prototype.ensureInitialValues = function(children, containerSize) {
  var totalPx = this._getDimensionValue(containerSize),
    sumOfAbsolutePx = 0,
    sumOfRelatives = 0,
    colLayoutDatas = children.map(function(c) {
      return c.layoutData;
    });

  children.forEach(function(comp) {
    var ld = comp.layoutData;
    if (ld.sizePx) {
      sumOfAbsolutePx += ld.sizePx;
    } else if (ld.initial < 0) {
      // use ui height
      ld.initialPx = this._getDimensionValue(this._getPreferredSize(comp));
      ld.sizePx = ld.initialPx;
      sumOfAbsolutePx += ld.sizePx;

    } else if (ld.relative) {
      sumOfRelatives += ld.initial;
    } else {
      ld.initialPx = ld.initial;
      ld.sizePx = ld.initialPx;
      sumOfAbsolutePx += ld.sizePx;
    }
    this.layoutDatasToReset.push(ld);
  }.bind(this));
  totalPx -= sumOfAbsolutePx;

  var relativeFactor = totalPx / sumOfRelatives;
  return colLayoutDatas.filter(function(ld) {
    return ld.relative && !ld.sizePx;
  }).reduce(function(restWidth, ld) {
    ld.initialPx = Math.max(30, relativeFactor * ld.initial);
    ld.sizePx = ld.initialPx;
    return restWidth - ld.sizePx;
  }, totalPx);

};

//functions differ from row to column mode

scout.FlexboxLayout.prototype.preferredLayoutSizeColumn = function($container, options) {
  return this._getChildren($container).reduce(function(size, c) {
    var prefSize = this._getPreferredSize(c);
    size.width = Math.max(prefSize.width, size.width);
    size.height += prefSize.height;
    return size;
  }.bind(this), new scout.Dimension(0, 0));
};

scout.FlexboxLayout.prototype.preferredLayoutSizeRow = function($container, options) {
  return this._getChildren($container).reduce(function(size, c) {
    var prefSize = this._getPreferredSize(c);
    size.height = Math.max(prefSize.height, size.height);
    size.width += prefSize.width;
    return size;
  }.bind(this), new scout.Dimension(0, 0));
};

scout.FlexboxLayout.prototype._getWidth = function(dimension) {
  return dimension.width;
};
scout.FlexboxLayout.prototype._getHeight = function(dimension) {
  return dimension.height;
};

scout.FlexboxLayout.prototype._layoutFromLayoutDataRow = function(children, containerSize) {
  children.reduce(function(x, comp) {
    var margins = comp.getMargins();
    var insets = comp.getInsets();
    var w = comp.layoutData.sizePx;
    var bounds = new scout.Rectangle(x - insets.left - margins.left, 0, w + insets.left + insets.right, containerSize.height);
    comp.setBounds(bounds);
    return x + w;
  }, 0);
};

scout.FlexboxLayout.prototype._layoutFromLayoutDataColumn = function(children, containerSize) {
  children.reduce(function(y, comp) {
    var margins = comp.getMargins();
    var insets = comp.getInsets();
    var h = comp.layoutData.sizePx;
    var bounds = new scout.Rectangle(0, y - insets.top- margins.top, containerSize.width, h+ insets.top + insets.bottom);
    comp.setBounds(bounds);
    return y + h;
  }, 0);
};
