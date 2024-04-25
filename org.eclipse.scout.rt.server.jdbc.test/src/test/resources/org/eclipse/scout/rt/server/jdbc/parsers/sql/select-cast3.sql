/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
SELECT DV.VALUE0 AS KEY
FROM TABLE2 DV
WHERE DV.ACTIVE = 1
  AND ( CAST ( DATE_TRUNC ( 'DAY'
    , CAST ( EVT_END AS TIMESTAMP ) ) AS TIMESTAMP ) + ( 1 ) * INTERVAL '1 day' ) <= STATEMENT_TIMESTAMP ( )
INTO :resultHolder
