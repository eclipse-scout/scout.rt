/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
SELECT CASE WHEN EVT_START>sysdate AND EVT_START<SYSDATE THEN 1 WHEN EVT_START>sysdate+10 AND EVT_START<SYSDATE+10 THEN 2 ELSE 3 END
FROM   ORS_VISIT WHERE  1=2
