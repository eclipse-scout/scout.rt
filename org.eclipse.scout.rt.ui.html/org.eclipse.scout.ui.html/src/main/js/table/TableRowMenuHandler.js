/**
 * Displays menu buttons to open a context menu and activate row action.
 *
 */
scout.TableRowMenuHandler = function(table) {
  this.table = table;

  var that = this;
  this.table.events.on(scout.Table.GUI_EVENT_ROWS_DRAWN, function(event) {
    that._onRowsDrawn(event.$rows);
  });
  this.table.events.on(scout.Table.GUI_EVENT_ROWS_SELECTED, function(event) {
    that._onRowsSelected(event.$rows);
  });
};

scout.TableRowMenuHandler.prototype._onRowsSelected = function($rows) {
  if (!$rows.length) {
    $('#RowMenu, #RowDrill, #RowMenuContainer').remove();
  }
};

scout.TableRowMenuHandler.prototype._onRowsDrawn = function($rows) {
  if (!$rows) {
    return;
  }

  $rows
    .on('mousedown', '', onMouseDown)
    .on('contextmenu', function(e) {
      e.preventDefault();
    });

  var that = this;

  function onMouseDown(event) {
    $(".table-row").one("mouseup.menuHandler", function(event) {
      onMouseUp(event);
    });

    function onMouseUp(event) {
      $(".table-row").off(".menuHandler");
      showSelectionMenu(event.pageX, event.pageY, event.which);
    }

    function showSelectionMenu(x, y, button) {
      // selection
      var $selectedRows = $('.row-selected'),
        $firstRow = $selectedRows.first();

      // make menu - if not already there
      var $RowDrill = $('#RowDrill');
      if ($RowDrill.length === 0) {
        $RowDrill = that.table.$dataScroll.appendDiv('RowDrill')
          .on('click', '', function() {
            that.table.sendRowAction($firstRow);
          });

        var h1 = $RowDrill.outerHeight();
        $RowDrill.height(0).animateAVCSD('height', h1, null, null, 75);
      }
      var $RowMenu = $('#RowMenu');
      if ($RowMenu.length === 0) {
        $RowMenu = that.table.$dataScroll.appendDiv('RowMenu')
          .on('click', '', clickRowMenu);

        var h2 = $RowMenu.outerHeight();
        $RowMenu.height(0).animateAVCSD('height', h2, null, null, 75);
      }

      // place menu
      // TODO cru: place on top if mouse movement goes up?
      var top = $selectedRows.last().offset().top - that.table.$dataScroll.offset().top + 32,
        left = Math.max(25, Math.min($firstRow.outerWidth() - 164, x - that.table.$dataScroll.offset().left - 13));

      $RowDrill.css('left', left - 16).css('top', top);
      $RowMenu.css('left', left + 16).css('top', top);

      // mouse over effect
      var $showMenu = $selectedRows
        .add($selectedRows.first().prev())
        .add($selectedRows.last().next())
        .add($selectedRows.last().next().next())
        .add($RowDrill)
        .add($RowMenu);

      $showMenu
        .on('mouseenter', '', enterSelection)
        .on('mouseleave', '', leaveSelection);

      if (button === 3) {
        clickRowMenu();
      }

      function enterSelection(event) {
        $RowDrill.animateAVCSD('height', h1, null, null, 75);
        $RowMenu.animateAVCSD('height', h2, null, null, 75);
      }

      function leaveSelection(event) {
        if (!$(event.toElement).is($showMenu) && !$('#RowMenuContainer').length) {
          $RowDrill.animateAVCSD('height', 6, null, null, 75);
          $RowMenu.animateAVCSD('height', 6, null, null, 75);
        }
      }

      function clickRowMenu() {
        if ($('#RowMenuContainer').length) {
          removeMenu();
        }

        var menus = that.table.model.selectionMenus;
        if (menus && menus.length > 0) {
          // create 2 container, animate do not allow overflow
          var $RowMenuContainer = $RowMenu.beforeDiv('RowMenuContainer')
            .css('left', left + 16).css('top', top);

          $showMenu = $showMenu.add($RowMenuContainer);

          // create menu-item and menu-button
          for (var i = 0; i < menus.length; i++) {
            if (menus[i].iconId) {
              $RowMenuContainer.appendDiv('', 'menu-button')
                .attr('id', menus[i].id)
                .attr('data-icon', menus[i].iconId)
                .attr('data-label', menus[i].text)
                .on('click', '', onMenuItemClicked)
                .hover(onHoverIn, onHoverOut);
            } else {
              $RowMenuContainer.appendDiv('', 'menu-item', menus[i].text)
                .attr('id', menus[i].id)
                .on('click', '', onMenuItemClicked);
            }
          }

          // wrap menu-buttons and add one div for label
          $('.menu-button', $RowMenuContainer).wrapAll('<div id="MenuButtons"></div>');
          $('#MenuButtons', $RowMenuContainer).appendDiv('MenuButtonsLabel');
          $RowMenuContainer.append($('#MenuButtons', $RowMenuContainer));

          // animated opening
          var h = $RowMenuContainer.outerHeight();
          $RowMenuContainer.css('height', 0).animateAVCSD('height', h);

          var t = parseInt($RowMenu.css('top'), 0);
          $RowMenu.css('top', t).animateAVCSD('top', t + h - 2);

          // every user action will close menu
          $('*').one('mousedown.rowMenu keydown.rowMenu mousewheel.rowMenu', removeMenu);
        }

        function onHoverIn() {
          $('#MenuButtonsLabel').text($(this).data('label'));
        }

        function onHoverOut() {
          $('#MenuButtonsLabel').text('');
        }

        function onMenuItemClicked() {}

        function removeMenu() {
          $.log('remove')
          var $RowMenuContainer = $('#RowMenuContainer'),
            h = $RowMenuContainer.outerHeight();

          $RowMenuContainer.animateAVCSD('height', 0, $.removeThis);

          var t = parseInt($RowMenu.css('top'), 0);
          $RowMenu.css('top', t).animateAVCSD('top', t - h + 2);
          $('*').off('.rowMenu');
        }
      }
    }
  }
};
