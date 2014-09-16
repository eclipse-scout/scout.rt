/**
 * JavaScript port of org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFormFieldGridData.
 * Merged with the base class LogicalGridData (for the default values). We don't need the base
 * class standalone, since we only used LGL for the group-box body in Html UI.
 */
scout.LogicalGridData = function(vararg) { // FIXME AWE: discuss with C.GU --> overloaded ctor
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
    // TODO AWE: impl. generic copyProperties?
    this.gridx = vararg.gridx;
    this.gridy = vararg.gridy;
    this.gridw = vararg.gridw;
    this.gridh = vararg.gridh;
    this.weightx = vararg.weightx;
    this.weighty = vararg.weighty;
    this.useUiWidth = vararg.useUiWidth;
    this.useUiHeight = vararg.useUiHeight;
    this.widthHint = vararg.widthHint;
    this.heightHint = vararg.heightHint;
    this.horizontalAlignment = vararg.horizontalAlignment;
    this.verticalAlignment = vararg.verticalAlignment;
    this.fillHorizontal = vararg.fillHorizontal;
    this.fillVertical = vararg.fillVertical;
  } else {
    this.topInset = vararg.topInset;    this.model = vararg;
  }
};

scout.LogicalGridData.prototype.validate = function() {
  if (!this.model) {
    return; // FIXME AWE: hacky, solve with subclass?
  }

  var data = this.model.gridData;
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

  // when having the label on top the container of the field must not have a fix size but use the calculated ui height instead.
  // FIXME AWE: (layout) schauen ob wir label-position unterstützen wollen für Html UI
  // if (this.formField.getLabelPosition() == IFormField.LABEL_POSITION_TOP) {
  // this.useUiHeight = true;
  // } else {
    this.useUiHeight = data.useUiHeight;
  // }

  this.horizontalAlignment = data.horizontalAlignment;
  this.verticalAlignment = data.verticalAlignment;
  this.fillHorizontal = data.fillHorizontal;
  this.fillVertical = data.fillVertical;
  this.widthHint = data.widthInPixel;
  this.heightHint = data.heightInPixel;
  if (this.weighty === 0 || (this.weighty < 0 && this.gridh <= 1)) {
    this.fillVertical = false;
  }

};

scout.LogicalGridData.prototype._inheritWeightY = function() {
  var d = this._inheritWeightYRec(this.model);
  if (d === null) {
    var data = this.model.gridData;
    if (data.weightY >= 0) {
      d = data.weightY;
    }
    else {
      d = data.h >= 2 ? data.h : 0;
    }
  }
  return d;
};

scout.LogicalGridData.prototype._inheritWeightYRec = function(f) {
  var found = false,
      sumWy = 0;
  if (f.getControlFields !== undefined) { // FIXME AWE: check if that works with SequenceBox too, what about an 'interface'?
    var i, inheritWeightY, child, children = f.getControlFields();
    for (i=0; i<children.length; i++) {
      child = children[i];
      if (child.visible) {
        var data = child.gridData;
        if (data.weightY < 0) {
          inheritWeightY = this._inheritWeightYRec(child);
          if (inheritWeightY !== null) {
            found = true;
            sumWy += inheritWeightY;
          }
        }
        else {
          found = true;
          sumWy += data.weightY;
        }
      }
    }
  }
  else {
    sumWy = (f.gridData.h >= 2 ? f.gridData.h : 0);
    found = true;
  }
  if (found) {
    return sumWy;
  }
  else {
    return null;
  }
};

