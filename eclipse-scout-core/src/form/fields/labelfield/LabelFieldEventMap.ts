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
import {Event, LabelField, PropertyChangeEvent, ValueFieldEventMap} from '../../../index';

export interface LabelFieldAppLinkActionEvent<T = LabelField> extends Event<T> {
  ref: string;
}

export default interface LabelFieldEventMap extends ValueFieldEventMap<string> {
  'appLinkAction': LabelFieldAppLinkActionEvent;
  'propertyChange:htmlEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:selectable': PropertyChangeEvent<boolean>;
  'propertyChange:wrapText': PropertyChangeEvent<boolean>;
}
