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
import {ColumnEventMap, Event, LookupCall, LookupResult, PropertyChangeEvent, SmartColumn, TableRow} from '../../index';

export interface SmartColumnCallDoneEvent<TValue = any, S extends SmartColumn<TValue> = SmartColumn<TValue>> extends Event<S> {
  result: LookupResult<TValue>;
}

export interface SmartColumnPrepareLookupCallEvent<TValue = any, S extends SmartColumn<TValue> = SmartColumn<TValue>> extends Event<S> {
  lookupCall: LookupCall<TValue>;
  row?: TableRow;
}

export default interface SmartColumnEventMap<TValue> extends ColumnEventMap {
  'lookupCallDone': SmartColumnCallDoneEvent<TValue>;
  'prepareLookupCall': SmartColumnPrepareLookupCallEvent<TValue>;
  'propertyChange:activeFilterEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:browseAutoExpandAll': PropertyChangeEvent<boolean>;
  'propertyChange:browseHierarchy': PropertyChangeEvent<boolean>;
  'propertyChange:browseLoadIncremental': PropertyChangeEvent<boolean>;
  'propertyChange:browseMaxRowCount': PropertyChangeEvent<number>;
  'propertyChange:codeType': PropertyChangeEvent<string>;
  'propertyChange:lookupCall': PropertyChangeEvent<LookupCall<any>>;
}
