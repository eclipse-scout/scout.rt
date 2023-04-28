/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

/**
 * Base interface for all data object entities.
 *
 * @see "org.eclipse.scout.rt.dataobject.IDoEntity"
 */
export interface DoEntity {
  _type?: string;
  _typeVersion?: string;
}
