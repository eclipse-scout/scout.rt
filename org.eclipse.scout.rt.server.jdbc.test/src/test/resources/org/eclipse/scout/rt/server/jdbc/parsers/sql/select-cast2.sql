/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
UPDATE TABLE1 AS T
SET LOCKED_BY_ID = :b__0,
    VERSION      = CAST((VERSION + 1) AS BIGINT)
WHERE T.ID = :b__1
  AND T.LOCKED_BY_ID IS NULL
  AND T.VERSION = :b__2
