/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {UiPreferences, UiPreferencesDo, UiPreferencesUpdateDo} from '../index';

/**
 * Abstract storage service for {@link UiPreferences}.
 *
 * An implementation can be provided by registering an object factory for this type.
 */
export class UiPreferencesStore { // cannot technically be abstract, because we use scout.create() to create an instance of the implementation

  async load(): Promise<UiPreferencesDo> {
    throw new Error('Not implemented');
  }

  async store(preferences: UiPreferencesDo): Promise<void> {
    throw new Error('Not implemented');
  }

  async subscribeForUpdates(handler: UiPreferencesUpdateHandler): Promise<void> {
    // (implementation optional)
  }
}

export type UiPreferencesUpdateHandler = (update: UiPreferencesUpdateDo) => void;
