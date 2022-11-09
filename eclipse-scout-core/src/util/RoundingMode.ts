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

/**
 * Enum providing rounding-modes for number columns and fields.
 *
 * @see RoundingMode.java
 */
export enum RoundingMode {
  UP = 'UP',
  DOWN = 'DOWN',
  CEILING = 'CEILING',
  FLOOR = 'FLOOR',
  HALF_UP = 'HALF_UP',
  HALF_DOWN = 'HALF_DOWN',
  HALF_EVEN = 'HALF_EVEN',
  UNNECESSARY = 'UNNECESSARY'
}
