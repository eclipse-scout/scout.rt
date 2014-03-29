// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.MenuHeader = function(desktopTable, $header, filterCallback, x, y) {
  //  var response = scout.sendSync('dataModel', model.outlineId, {"nodeId": model.nodeId}),
  //    dataModel = response.events[0].dataModel;

  $('.header-menu').remove();

  var id = $header.data('index'),
    column = desktopTable.model.table.columns[id];

  // create titel
  var $menuHeaderTitle = $('body').appendDiv('MenuHeaderTitle', 'header-menu')
    .css('left', x - 12).css('top', y - 4)
    .css('width', $header.width() + 18)
    .css('height', $header.parent().height() + 1)
    .text($header.text());

  // create container
  var $menuHeader = $('body').appendDiv('MenuHeader', 'header-menu')
    .css('left', x - 12).css('top', y + $header.parent().height() - 5);

  var $headerCommand = $menuHeader.appendDiv('HeaderCommand'),
    $headerFilter = $menuHeader.appendDiv('HeaderFilter'),
    $headerModel = $menuHeader.appendDiv('HeaderModel');

  // create buttons in command for order
  var $commandMove = $headerCommand.appendDiv('', 'header-group');
  $commandMove.appendDiv('', 'header-text')
    .data('label', 'Verschieben');

  $commandMove.appendDiv('HeaderCommandMoveTop', 'header-command')
    .data('label', 'an den Anfang')
    .click(moveTop);
  $commandMove.appendDiv('HeaderCommandMoveUp', 'header-command')
    .data('label', 'nach vorne')
    .click(moveUp);
  $commandMove.appendDiv('HeaderCommandMoveDown', 'header-command')
    .data('label', 'nach hinten')
    .click(moveDown);
  $commandMove.appendDiv('HeaderCommandMoveBottom', 'header-command')
    .data('label', 'an das Ende')
    .click(moveBottom);

  // create buttons in command for sorting
  var $commandSort = $headerCommand.appendDiv('', 'header-group');
  $commandSort.appendDiv('', 'header-text')
    .data('label', 'Sortierung');

  $commandSort.appendDiv('HeaderCommandSortUp', 'header-command')
    .data('label', 'aufsteigend')
    .click(function() {
      sort('up', false);
    });
  $commandSort.appendDiv('HeaderCommandSortDown', 'header-command')
    .data('label', 'absteigend')
    .click(function() {
      sort('down', false);
    });
  $commandSort.appendDiv('HeaderCommandSortUpAdd', 'header-command')
    .data('label', 'zusätzlich aufsteigend')
    .click(function() {
      sort('up', true);
    });
  $commandSort.appendDiv('HeaderCommandSortDownAdd', 'header-command')
    .data('label', 'zusätzlich absteigend')
    .click(function() {
      sort('down', true);
    });

  // create buttons in command for grouping
  log(column.type);

  if (column.type === '') {
    var $commandGroup = $headerCommand.appendDiv('', 'header-group');
    $commandGroup.appendDiv('', 'header-text')
      .data('label', 'Summe');

    $commandGroup.appendDiv('HeaderCommandGroupAll', 'header-command')
      .data('label', 'über alles')
      .click(groupAll);
    $commandGroup.appendDiv('HeaderCommandGroupSort', 'header-command')
      .data('label', 'gruppiert')
      .click(groupSort);
    $commandGroup.appendDiv('HeaderCommandGroupRemove', 'header-command')
      .data('label', 'entfernen')
      .click(groupRemove);
  }

  // create buttons in command for coloring
  if (column.type === '') {
    var $commandColor = $headerCommand.appendDiv('', 'header-group');
    $commandColor.appendDiv('', 'header-text')
      .data('label', 'Einfärben');

    $commandColor.appendDiv('HeaderCommandColorRed', 'header-command')
      .data('label', 'von Rot nach Grün')
      .click(groupAll);
    $commandColor.appendDiv('HeaderCommandColorGreen', 'header-command')
      .data('label', 'von Grün nach Rot')
      .click(groupSort);
    $commandColor.appendDiv('HeaderCommandColorBar', 'header-command')
      .data('label', 'mit Balkendiagramm')
      .click(groupRemove);
    $commandColor.appendDiv('HeaderCommandColorRemove', 'header-command')
      .data('label', 'entfernen')
      .click(groupRemove);
  }

  // create buttons in command for new columns
  var $commandColumn = $headerCommand.appendDiv('', 'header-group');
  $commandColumn.appendDiv('', 'header-text')
    .data('label', 'Spalte');

  $commandColumn.appendDiv('HeaderCommandColumnAdd', 'header-command')
    .data('label', 'hinzufügen')
    .click(columnAdd);
  $commandColumn.appendDiv('HeaderCommandColumnRemove', 'header-command')
    .data('label', 'entfernen')
    .click(columnRemove);


  // filter
  // TODO cru: add scrollbar
  $headerFilter.appendDiv('', 'header-text')
    .data('label', 'Filtern nach');

  var matrix = new Scout.DesktopMatrix(desktopTable.model.table),
    xAxis = matrix.addAxis(id, -1),
    dataAxis = matrix.addData(-1, -1),
    cube = matrix.calculateCube();

  for (var a = 0; a < xAxis.length; a++) {
    var key = xAxis[a],
      mark = xAxis.format(key),
      value = cube.getValue([key])[0];

    $headerFilter.appendDiv('', 'header-filter', mark)
      .attr('data-xAxis', key)
      .click(filterClick)
      .attr('data-value', value);
  }

  // name all label elements
  $('.header-text').each(function() {
    $(this).text($(this).data('label'));
  });

  // set events to buttons
  $headerCommand
    .on('mouseenter', '.header-command', enterCommand)
    .on('mouseleave', '.header-command', leaveCommand);

  // copy flags to menu
  if ($header.hasClass('sort-up')) $menuHeader.addClass('sort-up');
  if ($header.hasClass('sort-down')) $menuHeader.addClass('sort-down');
  if ($header.hasClass('filter')) $menuHeader.addClass('filter');

  // animated opening
  var h = $menuHeader.css('height');
  $menuHeader.css('height', 0)
    .animateAVCSD('height', h);

  // title should not be wider than menu
  $menuHeaderTitle.width(Math.min($menuHeaderTitle.width(), $menuHeader.width() - 14) );

  // every user action will close menu
  $('*').one('mousedown keydown mousewheel', removeMenu);

