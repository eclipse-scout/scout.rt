/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, LabelField, PropertyChangeEvent, ValueFieldEventMap} from '../../../index';

export interface LabelFieldAppLinkActionEvent<T = LabelField> extends Event<T> {
  ref: string;
}

export interface LabelFieldEventMap extends ValueFieldEventMap<string> {
  'appLinkAction': LabelFieldAppLinkActionEvent;
  'propertyChange:htmlEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:selectable': PropertyChangeEvent<boolean>;
  'propertyChange:wrapText': PropertyChangeEvent<boolean>;
}
