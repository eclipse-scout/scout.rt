/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, FieldStatus, FormFieldStatusPosition, Menu, PropertyChangeEvent, Status, WidgetEventMap} from '../../index';

export interface FieldStatusEventMap extends WidgetEventMap {
  'hideTooltip': Event<FieldStatus>;
  'showTooltip': Event<FieldStatus>;
  'statusMouseDown': Event<FieldStatus> & JQuery.MouseDownEvent;
  'propertyChange:autoRemove': PropertyChangeEvent<boolean>;
  'propertyChange:menus': PropertyChangeEvent<Menu[]>;
  'propertyChange:position': PropertyChangeEvent<FormFieldStatusPosition>;
  'propertyChange:status': PropertyChangeEvent<Status>;
}
