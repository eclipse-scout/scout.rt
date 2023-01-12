/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
