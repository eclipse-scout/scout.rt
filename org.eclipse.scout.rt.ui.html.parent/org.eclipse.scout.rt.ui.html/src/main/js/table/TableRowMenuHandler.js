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
  if (!$rows) {
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
    $('#RowMenu, #RowDrill, #RowMenuContainer').remove();

    $(".table-row").one("mouseup.menuHandler", function(event) {
      onMouseUp(event);
    });

    function onMouseUp(event) {
      $(".table-row").off(".menuHandler");
      showRowMenu(event.pageX, event.pageY, event.which);
    }

    function showRowMenu(x, y, button) {
      var $selectedRows = $('.row-selected'),
        $firstRow = $selectedRows.first();

      if ($selectedRows.length === 0) {
        return;
      }

      // make menu - if not already there
      var $rowDrill = $('#RowDrill');
      if ($rowDrill.length === 0) {
        $rowDrill = that.table.$dataScroll.appendDiv('RowDrill')
          .on('click', '', function() {
            that.table.sendRowAction($firstRow);
          });

        var h1 = $rowDrill.outerHeight();
        $rowDrill.height(0).animateAVCSD('height', h1, null, null, 75);
      }

      var menus;
      if ($selectedRows.length > 1) {
        menus = that.filterMultiSelectionRowMenus(that.table.model.menus);
      } else {
        menus = that.filterSingleSelectionRowMenus(that.table.model.menus);
      }

      var $rowMenu = $('#RowMenu');
      if (!menus || menus.length === 0) {
        if ($rowMenu.length > 0) {
          $rowMenu.remove();
        }
      } else {
        if ($rowMenu.length === 0) {
          $rowMenu = that.table.$dataScroll.appendDiv('RowMenu')
            .on('click', '', clickRowMenu);

          var h2 = $rowMenu.outerHeight();
          $rowMenu.height(0).animateAVCSD('height', h2, null, null, 75);
        }

        $rowMenu.data('menus', menus);
      }

      // place menu
      // TODO cru: place on top if mouse movement goes up?
      var top = $selectedRows.last().offset().top - that.table.$dataScroll.offset().top + 32,
        left = Math.max(25, Math.min($firstRow.outerWidth() - 164, x - that.table.$dataScroll.offset().left - 13));

      $rowDrill.css('left', left - 16).css('top', top);
      $rowMenu.css('left', left + 16).css('top', top);

      // mouse over effect
      var $showMenu = $selectedRows
        .add($selectedRows.first().prev())
        .add($selectedRows.last().next())
        .add($selectedRows.last().next().next())
        .add($rowDrill)
        .add($rowMenu);

      $showMenu
        .on('mouseenter', '', enterSelection)
        .on('mouseleave', '', leaveSelection);

      if (button === 3) {
        clickRowMenu();
      }

      function enterSelection(event) {
        $rowDrill.animateAVCSD('height', h1, null, null, 75);
        $rowMenu.animateAVCSD('height', h2, null, null, 75);
      }

      function leaveSelection(event) {
        if (!$(event.toElement).is($showMenu) && !$('#RowMenuContainer').length) {
          $rowDrill.animateAVCSD('height', 6, null, null, 75);
          $rowMenu.animateAVCSD('height', 6, null, null, 75);
        }
      }

      function clickRowMenu() {
        if ($('#RowMenuContainer').length) {
          removeMenu();
          return;
        }

        var menus = $rowMenu.data('menus');
        if (menus && menus.length > 0) {
          // create 2 container, animate do not allow overflow
          var $rowMenuContainer = $rowMenu.beforeDiv('RowMenuContainer')
            .css('left', left + 16).css('top', top);

          $showMenu = $showMenu.add($rowMenuContainer);

          // create menu-item and menu-button
          for (var i = 0; i < menus.length; i++) {
            if (menus[i].separator) {
              continue;
            }
            if (menus[i].iconId) {
              $rowMenuContainer.appendDiv('', 'menu-button')
                .attr('id', menus[i].id)
                .attr('data-icon', menus[i].iconId)
                .attr('data-label', menus[i].text)
                .on('click', '', onMenuItemClicked)
                .hover(onHoverIn, onHoverOut);
            } else {
              $rowMenuContainer.appendDiv('', 'menu-item', menus[i].text)
                .attr('id', menus[i].id)
                .on('click', '', onMenuItemClicked);
            }

            var menuAdapter = that.table.session.modelAdapterRegistry[menus[i].id];
            menuAdapter.sendAboutToShow();
          }

          // wrap menu-buttons and add one div for label
          $('.menu-button', $rowMenuContainer).wrapAll('<div id="MenuButtons"></div>');
          $('#MenuButtons', $rowMenuContainer).appendDiv('MenuButtonsLabel');
          $rowMenuContainer.append($('#MenuButtons', $rowMenuContainer));

          // animated opening
          var h = $rowMenuContainer.outerHeight();
          $rowMenuContainer.css('height', 0).animateAVCSD('height', h);

          var t = parseInt($rowMenu.css('top'), 0);
          $rowMenu.css('top', t).animateAVCSD('top', t + h - 2);

          // every user action will close menu
          var closingEvents = 'mousedown.rowMenu keydown.rowMenu mousewheel.rowMenu';
          $(document).one(closingEvents, removeMenu);
          $rowMenuContainer.one(closingEvents, $.suppressEvent); // menu is removed in 'click' event, see onMenuItemClicked()

          //FIXME CGU we need to wait for the server calls to be finished before showing the menus.
        }

        function onHoverIn() {
          $('#MenuButtonsLabel').text($(this).data('label'));
        }

        function onHoverOut() {
          $('#MenuButtonsLabel').text('');
        }

        function onMenuItemClicked() {
          removeMenu();
          that.table.session.send('menuAction', $(this).attr('id'));
        }

        function removeMenu(event) {
          var $rowMenuContainer = $('#RowMenuContainer');
          if (!$rowMenuContainer.length) {
            return; // Menu does not exist anymore
          }
          // Animate
          var h = $rowMenuContainer.outerHeight();
          $rowMenuContainer.animateAVCSD('height', 0, $.removeThis);
          var t = parseInt($rowMenu.css('top'), 0);
          $rowMenu.css('top', t).animateAVCSD('top', t - h + 2);
          // Remove all cleanup handlers
          $(document).off('.rowMenu');
        }
      }
    }
  }
};

scout.TableRowMenuHandler.prototype.filterSingleSelectionRowMenus = function(menus) {
  return scout.menus.filter(menus, ['SingleSelection']);
};

scout.TableRowMenuHandler.prototype.filterMultiSelectionRowMenus = function(menus) {
  return scout.menus.filter(menus, ['MultiSelection']);
};
