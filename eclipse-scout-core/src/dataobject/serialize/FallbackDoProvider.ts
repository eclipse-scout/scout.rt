/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, Constructor} from '../../index';

// FIXME CGU [js-bookmark] discuss name and add specs
export interface FallbackDoProvider {
  accept(rawObj: Record<string, any>): boolean;

  provide(): Constructor<BaseDoEntity>;
}
