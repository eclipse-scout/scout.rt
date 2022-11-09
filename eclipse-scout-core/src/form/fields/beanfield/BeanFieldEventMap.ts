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
import {BeanField, Event, ValueFieldEventMap} from '../../../index';

export interface AppLinkActionEvent<T = BeanField<object>> extends Event<T> {
  ref: string;
}

export interface BeanFieldEventMap<TValue extends object> extends ValueFieldEventMap<TValue> {
  'appLinkAction': AppLinkActionEvent;
}
