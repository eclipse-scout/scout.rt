// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.MenuTable = function(desktopTable, x, y) {

  $('.header-menu').remove();
  $('body').off('mousedown.remove');
  $('body').off('keydown.remove');

  // create titel
  var $menuTableTitle = $('body').appendDiv('MenuTableTitle', 'header-menu')
    .css('left', x - 8).css('top', y - 8);

  // create container
  var $menuTable = $('body').appendDiv('MenuTable', 'header-menu')
    .css('left', x - 243).css('top', y + 24);

  // every user action will close menu
  $('body').on('mousedown.remove', removeMenu);
  $('body').on('keydown.remove', removeMenu);


  // create buttons in command for set
  var $commandSet = $menuTable.appendDiv('', 'header-group');
  $commandSet.appendDiv('', 'header-text')
    .data('label', 'Spalten speichern');

  $commandSet.appendDiv('HeaderCommandSetUser1', 'header-command')
    .data('label', 'für Slot 1')
    .click();
  $commandSet.appendDiv('HeaderCommandSetUser2', 'header-command')
    .data('label', 'für Slot 2')
    .click();
  $commandSet.appendDiv('HeaderCommandSetUser3', 'header-command')
    .data('label', 'für Slot 3')
    .click();

  // create buttons in command for reset
  var $commandReset = $menuTable.appendDiv('', 'header-group');
  $commandReset.appendDiv('', 'header-text')
    .data('label', 'Spalten zurücksetzen');

  $commandReset.appendDiv('HeaderCommandResetUser1', 'header-command')
    .data('label', 'auf Slot 1')
    .click();
  $commandReset.appendDiv('HeaderCommandResetUser2', 'header-command')
    .data('label', 'auf Slot 2')
    .click();
  $commandReset.appendDiv('HeaderCommandResetUser3', 'header-command')
    .data('label', 'auf Slot 3')
    .click();
  $commandReset.appendDiv('HeaderCommandResetDefault', 'header-command')
  .data('label', 'auf Werkseinstellung')
  .click();

  // create buttons in command for export
  var $commandExport = $menuTable.appendDiv('', 'header-group');
  $commandExport.appendDiv('', 'header-text')
    .data('label', 'Exportieren');

  $commandExport.appendDiv('HeaderCommandExportExcel', 'header-command')
    .data('label', 'nach Excel')
    .click(exportExcel);
  $commandExport.appendDiv('HeaderCommandExportWord', 'header-command')
    .data('label', 'nach Word')
    .click();
  $commandExport.appendDiv('HeaderCommandExportPowerpoint', 'header-command')
    .data('label', 'nach Powerpoint')
    .click();
  $commandExport.appendDiv('HeaderCommandExportPDF', 'header-command')
    .data('label', 'nach PDF')
    .click();

  // create buttons in command for share
  var $commandShare = $menuTable.appendDiv('', 'header-group');
  $commandShare.appendDiv('', 'header-text')
    .data('label', 'Teilen');

  $commandShare.appendDiv('HeaderCommandShareEmail', 'header-command')
    .data('label', 'mit E-Mail')
    .click();
  $commandShare.appendDiv('HeaderCommandShareGoogle', 'header-command')
    .data('label', 'mit Sharepoint')
    .click();
  $commandShare.appendDiv('HeaderCommandShareTwitter', 'header-command')
    .data('label', 'mit Twitter')
    .click();
  $commandShare.appendDiv('HeaderCommandShareFacebook', 'header-command')
    .data('label', 'mit Facebook')
    .click();

  // text filter
  var $commandFilter = $menuTable.appendDiv('', 'header-group');
  $commandFilter.appendDiv('', 'header-text')
    .data('label', 'Filtern nach');

  $('<input id="FilterInput"></input>')
    .appendTo($commandFilter)
    .on('keydown', '', filterKey)
    .on('input paste', '', filterEnter)
    .val(desktopTable.model.table.filter);

  // name all label elements
  $('.header-text').each(function() {
    $(this).text($(this).data('label'));
  });

  // set events to buttons
  $menuTable
    .on('mouseenter', '.header-command', enterCommand)
    .on('mouseleave', '.header-command', leaveCommand);

  function removeMenu(event) {
    if ($menuTable.has($(event.target)).length === 0) {
      $menuTable.animateAVCSD('height', 0, function() {
        $menuTableTitle.remove();
        $menuTable.remove();
      });
      $('body').off('mousedown.remove');
      $('body').off('keydown.remove');
    }
  }

  function enterCommand() {
    var $command = $(this),
      $text = $command.siblings('.header-text');

    $text.text($text.data('label') + ' ' + $command.data('label'));
  }

  function leaveCommand() {
    var $command = $(this),
      $text = $command.siblings('.header-text');

    $text.text($text.data('label'));
  }

  function filterKey(event) {
    if (event.which == 27) {
      $menuTable.animateAVCSD('height', 0, function() {
        $menuTableTitle.remove();
        $menuTable.remove();
      });
      $('body').off('mousedown.remove');
      $('body').off('keydown.remove');
    }

    event.stopPropagation();
  }

  function filterEnter() {
    var $input = $(this);
    $input.val($input.val().toLowerCase());
    desktopTable.model.table.filter = $input.val();
    desktopTable.filter();
    event.stopPropagation();
  }

  function exportExcel() {
    var table = desktopTable.model.table;
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

    var c, column;
    for (c = 0; c < table.columns.length; c++) {
      column = table.columns[c];

      if (column.type == 'key') continue;

      html += '<td style="font-weight: bold;">' + column.text + '</td>';
    }

    html += '</tr>';

    for (var r = 0; r < table.rows.length; r++) {
      html += '<tr>';

      for (c = 0; c < table.columns.length; c++) {
        html += '<td>' + desktopTable.getText(c, r) + '</td>';
      }

      html += '</tr>';
    }

    html += '</table>';

    // open excel
    template = template.replace('{worksheet}', name).replace('{table}', html);
    template = window.btoa(unescape(encodeURIComponent(template)));
    window.location.href = uri + template;
  }
};
