/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * JavaScript port of org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFormFieldGridData.
 * Merged with the base class LogicalGridData (for the default values). We don't need the base
 * class standalone, since we only used LGL for the group-box body in Html UI.
 */
scout.LogicalGridData = function(vararg) {
  this.gridx = 0;
  this.gridy = 0;
  this.gridw = 1;
  this.gridh = 1;
  this.weightx = 0.0;
  this.weighty = 0.0;
  this.useUiWidth = false;
  this.useUiHeight = false;
  this.widthHint = 0;
  this.heightHint = 0;
  this.horizontalAlignment = -1;
  this.verticalAlignment = -1;
  this.fillHorizontal = true;
  this.fillVertical = true;
  this.topInset = 0;

  if (vararg instanceof scout.LogicalGridData) {
    // copy properties from LGD template
    scout.objects.copyProperties(vararg, this);
  } else if (vararg instanceof scout.Widget) {
    // work with widget / validate
    this.widget = vararg;
  } else {
    // NOP - default CTOR
  }
};

scout.LogicalGridData.prototype.validate = function() {
  if (!this.widget) {
    return;
  }

  var data = this.widget.gridData;
  // setup constraints
  this.gridx = data.x;
  this.gridy = data.y;
  this.gridw = data.w;
  this.gridh = data.h;
  this.weightx = data.weightX;
  this.weighty = data.weightY;
  if (this.weightx < 0) {
    // inherit
    this.weightx = Math.max(1.0, this.gridw);
  }
  if (this.weighty < 0) {
    // inherit
    this.weighty = this._inheritWeightY();
  }
  this.useUiWidth = data.useUiWidth;
  this.useUiHeight = data.useUiHeight;
  this.horizontalAlignment = data.horizontalAlignment;
  this.verticalAlignment = data.verticalAlignment;
  this.fillHorizontal = data.fillHorizontal;
  this.fillVertical = data.fillVertical;
  this.widthHint = data.widthInPixel;
  this.heightHint = data.heightInPixel;

  // when having the label on top, the row height has to be increased
  if (this.widget.labelVisible && this.widget.$label && this.widget.labelPosition === scout.FormField.LabelPosition.TOP) {
    this.logicalRowHeightAddition = scout.HtmlEnvironment.formRowHeight;
  }
};

scout.LogicalGridData.prototype._inheritWeightY = function() {
  var d = this._inheritWeightYRec(this.widget);
  if (d === null) {
    var data = this.widget.gridData;
    if (data.weightY >= 0) {
      d = data.weightY;
    } else {
      d = data.h >= 2 ? data.h : 0;
    }
  }
  return d;
};

scout.LogicalGridData.prototype._inheritWeightYRec = function(widget) {
  var found = false,
    sumWy = 0;

  // The children may have a dirty grid -> make sure the grid is valid before reading any grid data properties
  widget.validateLogicalGrid();

  if (widget instanceof scout.CompositeField) {
    var i, inheritWeightY, child, children = widget.getFields();
    for (i = 0; i < children.length; i++) {
      child = children[i];
      if (child.visible) {
        var data = child.gridData;
        if (data.weightY < 0) {
          inheritWeightY = this._inheritWeightYRec(child);
          if (inheritWeightY !== null) {
            found = true;
            sumWy += inheritWeightY;
          }
        } else {
          found = true;
          sumWy += data.weightY;
        }
      }
    }
  } else {
    sumWy = (widget.gridData.h >= 2 ? widget.gridData.h : 0);
    found = true;
  }
  if (found) {
    return sumWy;
  }
  return null;
};

scout.LogicalGridData.prototype.isValidateRoot = function() {
  return !this.useUiHeight && !this.useUiWidth && this.fillVertical && this.fillHorizontal;
};
