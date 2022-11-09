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
