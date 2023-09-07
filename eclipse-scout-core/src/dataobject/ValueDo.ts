/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DoEntity} from './../index';

/**
 * Wrapper data object for a generic value.
 *
 * In Java, we have a separate implementation for each data type. This
 * is because the Jackson object mapper cannot map a generic object.
 * In TypeScript, we do not have this issue, so there is just this
 * generic interface.
 *
 * @see "org.eclipse.scout.rt.dataobject.value.IValueDo"
 */
export interface ValueDo<T> extends DoEntity {
  value: T;
}
