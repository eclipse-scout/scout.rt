/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.eclipse.scout.commons.CompressedObjectWriter;
import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * RowVisitor implementation that stores the result set in a compressed
 * byte-array.
 */
public class ByteArrayRowVisitor implements ISelectStreamHandler {
  private static final long serialVersionUID = 1L;
  private static final int INITIAL_BUFFER_SIZE = 1024 * 1024; // 1MB
  private static final int DEFLATER_BUFFER_SIZE = 1024 * 10; // 10KB

  private CompressedObjectWriter out;

  public ByteArrayRowVisitor() throws ProcessingException {
    out = new CompressedObjectWriter(INITIAL_BUFFER_SIZE, DEFLATER_BUFFER_SIZE);
  }

  public void handleRow(Connection con, PreparedStatement stm, ResultSet rs, int rowIndex, List<SqlBind> values) throws ProcessingException {
    Object[] row = new Object[values.size()];
    for (int i = 0; i < row.length; i++) {
      row[i] = values.get(i).getValue();
    }
    out.compress(row);

    /*
     * [mvi, 2010-08-31]: Ticket 93222
     */
    out.resetWrittenObjectCache();
  }

  public void finished(Connection con, PreparedStatement stm, ResultSet rs, int rowCount) throws ProcessingException {
  }

  public byte[] getBytes() throws ProcessingException {
    return out.getCompressedBytes();
  }

  public void close() {
    out.close();
  }

}
