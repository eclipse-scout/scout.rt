/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, Column, ErrorHandler, Event, ITableCustomizerDo, IUserFilterStateDo, NumberColumn, NumberColumnAggregationFunction, NumberColumnBackgroundEffect, objects, ObjectWithType, PropertyChangeEvent, scout, strings, Table,
  TableClientUiPreferenceProfileDo, TableClientUiPreferencesDo, TableColumnClientUiPreferenceDo, TableUserFilter, UiPreferences, uiPreferences, UserFilterStateMappers
} from '../index';

/**
 * A singleton that represents all {@link Table}-specific UI preferences of the current user. It is populated during the start of the
 * application, so the preferences can be accessed synchronously.
 */
export class TableUiPreferences implements ObjectWithType {

  /**
   * Key for the current global preferences of a table, i.e. preferences that are not stored in a specific settings profile.
   */
  static readonly PROFILE_ID_GLOBAL = 'global-' + 'a134390b-bfef-4b9e-a14e-425df161e768';

  /**
   * Special key used to store table settings of a bookmarked table page. The bookmark support will consider this state as
   * the "factory settings" when the page is displayed in the bookmark outline.
   */
  static readonly PROFILE_ID_BOOKMARK = 'bookmark-' + 'aebcacd2-ddb6-4b7f-8673-d1585701d388';

  objectType: string;

  /** Map of all table preferences, indexed by table identifier (see {@link _computeTablePreferencesKey}). */
  protected _tablePreferencesMap: Map<string, TableClientUiPreferencesDo> = new Map();
  /** If > 0, table events are ignored. Useful when applying preferences. */
  protected _ignoreTableEventsCounter = 0;
  protected _columResizeTimeoutId: number;

  protected _tableColumnListener = this._onTableColumnEvent.bind(this);
  protected _tableTileModeListener = this._onTableTileModeChange.bind(this);

  // --------------------------------------

  /**
   * Imports the given data into the internal structures, so individual table preferences can be read and
   * modified by the various methods on this class. Any existing data is replaced.
   *
   * @internal should only be called from the registered {@link UiPreferencesHandler}!
   */
  _importTablePreferences(tablePreferences: TableClientUiPreferencesDo[]) {
    this._tablePreferencesMap.clear();
    tablePreferences?.forEach(tablePrefs => {
      let tableId = tablePrefs.tableId;
      let userPreferenceContext = tablePrefs.userPreferenceContext;
      let key = this._computeTablePreferencesKey(tableId, userPreferenceContext);
      this._tablePreferencesMap.set(key, tablePrefs);
    });
  }

  /**
   * Exports the current state of the internal data structures as persistable table preferences.
   *
   * @internal should only be called from the registered {@link UiPreferencesHandler}!
   */
  _exportTablePreferences(): TableClientUiPreferencesDo[] {
    return [...this._tablePreferencesMap.values()];
  }

  /**
   * Returns the preferences for the given table. If no preferences are registered yet, a new empty
   * preferences data object is created and stored.
   */
  protected _getOrCreateTablePreferences(table: Table): TableClientUiPreferencesDo {
    scout.assertParameter('table', table, Table);
    let tableId = table.buildUuidPath();
    let userPreferenceContext = table.userPreferenceContext;
    let key = this._computeTablePreferencesKey(tableId, userPreferenceContext);

    let prefs = this._tablePreferencesMap.get(key);
    if (!prefs) {
      prefs = this.create(table);
      this._tablePreferencesMap.set(key, prefs);
      this._scheduleStore();
    }
    return prefs;
  }

  protected _computeTablePreferencesKey(tableId: string, userPreferenceContext: string): string {
    return strings.join('#', tableId, userPreferenceContext);
  }

  protected _scheduleStore() {
    uiPreferences.scheduleStore(TableUiPreferences);
  }

  // --------------------------------------

