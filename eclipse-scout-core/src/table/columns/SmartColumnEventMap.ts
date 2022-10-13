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

export interface SmartColumnCallDoneEvent<S extends SmartColumn = SmartColumn> extends Event<S> {
  result: LookupResult<any>;
}

export interface SmartColumnPrepareLookupCallEvent<S extends SmartColumn = SmartColumn> extends Event<S> {
  lookupCall: LookupCall<any>;
  row?: TableRow;
}

export default interface SmartColumnEventMap extends ColumnEventMap {
  'lookupCallDone': SmartColumnCallDoneEvent;
  'prepareLookupCall': SmartColumnPrepareLookupCallEvent;
  'propertyChange:activeFilterEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:browseAutoExpandAll': PropertyChangeEvent<boolean>;
  'propertyChange:browseHierarchy': PropertyChangeEvent<boolean>;
  'propertyChange:browseLoadIncremental': PropertyChangeEvent<boolean>;
  'propertyChange:browseMaxRowCount': PropertyChangeEvent<number>;
  'propertyChange:codeType': PropertyChangeEvent<string>;
  'propertyChange:lookupCall': PropertyChangeEvent<LookupCall<any>>;
}
