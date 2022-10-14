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
import {Event, FieldStatus, Menu, PropertyChangeEvent, Status, WidgetEventMap} from '../../index';
import {FormFieldStatusPosition} from './FormField';

export default interface FieldStatusEventMap extends WidgetEventMap {
  'hideTooltip': Event<FieldStatus>;
  'showTooltip': Event<FieldStatus>;
  'statusMouseDown': Event<FieldStatus> & JQuery.MouseDownEvent;
  'propertyChange:autoRemove': PropertyChangeEvent<boolean>;
  'propertyChange:menus': PropertyChangeEvent<Menu[]>;
  'propertyChange:position': PropertyChangeEvent<FormFieldStatusPosition>;
  'propertyChange:status': PropertyChangeEvent<Status>;
}
