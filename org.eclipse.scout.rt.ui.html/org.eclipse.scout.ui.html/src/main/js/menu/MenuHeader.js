// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.MenuHeader = function(desktopTable, $header, x, y) {
  //  var response = scout.sendSync('dataModel', model.outlineId, {"nodeId": model.nodeId}),
  //    dataModel = response.events[0].dataModel;

  $('.header-menu').remove();
  $('body').off('mousedown.remove');
  $('body').off('keydown.remove');

  var pos = $header.index(),
    id = $header.data('index'),
    column = desktopTable.model.table.columns[id];

  // create titel
  var $menuHeaderTitle = $('body').appendDiv('MenuHeaderTitle', 'header-menu')
    .css('left', x - 12).css('top', y - 4)
    .css('width', $header.width() + 18)
    .css('height', $header.parent().height() + 1)
    .text($header.text());

  if (column.type == 'number') $menuHeaderTitle.css('text-align', 'right');

  // create container
  var $menuHeader = $('body').appendDiv('MenuHeader', 'header-menu')
    .css('left', x - 12).css('top', y + $header.parent().height() - 5);

  var $headerCommand = $menuHeader.appendDiv('HeaderCommand'),
    $headerFilter = $menuHeader.appendDiv('HeaderFilter')

  // every user action will close menu
  $('body').on('mousedown.remove', removeMenu);
  $('body').on('keydown.remove', removeMenu);

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

  var $sortUp = $commandSort.appendDiv('HeaderCommandSortUp', 'header-command')
    .data('label', 'aufsteigend')
    .click(function() {
      sort('up', false, $(this).hasClass('selected'));
    });
  var $sortDown = $commandSort.appendDiv('HeaderCommandSortDown', 'header-command')
    .data('label', 'absteigend')
    .click(function() {
      sort('down', false, $(this).hasClass('selected'));
    });
  var $sortUpAdd = $commandSort.appendDiv('HeaderCommandSortUpAdd', 'header-command')
    .data('label', 'zusätzlich aufsteigend')
    .click(function() {
      sort('up', true, $(this).hasClass('selected'));
    });
  var $sortDownAdd = $commandSort.appendDiv('HeaderCommandSortDownAdd', 'header-command')
    .data('label', 'zusätzlich absteigend')
    .click(function() {
      sort('down', true, $(this).hasClass('selected'));
    });

  sortSelect();

  // create buttons in command for grouping
  if (column.type === 'text' || column.type === 'date') {
    var $commandGroup = $headerCommand.appendDiv('', 'header-group');
    $commandGroup.appendDiv('', 'header-text')
      .data('label', 'Summe');

    var $groupAll = $commandGroup.appendDiv('HeaderCommandGroupAll', 'header-command')
      .data('label', 'über alles')
      .click(groupAll);

    var $groupSort = $commandGroup.appendDiv('HeaderCommandGroupSort', 'header-command')
      .data('label', 'gruppiert')
      .click(groupSort);

    groupSelect();
  }

  // create buttons in command for coloring
  if (column.type === 'number') {
    var $commandColor = $headerCommand.appendDiv('', 'header-group');
    $commandColor.appendDiv('', 'header-text')
      .data('label', 'Einfärben');

    $commandColor.appendDiv('HeaderCommandColorRed', 'header-command')
      .data('label', 'von Rot nach Grün')
      .click(colorRed);
    $commandColor.appendDiv('HeaderCommandColorGreen', 'header-command')
      .data('label', 'von Grün nach Rot')
      .click(colorGreen);
    $commandColor.appendDiv('HeaderCommandColorBar', 'header-command')
      .data('label', 'mit Balkendiagramm')
      .click(colorBar);
    $commandColor.appendDiv('HeaderCommandColorRemove', 'header-command')
      .data('label', 'entfernen')
      .click(colorRemove);
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
  $headerFilter.appendDiv('', 'header-text')
    .data('label', 'Filtern nach');

  var matrix = new Scout.DesktopMatrix(desktopTable),
    xAxis = matrix.addAxis(id, -1),
    dataAxis = matrix.addData(-1, -1),
    cube = matrix.calculateCube();

  var $headerFilterContainer = $headerFilter.appendDiv('HeaderFilterContainer'),
    $headerFilterScroll = $headerFilterContainer.appendDiv('HeaderFilterScroll');

  for (var a = 0; a < xAxis.length; a++) {
    var key = xAxis[a],
      mark = xAxis.format(key),
      value = cube.getValue([key])[0];

    var $filter = $headerFilterScroll.appendDiv('', 'header-filter', mark)
      .attr('data-xAxis', key)
      .click(filterClick)
      .attr('data-value', value);

    if (column.filter.indexOf(key) > -1) $filter.addClass('selected');
  }

  var containerHeight = $headerFilterContainer.get(0).offsetHeight,
    scrollHeight = $headerFilterScroll.get(0).scrollHeight;

  if (containerHeight < scrollHeight) {
    var scrollbar = new Scout.Scrollbar($headerFilterScroll , 'y');
    scrollbar.initThumb();
    $('.header-filter', $headerFilterScroll).css('width', 'calc(100% - 22px)');
  } else {
    $headerFilterScroll.css('height', 'auto');
    scrollHeight = $headerFilterScroll.get(0).offsetHeight;
    $headerFilterContainer.css('height', scrollHeight);
  }

  // name all label elements
  $('.header-text').each(function() {
    $(this).text($(this).data('label'));
  });

  // set events to buttons
  $headerCommand
    .on('mouseenter click', '.header-command', enterCommand)
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

  function removeMenu(event) {
    if ($menuHeader.has($(event.target)).length === 0) {
      $menuHeader.animateAVCSD('height', 0, function() {
        $menuHeader.remove();
        $menuHeaderTitle.remove();
      });

      $('body').off('mousedown.remove');
      $('body').off('keydown.remove');
    }
    return false;
  }

  // event handling
  function enterCommand() {
    var $command = $(this),
      $text = $command.siblings('.header-text'),
      text = $command.hasClass('selected') ? 'entfernen' : $command.data('label');

    $text.text($text.data('label') + ' ' + text);
  }

  function leaveCommand() {
    var $command = $(this),
      $text = $command.siblings('.header-text');

    $text.text($text.data('label'));
  }

  function moveTop() {
    desktopTable.moveColumn($header, pos, -1);
    pos = $header.index();
  }

  function moveUp() {
    desktopTable.moveColumn($header, pos, Math.max(pos - 4, -1));
    pos = $header.index();
  }

  function moveDown() {
    desktopTable.moveColumn($header, pos, Math.min(pos + 2, $('.header-item, .header-resize').length - 2));
    pos = $header.index();
  }

  function moveBottom() {
    desktopTable.moveColumn($header, pos, $('.header-item, .header-resize').length - 2);
    pos = $header.index();
  }

  function sort(dir, additional, remove) {
    desktopTable.groupChange($header, false, false);
    desktopTable.sortChange($header, dir, additional, remove);

    sortSelect();
    groupSelect();
  }

  function sortSelect() {
    var addIcon = '\uF067',
      sortCount = $('.header-item[data-sort-order]').length;

    $('.header-command', $commandSort).removeClass('selected');

    if (sortCount == 1) {
      if ($header.hasClass('sort-up')) {
        $sortUp.addClass('selected');
        addIcon = null;
      } else if ($header.hasClass('sort-down')) {
        $sortDown.addClass('selected');
        addIcon = null;
      }
    } else if (sortCount > 1) {
      if ($header.hasClass('sort-up')) {
        $sortUpAdd.addClass('selected');
        addIcon = parseInt($header.attr('data-sort-order'), 0) + 1;
      } else if ($header.hasClass('sort-down')) {
        $sortDownAdd.addClass('selected');
        addIcon = parseInt($header.attr('data-sort-order'), 0) + 1;
      }
    } else {
      addIcon = null;
    }

    if (addIcon) {
      $sortUpAdd.show().attr('data-icon', addIcon);
      $sortDownAdd.show().attr('data-icon', addIcon);
    } else {
      $sortUpAdd.hide();
      $sortDownAdd.hide();
    }
  }

  function groupAll() {
    desktopTable.groupChange($header, !$(this).hasClass('selected'), true);

    sortSelect();
    groupSelect();
  }

  function groupSort() {
    desktopTable.groupChange($header, !$(this).hasClass('selected'), false);

    sortSelect();
    groupSelect();
  }

  function groupSelect () {
    $groupAll.removeClass('selected');
    $groupSort.removeClass('selected');

    if  ($header.parent().hasClass('group-all')) $groupAll.addClass('selected');
    if  ($header.hasClass('group-sort')) $groupSort.addClass('selected');
  }

  function colorRed() {
    desktopTable.colorData('red', id);
  }

  function colorGreen() {
    desktopTable.colorData('green', id);
  }

  function colorBar() {
    desktopTable.colorData('bar', id);
  }

  function colorRemove() {
    desktopTable.colorData('remove', id);
  }


  function columnAdd() {}

  function columnChange() {}

  function columnRemove() {}

  function filterClick(event) {
    var $clicked = $(this);

    // change state
    if ($clicked.hasClass('selected')) {
      $clicked.removeClass('selected');
    } else {
      $clicked.addClass('selected');
    }

    //  prepare filter
    column.filter = [];

    //  find filter
    $('.selected', $headerFilter).each(function() {
      var dX = parseFloat($(this).attr('data-xAxis'));
      column.filter.push(dX);
    });

    // filter function
    if (column.filter.length) {
      column.filterFunc = function($row) {
        var textX = $row.children().eq(xAxis.column).text(),
          nX = xAxis.norm(textX);

        return (column.filter.indexOf(nX) > -1);
      }
    } else {
      column.filterFunc = null;
    }

    // callback to table
    desktopTable.filter();
  }
};