  /**
   * Installs or uninstalls UI preference support for the given table according to its {@link Table#uiPreferencesEnabled} flag.
   */
  updateUiPreferencesEnabled(table: Table) {
    scout.assertParameter('table', table, Table);
    if (table.uiPreferencesEnabled) {
      // Save current state as "factory defaults". User filters are not included.
      table.setInitialUiPreferences(this.createProfile(table));

      // If there is a stored GLOBAL profile, apply it now
      let prefs = this.get(table);
      this.apply(table, prefs, TableUiPreferences.PROFILE_ID_GLOBAL);

      // Install a table listener that stores all changes into the GLOBAL profile.
      // This is only done _after_ applying the initial state, so that no events were triggered.
      this._installTableListener(table);
    } else {
      // Uninstall listener
      this._uninstallTableListener(table);
    }
  }

  /**
   * Installs a table listener for all preference-related changes and stores them in the {@link TableUiPreferences#PROFILE_ID_GLOBAL} profile for that table.
   */
  protected _installTableListener(table: Table) {
    this._uninstallTableListener(table);
    table.on('columnMoved columnResized columnStructureChanged group sort aggregationFunctionChanged columnBackgroundEffectChanged', this._tableColumnListener);
    table.on('propertyChange:tileMode', this._tableTileModeListener);
  }

  /**
   * Uninstalls the listener installed by {@link _installTableListener}.
   */
  protected _uninstallTableListener(table: Table) {
    table.off('columnMoved columnResized columnStructureChanged group sort aggregationFunctionChanged columnBackgroundEffectChanged', this._tableColumnListener);
    table.off('propertyChange:tileMode', this._tableTileModeListener);
  }

  /**
   * Executes the specified runnable immediately. During its execution, all table events are ignored by this
   * {@link TableUiPreferences} instance. The events themselves are not suppressed, i.e. other listeners are
   * still triggered. Useful for making table adjustments that should _not_ be stored in the global profile.
   */
  withIgnoreTableEvents(runnable: () => void) {
    if (!runnable) {
      return;
    }
    this._ignoreTableEvents = true;
    try {
      runnable();
    } finally {
      this._ignoreTableEvents = false;
    }
  }

  protected get _ignoreTableEvents(): boolean {
    return this._ignoreTableEventsCounter > 0;
  }

  protected set _ignoreTableEvents(applyingTablePreferences: boolean) {
    if (applyingTablePreferences) {
      this._ignoreTableEventsCounter++;
    } else {
      this._ignoreTableEventsCounter = Math.max(0, this._ignoreTableEventsCounter - 1);
    }
  }

  protected _onTableColumnEvent(event: Event<Table>) {
    if (this._ignoreTableEvents) {
      return;
    }

    // FIXME bsh [js-bookmark] Find a better solution. It would convenient if there was a 'columnResizeEnd' event that is only triggered if the user has finished changing the size.
    clearTimeout(this._columResizeTimeoutId);
    if (event.type === 'columnResized') {
      this._columResizeTimeoutId = setTimeout(() => {
        this.storeGlobalProfile(event.source);
      }, 750); // same delay as in TableAdapter#_sendColumnResized
    } else {
      this.storeGlobalProfile(event.source);
    }
  }

  protected _onTableTileModeChange(event: PropertyChangeEvent<boolean, Table>) {
    if (this._ignoreTableEvents) {
      return;
    }
    this.store(event.source);
  }

  // --------------------------------------

  /**
   * Returns the preferences for the given table, or `null` if no preferences are registered yet.
   */
  get(table: Table): TableClientUiPreferencesDo {
    scout.assertParameter('table', table, Table);
    let tableId = table.buildUuidPath();
    let userPreferenceContext = table.userPreferenceContext;
    let key = this._computeTablePreferencesKey(tableId, userPreferenceContext);

    return this._tablePreferencesMap.get(key);
  }

  /**
   * Returns the profile with the given id from the given table preferences. If no such profile exists, `undefined` is returned.
   */
  getProfile(prefs: TableClientUiPreferencesDo, profileId: string): TableClientUiPreferenceProfileDo {
    return prefs?.tablePreferenceProfiles?.get(profileId);
  }

