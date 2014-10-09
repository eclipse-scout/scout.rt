// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TableHeaderMenu = function(table, $header, x, y, session) {
  var pos = table._header.getColumnViewIndex($header),
    column = $header.data('column');

  // label title
  $header.addClass('menu-open');

  // create container
  var $menuHeader = table.$container.appendDIV('table-header-menu')
    .css('left', x).css('top', y + $header.parent().height() + 1);

  $menuHeader.appendDIV('table-header-menu-whiter').width($header[0].offsetWidth - 2);

  this.$headerMenu = $menuHeader;
  this.$header = $header;

  // every user action will close menu
  $('body').on('mousedown.remove', onMouseDown);
  $('body').on('keydown.remove', removeMenu);

  // create buttons in command for order
  var $commandMove = $menuHeader.appendDIV('header-group');
  $commandMove.appendDIV('header-text')
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
  var $commandSort = $menuHeader.appendDIV('header-group');
  $commandSort.appendDIV('header-text')
    .data('label', 'Sortierung');

  var $sortAsc = $commandSort.appendDiv('HeaderCommandSortAsc', 'header-command')
    .data('label', 'aufsteigend')
    .click(function() {
      sort('asc', false, $(this).hasClass('selected'));
    });
  var $sortDesc = $commandSort.appendDiv('HeaderCommandSortDesc', 'header-command')
    .data('label', 'absteigend')
    .click(function() {
      sort('desc', false, $(this).hasClass('selected'));
    });
  var $sortAscAdd = $commandSort.appendDiv('HeaderCommandSortAscAdd', 'header-command')
    .data('label', 'zusätzlich aufsteigend')
    .click(function() {
      sort('asc', true, $(this).hasClass('selected'));
    });
  var $sortDescAdd = $commandSort.appendDiv('HeaderCommandSortDescAdd', 'header-command')
    .data('label', 'zusätzlich absteigend')
    .click(function() {
      sort('desc', true, $(this).hasClass('selected'));
    });

  sortSelect();

  // create buttons in command for grouping
  if (column.type === 'text' || column.type === 'date') {
    var $commandGroup = $menuHeader.appendDIV('header-group');
    $commandGroup.appendDIV('header-text')
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
    var $commandColor = $menuHeader.appendDIV('header-group');
    $commandColor.appendDIV('header-text')
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
  var $commandColumn = $menuHeader.appendDIV('header-group');
  $commandColumn.appendDIV('header-text')
    .data('label', 'Spalte');

  $commandColumn.appendDiv('HeaderCommandColumnAdd', 'header-command')
    .data('label', 'hinzufügen')
    .click(columnAdd);
  $commandColumn.appendDiv('HeaderCommandColumnRemove', 'header-command')
    .data('label', 'entfernen')
    .click(columnRemove);

  // filter
  var $headerFilter = $menuHeader.appendDIV('header-group-filter');
  $headerFilter.appendDIV('header-text')
    .data('label', 'Filtern nach');

  var group = (column.type === 'date') ?  3 : -1,
    matrix = new scout.ChartTableControlMatrix(table, session),
    xAxis = matrix.addAxis(column, group),
    dataAxis = matrix.addData(-1, -1),
    cube = matrix.calculateCube();

  var $headerFilterContainer = $headerFilter.appendDIV('header-filter-container'),
    $headerFilterScroll = scout.Scrollbar2.install($headerFilterContainer);

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

  if (containerHeight >= scrollHeight) {
    $headerFilterScroll.css('height', 'auto');
    scrollHeight = $headerFilterScroll.get(0).offsetHeight;
    $headerFilterContainer.css('height', scrollHeight);
  }

  // name all label elements
  $('.header-text').each(function() {
    $(this).text($(this).data('label'));
  });

  // set events to buttons
  $header
    .on('mouseenter click', '.header-command', enterCommand)
    .on('mouseleave', '.header-command', leaveCommand);

  // copy flags to menu
  if ($header.hasClass('sort-asc')) $menuHeader.addClass('sort-asc');
  if ($header.hasClass('sort-desc')) $menuHeader.addClass('sort-desc');
  if ($header.hasClass('filter')) $menuHeader.addClass('filter');

  var that = this;

  function onMouseDown(event) {
    if ($header.is($(event.target))) {
      return;
    }

    removeMenu(event);
  }

  function removeMenu(event) {
    if ($menuHeader.has($(event.target)).length === 0) {
      that.remove();
    }
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
    table.moveColumn($header, pos, 0);
    pos = table._header.getColumnViewIndex($header);
  }

  function moveUp() {
    table.moveColumn($header, pos, Math.max(pos - 1, 0));
    pos = table._header.getColumnViewIndex($header);
  }

  function moveDown() {
    table.moveColumn($header, pos, Math.min(pos + 1, table._header.findHeaderItems().length - 1));
    pos = table._header.getColumnViewIndex($header);
  }

  function moveBottom() {
    table.moveColumn($header, pos, table._header.findHeaderItems().length - 1);
    pos = table._header.getColumnViewIndex($header);
  }

  function sort(dir, additional, remove) {
    table.group($header, false, false);
    table.sort($header, dir, additional, remove);

    sortSelect();
    groupSelect();
  }

  function sortSelect() {
    var addIcon = '\uF067',
      sortCount = getSortColumnCount(),
      column = $header.data('column');

    $('.header-command', $commandSort).removeClass('selected');

    if (sortCount == 1) {
      if ($header.hasClass('sort-asc')) {
        $sortAsc.addClass('selected');
        addIcon = null;
      } else if ($header.hasClass('sort-desc')) {
        $sortDesc.addClass('selected');
        addIcon = null;
      }
    } else if (sortCount > 1) {
      if ($header.hasClass('sort-asc')) {
        $sortAscAdd.addClass('selected');
        addIcon = column.sortIndex + 1;
      } else if ($header.hasClass('sort-desc')) {
        $sortDescAdd.addClass('selected');
        addIcon = column.sortIndex + 1;
      }
    } else {
      addIcon = null;
    }

    if (addIcon) {
      $sortAscAdd.show().attr('data-icon', addIcon);
      $sortDescAdd.show().attr('data-icon', addIcon);
    } else {
      $sortAscAdd.hide();
      $sortDescAdd.hide();
    }
  }

  function getSortColumnCount() {
    var sortCount = 0;

    for (var i=0; i<table.columns.length; i++) {
      if (table.columns[i].sortActive) {
        sortCount++;
      }
    }

    return sortCount;
  }

  function groupAll() {
    table.group($header, !$(this).hasClass('selected'), true);

    sortSelect();
    groupSelect();
  }

  function groupSort() {
    table.group($header, !$(this).hasClass('selected'), false);

    sortSelect();
    groupSelect();
  }

  function groupSelect () {
    $groupAll.removeClass('selected');
    $groupSort.removeClass('selected');

    if ($header.parent().hasClass('group-all')) $groupAll.addClass('selected');
    if ($header.hasClass('group-sort')) $groupSort.addClass('selected');
  }

  function colorRed() {
    table.colorData('red', column);
  }

  function colorGreen() {
    table.colorData('green', column);
  }

  function colorBar() {
    table.colorData('bar', column);
  }

  function colorRemove() {
    table.colorData('remove', column);
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
        var row = table.getModelRowById($row.attr('id')),
          textX = table.getCellValue(xAxis.column, row),
          nX = xAxis.norm(textX);
        return (column.filter.indexOf(nX) > -1);
      };
    } else {
      column.filterFunc = null;
    }

    // callback to table
    table.filter();
  }
};

scout.TableHeaderMenu.prototype.remove = function() {
  this.$headerMenu.remove();
  this.$header.removeClass('menu-open');
  $('body').off('mousedown.remove');
  $('body').off('keydown.remove');
};

scout.TableHeaderMenu.prototype.isOpenFor = function($header) {
  return this.$header.is($header) && this.$header.hasClass('menu-open');
};

scout.TableHeaderMenu.prototype.isOpen = function() {
  return this.$header.hasClass('menu-open');
};
