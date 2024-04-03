/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  aria, Event, EventHandler, fields, FocusFilterFieldKeyStroke, graphics, HtmlComponent, InitModelOf, InputFieldKeyStrokeContext, MenuBarLayout, PropertyChangeEvent, scout, SomeRequired, Status, strings, Table, TableControl,
  TableFilterAddedEvent, TableFilterRemovedEvent, TableFooterLayout, TableFooterModel, TableMaxResultsHelper, TableRowsInsertedEvent, TableRowsSelectedEvent, TableTextUserFilter, TableUserFilter, Tooltip, Widget
} from '../index';
import $ from 'jquery';

export class TableFooter extends Widget implements TableFooterModel {
  declare model: TableFooterModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'table'>;

  table: Table;
  filterText: string;

  animating: boolean;
  open: boolean;
  resizing: boolean;
  selectedControl: TableControl;
  searchFieldKeyStrokeContext: InputFieldKeyStrokeContext;
  $controlContent: JQuery;
  $controlContainer: JQuery;
  $resizer: JQuery;
  $clearIcon: JQuery<HTMLSpanElement>;

  /** @internal */
  _$controls: JQuery;
  /** @internal */
  _$info: JQuery;
  /** @internal */
  _compactStyle: boolean;
  /** @internal */
  _tableInfoTooltip: Tooltip;
  /** @internal */
  _tableStatusTooltip: Tooltip;
  /** @internal */
  _$infoLoad: JQuery;
  /** @internal */
  _$infoSelection: JQuery;

  protected _tableRowsChangedHandler: EventHandler<TableRowsInsertedEvent>;
  protected _tableFilterHandler: EventHandler<Event<Table>>;
  protected _tableFilterAddedHandler: EventHandler<TableFilterAddedEvent>;
  protected _tableFilterRemovedHandler: EventHandler<TableFilterRemovedEvent>;
  protected _tableRowsSelectedHandler: EventHandler<TableRowsSelectedEvent>;
  protected _tableStatusChangedHandler: EventHandler<Event<Table>>;
  protected _tablePropertyChangeHandler: EventHandler<PropertyChangeEvent<any, Table>>;
  protected _focusFilterFieldKeyStroke: FocusFilterFieldKeyStroke;
  protected _autoHideTableStatusTooltipTimeoutId: number;
  protected _$window: JQuery<Window>;
  protected _$body: JQuery<Body>;
  protected _$infoFilter: JQuery;
  protected _$infoTableStatus: JQuery;
  protected _$infoTableStatusIcon: JQuery;
  protected _$textFilter: JQuery<HTMLInputElement>;

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

  protected override _init(options: InitModelOf<this>) {
    super._init(options);

    // Keystroke context for the search field.
    this.searchFieldKeyStrokeContext = new InputFieldKeyStrokeContext();
    this.searchFieldKeyStrokeContext.$bindTarget = () => this._$textFilter;
    this.searchFieldKeyStrokeContext.$scopeTarget = () => this._$textFilter;
    this._focusFilterFieldKeyStroke = null;
  }

  protected override _render() {
    let filter, $filter;

    this.$container = this.$parent.appendDiv('table-footer');
    this._$window = this.$parent.window();
    this._$body = this.$parent.body();

    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new TableFooterLayout(this));

    // --- container for an open control ---
    this.$controlContainer = this.$container.appendDiv('table-control-container').hide();
    this.$controlContent = this.$controlContainer.appendDiv('table-control-content focus-boundary');

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

    aria.role(this._$infoLoad, 'status');

    // filter info ("X rows filtered by Y, click to remove filter")
    this._$infoFilter = this._$info
      .appendDiv('table-info-item table-info-filter')
      .on('click', '', this._onInfoFilterClick.bind(this));

    aria.role(this._$infoFilter, 'status');

    // selection info ("X rows selected, click to select all/none")
    this._$infoSelection = this._$info
      .appendDiv('table-info-item table-info-selection')
      .on('click', '', this._onInfoSelectionClick.bind(this));

    aria.role(this._$infoSelection, 'status');

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

  override getFocusableElement(): JQuery {
    return this._$textFilter;
  }

  protected override _renderProperties() {
    this._updateHasFilterText();
  }

