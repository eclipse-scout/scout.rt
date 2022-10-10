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
import {IFrame, PropertyChangeEvent, WidgetEventMap} from '../index';

export default interface IFrameEventMap extends WidgetEventMap {
  'propertyChange:location': PropertyChangeEvent<string, IFrame>;
  'propertyChange:sandboxEnabled': PropertyChangeEvent<boolean, IFrame>;
  'propertyChange:sandboxPermissions': PropertyChangeEvent<string, IFrame>;
  'propertyChange:scrollBarEnabled': PropertyChangeEvent<boolean, IFrame>;
  'propertyChange:trackLocation': PropertyChangeEvent<boolean, IFrame>;
}
