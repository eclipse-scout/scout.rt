scout.TableLayout = function() {
};

/**
 * @param groupBox
 * @param fields fields in group-box without system buttons
 */
scout.TableLayout.prototype.render = function($parent, groupBox, fields) {
  var tmp = this._analyzeFields(fields);
  var maxY = tmp[0], fieldMap = tmp[1], $tr, $td, field, y, i;
  var $table = $('<table>').
    addClass('form-grid').
    addClass('cols-' + groupBox.gridColumnCount);

  $parent.append($table);
  for (y = 0; y <= maxY; y++) {
    $tr = $('<tr>');
    $table.append($tr);
    if (fieldMap.hasOwnProperty(y)) {
      for (i = 0; i < fieldMap[y].length; i++) {
        field = fieldMap[y][i];
        $td = $('<td>').addClass('form-field');
        $tr.append($td);
        if (field.gridData.w > 1) {
          $td.attr('colspan', field.gridData.w);
        }
        if (field.gridData.h > 1) {
          $td.attr('rowspan', field.gridData.h);
        }
        field.render($td);
      }
    }
  }
};

scout.TableLayout.prototype._analyzeFields = function(fields) {
  var fieldMap = {}, // key = y| value = array of fields
    maxY = 0, fieldsByY, i, field, gridData, y;

  for (i = 0; i < fields.length; i++) {
    field = fields[i];
    gridData = field.gridData;
    y = gridData.y + gridData.h - 1;
    if (y > maxY) {
      maxY = y;
    }
    if (fieldMap.hasOwnProperty(gridData.y)) {
      fieldsByY = fieldMap[gridData.y];
    } else {
      fieldsByY = [];
      fieldMap[gridData.y] = fieldsByY;
    }
    fieldsByY.push(field);
  }
  // TODO AWE: (layout) sorty fields by X
  return [maxY, fieldMap];
};
