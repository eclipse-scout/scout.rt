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
import {Status, WidgetModel} from '../index';
import {StatusSeverity} from '../status/Status';

export default interface NotificationModel extends WidgetModel {
  message?: string;
  severity?: StatusSeverity;
  iconId?: string;
  closable?: boolean;
  htmlEnabled?: boolean;
  status?: Status;
}
