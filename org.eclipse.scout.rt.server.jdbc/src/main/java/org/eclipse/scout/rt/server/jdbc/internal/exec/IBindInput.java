/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jdbc.internal.exec;

import org.eclipse.scout.rt.server.jdbc.SqlBind;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

interface IBindInput {

  IToken getToken();

  /**
   * @return true if this bind corresponds to a jdbc "?" bind
   */
  boolean isJdbcBind(ISqlStyle sqlStyle);

  int getJdbcBindIndex();

  void setJdbcBindIndex(int index);

  boolean isBatch();

  boolean hasBatch(int i);

  void setNextBatchIndex(int i);

  SqlBind produceSqlBindAndSetReplaceToken(ISqlStyle sqlStyle);
}
