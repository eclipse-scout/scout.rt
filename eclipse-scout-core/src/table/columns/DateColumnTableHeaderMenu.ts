/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  Action, aria, ColumnUserFilter, ContextMenuPopup, DateColumn, DateColumnUserFilter, DateFormat, DateGroupType, dates, Event, icons, keys, Menu, MenuModel, scout, strings, TableHeaderMenu, TableHeaderMenuButton, TableHeaderMenuGroup,
  TableHeaderMenuModel, TableMatrix
} from '../../index';

export class DateColumnTableHeaderMenu extends TableHeaderMenu implements DateColumnTableHeaderMenuModel {
  declare model: DateColumnTableHeaderMenuModel;
  declare column: DateColumn & DateColumnWithFilterType;
  declare filter: DateColumnUserFilter;

  groupingGroupTypeAction: Action;
  filterGroupTypeAction: Action;
  protected _contextMenu: ContextMenuPopup;

  // ------------------------------------------------------------------
  // Grouping
  // ------------------------------------------------------------------

  protected override _renderGroupingGroup(): TableHeaderMenuGroup {
    let group = super._renderGroupingGroup();

    if (this.column.grouped) {
      // Render an alternative group title in the form "Grouped by [group-type]", where the group type is a clickable link.
      // The user can use this action to change the group type without having to ungroup and re-group the column. Clicking
      // the group button again will remove the grouping.
      let $textWithGroupType = group.$text.afterDiv('table-header-menu-group-text');
      $textWithGroupType.appendSpan().text(this.session.text('ui.GroupingBy') + ' ');

      let groupTypeText = this._formatGroupType(this.column.groupType) || this._formatGroupFormat(this.column.groupFormat);
      this.groupingGroupTypeAction = scout.create(Action, {
        id: 'ChangeGroupTypeAction',
        parent: this,
        cssClass: 'table-header-menu-date-group-type-action app-link',
        text: groupTypeText
      });
      this.groupingGroupTypeAction.render($textWithGroupType);
      aria.label(this.groupingGroupTypeAction.$container, strings.join(' ', this.session.text('ui.GroupingBy'), groupTypeText));
      this.groupingGroupTypeAction.on('action', event => this._onGroupButtonAction(event));

      // Show either the normal $text and the $textWithAction, depending on whether there is a "current" item
      const updateTextVisibility = () => {
        group.$text.setVisible(!!group.currentGroupItem);
        $textWithGroupType.setVisible(!group.currentGroupItem);
      };
      updateTextVisibility();
      group.on('propertyChange:currentGroupItem', event => updateTextVisibility());

      // Because the group type action is hidden when one of the buttons is focused, the normal backward
      // tab traversal would skip it. Therefore, we intercept the shift-tab keystroke and make the action
      // visible first, before manually focusing it.
      group.$container.on('keydown', event => {
        if (event.which === keys.TAB && event.shiftKey && !this.groupingGroupTypeAction.get$Focusable().is(event.target)) {
          event.preventDefault();
          group.setCurrentGroupItem(null);
          this.groupingGroupTypeAction.focus();
        }
      });
    }

    // Because we want to open a context menu popup when the column is not yet grouped, we have to disable
    // the default toggle action -> handle in _onGroupButtonAction().
    group.children.forEach(child => {
      if (child instanceof TableHeaderMenuButton) {
        child.setToggleAction(false);
        child.addCssClass('togglable'); // needed for remove icon
      }
    });

    return group;
  }

