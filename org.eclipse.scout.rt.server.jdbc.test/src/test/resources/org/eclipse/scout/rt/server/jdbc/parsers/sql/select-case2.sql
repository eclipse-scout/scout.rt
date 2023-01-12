/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
SELECT    CQ.QUESTION_NR
, CASE WHEN LENGTH ( REGEXP_REPLACE ( CQ.CODE_NAME , '<[^>]+>' , '' ) ) > 30 THEN SUBSTR ( REGEXP_REPLACE ( CQ.CODE_NAME , '<[^>]+>' , '' ) , 0 , 30 ) || '...' ELSE REGEXP_REPLACE ( CQ.CODE_NAME , '<[^>]+>' , '' ) END NAME , CQ.SORTCODE , AVG ( CA.MARK ) , MIN ( CA.MARK ) , MAX ( CA.MARK ) , COUNT ( CA.PERSON_NR ) FROM ORS_COURSE_QUESTIONARY CQ , ORS_COURSE_ANSWER CA WHERE 1 = 1 AND CQ.COURSE_NR = :courseNr AND CQ.QUESTION_NR = CA.QUESTION_NR (+) GROUP BY CQ.QUESTION_NR , CQ.CODE_NAME , CQ.SORTCODE ORDER BY CQ.SORTCODE
