// FIXME AWE: remove this file (LogicalGridDataBuilder.js)

///**
// * JavaScript port of org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder.
// */
//scout.LogicalGridDataBuilder = function() {
//};
//
//// ip = input, op = output
//scout.LogicalGridDataBuilder.prototype.build = function(ip) {
//  var op = new scout.LogicalGridData();
//  op.gridx = ip.x;
//  op.gridy = ip.y;
//  op.gridw = ip.w;
//  op.gridh = ip.h;
//  op.weightx = ip.weightX;
//  op.weighty = ip.weightY;
//  if (op.weightx < 0) {
//    // inherit
//    op.weightx = Math.max(1.0, op.gridw);
//  }
//  if (op.weighty < 0) {
//    // inherit
//    // TODO AWE: (layout) impl. _inheritWeightY
//    // op.weighty = this._inheritWeightY(m_scoutField);
//  }
//  op.useUiWidth = ip.useUiWidth;
//
//  // When having the label on top the container of the field must not have a fix size but use the calculated ui height instead.
//  /*
//  TODO AWE: (layout) impl. label position special handling
//  if (m_scoutField.getLabelPosition() == IFormField.LABEL_POSITION_TOP) {
//    op.useUiHeight = true;
//  }
//  else {
//  */
//    op.useUiHeight = ip.useUiHeight;
//  //}
//
//  op.horizontalAlignment = ip.horizontalAlignment;
//  op.verticalAlignment = ip.verticalAlignment;
//  op.fillHorizontal = ip.fillHorizontal;
//  op.fillVertical = ip.fillVertical;
//  op.widthHint = ip.widthInPixel;
//  op.heightHint = ip.heightInPixel;
//  if (op.weighty === 0 || (op.weighty < 0 && op.gridh <= 1)) {
//    op.fillVertical = false;
//  }
//  return op;
//};
//
//scout.LogicalGridDataBuilder.prototype._inheritWeightY = function(f) {
//  // see: SwingScoutFormFieldGridData
//};
//
//scout.LogicalGridDataBuilder.prototype._inheritWeightYRec = function(f) {
//  //see: SwingScoutFormFieldGridData
//};
//
