/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Button, Event, FormFieldEventMap, PropertyChangeEvent, Widget} from '../../../index';

export interface ButtonEventMap extends FormFieldEventMap {
  'click': Event<Button>;
  'propertyChange:defaultButton': PropertyChangeEvent<boolean>;
  'propertyChange:iconId': PropertyChangeEvent<string>;
  'propertyChange:keyStroke': PropertyChangeEvent<string>;
  'propertyChange:keyStrokeScope': PropertyChangeEvent<Widget>;
  'propertyChange:preventDoubleClick': PropertyChangeEvent<boolean>;
  'propertyChange:selected': PropertyChangeEvent<boolean>;
  'propertyChange:shrinkable': PropertyChangeEvent<boolean>;
  'propertyChange:stackable': PropertyChangeEvent<boolean>;
}