<<<<<<< HEAD
  function removeMenu(event) {
    $menuHeader.animateAVCSD('height', 0, function() {
      $menuHeader.remove();
      $menuHeaderTitle.remove();
    });
=======
  function removeMenu() {
    $menuHeader.animateAVCSD('height', 32, $.removeThis);
>>>>>>> refs/remotes/origin/develop
    return true;
  }

  // event handling

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

  function sort(dir, additional) {
    desktopTable.sortChange(id, dir, additional);
  }

  function groupAll() {
    if (!$(this).hasClass('selected')) {
      $(this).selectOne('selected');
      desktopTable.sumData(true);
    }
  }

  function groupSort() {
    $(this).selectOne('selected');
    desktopTable.sumData(true, id);
  }

  function groupRemove() {
    $(this).siblings().removeClass('selected');
    desktopTable.sumData(false);
  }

  function columnAdd() {}

  function columnChange() {}

  function columnRemove() {}

  function moveTop() {
    var id = $('.selected', $organizeColumn).index();
    moveTo(id, -1);
  }

  function moveUp() {
    var id = $('.selected', $organizeColumn).index();
    moveTo(id, Math.max(id - 2, -1));
  }

  function moveDown() {
    var id = $('.selected', $organizeColumn).index();
    moveTo(id, Math.min(id + 1, columns.length - 1));
  }

  function moveBottom() {
    var id = $('.selected', $organizeColumn).index();
    moveTo(id, columns.length - 1);
  }

  function moveTo(oldPos, newPos) {
    var $columns = $('.column-item'),
      $move = $columns.eq(oldPos);

    // store old position
    $columns.each(function() {
      $(this).data('old-top', $(this).offset().top);
    });

    // change order in dom
    if (newPos == -1) {
      $organizeColumn.prepend($move);
    } else {
      $columns.eq(newPos).after($move);
    }

    // move to old position and then animate
    $columns.each(function(i) {
      $(this).css('top', $(this).data('old-top') - $(this).offset().top)
        .animateAVCSD('top', 0);
    });
  }

  function filterClick(event) {
    var $clicked = $(this);

    // change state
    if (event.ctrlKey) {
      if ($clicked.hasClass('selected')) {
        $clicked.removeClass('selected');
      } else {
        $clicked.addClass('selected');
      }
    } else {
      $clicked.selectOne();
    }

    //  prepare filter
    var filters = [];

    //  find all filter
    $('.selected', $headerFilter).each(function() {
      var dX = parseFloat($(this).attr('data-xAxis'));
      filters.push(dX);
    });

    //  filter function
    var testFunc = function($row) {
      var textX = $row.children().eq(xAxis.column).text(),
        nX = xAxis.norm(textX);

      return (filters.indexOf(nX) > -1);
    };

    // callback to table
    filterCallback(testFunc);
  }
};
