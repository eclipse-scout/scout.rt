/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, FormField, FormFieldEventMap, PropertyChangeEvent, SplitBox} from '../../../index';

export interface SplitBoxPositionChangeEvent<T = SplitBox> extends Event<T> {
  position: number;
}

export interface SplitBoxEventMap extends FormFieldEventMap {
  'positionChange': SplitBoxPositionChangeEvent;
  'propertyChange:collapsibleField': PropertyChangeEvent<FormField>;
  'propertyChange:fieldCollapsed': PropertyChangeEvent<boolean>;
  'propertyChange:fieldMinimized': PropertyChangeEvent<boolean>;
  'propertyChange:minSplitterPosition': PropertyChangeEvent<number>;
  'propertyChange:minimizeEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:splitterPosition': PropertyChangeEvent<number>;
  'propertyChange:splitterPositionType': PropertyChangeEvent<string>;
}
