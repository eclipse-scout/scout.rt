/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {fields, graphics, HtmlComponent, InputFieldKeyStrokeContext, MenuBarLayout, scout, Status, strings, Table, TableFooterLayout, TableTextUserFilter, Widget} from '../index';
import $ from 'jquery';
import FocusFilterFieldKeyStroke from '../keystroke/FocusFilterFieldKeyStroke';

export default class TableFooter extends Widget {

  constructor() {
    super();

    this._compactStyle = false;
    this.animating = false;
    this.open = false;
    this.resizing = false;
    this.table = null;
    this._tableRowsChangedHandler = this._onTableRowsChanged.bind(this);
    this._tableFilterHandler = this._onTableFilter.bind(this);
    this._tableFilterAddedHandler = this._onTableFilterAdded.bind(this);
    this._tableFilterRemovedHandler = this._onTableFilterRemoved.bind(this);
    this._tableRowsSelectedHandler = this._onTableRowsSelected.bind(this);
    this._tableStatusChangedHandler = this._onTableStatusChanged.bind(this);
    this._tablePropertyChangeHandler = this._onTablePropertyChange.bind(this);
  }

  _init(options) {
    super._init(options);

    // Keystroke context for the search field.
    this.searchFieldKeyStrokeContext = new InputFieldKeyStrokeContext();
    this.searchFieldKeyStrokeContext.$bindTarget = function() {
      return this._$textFilter;
    }.bind(this);
    this.searchFieldKeyStrokeContext.$scopeTarget = function() {
      return this._$textFilter;
    }.bind(this);

    this._focusFilterFieldKeyStroke = null;
  }

  _render() {
    let filter, $filter;

    this.$container = this.$parent.appendDiv('table-footer');
    this._$window = this.$parent.window();
    this._$body = this.$parent.body();

    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new TableFooterLayout(this));

    // --- container for an open control ---
    this.$controlContainer = this.$container.appendDiv('table-control-container').hide();
    this.$controlContent = this.$controlContainer.appendDiv('table-control-content');

    // --- table controls section ---
    this._$controls = this.$container.appendDiv('table-controls');

    // --- info section ---
    this._$info = this.$container.appendDiv('table-info');

    // text filter
    $filter = this._$info.appendDiv('table-filter');
    this._$textFilter = fields.makeTextField(this.$container, 'table-text-filter')
      .appendTo($filter)
      .on('input', '', this._createOnFilterFieldInputFunction().bind(this))
      .placeholder(this.session.text('ui.FilterBy_'));

    this.table.$container.data('filter-field', this._$textFilter);
    this._focusFilterFieldKeyStroke = new FocusFilterFieldKeyStroke(this.table);
    this.table.keyStrokeContext.registerKeyStroke(this._focusFilterFieldKeyStroke);

    filter = this.table.getFilter(TableTextUserFilter.TYPE);
    if (filter) {
      this._$textFilter.val(filter.text);
    }
    this.$clearIcon = $filter.appendSpan('clear-icon unfocusable action text-field-icon')
      .on('mousedown', this._onDeleteFilterMouseDown.bind(this));

    // load info ("X rows loaded, click to reload")
    this._$infoLoad = this._$info
      .appendDiv('table-info-item table-info-load')
      .on('click', '', this._onInfoLoadClick.bind(this));

    // filter info ("X rows filtered by Y, click to remove filter")
    this._$infoFilter = this._$info
      .appendDiv('table-info-item table-info-filter')
      .on('click', '', this._onInfoFilterClick.bind(this));

    // selection info ("X rows selected, click to select all/none")
    this._$infoSelection = this._$info
      .appendDiv('table-info-item table-info-selection')
      .on('click', '', this._onInfoSelectionClick.bind(this));

    // table status
    this._$infoTableStatus = this._$info
      .appendDiv('table-info-item table-info-status')
      .on('mousedown', this._onStatusMouseDown.bind(this));
    this._$infoTableStatusIcon = this._$infoTableStatus
      .appendSpan('icon font-icon');

    // ------

