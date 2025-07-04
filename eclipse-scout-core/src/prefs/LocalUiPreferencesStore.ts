/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {dataObjects, UiPreferencesDo, UiPreferencesStore, webstorage} from '../index';

/**
 * Loads and stores {@link UiPreferencesDo} from/to the browser's local storage.
 */
export class LocalUiPreferencesStore extends UiPreferencesStore {

  /**
   * The key to use when reading/writing to the local storage.
   */
  protected get _storeId(): string {
    return `scout:uiPreferences:${window.location.pathname}`;
  }

  protected override async _load(): Promise<UiPreferencesDo> {
    let json = webstorage.getItemFromLocalStorage(this._storeId);
    return dataObjects.parse(json, UiPreferencesDo);
  }

  protected override async _store(preferences: UiPreferencesDo): Promise<void> {
    if (preferences) {
      let json = dataObjects.stringify(preferences);
      webstorage.setItemToLocalStorage(this._storeId, json);
    } else {
      webstorage.removeItemFromLocalStorage(this._storeId);
    }
  }
}
