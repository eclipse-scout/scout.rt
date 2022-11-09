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
import {ActionEventMap, Event, Menu, MenuFilter, MenuStyle, PropertyChangeEvent, SubMenuVisibility} from '../index';


export interface MenuEventMap extends ActionEventMap {
  'focus': Event<Menu>;
  'propertyChange:childActions': PropertyChangeEvent<Menu[]>;
  'propertyChange:defaultMenu': PropertyChangeEvent<boolean>;
  'propertyChange:menuFilter': PropertyChangeEvent<MenuFilter>;
  'propertyChange:menuStyle': PropertyChangeEvent<MenuStyle>;
  'propertyChange:menuTypes': PropertyChangeEvent<string[]>;
  'propertyChange:overflown': PropertyChangeEvent<boolean>;
  'propertyChange:shrinkable': PropertyChangeEvent<boolean>;
  'propertyChange:stackable': PropertyChangeEvent<boolean>;
  'propertyChange:subMenuVisibility': PropertyChangeEvent<SubMenuVisibility>;
}
