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
import {Event, PropertyChangeEvent, SimpleTab, Status, WidgetEventMap} from '../index';

export default interface SimpleTabEventMap extends WidgetEventMap {
  'click': Event<SimpleTab>;
  'propertyChange:closable': PropertyChangeEvent<boolean, SimpleTab>;
  'propertyChange:iconId': PropertyChangeEvent<string, SimpleTab>;
  'propertyChange:saveNeeded': PropertyChangeEvent<boolean, SimpleTab>;
  'propertyChange:saveNeededVisible': PropertyChangeEvent<boolean, SimpleTab>;
  'propertyChange:selected': PropertyChangeEvent<boolean, SimpleTab>;
  'propertyChange:status': PropertyChangeEvent<Status, SimpleTab>;
  'propertyChange:subTitle': PropertyChangeEvent<string, SimpleTab>;
  'propertyChange:title': PropertyChangeEvent<string, SimpleTab>;
}
