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
import {Event, Group, GroupCollapseStyle, PropertyChangeEvent, Widget, WidgetEventMap} from '../index';

export interface GroupEventMap extends WidgetEventMap {
  'bodyHeightChange': Event<Group>;
  'bodyHeightChangeDone': Event<Group>;
  'propertyChange:body': PropertyChangeEvent<Widget>;
  'propertyChange:collapseStyle': PropertyChangeEvent<GroupCollapseStyle>;
  'propertyChange:collapsed': PropertyChangeEvent<boolean>;
  'propertyChange:collapsible': PropertyChangeEvent<boolean>;
  'propertyChange:header': PropertyChangeEvent<Widget>;
  'propertyChange:headerFocusable': PropertyChangeEvent<boolean>;
  'propertyChange:headerVisible': PropertyChangeEvent<boolean>;
  'propertyChange:iconId': PropertyChangeEvent<string>;
  'propertyChange:title': PropertyChangeEvent<string>;
  'propertyChange:titleSuffix': PropertyChangeEvent<string>;
}
