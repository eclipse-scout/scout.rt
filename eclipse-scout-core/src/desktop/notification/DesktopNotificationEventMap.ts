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
import {NativeNotificationVisibility, NotificationEventMap, PropertyChangeEvent, Status} from '../../index';

export interface DesktopNotificationEventMap extends NotificationEventMap {
  'propertyChange:nativeNotificationShown': PropertyChangeEvent<boolean>;
  'propertyChange:nativeNotificationStatus': PropertyChangeEvent<Status>;
  'propertyChange:nativeNotificationTitle': PropertyChangeEvent<string>;
  'propertyChange:nativeNotificationVisibility': PropertyChangeEvent<NativeNotificationVisibility>;
}
