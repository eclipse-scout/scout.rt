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
package org.eclipse.scout.rt.server.services.common.jdbc.internal.exec;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Date;

import org.eclipse.scout.commons.holders.IntegerHolder;
import org.eclipse.scout.commons.holders.LongHolder;
import org.eclipse.scout.commons.holders.NVPair;
import org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

public class TestStatementProcessor {

  public static void main(String[] args) throws Exception {
    new TestStatementProcessor().testSelectLike();
    // new TestStatementProcessor().testFormData();
    // new TestStatementProcessor().testLookupCall();
    // new TestStatementProcessor().testStoredProc();
  }

  public void testLookupCall() throws Exception {
    LookupCall call = new LookupCall() {

      private static final long serialVersionUID = 1L;
    };
    //
    new StatementProcessor(
        new AbstractSqlService() {
        },
        "SELECT P.PERSON_NR,P.NAME" +
            "FROM PERSON P " +
            "WHERE P.PERSON_NR=:key " +
            "AND P.NAME like '%'||:text||'%'",
        new Object[]{call}).simulate();
  }

  public void testSelectLike() throws Exception {
    new StatementProcessor(
        new AbstractSqlService() {
        },
        "SELECT BP_NR FROM FLM_BP WHERE BP_NO LIKE :bpNo INTO :bpNr",
        new Object[]{new NVPair("bpNo", "12"), new NVPair("bpNr", new LongHolder())}).simulate();
  }

  public void testFormData() throws Exception {
    IntegerHolder countConcurrent = new IntegerHolder();
    PersonFormData formData = new PersonFormData();
    formData.getAddressTable().addRow();
    formData.getAddressTable().addRow();
    //
    new StatementProcessor(
        new AbstractSqlService() {
        },
        "SELECT COUNT(*) " +
            "FROM PERSON P " +
            "WHERE NVL(:birthdate,TO_DATE('1.1.3000','dd.mm.yyyy')) >= SYSDATE " +
            "AND :name like '%Me%' " +
            "AND :{addressTable.street} like '%Park%' " +
            "INTO :countConcurrent ",
        new Object[]{formData, new NVPair("countConcurrent", countConcurrent)}).simulate();
  }

  public void testStoredProc() throws Exception {
    DriverManager.registerDriver((Driver) Class.forName("oracle.jdbc.OracleDriver").newInstance());
    Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@db_bsi4.bsiag.local:1521:BSICRM", "ORSUSER", "ORSUSER");
    //
    IntegerHolder used = new IntegerHolder();
    //
    new StatementProcessor(
        new AbstractSqlService() {
        },
        "DECLARE " +
            "   v_used  PLS_INTEGER := 2; " +
            "BEGIN " +
            "   :[OUT]used := v_used; " +
            "END; ",
        new Object[]{new NVPair("used", used)}
    // ).simulate();
    ).processStoredProcedure(conn, new PreparedStatementCache(10), null);
    System.out.println("used=" + used.getValue());
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
