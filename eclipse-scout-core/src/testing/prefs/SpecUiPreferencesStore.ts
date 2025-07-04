/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {scout, uiPreferences, UiPreferencesDo, UiPreferencesStore, UiPreferencesUpdateHandler} from '../../index';

let specStore: SpecUiPreferencesStore = null;
let originalStore: UiPreferencesStore = null;

/**
 * {@link UiPreferencesStore} for jasmine tests. For each spec, a new instance is created, so any changed
 * preferences are not carried over to the next spec. Use {@link SpecUiPreferencesStore#get} to access
 * the current instance.
 */
export class SpecUiPreferencesStore extends UiPreferencesStore {

  // In-memory storage
  preferences: UiPreferencesDo = null;
  loadCount = 0;
  storeCount = 0;
  subscribers: UiPreferencesUpdateHandler[] = [];

  protected override async _load(): Promise<UiPreferencesDo> {
    this.loadCount++;
    return this.preferences;
  }

  protected override async _store(preferences: UiPreferencesDo): Promise<void> {
    this.storeCount++;
    this.preferences = preferences;
  }

  protected override async _subscribeForUpdates(handler: UiPreferencesUpdateHandler): Promise<void> {
    this.subscribers.push(handler);
  }

  // ---------------------------

  static install() {
    if (specStore) {
      SpecUiPreferencesStore.uninstall();
    }
    specStore = new SpecUiPreferencesStore();
    originalStore = uiPreferences.replaceStore(specStore);
  }

  static uninstall() {
    scout.assertValue(specStore, 'Mock not installed');
    uiPreferences.replaceStore(originalStore);
    specStore = null;
    originalStore = null;
  }

  static get(): SpecUiPreferencesStore {
    return scout.assertValue(specStore, 'Mock not installed');
  }
}