  /**
   * Creates a new data object consisting of all profile-independent preferences for the given table, according to its current state.
   *
   * Note: the `tablePreferences` map is *not* set automatically.
   */
  create(table: Table): TableClientUiPreferencesDo {
    scout.assertParameter('table', table, Table);
    return scout.create(TableClientUiPreferencesDo, {
      tableId: table.buildUuidPath(),
      userPreferenceContext: table.userPreferenceContext,
      tileMode: table.tileMode
    });
  }

  /**
   * Creates a new data object consisting of all profile-dependent preferences for the given table, according to its current state.
   */
  createProfile(table: Table, options?: CreateTablePreferenceProfileOptions): TableClientUiPreferenceProfileDo {
    let columnPreferences = this.createColumnPreferences(table);
    let userFilters = options?.includeUserFilters ? this.createUserFilterStates(table) : null;
    let customizerData = this.createCustomizerData(table);

    return scout.create(TableClientUiPreferenceProfileDo, {
      columns: arrays.nullIfEmpty(columnPreferences) || undefined,
      userFilters: arrays.nullIfEmpty(userFilters) || undefined,
      tableCustomizerData: customizerData || undefined
    });
  }

  /**
   * Creates a list of new data objects consisting of the preferences for each column of the given table, according to their current state.
   * The result is never `null`. Invisible columns are included, while `guiOnly` columns are ignored.
   */
  createColumnPreferences(table: Table): TableColumnClientUiPreferenceDo[] {
    scout.assertParameter('table', table, Table);
    return table.columns
      .filter(column => !column.guiOnly)
      .map((column, index) => {
        return scout.create(TableColumnClientUiPreferenceDo, {
          columnId: column.buildUuid(),
          viewIndex: index,
          visible: column.visibleIgnoreCompacted, // in compact mode, all columns would be invisible otherwise
          width: column.width,
          sortOrder: column.sortIndex,
          sortAscending: column.sortAscending,
          groupingActive: column.grouped,
          aggregationFunctionId: column instanceof NumberColumn ? column.aggregationFunction : undefined,
          backgroundEffectId: column instanceof NumberColumn ? column.backgroundEffect : undefined
        });
      });
  }

  /**
   * Creates a list of new data objects consisting of the state of each {@link TableUserFilter} of the given table.
   * The result is never `null`. Only user filters with a registered {@link UserFilterStateMapper} are returned.
   */
  createUserFilterStates(table: Table): IUserFilterStateDo[] {
    scout.assertParameter('table', table, Table);
    return table.filters
      .filter(filter => filter instanceof TableUserFilter)
      .map((filter: TableUserFilter) => {
        for (let mapper of UserFilterStateMappers.all()) {
          let filterState = mapper.tryToDo(table, filter);
          if (filterState) {
            return filterState;
          }
        }
        scout.create(ErrorHandler, {displayError: false, sendError: true}).handle(`Unable to map filter to data object [table=${table.id}, filterType=${filter?.filterType}, filterLabel=${filter?.createLabel()}`);
        return null;
      })
      .filter(Boolean);
  }

  /**
   * If the table is customizable, returns the customizer data. Otherwise, `null` is returned.
   */
  createCustomizerData(table: Table): ITableCustomizerDo {
    scout.assertParameter('table', table, Table);
    return table.isCustomizable() ? table.customizer.getCustomizerData() : null;
  }

  // --------------------------------------

  /**
   * Stores the given profile under the given profileId in the table preferences of the given table.
   */
  storeProfile(table: Table, profileId: string, profile: TableClientUiPreferenceProfileDo) {
    if (!profileId || !profile) {
      return;
    }

    let prefs = this._getOrCreateTablePreferences(table);

    // Check if store is necessary
    let existingProfile = this.getProfile(prefs, profileId);
    if (existingProfile) {
      if (profile.equals(existingProfile)) {
        // The new profile is identical to the already stored profile
        return;
      }
    } else {
      if (profileId === TableUiPreferences.PROFILE_ID_GLOBAL && profile.equals(table.initialUiPreferences)) {
        // If the new profile is equal to the default state, it is not necessary to store it as the global profile.
        // For other profileIds, we always want to store, because they are explicitly created by the user.
        return;
      }
    }

    prefs.tablePreferenceProfiles = prefs.tablePreferenceProfiles || new Map();
    prefs.tablePreferenceProfiles.set(profileId, profile);
    this._scheduleStore();
  }

