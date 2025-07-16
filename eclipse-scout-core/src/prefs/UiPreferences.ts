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
  arrays, ErrorHandler, Event, ITableCustomizerDo, IUserFilterStateDo, NumberColumn, objects, ObjectWithType, PropertyChangeEvent, scout, strings, Table, TableClientUiPreferenceProfileDo, TableClientUiPreferencesDo,
  TableColumnClientUiPreferenceDo, TableUserFilter, UiPreferencesDo, UiPreferencesStore, UiPreferencesUpdateDo, UserFilterStateMappers
} from '../index';
import $ from 'jquery';

/**
 * A singleton that represents all UI preferences of the current user. It is populated during the start of the application,
 * so the preferences can be accessed synchronously.
 */
export class UiPreferences implements ObjectWithType {

  /**
   * Key for the current global preferences of a table, i.e. preferences that are not stored in a specific settings profile.
   */
  static TABLE_PREFERENCE_PROFILE_ID_GLOBAL = 'global-' + 'a134390b-bfef-4b9e-a14e-425df161e768';

  /**
   * Special key used to store table settings of a bookmarked table page. The bookmark support will consider this state as
   * the "factory settings" when the page is displayed in the bookmark outline.
   */
  static TABLE_PREFERENCE_PROFILE_ID_BOOKMARK = 'bookmark-' + 'aebcacd2-ddb6-4b7f-8673-d1585701d388';

  objectType: string;

  protected _store: UiPreferencesStore;
  protected _storeTimeoutId = 0;
  protected _storeDelay = 0;

  /** All loaded preferences */
  protected _preferences: UiPreferencesDo;

  /** Map of all table preferences in {@link _preferences}, indexed by table identifier (see {@link _computeTablePreferencesKey}). */
  protected _tablePreferencesMap: Map<string, TableClientUiPreferencesDo>;
  /** If > 0, table events are ignored. Useful when applying preferences. */
  protected _ignoreTableEventsCounter = 0;

  protected _tableColumnListener = this._onTableColumnEvent.bind(this);
  protected _tableTileModeListener = this._onTableTileModeChange.bind(this);

  // --------------------------------------

  constructor() {
    this._initStore();
  }

  protected _initStore() {
    this.replaceStore(scout.create(UiPreferencesStore));
  }

  /**
   * Replaces the {@link UiPreferencesStore} for this singleton object. This is intended to be used in tests only.
   * In a normal application, the store should not be changed dynamically. Instead, register the desired store
   * implementation via {@link ObjectFactory}.
   *
   * **Important:** This method clears internal data structures, but does *not* automatically reload preferences
   * from the new store. To do so, {@link load} has to be called manually.
   *
   * @returns the old store
   */
  replaceStore(store: UiPreferencesStore): UiPreferencesStore {
    let oldStore = this._store;
    this._store = scout.assertParameter('store', store);
    this._initPreferences(null); // reset cached data
    return oldStore;
  }

  bootstrap(): JQuery.Promise<void> {
    return $.resolvedPromise()
      .then(() => this._subscribeForUpdates())
      .then(() => this.load());
  }

  /**
   * Loads preferences from the {@link UiPreferencesStore} into this singleton object.
   */
  load(): JQuery.Promise<void> {
    return this._store.load()
      .then(preferences => this._initPreferences(preferences));
  }

  /**
   * Writes the current state of this singleton object to the {@link UiPreferencesStore}.
   */
  store(): JQuery.Promise<void> {
    return this._store.store(this._preferences);
  }

  /**
   * Schedules a task to call {@link store}. This method is to be called whenever a preference has been changed.
   * By scheduling a task rather than storing immediately, we can coalesce multiple store requests into a single one.
   */
  scheduleStore() {
    clearTimeout(this._storeTimeoutId);
    this._storeTimeoutId = setTimeout(() => {
      this.store()
        .catch(error => {
          // Unable to store UI preferences -> log silently
          scout.create(ErrorHandler, {displayError: false, sendError: true}).handle(error);
        });
    }, this._storeDelay);
  }

  protected _subscribeForUpdates(): JQuery.Promise<void> {
    return this._store.subscribeForUpdates(event => this._onPreferencesUpdate(event));
  }

  protected _onPreferencesUpdate(update: UiPreferencesUpdateDo) {
    this._initPreferences(update?.preferences);
  }

