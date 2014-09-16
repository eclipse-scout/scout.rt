// TODO AWE: delete this file (TableLayout.js)
//scout.TableLayout = function() {
//};
//
///**
// * @param groupBox
// * @param fields fields in group-box without system buttons
// */
//scout.TableLayout.prototype.render = function($parent, groupBox, fields) {
//  var tmp = this._analyzeFields(fields);
//  var maxY = tmp[0], fieldMap = tmp[1], $tr, $td, field, y, i;
//
//  /**
//   * For each TD we increase the widthSum by 1, colspan on a TD also increases the sum.
//   * When sum is smaller than the configured column count we must add a filler TD to avoid
//   * HTML layout problems. Reproduce with a table with cols-2, 1 tr with 1 td and colspan=2,
//   * 1 tr with 1 td without colspan --> last td has wrong width (100% instead of 50%).
//   */
//  var widthSum = 0;
//  var $table = $('<table>').
//    addClass('form-grid').
//    addClass('cols-' + groupBox.gridColumnCount);
//
//  $parent.append($table);
//  for (y = 0; y <= maxY; y++) {
//    $tr = $('<tr>').appendTo($table);
//    if (fieldMap.hasOwnProperty(y)) {
//      widthSum = 0;
//      for (i = 0; i < fieldMap[y].length; i++) {
//        field = fieldMap[y][i];
//        $td = $('<td>').appendTo($tr);
//        widthSum++;
//        if (field.gridData.w > 1) {
//          $td.attr('colspan', field.gridData.w);
//          widthSum += field.gridData.w - 1;
//        }
//        if (field.gridData.h > 1) {
//          $td.attr('rowspan', field.gridData.h);
//        }
//        field.render($td);
//      }
//      var widthDiff = groupBox.gridColumnCount - widthSum;
//      for (i = 0; i < widthDiff; i++) {
//        $('<td>').addClass('filler').appendTo($tr);
//      }
//    }
//  }
//};
//
//scout.TableLayout.prototype._analyzeFields = function(fields) {
//  var fieldMap = {}, // key = y| value = array of fields
//    maxY = 0, fieldsByY, i, field, gridData, y;
//
//  for (i = 0; i < fields.length; i++) {
//    field = fields[i];
//    gridData = field.gridData;
//    y = gridData.y + gridData.h - 1;
//    if (y > maxY) {
//      maxY = y;
//    }
//    if (fieldMap.hasOwnProperty(gridData.y)) {
//      fieldsByY = fieldMap[gridData.y];
//    } else {
//      fieldsByY = [];
//      fieldMap[gridData.y] = fieldsByY;
//    }
//    fieldsByY.push(field);
//  }
//  // TODO AWE: (layout) sorty fields by X
//  return [maxY, fieldMap];
//};
