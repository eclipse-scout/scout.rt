/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
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
 * Implementation detail: Is no BaseDoEntity as there are several corresponding implementations on Java side.
 * This would require a specific ValueDoDeserializer/Serializer which could handle all the different typeNames that might come from the server (extensible list)
 */
export interface ValueDo<T> extends DoEntity {
  value: T;
}