  protected override _onGroupButtonAction(event: Event<TableHeaderMenuButton> | Event<Action>) {
    // This handler is either called from a TableHeaderMenuButton or the "change group type" action created in _renderGroupingGroup()
    const anchor = event.source;

    if (this._contextMenu) {
      let isOwnContextMenu = this._contextMenu.anchor === anchor;
      this._contextMenu.close();
      if (isOwnContextMenu) {
        return; // toggle only -> done
      }
    }

    // If button was already selected, just ungroup the column (don't show the context menu)
    if (anchor instanceof TableHeaderMenuButton && anchor.selected) {
      this.table.group(this.column, undefined, undefined, true); // ungroup
      this.close();
      return;
    }

    // Create menus
    let menus = this._createGroupTypeMenus();

    let specialGroupFormat = this.column.groupFormat.pattern;
    let hasSpecialGroupFormat = specialGroupFormat !== 'yyyy';
    if (hasSpecialGroupFormat) {
      // Show additional menu to change grouping according to a custom format
      let specialGroupFormatMenu = scout.create(DateGroupTypeMenu, {
        parent: this,
        text: this._formatGroupFormat(this.column.groupFormat),
        groupType: null,
        hint: dates.format(new Date(), this.session.locale, specialGroupFormat)
      });
      menus.unshift(specialGroupFormatMenu);
    }

    const updateGrouping = (groupType: DateGroupType) => {
      // Unless there is an active filter, reset the previously selected filter group type, so the next time
      // we open the popup, the filter table will inherit this.column.groupType by default.
      if (!this.filter.tableFilterActive()) {
        this.column.__filterGroupType = null;
      }
      // Set group type and apply grouping
      this.column.setGroupType(groupType);
      this.table.group(this.column, undefined, anchor instanceof TableHeaderMenuButton ? anchor.additional : true);
      this.close();
    };

    let currentGroupType = this.column.groupType ?? (hasSpecialGroupFormat ? null : DateGroupType.YEAR);
    menus.forEach(menu => {
      if (menu.groupType === currentGroupType) {
        menu.setIconId(icons.CHECKED_BOLD);
      }
      menu.on('action', event => {
        if (this.column.grouped && menu.groupType === currentGroupType) {
          // already grouped by this type
          this.close();
          return;
        }
        updateGrouping(menu.groupType);
      });
    });

    // Create context menu
    this._contextMenu = scout.create(ContextMenuPopup, {
      parent: this,
      menuItems: menus,
      anchor: anchor,
      cloneMenuItems: false,
      closeOnAnchorMouseDown: false // we use our own toggle logic
    });
    if (anchor instanceof TableHeaderMenuButton) {
      anchor.parent.setActiveGroupItem(anchor);
    }
    this._contextMenu.one('destroy', event => {
      if (anchor instanceof TableHeaderMenuButton) {
        anchor.parent.setActiveGroupItem(null);
      }
      if (this._contextMenu === event.source) {
        this._contextMenu = null;
      }
    });
    this._contextMenu.open();
  }

  // ------------------------------------------------------------------
  // Filter
  // ------------------------------------------------------------------

  protected override _createFilter(): ColumnUserFilter {
    let filter = super._createFilter() as DateColumnUserFilter;
    // Initialize with the previously selected group type the active column group type
    filter.groupType = this.column.__filterGroupType || (this.column.grouped ? this.column.groupType : null);
    return filter;
  }

  protected override _renderFilterTable(): JQuery {
    let $filterTable = super._renderFilterTable();

    // Create an additional action to change the group type in the filter table (only if there are at least two menus to choose from)
    this.filterGroupTypeAction = scout.create(Action, {
      id: 'FilterChangeGroupTypeAction',
      parent: this,
      tooltipText: this.session.text('GroupBy'),
      cssClass: 'button borderless table-header-menu-filter-date-group-type'
    });
    this.filterGroupTypeAction.on('action', this._onFilterChangeGroupTypeAction.bind(this));

    this.filterGroupTypeAction.render(this.filterSortOrderAction.$parent);
    this.filterGroupTypeAction.$container.insertBefore(this.filterSortOrderAction.$container);

    return $filterTable;
  }