  /**
   * Renames a table preference profile and stores it.
   */
  renameProfile(table: Table, oldProfileId: string, newProfileId: string) {
    if (!oldProfileId || !newProfileId || oldProfileId === newProfileId) {
      return;
    }

    let prefs = this.get(table);
    let profile = prefs?.tablePreferenceProfiles?.get(oldProfileId);
    if (profile) {
      prefs.tablePreferenceProfiles.set(newProfileId, profile);
      prefs.tablePreferenceProfiles.delete(oldProfileId);
      this._scheduleStore();
    }
  }

  /**
   * Removes the specified profile from the table preferences and stores it.
   */
  removeProfile(table: Table, profileId: string) {
    if (!profileId) {
      return;
    }

    let prefs = this.get(table);
    let profile = prefs?.tablePreferenceProfiles?.get(profileId);
    if (profile) {
      prefs.tablePreferenceProfiles.delete(profileId);
      this._scheduleStore();
    }
  }

  /**
   * Updates and stores the profile-independent table preferences to match the current state of the table.
   */
  store(table: Table) {
    let prefs = this._getOrCreateTablePreferences(table);

    if (this._storeTableTileMode(table, prefs)) {
      this._scheduleStore();
    }
  }

  protected _storeTableTileMode(table: Table, prefs: TableClientUiPreferencesDo): boolean {
    if (prefs.tileMode !== table.tileMode) {
      prefs.tileMode = table.tileMode;
      return true;
    }

    return false; // nothing to do
  }

  /**
   * Stores the current profile-dependent preferences of the given table in the {@link TableUiPreferences#PROFILE_ID_GLOBAL} profile.
   */
  storeGlobalProfile(table: Table) {
    this.storeProfile(table, TableUiPreferences.PROFILE_ID_GLOBAL, this.createProfile(table));
  }

  /**
   * Removes the {@link TableUiPreferences#PROFILE_ID_GLOBAL} profile for the given table.
   */
  clearGlobalProfile(table: Table) {
    this.removeProfile(table, TableUiPreferences.PROFILE_ID_GLOBAL);
  }

  // --------------------------------------

  /**
   * Applies the given preferences to the given table, i.e. changes the table state to match the preferences. If a `profileId` is given
   * and the table preferences contain a profile with that id, it is applied as well. Otherwise, only profile-independent preferences
   * are applied.
   */
  apply(table: Table, prefs: TableClientUiPreferencesDo, profileId?: string, options?: ApplyTablePreferencesOptions) {
    if (!prefs) {
      return; // nothing to apply
    }
    scout.assertParameter('table', table, Table);

    this.withIgnoreTableEvents(() => {
      this._applyTablePreferencesInternal(table, prefs, options);

      let profile = this.getProfile(prefs, profileId);
      if (profile) {
        this._applyTablePreferenceProfileInternal(table, profile, options);
      }
    });
  }

  /**
   * Applies the given preference profile to the given table, i.e. changes the table state to match the profile.
   */
  applyProfile(table: Table, profile: TableClientUiPreferenceProfileDo, options?: ApplyTablePreferencesOptions) {
    if (!profile) {
      return; // nothing to apply
    }
    scout.assertParameter('table', table, Table);

    this.withIgnoreTableEvents(() => {
      this._applyTablePreferenceProfileInternal(table, profile, options);
    });
  }

  protected _applyTablePreferencesInternal(table: Table, prefs: TableClientUiPreferencesDo, options?: ApplyTablePreferencesOptions) {
    table.setTileMode(prefs.tileMode);
  }

