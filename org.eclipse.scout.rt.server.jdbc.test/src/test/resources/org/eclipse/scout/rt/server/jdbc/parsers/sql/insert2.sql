/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
INSERT INTO ORS_PROBLEM_HISTORY ( PROBLEM_HISTORY_NR , PROBLEM_NR , EVT_START , USER_NR , PERSON_NR , PHASE_UID , NOTES , CLASSIFICATION_UID , WORK_TOTAL , SPRINT , PRIORITY , STORY_POINTS , OFFER_VALUE ) VALUES ( :ticketHistoryNr , :ticket , SYSDATE , :editor , :inChargeOf , :phase , :comment , NVL ( :classification , 0 ) , :effortEstimation , :sprint , :priority , :storyPoints , ( SELECT :costEstimation * C.EXCHANGE_RATE FROM ORS_PROJECT P , ORS_CURRENCY C WHERE P.PROJECT_NR = :project AND P.CURRENCY_UID = C.CURRENCY_UID AND P.CURRENCY_DATE = C.CURRENCY_DATE ) )