    this._renderControls();
    this._renderInfo();
    this._updateInfoVisibility();

    this.table.on('rowsInserted', this._tableRowsChangedHandler);
    this.table.on('rowsDeleted', this._tableRowsChangedHandler);
    this.table.on('allRowsDeleted', this._tableRowsChangedHandler);
    this.table.on('filter', this._tableFilterHandler);
    this.table.on('filterAdded', this._tableFilterAddedHandler);
    this.table.on('filterRemoved', this._tableFilterRemovedHandler);
    this.table.on('rowsSelected', this._tableRowsSelectedHandler);
    this.table.on('statusChanged', this._tableStatusChangedHandler);
    this.table.on('propertyChange', this._tablePropertyChangeHandler);

    this.session.keyStrokeManager.installKeyStrokeContext(this.searchFieldKeyStrokeContext);
  }

  getFocusableElement() {
    return this._$textFilter;
  }

  _renderProperties() {
    this._updateHasFilterText();
  }

  _remove() {
    this.session.keyStrokeManager.uninstallKeyStrokeContext(this.searchFieldKeyStrokeContext);
    this._hideTableStatusTooltip();
    this.$resizer = null;
    this.$controlContainer.stop(false, true);
    this.animating = false; // Animation may not be started yet due to the delay, hence complete callback may not be executed -> make sure the flag is reset anyway
    this.open = false;

    this.table.keyStrokeContext.unregisterKeyStroke(this._focusFilterFieldKeyStroke);
    this._focusFilterFieldKeyStroke = null;

    this.table.off('rowsInserted', this._tableRowsChangedHandler);
    this.table.off('rowsDeleted', this._tableRowsChangedHandler);
    this.table.off('allRowsDeleted', this._tableRowsChangedHandler);
    this.table.off('filter', this._tableFilterHandler);
    this.table.off('filterAdded', this._tableFilterAddedHandler);
    this.table.off('filterRemoved', this._tableFilterRemovedHandler);
    this.table.off('rowsSelected', this._tableRowsSelectedHandler);
    this.table.off('statusChanged', this._tableStatusChangedHandler);
    this.table.off('propertyChange', this._tablePropertyChangeHandler);

    super._remove();
  }

  _renderResizerVisible() {
    if (this.selectedControl.resizerVisible) {
      this._renderResizer();
      this.$controlContainer.addClass('has-resizer');
    } else if (this.$resizer) {
      this.$resizer.remove();
      this.$resizer = null;
      this.$controlContainer.removeClass('has-resizer');
    }
  }

  _renderResizer() {
    if (this.$resizer) {
      return;
    }
    this.$resizer = this.$controlContainer.prependDiv('table-control-resize')
      .on('mousedown', '', resize.bind(this));

    function resize(event) {
      // Remember current height and start position
      let startHeight = this.$controlContainer.height(),
        startX = Math.floor(event.pageY);
      this._$window
        .on('mousemove.tablefooter', resizeMove.bind(this))
        .one('mouseup', resizeEnd.bind(this));
      this._$body.addClass('row-resize');
      this.resizing = true;

      function resizeMove(event) {
        if (!this.rendered) {
          // footer may be removed in the meantime
          return;
        }
        // Calculate position delta
        let x = Math.floor(event.pageY);
        let dx = x - startX;
        // Ensure control container does not get bigger than the table
        let maxHeight = this.table.$container.height() - this.table.footer.$container.height();
        // Calculate new height of table control container
        let newHeight = Math.min(startHeight - dx, maxHeight);
        this.$controlContainer.height(newHeight);
        let controlContainerInsets = graphics.insets(this.$controlContainer);
        this.$controlContent.outerHeight(newHeight - controlContainerInsets.vertical());
        this._revalidateTableLayout();
      }

      function resizeEnd() {
        if (this.selectedControl && this.rendered && this.$controlContainer.height() < 100) {
          this.selectedControl.setSelected(false);
        }

        this._$window.off('mousemove.tablefooter');
        this._$body.removeClass('row-resize');
        this.resizing = false;
      }

      return false;
    }
  }

  _renderControls() {
    let controls = this.table.tableControls;
    if (controls) {
      controls.forEach(control => {
        control.setParent(this);
        control.render(this._$controls);
      });
    } else {
      this._$controls.empty();
    }
  }

  _renderInfo() {
    this._renderInfoLoad();
    this._renderInfoTableStatus();
    this._renderInfoFilter();
    this._renderInfoSelection();
  }

  _renderInfoLoad() {
    let $info = this._$infoLoad,
      numRows = this.table.rows.length,
      estRows = this.table.estimatedRowCount,
      maxRows = this.table.maxRowCount;

    $info.empty();
    if (!this._compactStyle) {
      if (numRows <= 1) {
        $info.appendSpan().text(this.session.text('ui.NumRowLoaded', this.computeCountInfo(numRows)));
      } else if (estRows && estRows > numRows) {
        $info.appendSpan().text(this.session.text('ui.NumRowsLoaded', this.computeCountInfo(numRows, estRows)));
      } else {
        $info.appendSpan().text(this.session.text('ui.NumRowsLoaded', this.computeCountInfo(numRows)));
      }
      if (this.table.hasReloadHandler) {
        if (estRows && maxRows && numRows < estRows && numRows < maxRows) {
          if (estRows < maxRows) {
            $info.appendSpan('table-info-button').text(this.session.text('ui.LoadAllData')).appendTo($info);
          } else {
            $info.appendSpan('table-info-button').text(this.session.text('ui.LoadNData', this.computeCountInfo(maxRows))).appendTo($info);
          }
        } else {
          $info.appendSpan('table-info-button').text(this.session.text('ui.ReloadData')).appendTo($info);
        }
      }
    } else {
      if (numRows <= 1) {
        $info.appendSpan().text(this.session.text('ui.NumRowLoadedMin'));
      } else {
        $info.appendSpan().text(this.session.text('ui.NumRowsLoadedMin'));
      }
      $info.appendSpan('table-info-button').text(this.computeCountInfo(numRows));
    }
    $info.setEnabled(this.table.hasReloadHandler);

    if (!this.htmlComp.layouting) {
      this.invalidateLayoutTree(false);
    }
  }

  _renderInfoFilter() {
    let $info = this._$infoFilter;
    let numRowsFiltered = this.table.filteredRows().length;
    let filteredBy = this.table.filteredBy().join(', '); // filteredBy() returns an array

    $info.empty();
    if (!this._compactStyle) {
      if (filteredBy) {
        if (numRowsFiltered <= 1) {
          $info.appendSpan().text(this.session.text('ui.NumRowFilteredBy', this.computeCountInfo(numRowsFiltered), filteredBy));
        } else {
          $info.appendSpan().text(this.session.text('ui.NumRowsFilteredBy', this.computeCountInfo(numRowsFiltered), filteredBy));
        }
      } else {
        if (numRowsFiltered <= 1) {
          $info.appendSpan().text(this.session.text('ui.NumRowFiltered', this.computeCountInfo(numRowsFiltered)));
        } else {
          $info.appendSpan().text(this.session.text('ui.NumRowsFiltered', this.computeCountInfo(numRowsFiltered)));
        }
      }
      if (this.table.hasUserFilter()) {
        $info.appendSpan('table-info-button').text(this.session.text('ui.RemoveFilter')).appendTo($info);
      }
    } else {
      if (numRowsFiltered <= 1) {
        $info.appendSpan().text(this.session.text('ui.NumRowFilteredMin'));
      } else {
        $info.appendSpan().text(this.session.text('ui.NumRowsFilteredMin'));
      }
      $info.appendSpan('table-info-button').text(this.computeCountInfo(numRowsFiltered));
    }

    if (!this.htmlComp.layouting) {
      this.invalidateLayoutTree(false);
    }
  }

  _renderInfoSelection() {
    let $info = this._$infoSelection,
      numRows = this.table.filteredRows().length,
      numRowsSelected = this.table.selectedRows.length,
      all = numRows > 0 && numRows === numRowsSelected;

    $info.empty();
    if (!this._compactStyle) {
      if (numRowsSelected <= 1) {
        $info.appendSpan().text(this.session.text('ui.NumRowSelected', this.computeCountInfo(numRowsSelected)));
      } else {
        $info.appendSpan().text(this.session.text('ui.NumRowsSelected', this.computeCountInfo(numRowsSelected)));
      }
      $info.appendSpan('table-info-button').text(this.session.text(all ? 'ui.SelectNone' : 'ui.SelectAll')).appendTo($info);
    } else {
      if (numRowsSelected <= 1) {
        $info.appendSpan().text(this.session.text('ui.NumRowSelectedMin'));
      } else {
        $info.appendSpan().text(this.session.text('ui.NumRowsSelectedMin'));
      }
      $info.appendSpan('table-info-button').text(this.computeCountInfo(numRowsSelected));
    }

    if (!this.htmlComp.layouting) {
      this.invalidateLayoutTree(false);
    }
  }

  _renderInfoTableStatus() {
    let $info = this._$infoTableStatus;
    let tableStatus = this.table.tableStatus;
    $info.removeClass(Status.SEVERITY_CSS_CLASSES);
    if (tableStatus) {
      $info.addClass(tableStatus.cssClass());
    }

    if (!this.htmlComp.layouting) {
      this.invalidateLayoutTree(false);
    }
  }

  _updateInfoVisibility() {
    this._updateInfoFilterVisibility();
    this._updateInfoSelectionVisibility();
    this._updateInfoTableStatusVisibility();
  }

  _updateInfoFilterVisibility() {
    let visible = this.table.filteredBy().length > 0;
    this._setInfoVisible(this._$infoFilter, visible);
  }

  _updateInfoSelectionVisibility() {
    let visible = this.table.multiSelect;
    this._setInfoVisible(this._$infoSelection, visible);
  }

  _updateInfoTableStatusVisibility() {
    let visible = this.table.tableStatus;
    if (visible) {
      // If the uiState of the tableStatus was not set to hidden (either manually by the
      // user or automatically by a timeout or other event), show the tooltip when the
      // "info visible" animation has finished. Otherwise, we don't show the tooltip to
      // not disturb the user.
      let complete = null;
      if (!scout.isOneOf(this.table.tableStatus.uiState, 'user-hidden', 'auto-hidden')) {
        this._$infoTableStatus.addClass('tooltip-active'); // color icon before animation starts
        complete = function() {
          // Same check is required again, because this function is called asynchronously
          if (this.table.tableStatus && !scout.isOneOf(this.table.tableStatus.uiState, 'user-hidden', 'auto-hidden')) {
            this._showTableStatusTooltip();
          }
        }.bind(this);
      }
      this._setInfoVisible(this._$infoTableStatus, true, complete);
    } else {
      this._hideTableStatusTooltip();
      this._setInfoVisible(this._$infoTableStatus, false);
    }
  }

  _setInfoVisible($info, visible, complete) {
    if ($info.isVisible() === visible && !(visible && $info.hasClass('hiding'))) {
      if (complete) {
        complete();
      }
      return;
    }
    let animate = this.rendered; // Animate only on a user interaction, no while the table gets rendered
    if (!animate) {
      $info.setVisible(visible);
      return;
    }
    if (visible) {
      let animationOpts = {
        progress: this.revalidateLayout.bind(this),
        complete: () => {
          if (complete) {
            complete();
          }
        }
      };
      // Save complete function so that layout may use it
      $info.data('animationComplete', animationOpts.complete);
      // If info is shown the first time, set the width to 0 to make animation work
      if ($info[0].style.width === '') {
        $info.cssWidth(0);
      }
      $info.stop().removeClass('hiding').setVisible(true).widthToContent(animationOpts);
    } else {
      // Mark element as hiding so that the layout does not try to resize it
      $info.addClass('hiding');
      $info.stop().animate({
        width: 0
      }, {
        progress: this.revalidateLayout.bind(this),
        complete: () => {
          $info.removeClass('hiding');
          $info.setVisible(false);
        }
      });
    }
  }

  _toggleTableInfoTooltip($info, tooltipType) {
    if (this._tableInfoTooltip) {
      this._tableInfoTooltip.destroy();
    } else {
      this._tableInfoTooltip = scout.create(tooltipType, {
        parent: this,
        tableFooter: this,
        arrowPosition: 50,
        arrowPositionUnit: '%',
        $anchor: $info
      });
      this._tableInfoTooltip.one('destroy', () => {
        this._tableInfoTooltip = null;
      });
      this._tableInfoTooltip.render();
    }
  }

  // n: row count
  // m: total count, optional. Meaning is '3 of 10 rows'
  computeCountInfo(n, m) {
    n = scout.nvl(n, 0);
    if (m) {
      return this.session.text('ui.TableRowCount',
        this.session.text('ui.CountOfApproxTotal',
          this.session.locale.decimalFormat.format(n),
          this.session.locale.decimalFormat.format(m)));
    }

    if (n === 0) {
      if (this._compactStyle) {
        return this.session.text('ui.TableRowCount', 0);
      }
      return this.session.text('ui.TableRowCount0');

    } else if (n === 1) {
      return this.session.text('ui.TableRowCount1');
    }
    return this.session.text('ui.TableRowCount', this.session.locale.decimalFormat.format(n));

  }

  /* open, close and resize of the container */

  _revalidateTableLayout() {
    this.table.htmlComp.revalidateLayoutTree();
  }

  openControlContainer(control) {
    if (this.open) {
      // Calling open again may resize the container -> don't return
    }
    let currentControl = this.$controlContent.data('control');
    if (this.animating && currentControl !== control) {
      // Make sure the existing content is removed if the close animation was aborted and another control selected while the container is still closing
      // (The done callback won't be executed when calling stop(true))
      currentControl.onControlContainerClosed();
    }
    this.animating = true;
    this.open = true;

    let allowedControlHeight = this.computeControlContainerHeight(this.table, control);

    let insets = graphics.insets(this.$controlContainer);
    this.$controlContent.outerHeight(allowedControlHeight - insets.vertical());

    // If container is opened the first time, set the height to 0 to make animation work
    if (this.$controlContainer[0].style.height === '') {
      this.$controlContainer.outerHeight(0);
    }

    if (this.$controlContainer.outerHeight() > allowedControlHeight) {
      // Container gets smaller -> layout first to prevent having a white area
      this.table.invalidateLayoutTree();
    }

    // open container, stop existing (close) animations before
    // use delay to make sure form is rendered and layouted with new size
    this.$controlContainer.stop(true).show().delay(1).animate({
      height: allowedControlHeight
    }, {
      duration: this.rendered ? control.animateDuration : 0,
      complete: function() {
        this.animating = false;
        control.onControlContainerOpened();
        this.table.invalidateLayoutTree();
      }.bind(this)
    });
  }

  closeControlContainer(control, options) {
    if (!this.open) {
      return;
    }
    options = $.extend({}, {animate: true}, options);
    this.open = false;
    this.animating = true;
    this.table.invalidateLayoutTree();

    let completeFunc = function() {
      this.animating = false;
      this.$controlContainer.hide();
      control.onControlContainerClosed();
    }.bind(this);

    if (options.animate) {
      this.$controlContainer.stop(true).show().animate({
        height: 0
      }, {
        duration: control.animateDuration,
        complete: completeFunc
      });
    } else {
      completeFunc();
    }
  }

  computeControlContainerHeight(table, control, growControl) {
    let menuBarHeight = 0,
      footerHeight = 0,
      containerHeight = graphics.size(table.$container).height,
      maxControlHeight,
      controlContainerHeight = 0,
      dataMargins = graphics.margins(scout.nvl(table.$data, table.$container)),
      dataMarginsHeight = dataMargins.top + dataMargins.bottom,
      menuBar = table.menuBar,
      footer = table.footer,
      htmlContainer = table.htmlComp,
      containerSize = htmlContainer.availableSize()
        .subtract(htmlContainer.insets());

    if (!footer) {
      return;
    }

    if (table.menuBarVisible && menuBar.visible) {
      let htmlMenuBar = HtmlComponent.get(menuBar.$container);
      menuBarHeight = MenuBarLayout.size(htmlMenuBar, containerSize).height;
    }
    // Layout table footer and add size of footer (including the control content) to 'height'
    footerHeight = graphics.size(footer.$container).height;
    if (footer.open) {
      if (footer.animating) {
        // Layout may be called when container stays open but changes its size using an animation.
        // At that time the controlContainer has not yet the final size, therefore measuring is not possible, but not necessary anyway.
        controlContainerHeight = scout.nvl(control && control.height, controlContainerHeight);
      } else {
        // Measure the real height
        controlContainerHeight = graphics.size(footer.$controlContainer).height;
        // Expand control height? (but only if not resizing)
        if (!footer.resizing && growControl) {
          controlContainerHeight = Math.max(control && control.height, controlContainerHeight);
        }
      }
    }
    // Crop control height (don't do it if table does not have the correct size yet)
    if (this.table.htmlComp.layouted) {
      maxControlHeight = containerHeight - (dataMarginsHeight + menuBarHeight + footerHeight);
      controlContainerHeight = Math.min(controlContainerHeight, maxControlHeight);
    }
    return controlContainerHeight;
  }

  _hideTableStatusTooltip() {
    clearTimeout(this._autoHideTableStatusTooltipTimeoutId);
    if (this._tableStatusTooltip) {
      this._tableStatusTooltip.destroy();
    }
  }

  _showTableStatusTooltip() {
    // Remove existing tooltip (might have the wrong css class)
    if (this._tableStatusTooltip) {
      this._tableStatusTooltip.destroy();
      this._tableStatusTooltip = null;
    }

    // Check needed because the table footer might already be removed again when this
    // callback is executed (e.g. when the user clicks on another page while the opening
    // animation is still running).
    if (!this.rendered && !this.rendering) {
      return;
    }

    let tableStatus = this.table.tableStatus;
    let text = (tableStatus ? tableStatus.message : null);
    if (strings.empty(text)) {
      return; // Refuse to show empty tooltip
    }

    // Create new tooltip
    let opts = {
      parent: this,
      text: text,
      severity: tableStatus.severity,
      autoRemove: !tableStatus.isError(),
      $anchor: this._$infoTableStatusIcon
    };
    this._tableStatusTooltip = scout.create('Tooltip', opts);
    this._tableStatusTooltip.one('destroy', () => {
      this._tableStatusTooltip = null;
    });
    this._tableStatusTooltip.render();

    // Adjust icon style
    this._$infoTableStatus.addClass('tooltip-active');
    this._tableStatusTooltip.on('remove', () => {
      this._$infoTableStatus.removeClass('tooltip-active');
      // When the tooltip is removed (e.g. because of the auto-remove timeout, or
      // The user clicked somewhere) set the uiStatus accordingly. Otherwise, it
      // might pop up again when the table layout is revalidated.
      clearTimeout(this._autoHideTableStatusTooltipTimeoutId);
      if (this.table.tableStatus && !this.table.tableStatus.isError()) {
        this.table.tableStatus.uiState = 'auto-hidden';
      }
    });

    // Auto-hide unimportant messages
    clearTimeout(this._autoHideTableStatusTooltipTimeoutId);
    if (!tableStatus.isError() && !tableStatus.isWarning() && !tableStatus.uiState) {
      // Already set status to 'auto-hidden', in case the user changes outline before timeout elapses
      this.table.tableStatus.uiState = 'auto-hidden';
      this._autoHideTableStatusTooltipTimeoutId = setTimeout(() => {
        this._hideTableStatusTooltip();
      }, 5000);
    }
  }

  _updateHasFilterText() {
    this._$textFilter.toggleClass('has-text', !!this._$textFilter.val());
  }

  onControlSelected(control) {
    let previousControl = this.selectedControl;
    this.selectedControl = control;

    if (control) {
      this._renderResizerVisible();
      if (previousControl && previousControl.height !== control.height) {
        this.openControlContainer(control);
      }
    }
  }

  _onStatusMouseDown(event) {
    // Toggle tooltip
    if (this._tableStatusTooltip) {
      this._hideTableStatusTooltip();
      this.table.tableStatus.uiState = 'user-hidden';
    } else {
      this._showTableStatusTooltip();
      if (this._tableStatusTooltip.rendered) {
        this.table.tableStatus.uiState = 'user-shown';
      }
    }
  }

  _createOnFilterFieldInputFunction() {
    let debounceFunction = $.debounce(this._applyFilter.bind(this));
    return function(event) {
      this._updateHasFilterText();
      // debounced filter
      debounceFunction();
    };
  }

  _onDeleteFilterMouseDown(event) {
    this._$textFilter.val('');
    this._updateHasFilterText();
    this._applyFilter();
    event.preventDefault();
  }

  _applyFilter(event) {
    let filter,
      filterText = this._$textFilter.val();
    if (this.filterText !== filterText) {
      this.filterText = filterText;
      if (filterText) {
        filter = scout.create('TableTextUserFilter', {
          session: this.session,
          table: this.table
        });

        filter.text = filterText;
        this.table.addFilter(filter);
      } else {
        this.table.removeFilterByKey(TableTextUserFilter.TYPE);
      }
    }
  }

  _onInfoLoadClick() {
    if (!this._$infoLoad.isEnabled()) {
      return;
    }
    if (this._compactStyle) {
      this._toggleTableInfoTooltip(this._$infoLoad, 'TableInfoLoadTooltip');
    } else {
      let numRows = this.table.rows.length;
      let estRows = this.table.estimatedRowCount;
      let maxRows = this.table.maxRowCount;
      if (estRows && maxRows && numRows < estRows && numRows < maxRows) {
        this.table.reload(Table.ReloadReason.OVERRIDE_ROW_LIMIT);
      } else {
        this.table.reload();
      }
    }
  }

  _onInfoFilterClick() {
    if (this._compactStyle) {
      this._toggleTableInfoTooltip(this._$infoFilter, 'TableInfoFilterTooltip');
    } else {
      this.table.resetUserFilter();
    }
  }

  _onInfoSelectionClick() {
    if (this._compactStyle) {
      this._toggleTableInfoTooltip(this._$infoSelection, 'TableInfoSelectionTooltip');
    } else {
      this.table.toggleSelection();
    }
  }

  _onTableRowsChanged(event) {
    this._renderInfoLoad();
  }

  _onTableFilter(event) {
    this._renderInfoFilter();
    this._updateInfoFilterVisibility();
    this._renderInfoSelection();
  }

  _onTableFilterAdded(event) {
    this._renderInfoFilter();
    this._updateInfoFilterVisibility();
    if (event.filter.filterType === TableTextUserFilter.TYPE) {
      // Do not update the content when the value does not change. This is the case when typing text in
      // the UI. If we would call val() unconditionally, the current cursor position will get lost.
      let currentText = this._$textFilter.val();
      if (currentText !== event.filter.text) {
        this._$textFilter.val(event.filter.text);
        this._updateHasFilterText();
        this._applyFilter();
      }
    }
  }

  _onTableFilterRemoved(event) {
    this._renderInfoFilter();
    this._updateInfoFilterVisibility();
    if (event.filter.filterType === TableTextUserFilter.TYPE) {
      this._$textFilter.val('');
      this._updateHasFilterText();
      this._applyFilter();
    }
  }

  _onTableRowsSelected(event) {
    this._renderInfoSelection();
  }

  _onTableStatusChanged(event) {
    this._renderInfoTableStatus();
    this._updateInfoTableStatusVisibility();
  }

  _onTablePropertyChange(event) {
    if (event.propertyName === 'multiSelect') {
      this._updateInfoSelectionVisibility();
    }
  }
}