  protected override _remove() {
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

  protected _renderResizerVisible() {
    if (this.selectedControl.resizerVisible) {
      this._renderResizer();
      this.$controlContainer.addClass('has-resizer');
    } else if (this.$resizer) {
      this.$resizer.remove();
      this.$resizer = null;
      this.$controlContainer.removeClass('has-resizer');
    }
  }

  protected _renderResizer() {
    if (this.$resizer) {
      return;
    }
    this.$resizer = this.$controlContainer.prependDiv('table-control-resize')
      .on('mousedown', '', resize.bind(this));

    function resize(event: JQuery.MouseDownEvent) {
      // Remember current height and start position
      let startHeight: number = this.$controlContainer.height(),
        startX = Math.floor(event.pageY);
      this._$window
        .on('mousemove.tablefooter', resizeMove.bind(this))
        .one('mouseup', resizeEnd.bind(this));
      this._$body.addClass('row-resize');
      this.resizing = true;

      function resizeMove(event: JQuery.MouseMoveEvent) {
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

  /** @internal */
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

  /** @internal */
  _renderInfo() {
    this._renderInfoLoad();
    this._renderInfoTableStatus();
    this._renderInfoFilter();
    this._renderInfoSelection();
  }

  protected _renderInfoLoad() {
    let $info = this._$infoLoad,
      numRows = this.table.rows.length,
      estRows = this.table.estimatedRowCount,
      maxRows = this.table.maxRowCount;

    $info.empty();
    let $infoButton;
    if (!this._compactStyle) {
      if (numRows <= 1) {
        $info.appendSpan().text(this.session.text('ui.NumRowLoaded', this.computeCountInfo(numRows)));
      } else if (estRows && estRows > numRows) {
        $info.appendSpan().text(this.session.text('ui.NumRowsLoaded', this.computeCountInfo(numRows, estRows)));
      } else {
        $info.appendSpan().text(this.session.text('ui.NumRowsLoaded', this.computeCountInfo(numRows)));
      }
      if (this.table.hasReloadHandler) {
        if (scout.create(TableMaxResultsHelper).isLoadMoreDataPossible(numRows, estRows, maxRows)) {
          if (estRows < maxRows) {
            $infoButton = $info.appendSpan('table-info-button').text(this.session.text('ui.LoadAllData')).appendTo($info);
          } else {
            $infoButton = $info.appendSpan('table-info-button').text(this.session.text('ui.LoadNData', this.computeCountInfo(maxRows))).appendTo($info);
          }
        } else {
          $infoButton = $info.appendSpan('table-info-button').text(this.session.text('ui.ReloadData')).appendTo($info);
        }
      }
    } else {
      if (numRows <= 1) {
        $info.appendSpan().text(this.session.text('ui.NumRowLoadedMin'));
      } else {
        $info.appendSpan().text(this.session.text('ui.NumRowsLoadedMin'));
      }
      $infoButton = $info.appendSpan('table-info-button').text(this.computeCountInfo(numRows));
    }
    $info.setEnabled(this.table.hasReloadHandler);

    // hide info button from screen reader, screen reader users use shortcuts
    if ($infoButton) {
      aria.hidden($infoButton, true);
    }

    if (!this.htmlComp.layouting) {
      this.invalidateLayoutTree(false);
    }
  }

  protected _renderInfoFilter() {
    let $info = this._$infoFilter;
    let numRowsFiltered = this.table.filteredRows().length;
    let filteredBy = this.table.filteredBy().join(', '); // filteredBy() returns an array

    $info.empty();
    let $infoButton;
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
        $infoButton = $info.appendSpan('table-info-button').text(this.session.text('ui.RemoveFilter')).appendTo($info);
      }
    } else {
      if (numRowsFiltered <= 1) {
        $info.appendSpan().text(this.session.text('ui.NumRowFilteredMin'));
      } else {
        $info.appendSpan().text(this.session.text('ui.NumRowsFilteredMin'));
      }
      $infoButton = $info.appendSpan('table-info-button').text(this.computeCountInfo(numRowsFiltered));
    }

    // hide info button from screen reader, screen reader users use shortcuts
    if ($infoButton) {
      aria.hidden($infoButton, true);
    }

    if (!this.htmlComp.layouting) {
      this.invalidateLayoutTree(false);
    }
  }

  protected _renderInfoSelection() {
    let $info = this._$infoSelection,
      numRows = this.table.filteredRows().length,
      numRowsSelected = this.table.selectedRows.length,
      all = numRows > 0 && numRows === numRowsSelected;

    $info.empty();
    let $infoButton;
    if (!this._compactStyle) {
      if (numRowsSelected <= 1) {
        $info.appendSpan().text(this.session.text('ui.NumRowSelected', this.computeCountInfo(numRowsSelected)));
      } else {
        $info.appendSpan().text(this.session.text('ui.NumRowsSelected', this.computeCountInfo(numRowsSelected)));
      }
      $infoButton = $info.appendSpan('table-info-button').text(this.session.text(all ? 'ui.SelectNone' : 'ui.SelectAll')).appendTo($info);
    } else {
      if (numRowsSelected <= 1) {
        $info.appendSpan().text(this.session.text('ui.NumRowSelectedMin'));
      } else {
        $info.appendSpan().text(this.session.text('ui.NumRowsSelectedMin'));
      }
      $infoButton = $info.appendSpan('table-info-button').text(this.computeCountInfo(numRowsSelected));
    }

    // hide info button from screen reader, screen reader users use shortcuts
    if ($infoButton) {
      aria.hidden($infoButton, true);
    }

    if (!this.htmlComp.layouting) {
      this.invalidateLayoutTree(false);
    }
  }

  protected _renderInfoTableStatus() {
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

  protected _updateInfoVisibility() {
    this._updateInfoFilterVisibility();
    this._updateInfoSelectionVisibility();
    this._updateInfoTableStatusVisibility();
  }

  protected _updateInfoFilterVisibility() {
    let visible = this.table.filteredBy().length > 0;
    this._setInfoVisible(this._$infoFilter, visible);
  }

  protected _updateInfoSelectionVisibility() {
    let visible = this.table.multiSelect;
    this._setInfoVisible(this._$infoSelection, visible);
  }

  protected _updateInfoTableStatusVisibility() {
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

  protected _setInfoVisible($info: JQuery, visible: boolean, complete?: () => void) {
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
      $info.stop().removeClass('hiding').setVisible(true).cssWidthToContentAnimated(animationOpts);
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

  protected _toggleTableInfoTooltip($info: JQuery, tooltipType: string) {
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

  /**
   * Meaning is '3 of 10 rows'
   * @param n row count
   * @param m total count, optional.
   */
  computeCountInfo(n: number, m?: number): string {
    n = scout.nvl(n, 0);
    if (m) {
      return this.session.text('ui.TableRowCount',
        this.session.text('ui.CountOfApproxTotal',
          this.session.locale.decimalFormat.format(n),
          this.session.locale.decimalFormat.format(m)));
    }

    if (n === 0) {
      if (this._compactStyle) {
        return this.session.text('ui.TableRowCount', this.session.locale.decimalFormat.format(0));
      }
      return this.session.text('ui.TableRowCount0');
    } else if (n === 1) {
      return this.session.text('ui.TableRowCount1');
    }
    return this.session.text('ui.TableRowCount', this.session.locale.decimalFormat.format(n));
  }

  /* open, close and resize of the container */

  protected _revalidateTableLayout() {
    this.table.htmlComp.revalidateLayoutTree();
  }

  openControlContainer(control: TableControl) {
    if (this.open) {
      // Calling open again may resize the container -> don't return
    }
    let currentControl = this.$controlContent.data('control') as TableControl;
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

  closeControlContainer(control: TableControl, options?: { animate?: boolean }) {
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

  computeControlContainerHeight(table: Table, control: TableControl, growControl?: boolean): number {
    let menuBarHeight = 0,
      footerHeight = 0,
      containerHeight = graphics.size(table.$container).height,
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
      let maxControlHeight = containerHeight - (dataMarginsHeight + menuBarHeight + footerHeight);
      controlContainerHeight = Math.min(controlContainerHeight, maxControlHeight);
    }
    return controlContainerHeight;
  }

  protected _hideTableStatusTooltip() {
    clearTimeout(this._autoHideTableStatusTooltipTimeoutId);
    if (this._tableStatusTooltip) {
      this._tableStatusTooltip.destroy();
    }
  }

  protected _showTableStatusTooltip() {
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
    this._tableStatusTooltip = scout.create(Tooltip, {
      parent: this,
      text: text,
      severity: tableStatus.severity,
      autoRemove: !tableStatus.isError(),
      $anchor: this._$infoTableStatusIcon
    });
    this._tableStatusTooltip.one('destroy', () => {
      this._tableStatusTooltip = null;
    });
    this._tableStatusTooltip.render();
    aria.role(this._tableStatusTooltip.$content, 'alert');

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
      this._autoHideTableStatusTooltipTimeoutId = setTimeout(() => this._hideTableStatusTooltip(), 5000);
    }
  }

  protected _updateHasFilterText() {
    this._$textFilter.toggleClass('has-text', !!this._$textFilter.val());
  }

  onControlSelected(control: TableControl) {
    let previousControl = this.selectedControl;
    this.selectedControl = control;

    if (control) {
      this._renderResizerVisible();
      if (previousControl && previousControl.height !== control.height) {
        this.openControlContainer(control);
      }
    }
  }

  protected _onStatusMouseDown(event: JQuery.MouseDownEvent) {
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

  protected _createOnFilterFieldInputFunction(): (event: JQuery.TriggeredEvent) => void {
    let debounceFunction = $.debounce(this._applyFilter.bind(this));
    return function(event) {
      this._updateHasFilterText();
      // debounced filter
      debounceFunction();
    };
  }

  protected _onDeleteFilterMouseDown(event: JQuery.MouseDownEvent) {
    this._$textFilter.val('');
    this._updateHasFilterText();
    this._applyFilter();
    event.preventDefault();
  }

  protected _applyFilter() {
    let filterText = this._$textFilter.val() as string;
    if (this.filterText !== filterText) {
      this.filterText = filterText;
      if (filterText) {
        let filter = scout.create(TableTextUserFilter, {
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

  protected _onInfoLoadClick() {
    if (!this._$infoLoad.isEnabled()) {
      return;
    }
    if (this._compactStyle) {
      this._toggleTableInfoTooltip(this._$infoLoad, 'TableInfoLoadTooltip');
    } else {
      let numRows = this.table.rows.length;
      let estRows = this.table.estimatedRowCount;
      let maxRows = this.table.maxRowCount;
      if (scout.create(TableMaxResultsHelper).isLoadMoreDataPossible(numRows, estRows, maxRows)) {
        this.table.reload(Table.ReloadReason.OVERRIDE_ROW_LIMIT);
      } else {
        this.table.reload();
      }
    }
  }

  protected _onInfoFilterClick() {
    if (this._compactStyle) {
      this._toggleTableInfoTooltip(this._$infoFilter, 'TableInfoFilterTooltip');
    } else {
      this.table.resetUserFilter();
    }
  }

  protected _onInfoSelectionClick() {
    if (this._compactStyle) {
      this._toggleTableInfoTooltip(this._$infoSelection, 'TableInfoSelectionTooltip');
    } else {
      this.table.toggleSelection();
    }
  }

  protected _onTableRowsChanged(event: TableRowsInsertedEvent) {
    this._renderInfoLoad();
  }

  protected _onTableFilter(event: Event<Table>) {
    this._renderInfoFilter();
    this._updateInfoFilterVisibility();
    this._renderInfoSelection();
  }

  protected _onTableFilterAdded(event: TableFilterAddedEvent) {
    this._renderInfoFilter();
    this._updateInfoFilterVisibility();
    if (event.filter instanceof TableUserFilter && event.filter.filterType === TableTextUserFilter.TYPE) {
      // Do not update the content when the value does not change. This is the case when typing text in
      // the UI. If we called val() unconditionally, the current cursor position will get lost.
      let textFilter = event.filter as TableTextUserFilter;
      let currentText = this._$textFilter.val();
      if (currentText !== textFilter.text) {
        this._$textFilter.val(textFilter.text);
        this._updateHasFilterText();
        this._applyFilter();
      }
    }
  }

  protected _onTableFilterRemoved(event: TableFilterRemovedEvent) {
    this._renderInfoFilter();
    this._updateInfoFilterVisibility();
    if (event.filter instanceof TableUserFilter && event.filter.filterType === TableTextUserFilter.TYPE) {
      this._$textFilter.val('');
      this._updateHasFilterText();
      this._applyFilter();
    }
  }

  protected _onTableRowsSelected(event: TableRowsSelectedEvent) {
    this._renderInfoSelection();
  }

  protected _onTableStatusChanged(event: Event<Table>) {
    this._renderInfoTableStatus();
    this._updateInfoTableStatusVisibility();
  }

  protected _onTablePropertyChange(event: PropertyChangeEvent<any, Table>) {
    if (event.propertyName === 'multiSelect') {
      this._updateInfoSelectionVisibility();
    }
  }
}
