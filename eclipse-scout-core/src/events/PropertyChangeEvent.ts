/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event} from '../index';

export interface PropertyChangeEvent<TValue = any, TSource = object> extends Event<TSource> {
  propertyName: string;
  oldValue: TValue;
  newValue: TValue;
}

export function isPropertyChangeEvent(event: any): event is PropertyChangeEvent {
  return event instanceof Event && !!(event as PropertyChangeEvent).propertyName;
}
