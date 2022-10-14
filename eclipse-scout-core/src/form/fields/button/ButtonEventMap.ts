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
import {Button, Event, FormFieldEventMap, PropertyChangeEvent, Widget} from '../../../index';

export default interface ButtonEventMap extends FormFieldEventMap {
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