  protected _applyTablePreferenceProfileInternal(table: Table, profile: TableClientUiPreferenceProfileDo, options?: ApplyTablePreferencesOptions) {
    // Order is important! Applying column preferences requires custom columns to be injected first
    this._applyCustomizerData(table, profile.tableCustomizerData, options);
    this._applyColumnPreferences(table, profile.columns, options);
    this._applyUserFilterStates(table, profile.userFilters, options);
  }

  protected _applyCustomizerData(table: Table, customizerData: ITableCustomizerDo, options?: ApplyTablePreferencesOptions) {
    if (table.isCustomizable()) {
      table.customizer.setCustomizerData(customizerData);
    }
  }

  protected _applyColumnPreferences(table: Table, columnPreferences: TableColumnClientUiPreferenceDo[], options?: ApplyTablePreferencesOptions) {
    let columnPreferencesMap = new Map(arrays.ensure(columnPreferences).map(pref => [pref.columnId, pref]));

    // Sort existing columns according to the order specified in the preferences
    let newColumns = table.columns
      .filter(c => !c.guiOnly) // will be recreated by _setColumns
      .sort((c1, c2) => { // reorder
        let viewIndex1 = columnPreferencesMap.get(c1.buildUuid())?.viewIndex ?? Infinity;
        let viewIndex2 = columnPreferencesMap.get(c2.buildUuid())?.viewIndex ?? Infinity;
        return viewIndex1 - viewIndex2;
      });
    // Apply preferences to existing columns. Columns without corresponding entry in the preferences are left
    // untouched, while preference entries without corresponding column are simply ignored.
    newColumns.forEach(column => this._applyColumnPreferencesToColumn(column, columnPreferencesMap.get(column.buildUuid())));

    table.setColumns(newColumns);
  }

  protected _applyColumnPreferencesToColumn(column: Column<any>, columnPreferences: TableColumnClientUiPreferenceDo) {
    if (!columnPreferences) {
      return; // can happen if preferences are applied before the customizer is installed or if preferences contains obsolete data
    }

    // Use setter for 'visible' property because it is a multidimensional property
    column.setVisible(columnPreferences.visible, false); // parameter 'false' skips call of onColumnVisibilityChanged()

    // Don't use setter for 'width' property to prevent unnecessarily redrawing the table (will be done again later in setColumns anyway)
    column.width = columnPreferences.width;

    // Properties without setter (changes will be applied later by _setColumns)
    column.sortIndex = columnPreferences.sortOrder;
    column.sortAscending = columnPreferences.sortAscending;
    column.sortActive = column.sortIndex >= 0;
    column.grouped = columnPreferences.groupingActive;

    if (column instanceof NumberColumn) {
      // Use setters to correctly update internal structures (e.g. aggrStart function)
      column.setAggregationFunction(columnPreferences.aggregationFunctionId as NumberColumnAggregationFunction);
      column.setBackgroundEffect(columnPreferences.backgroundEffectId as NumberColumnBackgroundEffect);
    }
  }

  protected _applyUserFilterStates(table: Table, userFilterStates: IUserFilterStateDo[], options?: ApplyTablePreferencesOptions) {
    if (options?.applyUserFilters) { // true when showing a bookmark
      table.applyUserFilterStates(userFilterStates);
    }
  }
}

// --------------------------------------

export interface CreateTablePreferenceProfileOptions {
  /**
   * Specifies whether to include the state of user filters ({@link IUserFilterStateDo}) in the preference profile. Default is `false`.
   */
  includeUserFilters: boolean;
}

export interface ApplyTablePreferencesOptions {
  /**
   * Specifies whether to apply user filter states from the preference profile to the table. Default is `false`.
   */
  applyUserFilters?: boolean;
}

// --------------------------------------

export const tableUiPreferences = objects.createSingletonProxy(TableUiPreferences);

UiPreferences.registerHandler(TableUiPreferences, {
  importPreferences: preferences => {
    tableUiPreferences._importTablePreferences(preferences.tablePreferences);
  },
  exportPreferences: preferences => {
    preferences.tablePreferences = tableUiPreferences._exportTablePreferences();
  }
});
