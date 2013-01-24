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
package org.eclipse.scout.rt.services.common.jdbc;

import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService;
import org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle;
import org.eclipse.scout.rt.services.common.jdbc.style.DerbySqlStyle;

/**
 * Cause Derby supports no sequences, we model this behavior with a table
 * with only one column AND entry of type BIGINT (or other numeric type).
 * This table needs to have at the beginning this one entry. The default
 * column name is "LAST_VAL". With the method
 * {@link AbstractDerbySqlService#getConfiguredSequenceColumnName()} one can
 * customize the name of this column.
 *
 * NOTE: With "CREATE SYNONYM DUAL FOR SYSIBM.SYSDUMMY1" one can better reuse
 *       Oracle styled SQL
 */
public abstract class AbstractDerbySqlService extends AbstractSqlService{

  public String getConfiguredSequenceColumnName() throws ProcessingException {
    return "LAST_VAL";
  }

  @Override
  protected String getConfiguredJdbcDriverName(){
    return "org.apache.derby.jdbc.EmbeddedDriver";
  }

  @Override
  public Long getSequenceNextval(String sequenceName) throws ProcessingException{
    //increase
    String update = "UPDATE " + sequenceName + " SET " + getConfiguredSequenceColumnName() + " = " + getConfiguredSequenceColumnName() + " + 1";
    createStatementProcessor(update,null,0).processModification(getTransaction(),getStatementCache(),null);

    //read
    String s = "SELECT " + getConfiguredSequenceColumnName() + " FROM " + sequenceName;
    Object[][] ret = createStatementProcessor(s,null,0).processSelect(getTransaction(),getStatementCache(),null);
    if(ret.length == 1){
      return NumberUtility.toLong(NumberUtility.nvl((Number)ret[0][0], 0));
    }
    return 0L;
  }


  @Override
  protected String getConfiguredJdbcMappingName(){
    return "jdbc:derby:<path to db>";
  }

  @Override
  protected Class<? extends ISqlStyle> getConfiguredSqlStyle(){
    return DerbySqlStyle.class;
  }

}
