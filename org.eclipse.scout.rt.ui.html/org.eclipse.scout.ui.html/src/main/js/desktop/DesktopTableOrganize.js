// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.DesktopTableOrganize = function (scout, $controlContainer, model, columns, table) {
//  var response = scout.send('dataModel', model.outlineId, {"nodeId": model.nodeId}),
//    dataModel = response.events[0].dataModel;

  $controlContainer.empty();
  var $organizeColumn = $controlContainer.appendDiv('OrganizeColumn'),
    $organizeCommand = $controlContainer.appendDiv('OrganizeCommand'),
    $organizeDialog = $controlContainer.appendDiv('OrganizeDialog');

  // draw all columns
  for (var c = 0; c < columns.length; c++) {
    var column = columns[c];

    if (column.type == 'key') continue;

    $column = $organizeColumn.appendDiv('', 'column-item', column.text)
      .data('column', c)
      .on('click', '', selectColumn);


    if (column.$div.hasClass('sort-up')) {
      $column.appendDiv('', 'column-flag sort-up');
    }

    if (column.$div.hasClass('sort-down')) {
      $column.appendDiv('', 'column-flag sort-down');
    }

    if (column.$div.hasClass('sort-filter')) {
      $column.appendDiv('', 'column-flag sort-filter');
    }
  }

  $organizeCommand.appendDiv('', 'command-item sort-up');
  $organizeCommand.appendDiv('', 'command-item sort-down');
  $organizeCommand.appendDiv('', 'command-item filter');
  $organizeCommand.appendDiv('', 'command-item group');
  $organizeCommand.appendDiv('', 'command-item add');
  $organizeCommand.appendDiv('', 'command-item remove');
  $organizeCommand.appendDiv('', 'command-item change');
  $organizeCommand.appendDiv('', 'command-item move-up');
  $organizeCommand.appendDiv('', 'command-item move-down');

  // draw all commands

  // prepare command section

  function selectColumn () {
    var $clicked = $(this);
    $clicked.selectOne('selected');
  }

  // test
  // exportExcel('Datenblatt', table);

  function exportExcel (name, table) {
      // http://jsfiddle.net/cmewv/537/
      var uri = 'data:application/vnd.ms-excel;base64,',
        template = '<html xmlns:o="urn:schemas-microsoft-com:office:office"' +
          'xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40"><head>' +
          '<!--[if gte mso 9]><xml><x:ExcelWorkbook><x:ExcelWorksheets><x:ExcelWorksheet><x:Name>{worksheet}' +
          '</x:Name><x:WorksheetOptions><x:DisplayGridlines/></x:WorksheetOptions></x:ExcelWorksheet>' +
          '</x:ExcelWorksheets></x:ExcelWorkbook></xml><![endif]--><meta http-equiv="content-type" ' +
          'content="text/plain; charset=UTF-8"/></head><body><table>{table}</table></body></html>';

      // build html table
      var html = '<table><tr>';

      for (var c = 0; c < columns.length; c++) {
        var column = model.table.columns[c];

        if (column.type == 'key') continue;

        html += '<td style="font-weight: bold;">' + column.text + '</td>';
      }

      html += '</tr>';

      for (var r = 0; r < table.length; r++) {
        html += '<tr>';

        for (var c = 0; c < columns.length; c++) {
          var column = model.table.columns[c],
            value = table[r][c];

          if (column.type == 'key') continue;

          html += '<td>' + value + '</td>';
        }

        html += '</tr>';
      }

      html += '</table>';

      // open excel
      template = template.replace('{worksheet}', name).replace('{table}', html);
      template = window.btoa(unescape(encodeURIComponent(template)));
      window.location.href = uri + template;
    };
};
