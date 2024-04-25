/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
select value_0 as key
from table2
where active = 1
  and coalesce (
    ( state_expiry )
    , ( ( cast ( cast ( statement_timestamp ( ) as date ) as timestamp ) + ( 1 ) * interval '1 day' ) )
  )
    > cast ( statement_timestamp ( ) as date )
  and state_no = 1
