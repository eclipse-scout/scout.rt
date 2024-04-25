/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
SELECT DV.VALUE0 AS KEY,
          DV.VALUE1,
          CAST( DV.VALUE2 AS INT ) + 10 AS VALUE2 ,
          DV.VALUE3
FROM TABLE1 D, TABLE2 DV
WHERE DV.ACTIVE = 1
