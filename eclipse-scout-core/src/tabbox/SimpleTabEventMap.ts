/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, PropertyChangeEvent, SimpleTab, SimpleTabView, Status, WidgetEventMap} from '../index';

export interface SimpleTabEventMap<TView extends SimpleTabView = SimpleTabView> extends WidgetEventMap {
  'click': Event<SimpleTab<TView>>;
  'propertyChange:closable': PropertyChangeEvent<boolean>;
  'propertyChange:iconId': PropertyChangeEvent<string>;
  'propertyChange:saveNeeded': PropertyChangeEvent<boolean>;
  'propertyChange:saveNeededVisible': PropertyChangeEvent<boolean>;
  'propertyChange:selected': PropertyChangeEvent<boolean>;
  'propertyChange:status': PropertyChangeEvent<Status>;
  'propertyChange:subTitle': PropertyChangeEvent<string>;
  'propertyChange:title': PropertyChangeEvent<string>;
}
