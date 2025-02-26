/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LookupCallColumnEventMap, LookupCallColumnLookupCallDoneEvent, LookupCallColumnPrepareLookupCallEvent, PropertyChangeEvent, SmartColumn} from '../../index';

/**
 * @deprecated use {@link LookupCallColumnLookupCallDoneEvent} instead
 */
// eslint-disable-next-line @typescript-eslint/no-empty-interface
export interface SmartColumnCallDoneEvent<TValue = any, S extends SmartColumn<TValue> = SmartColumn<TValue>> extends LookupCallColumnLookupCallDoneEvent<TValue, TValue, S> {
}

/**
 * @deprecated use {@link LookupCallColumnPrepareLookupCallEvent} instead
 */
// eslint-disable-next-line @typescript-eslint/no-empty-interface
export interface SmartColumnPrepareLookupCallEvent<TValue = any, S extends SmartColumn<TValue> = SmartColumn<TValue>> extends LookupCallColumnPrepareLookupCallEvent<TValue, TValue, S> {
}

export interface SmartColumnEventMap<TValue> extends LookupCallColumnEventMap<TValue> {
  'lookupCallDone': SmartColumnCallDoneEvent<TValue>;
  'prepareLookupCall': SmartColumnPrepareLookupCallEvent<TValue>;
  'propertyChange:activeFilterEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:browseAutoExpandAll': PropertyChangeEvent<boolean>;
  'propertyChange:browseLoadIncremental': PropertyChangeEvent<boolean>;
}
