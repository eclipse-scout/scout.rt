/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jdbc.internal.exec;

import static org.junit.Assert.assertFalse;

import java.util.Date;

import org.eclipse.scout.rt.platform.holders.IntegerHolder;
import org.eclipse.scout.rt.platform.holders.LongHolder;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.platform.internal.BeanInstanceUtil;
import org.eclipse.scout.rt.server.TestJdbcServerSession;
import org.eclipse.scout.rt.server.jdbc.AbstractSqlService;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit Test for {@link StatementProcessor}
 */
@RunWith(ServerTestRunner.class)
@RunWithServerSession(TestJdbcServerSession.class)
@RunWithSubject("default")
public class StatementProcessorTest {

  @Test
  public void testLookupCall() throws Exception {
    LookupCall call = new LookupCall() {
      private static final long serialVersionUID = 1L;
    };

    //
    AbstractSqlService sqlService = new AbstractSqlService() {
    };
    BeanInstanceUtil.initializeBeanInstance(sqlService);
    StatementProcessor sp = new StatementProcessor(
        sqlService,
        "SELECT P.PERSON_NR,P.NAME"
            + " FROM PERSON P "
            + " WHERE P.PERSON_NR=:key "
            + " AND P.NAME like '%'||:text||'%'",
        new Object[]{call});
    sp.simulate();

    String sqlPlainTextDump = sp.createSqlDump(false, true);
    assertFalse(sqlPlainTextDump.contains("UNPARSED"));
  }

  @Test
  public void testSelectLike() throws Exception {
    AbstractSqlService sqlService = new AbstractSqlService() {
    };
    BeanInstanceUtil.initializeBeanInstance(sqlService);
    StatementProcessor sp = new StatementProcessor(
        sqlService,
        "SELECT BP_NR FROM FLM_BP WHERE BP_NO LIKE :bpNo INTO :bpNr",
        new Object[]{new NVPair("bpNo", "12"), new NVPair("bpNr", new LongHolder())});
    sp.simulate();

    String sqlPlainTextDump = sp.createSqlDump(false, true);
    assertFalse(sqlPlainTextDump.contains("UNPARSED"));
  }

  @Test
  public void testFormData() throws Exception {
    IntegerHolder countConcurrent = new IntegerHolder();
    PersonFormData formData = new PersonFormData();
    formData.getAddressTable().addRow();
    formData.getAddressTable().addRow();
    //
    AbstractSqlService sqlService = new AbstractSqlService() {
    };
    BeanInstanceUtil.initializeBeanInstance(sqlService);
    StatementProcessor sp = new StatementProcessor(
        sqlService,
        "SELECT COUNT(*) "
            + "FROM PERSON P "
            + "WHERE NVL(:birthdate,TO_DATE('1.1.3000','dd.mm.yyyy')) >= SYSDATE "
            + "AND :name like '%Me%' "
            + "AND :{addressTable.street} like '%Park%' "
            + "INTO :countConcurrent ",
        new Object[]{formData, new NVPair("countConcurrent", countConcurrent)});
    sp.simulate();

    String sqlPlainTextDump = sp.createSqlDump(false, true);
    assertFalse(sqlPlainTextDump.contains("UNPARSED"));
  }

  public static class PersonFormData extends AbstractFormData {
    private static final long serialVersionUID = 1L;

    public long getPersonId() {
      return 1;
    }

    public Name getName() {
      return getFieldByClass(Name.class);
    }

    public Birthdate getBirthdate() {
      return getFieldByClass(Birthdate.class);
    }

    public AddressTable getAddressTable() {
      return getFieldByClass(AddressTable.class);
    }

    public class Name extends AbstractValueFieldData<String> {

      private static final long serialVersionUID = 1L;
    }

    public class Birthdate extends AbstractValueFieldData<Date> {

      private static final long serialVersionUID = 1L;
    }

    public class AddressTable extends AbstractTableFieldData {

      private static final long serialVersionUID = 1L;

      @Override
      public int getColumnCount() {
        return 2;
      }

      @Override
      public Object getValueAt(int row, int column) {
        switch (column) {
          case 0:
            return getAddressId(row);
          case 1:
            return getStreet(row);
          default:
            return null;
        }
      }

      @Override
      public void setValueAt(int row, int column, Object value) {
        switch (column) {
          case 0:
            setAddressId(row, (Long) value);
            break;
          case 1:
            setStreet(row, (String) value);
            break;
        }
      }

      public Long getAddressId(int row) {
        return (Long) getValueInternal(row, 0);
      }

      public void setAddressId(int row, Long addressType) {
        setValueInternal(row, 0, addressType);
      }

      public String getStreet(int row) {
        return (String) getValueInternal(row, 1);
      }

      public void setStreet(int row, String street) {
        setValueInternal(row, 1, street);
      }
    }
  }

}