  protected _onFilterChangeGroupTypeAction(event: Event<Action>) {
    const anchor = event.source;

    if (this._contextMenu) {
      let isOwnContextMenu = this._contextMenu.anchor === anchor;
      this._contextMenu.close();
      if (isOwnContextMenu) {
        return; // toggle only -> done
      }
    }

    // Create menus
    let menus = this._createGroupTypeMenus();

    const updateFilter = (groupType: DateGroupType) => {
      this.column.__filterGroupType = groupType;
      this.filter.groupType = groupType;
      this.filter.selectedValues = [];
      this.filter.calculate();
      this._updateTableFilter();
      this._reloadFilterTable();
    };

    let currentGroupType = this.filter.groupType ?? DateGroupType.YEAR;
    menus.forEach(menu => {
      if (menu.groupType === currentGroupType) {
        menu.setIconId(icons.CHECKED_BOLD);
      }
      menu.on('action', event => {
        if (menu.groupType === currentGroupType) {
          // already grouped by this type
          return;
        }
        updateFilter(menu.groupType);
      });
    });

    // Create context menu
    this._contextMenu = scout.create(ContextMenuPopup, {
      parent: this,
      menuItems: menus,
      anchor: anchor,
      cloneMenuItems: false,
      closeOnAnchorMouseDown: false // we use our own toggle logic
    });
    anchor.$container?.addClass('selected has-popup');
    this._contextMenu.one('destroy', event => {
      anchor.$container?.removeClass('selected has-popup');
      if (this._contextMenu === event.source) {
        this._contextMenu = null;
      }
    });
    this._contextMenu.open();
  }

  // ------------------------------------------------------------------
  // Helpers
  // ------------------------------------------------------------------

  protected _createGroupTypeMenus(): DateGroupTypeMenu[] {
    const groupTypes = [
      DateGroupType.YEAR,
      DateGroupType.MONTH,
      DateGroupType.MONTH_AND_YEAR,
      DateGroupType.CALENDAR_WEEK,
      DateGroupType.WEEKDAY,
      DateGroupType.DATE
    ];
    return groupTypes.map(groupType => scout.create(DateGroupTypeMenu, {
      parent: this,
      text: this._formatGroupType(groupType),
      groupType: groupType,
      hint: this._formatGroupTypeHint(groupType)
    }));
  }

  protected _formatGroupType(groupType: DateGroupType): string {
    switch (groupType) {
      case DateGroupType.YEAR:
        return this.session.text('DateGroupTypeYear');
      case DateGroupType.MONTH:
        return this.session.text('DateGroupTypeMonth');
      case DateGroupType.MONTH_AND_YEAR:
        return this.session.text('DateGroupTypeMonthAndYear');
      case DateGroupType.CALENDAR_WEEK:
        return this.session.text('DateGroupTypeWeekOfYear');
      case DateGroupType.WEEKDAY:
        return this.session.text('DateGroupTypeWeekday');
      case DateGroupType.DATE:
        return this.session.text('DateGroupTypeDate');
    }
    return groupType || null;
  }

  protected _formatGroupTypeHint(groupType: DateGroupType): string {
    if (groupType) {
      let matrix = new TableMatrix(this.table);
      let axis = matrix.addAxis(this.column, TableMatrix.resolveDateGroup(groupType));
      return axis.format(axis.norm(new Date()));
    }
    return null;
  }

  protected _formatGroupFormat(groupFormat: DateFormat): string {
    if (groupFormat?.pattern) {
      return groupFormat.pattern === 'yyyy' ? this.session.text('DateGroupTypeYear') : groupFormat.pattern;
    }
    return null;
  }
}

export interface DateColumnTableHeaderMenuModel extends TableHeaderMenuModel {
  column?: DateColumn;
}

export interface DateGroupTypeMenuModel extends MenuModel {
  groupType: DateGroupType;
  hint?: string;
}

export class DateGroupTypeMenu extends Menu {
  declare model: DateGroupTypeMenuModel;

  groupType: DateGroupType;
  hint: string;

  $hint: JQuery;

  protected override _render() {
    super._render();
    this.$container.addClass('date-group-type-menu');
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderHint();
  }

  setHint(hint: string) {
    this.setProperty('hint', hint);
  }

  protected _renderHint() {
    if (this.hint) {
      this.$hint = this.$hint || this.$container.appendSpan('hint');
      this.$hint.text(this.hint);
    } else {
      this.$hint?.remove();
      this.$hint = null;
    }
  }

  protected override _renderText() {
    super._renderText();
    // Ensure $hint is positioned after $text
    if (this.$text && this.$hint) {
      this.$hint.insertAfter(this.$text);
    }
  }
}

interface DateColumnWithFilterType {
  /**
   * Temporarily holds the selected group type of the filter table when no filter is active.
   * Allows restoring the previous group type when the header menu is opened again.
   */
  __filterGroupType: DateGroupType;
}

// FIXME bsh: Add test
