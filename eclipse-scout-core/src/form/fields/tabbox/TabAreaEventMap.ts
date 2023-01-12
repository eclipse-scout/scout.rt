/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {PropertyChangeEvent, Tab, TabAreaStyle, WidgetEventMap} from '../../../index';

export interface TabAreaEventMap extends WidgetEventMap {
  'propertyChange:displayStyle': PropertyChangeEvent<TabAreaStyle>;
  'propertyChange:hasSubLabel': PropertyChangeEvent<boolean>;
  'propertyChange:selectedTab': PropertyChangeEvent<Tab>;
  'propertyChange:tabs': PropertyChangeEvent<Tab[]>;
}
