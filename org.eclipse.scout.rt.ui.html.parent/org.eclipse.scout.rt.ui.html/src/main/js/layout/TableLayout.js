
scout.TableLayout = function() {

};

/**
 * @param groupBox
 * @param fields fields in group-box without system buttons
 */
scout.TableLayout.prototype.render = function($parent, groupBox, fields) {
  var cssClass = 'cols-' + groupBox.gridColumnCount;
  var id = 'GroupBox' + groupBox.id;
  // FIXME Use $.makeDiv
  var html = '<div id="' + id + '" class="group-box">' +
             '  <div class="group-box-title">Groupbox 1</div>' +
             '  <table class="form-grid ' + cssClass + '">' +
             '  </table>' +
             '</div>';
  var $groupBox = $(html);
  var $table = $groupBox.find('table');
  var tmp = this._analyzeFields(fields);
  var maxY = tmp[0],
      fieldMap = tmp[1];

  var $tr, $td, field, y, i;
  for (y=0; y<=maxY; y++) {
    $tr = $('<tr></tr>');
    if (fieldMap.hasOwnProperty(y)) {
      for (i=0; i<fieldMap[y].length; i++) {
        field = fieldMap[y][i];
        $td = $('<td class="form-field"></td>');
        if (field.gridData.w > 1) {
          $td.attr('colspan', field.gridData.w);
        }
        if (field.gridData.h > 1) {
          $td.attr('rowspan', field.gridData.h);
        }
        $tr.append($td);
        field.render($td);
      }
    }
    $table.append($tr);
  }
  $parent.append($table);
};

scout.TableLayout.prototype._analyzeFields = function(fields) {
  var fieldMap = {}, // key = y| value = array of fields
      maxY = 0,
      fieldsByY;
  for (var i=0; i<fields.length; i++) {
    var field = fields[i];
    var gridData = field.gridData;
    var y = gridData.y + gridData.h - 1;
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
  // TODO AWE: sorty fields by X
  return [maxY, fieldMap];
};
