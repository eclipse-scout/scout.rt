/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
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
 * Implementation detail: This is not a BaseDoEntity because there are several corresponding implementations on the Java side.
 * Supporting all type names received from the server would require a dedicated, extensible ValueDoDeserializer/Serializer.
 */
export interface ValueDo<T> extends DoEntity {
  value: T;
}
