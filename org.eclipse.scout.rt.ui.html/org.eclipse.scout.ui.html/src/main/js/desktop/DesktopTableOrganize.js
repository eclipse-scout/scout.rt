// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.DesktopTableOrganize = function (scout, $controlContainer, model, columns, desktopTable) {
//  var response = scout.sendSync('dataModel', model.outlineId, {"nodeId": model.nodeId}),
//    dataModel = response.events[0].dataModel;

  $controlContainer.empty();
  var $organizeColumn = $controlContainer.appendDiv('OrganizeColumn'),
    $organizeCommand = $controlContainer.appendDiv('OrganizeCommand'),
    $organizeDialog = $controlContainer.appendDiv('OrganizeDialog');

  // draw all columns
  drawColumns();

  // draw all commands

  var $commandSort = $organizeCommand.appendDiv('', 'command-group');
  $commandSort.appendDiv('', 'command-text')
    .data('label', 'Sortierung');
  $commandSort.appendDiv('', 'command-item sort-up')
    .data('label', 'aufsteigend')
    .click(function () {sort('up', false); });
  $commandSort.appendDiv('', 'command-item sort-down')
    .data('label', 'absteigend')
    .click(function () {sort('down', false); });
  $commandSort.appendDiv('', 'command-item sort-up-add')
    .data('label', 'zusätzlich aufsteigend')
    .click(function () {sort('up', true); });
  $commandSort.appendDiv('', 'command-item sort-down-add')
    .data('label', 'zusätzlich absteigend')
    .click(function () {sort('down', true); });

  var $commandFilter = $organizeCommand.appendDiv('', 'command-group');
  $commandFilter.appendDiv('', 'command-text')
    .data('label', 'Filter');
  $commandFilter.appendDiv('', 'command-item filter')
    .data('label', 'setzen')
    .click(filterAdd);
  $commandFilter.appendDiv('', 'command-item filter-remove')
    .data('label', 'entfernen')
    .click(filterRemove);

  var $commandGroup = $organizeCommand.appendDiv('', 'command-group');
  $commandGroup.appendDiv('', 'command-text')
    .data('label', 'Summe');
  $commandGroup.appendDiv('', 'command-item group-all')
    .data('label', 'über alles')
    .click(groupAll);
  $commandGroup.appendDiv('', 'command-item group-sort')
    .data('label', 'gruppiert')
    .click(groupSort);
  $commandGroup.appendDiv('', 'command-item group-remove')
    .data('label', 'entfernen')
    .click(groupRemove);

  var $commandColumn = $organizeCommand.appendDiv('', 'command-group');
  $commandColumn.appendDiv('', 'command-text')
    .data('label', 'Spalte');
  $commandColumn.appendDiv('', 'command-item column-add')
    .data('label', 'hinzufügen')
    .click(columnAdd);
  $commandColumn.appendDiv('', 'command-item column-change')
    .data('label', 'ändern')
    .click(columnChange);
  $commandColumn.appendDiv('', 'command-item column-remove')
    .data('label', 'entfernen')
    .click(columnRemove);

  var $commandMove = $organizeCommand.appendDiv('', 'command-group');
  $commandMove.appendDiv('', 'command-text')
    .data('label', 'Verschieben');
  $commandMove.appendDiv('', 'command-item move-top')
    .data('label', 'nach ganz oben')
    .click(moveTop);
  $commandMove.appendDiv('', 'command-item move-up')
    .data('label', 'nach oben')
    .click(moveUp);
  $commandMove.appendDiv('', 'command-item move-down')
    .data('label', 'nach unten')
    .click(moveDown);
  $commandMove.appendDiv('', 'command-item move-bottom')
    .data('label', 'nach ganz unten')
    .click(moveBottom);

  // name all label elements
  $('.command-text').each( function () {
    $(this).text($(this).data('label'));
  });

  // set events to buttons

  $organizeCommand
    .on('mouseenter', '.command-item', enterCommand)
    .on('mouseleave', '.command-item', leaveCommand);

  // prepare command section


  // event handling
  function selectColumn () {
    var $clicked = $(this);
    $clicked.selectOne('selected');
  }

  function enterCommand () {
    var $command = $(this),
      $text = $command.siblings('.command-text');

    $text.text( $text.data('label') + ' ' + $command.data('label'));
  }

  function leaveCommand () {
    var $command = $(this),
      $text = $command.siblings('.command-text');

    $text.text($text.data('label'));
  }

  function sort (dir, additional) {
    var id = $('.selected', $organizeColumn).index();
    desktopTable.sortChange(id, dir, additional);
    drawColumns();
  }

  function filterAdd () {
    var id = $('.selected', $organizeColumn).index();
    columns[id].$div.addClass('filter');
    drawColumns();
  }

  function filterRemove () {
    var id = $('.selected', $organizeColumn).index();
    columns[id].$div.removeClass('filter');
    drawColumns();
  }

  function groupAll () {
    if (!$(this).hasClass('selected')) {
      $(this).selectOne('selected');
      desktopTable.sumData(true);
      drawColumns();
    }
  }

  function groupSort () {
    $(this).selectOne('selected');

    var id = $('.selected', $organizeColumn).index();
    desktopTable.sortChange(id, 'up', false);
    desktopTable.sumData(true, id);

    drawColumns();
  }

  function groupRemove () {
    $(this).siblings().removeClass('selected');
    desktopTable.sumData(false);
  }

  function columnAdd () {
  }

  function columnChange () {
  }

  function columnRemove () {
  }

  function moveTop () {
    var id = $('.selected', $organizeColumn).index();
    moveTo(id, -1);
  }

  function moveUp() {
    var id = $('.selected', $organizeColumn).index();
    moveTo(id, Math.max(id - 2, -1));
  }

  function moveDown () {
    var id = $('.selected', $organizeColumn).index();
    moveTo(id, Math.min(id + 1, columns.length - 1));
  }

  function moveBottom () {
    var id = $('.selected', $organizeColumn).index();
    moveTo(id, columns.length - 1);
  }

  function moveTo (oldPos, newPos) {
    var $columns = $('.column-item'),
      $move = $columns.eq(oldPos);

    // store old position
    $columns.each(function () {
      $(this).data('old-top', $(this).offset().top);
    });

    // change order in dom
    if (newPos == -1) {
      $organizeColumn.prepend($move);
    } else {
      $columns.eq(newPos).after($move);
    }

    // move to old position and then animate
    $columns.each(function (i) {
      $(this).css('top', $(this).data('old-top') - $(this).offset().top)
        .animateAVCSD('top', 0);
    });

    // FIXME cru: change columns, even table and header
    // columns.splice(newPos + 1, 0, columns.splice(oldPos, 1)[0]);
  }

  function drawColumns () {
    var selected = $('.selected', $organizeColumn).index();

    $organizeColumn.empty();

    for (var c = 0; c < columns.length; c++) {
      var column = columns[c];

      if (column.type == 'key') continue;

      $column = $organizeColumn.appendDiv('', 'column-item', column.text)
        .data('column', c)
        .on('click', '', selectColumn);

      if (column.$div.hasClass('sort-up')) {
        $column.appendDiv('', 'column-flag sort-up')
          .attr('data-sort-order', column.$div.data('sort-order') + 1);
      }

      if (column.$div.hasClass('sort-down')) {
        $column.appendDiv('', 'column-flag sort-down')
          .attr('data-sort-order', column.$div.data('sort-order') + 1);;
      }
      if (column.$div.hasClass('filter')) {
        log(1);
        $column.appendDiv('', 'column-flag filter');
      }
    }
    if (selected >= 0) {
      $('.column-item').eq(selected).addClass('selected');
    } else {
      $('.column-item').first().addClass('selected');
    }

    // if only one column is sorted remove small numbers indicating order
    var $sorted = $('.column-flag.sort-up, .column-flag.sort-down');
    if ($sorted.length === 1) $sorted.attr('data-sort-order', '');
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
