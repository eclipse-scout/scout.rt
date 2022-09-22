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
import {Event, Label, PropertyChangeEvent, WidgetEventMap} from '../index';

export interface LabelAppLinkActionEvent<L extends Label = Label> extends Event<L> {
  ref: string;
}

export default interface LabelEventMap extends WidgetEventMap {
  'appLinkAction': LabelAppLinkActionEvent;
  'propertyChange:htmlEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:scrollable': PropertyChangeEvent<boolean>;
  'propertyChange:value': PropertyChangeEvent<string>;
}
