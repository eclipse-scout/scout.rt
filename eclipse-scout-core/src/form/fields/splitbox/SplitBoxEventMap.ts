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