  protected _initPreferences(preferences: UiPreferencesDo) {
    this._preferences = preferences || scout.create(UiPreferencesDo); // never null
    this._initTablePreferences();
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

  // --------------------------------------

  protected _initTablePreferences() {
    this._tablePreferencesMap = new Map();
    this._preferences?.tablePreferences?.forEach(tablePrefs => {
      let tableId = tablePrefs.tableId;
      let userPreferenceContext = tablePrefs.userPreferenceContext;
      let key = this._computeTablePreferencesKey(tableId, userPreferenceContext);
      this._tablePreferencesMap.set(key, tablePrefs);
    });
  }

  protected _computeTablePreferencesKey(tableId: string, userPreferenceContext: string): string {
    return strings.join('#', tableId, userPreferenceContext);
  }

  // --------------------------------------

  /**
   * Returns the preferences for the given table, or `null` if no preferences are registered yet.
   */
  getTablePreferences(table: Table): TableClientUiPreferencesDo {
    scout.assertParameter('table', table, Table);
    let tableId = table.buildUuidPath();
    let userPreferenceContext = table.userPreferenceContext;
    let key = this._computeTablePreferencesKey(tableId, userPreferenceContext);

    return this._tablePreferencesMap.get(key);
  }

  /**
   * Returns the preferences for the given table. If no preferences are registered yet, a new empty
   * preferences data object is created and stored.
   */
  getOrCreateTablePreferences(table: Table): TableClientUiPreferencesDo {
    scout.assertParameter('table', table, Table);
    let tableId = table.buildUuidPath();
    let userPreferenceContext = table.userPreferenceContext;
    let key = this._computeTablePreferencesKey(tableId, userPreferenceContext);

    let prefs = this._tablePreferencesMap.get(key);
    if (!prefs) {
      prefs = this.createTablePreferences(table);
      this._preferences.tablePreferences = this._preferences.tablePreferences || [];
      this._preferences.tablePreferences.push(prefs);
      this._tablePreferencesMap.set(key, prefs);
      this.scheduleStore();
    }
    return prefs;
  }

  /**
   * Returns the profile with the given id from the given table preferences. If no such profile exists, `undefined` is returned.
   */
  getTablePreferenceProfile(prefs: TableClientUiPreferencesDo, profileId: string): TableClientUiPreferenceProfileDo {
    return prefs?.tablePreferenceProfiles?.get(profileId);
  }

  /**
   * Creates a new data object consisting of all profile-independent preferences for the given table, according to its current state.
   *
   * Note: the `tablePreferences` map is *not* set automatically.
   */
  createTablePreferences(table: Table): TableClientUiPreferencesDo {
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
  createTablePreferenceProfile(table: Table, options?: CreateTablePreferenceProfileOptions): TableClientUiPreferenceProfileDo {
    let columnPreferences = this.createTableColumnPreferences(table);
    let userFilters = options?.includeUserFilters ? this.createTableUserFilterStates(table) : null;
    let customizerData = this.createTableCustomizerData(table);

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
  createTableColumnPreferences(table: Table): TableColumnClientUiPreferenceDo[] {
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
  createTableUserFilterStates(table: Table): IUserFilterStateDo[] {
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
  createTableCustomizerData(table: Table): ITableCustomizerDo {
    scout.assertParameter('table', table, Table);
    return table.isCustomizable() ? table.customizer.getCustomizerData() : null;
  }

  // --------------------------------------

  /**
   * Installs a table listener for all preference-related changes and stores them in the {@link UiPreferences#TABLE_PREFERENCE_PROFILE_ID_GLOBAL} profile for that table.
   */
  installTableListener(table: Table) {
    scout.assertParameter('table', table, Table);
    this.uninstallTableListener(table);
    table.on('columnMoved columnResized columnStructureChanged group sort aggregationFunctionChanged columnBackgroundEffectChanged', this._tableColumnListener);
    table.on('propertyChange:tileMode', this._tableTileModeListener);
  }

  /**
   * Uninstalls the listener installed by {@link installTableListener}.
   */
  uninstallTableListener(table: Table) {
    scout.assertParameter('table', table, Table);
    table.off('columnMoved columnResized columnStructureChanged group sort aggregationFunctionChanged columnBackgroundEffectChanged', this._tableColumnListener);
    table.off('propertyChange:tileMode', this._tableTileModeListener);
  }

  protected _onTableColumnEvent(event: Event<Table>) {
    if (this._ignoreTableEvents) {
      return;
    }
    // FIXME bsh [js-bookmark] Find a better solution. It would convenient if there was a 'columnResizeEnd' event that is only triggered if the user has finished changing the size.
    let oldStoreDelay = this._storeDelay;
    if (event.type === 'columnResized') {
      this._storeDelay = 750; // same delay as in TableAdapter#_sendColumnResized
    }
    try {
      this.storeGlobalTablePreferenceProfile(event.source);
    } finally {
      this._storeDelay = oldStoreDelay;
    }
  }

  protected _onTableTileModeChange(event: PropertyChangeEvent<boolean, Table>) {
    if (this._ignoreTableEvents) {
      return;
    }
    this.storeTablePreferences(event.source);
  }

  // --------------------------------------

  /**
   * Applies the given preferences to the given table, i.e. changes the table state to match the preferences. If a `profileId` is given
   * and the table preferences contain a profile with that id, it is applied as well. Otherwise, only profile-independent preferences
   * are applied.
   */
  applyTablePreferences(table: Table, prefs: TableClientUiPreferencesDo, profileId?: string, options?: ApplyTablePreferencesOptions) {
    if (!prefs) {
      return; // nothing to apply
    }
    scout.assertParameter('table', table, Table);

    this._ignoreTableEvents = true;
    try {
      this._applyTablePreferencesInternal(table, prefs, options);

      let profile = this.getTablePreferenceProfile(prefs, profileId);
      if (profile) {
        this._applyTablePreferenceProfileInternal(table, profile, options);
      }
    } finally {
      this._ignoreTableEvents = false;
    }
  }

  /**
   * Applies the given preference profile to the given table, i.e. changes the table state to match the profile.
   */
  applyTablePreferenceProfile(table: Table, profile: TableClientUiPreferenceProfileDo, options?: ApplyTablePreferencesOptions) {
    if (!profile) {
      return; // nothing to apply
    }
    scout.assertParameter('table', table, Table);

    this._ignoreTableEvents = true;
    try {
      this._applyTablePreferenceProfileInternal(table, profile, options);
    } finally {
      this._ignoreTableEvents = false;
    }
  }

  protected _applyTablePreferencesInternal(table: Table, prefs: TableClientUiPreferencesDo, options?: ApplyTablePreferencesOptions) {
    table.setTileMode(prefs.tileMode);
  }

  protected _applyTablePreferenceProfileInternal(table: Table, profile: TableClientUiPreferenceProfileDo, options?: ApplyTablePreferencesOptions) {
    // Order is important! Applying column preferences requires custom columns to be injected first
    if (table.isCustomizable()) {
      table.customizer.setCustomizerData(profile.tableCustomizerData);
    }

    table.applyColumnPreferences(profile.columns);

    if (options?.applyUserFilters) { // true when showing a bookmark
      table.applyUserFilterStates(profile.userFilters);
    }
  }

  // --------------------------------------

  /**
   * Stores the given profile under the given profileId in the table preferences of the given table.
   */
  storeTablePreferenceProfile(table: Table, profileId: string, profile: TableClientUiPreferenceProfileDo) {
    if (!profileId || !profile) {
      return;
    }

    let prefs = this.getOrCreateTablePreferences(table);
    let existingProfile = this.getTablePreferenceProfile(prefs, profileId);
    if (existingProfile) {
      if (profile.equals(existingProfile)) {
        return; // nothing to do (the new profile is the same as the already stored profile)
      }
    } else {
      if (profile.equals(table.initialUiPreferences)) {
        return; // nothing to do (no old profile exists and the new profile is equal to the default state)
      }
    }

    prefs.tablePreferenceProfiles = prefs.tablePreferenceProfiles || new Map();
    prefs.tablePreferenceProfiles.set(profileId, profile);
    this.scheduleStore();
  }

  /**
   * Renames a table preference profile and stores it.
   */
  renameTablePreferenceProfile(table: Table, oldProfileId: string, newProfileId: string) {
    if (!oldProfileId || !newProfileId || oldProfileId === newProfileId) {
      return;
    }

    let prefs = this.getTablePreferences(table);
    let profile = prefs?.tablePreferenceProfiles?.get(oldProfileId);
    if (profile) {
      prefs.tablePreferenceProfiles.set(newProfileId, profile);
      prefs.tablePreferenceProfiles.delete(oldProfileId);
      this.scheduleStore();
    }
  }

  /**
   * Removes the specified profile from the table preferences and stores it.
   */
  removeTablePreferenceProfile(table: Table, profileId: string) {
    if (!profileId) {
      return;
    }

    let prefs = this.getTablePreferences(table);
    let profile = prefs?.tablePreferenceProfiles?.get(profileId);
    if (profile) {
      prefs.tablePreferenceProfiles.delete(profileId);
      this.scheduleStore();
    }
  }

  /**
   * Stores the profile-independent table preferences to match the current state of the table.
   */
  storeTablePreferences(table: Table) {
    let prefs = this.getOrCreateTablePreferences(table);

    if (this._storeTableTileMode(table, prefs)) {
      this.scheduleStore();
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
   * Stores the current profile-dependent preferences of the given table in the {@link UiPreferences#TABLE_PREFERENCE_PROFILE_ID_GLOBAL} profile.
   */
  storeGlobalTablePreferenceProfile(table: Table) {
    this.storeTablePreferenceProfile(table, UiPreferences.TABLE_PREFERENCE_PROFILE_ID_GLOBAL, this.createTablePreferenceProfile(table));
  }

  /**
   * Removes the {@link UiPreferences#TABLE_PREFERENCE_PROFILE_ID_GLOBAL} profile for the given table.
   */
  clearGlobalTablePreferenceProfile(table: Table) {
    this.removeTablePreferenceProfile(table, UiPreferences.TABLE_PREFERENCE_PROFILE_ID_GLOBAL);
  }
}

export const uiPreferences = objects.createSingletonProxy(UiPreferences);

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
