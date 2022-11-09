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
import {Menu, MenuBarEllipsisPosition, MenuBarPosition, PropertyChangeEvent, WidgetEventMap} from '../../index';

export interface MenuBarEventMap extends WidgetEventMap {
  'propertyChange:defaultMenu': PropertyChangeEvent<Menu>;
  'propertyChange:ellipsisPosition': PropertyChangeEvent<MenuBarEllipsisPosition>;
  'propertyChange:hiddenByUi': PropertyChangeEvent<boolean>;
  'propertyChange:menuItems': PropertyChangeEvent<Menu[]>;
  'propertyChange:position': PropertyChangeEvent<MenuBarPosition>;
}
