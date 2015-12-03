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
package org.eclipse.scout.rt.shared.data.form;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.AbstractComposerData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Bug 349468: Cloning of a form data does not replace the inner synthetic member see {@link AbstractFormData}
 */
@RunWith(PlatformTestRunner.class)
public class FormDataCloneTest {

  private int m_outCount;
  private int m_inCount;

  @Test
  @SuppressWarnings("resource")
  public void test() throws Exception {
    CompanySearchFormData d = new CompanySearchFormData();
    d.getActive().setValue(true);
    d.getCurrency().setValue(1000654L);
    d.getCity().setValue("berne");
    d.getPersonIdProperty().setValue(1234L);
    d = (CompanySearchFormData) d.clone();
    //
    m_outCount = 0;
    m_inCount = 0;
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    new OutVisitor(b).writeObject(d);
    new InVisitor(new ByteArrayInputStream(b.toByteArray())).readObject();
    assertEquals(1, m_outCount);
    assertEquals(1, m_inCount);
  }

  private class OutVisitor extends ObjectOutputStream {
    public OutVisitor(OutputStream out) throws IOException {
      super(out);
      enableReplaceObject(true);
    }

    @Override
    protected Object replaceObject(Object obj) throws IOException {
      if (obj instanceof AbstractFormData) {
        m_outCount++;
      }
      return obj;
    }
  }

  private class InVisitor extends ObjectInputStream {
    public InVisitor(InputStream in) throws IOException {
      super(in);
      enableResolveObject(true);
    }

    @Override
    protected Object resolveObject(Object obj) throws IOException {
      if (obj instanceof AbstractFormData) {
        m_inCount++;
      }
      return obj;
    }
  }

  public static class CompanySearchFormData extends AbstractFormData {
    private static final long serialVersionUID = 1L;

    public CompanySearchFormData() {
    }

    public PersonIdProperty getPersonIdProperty() {
      return getPropertyByClass(PersonIdProperty.class);
    }

    /**
     * access method for property PersonId.
     */
    public Long getPersonId() {
      return getPersonIdProperty().getValue();
    }

    /**
     * access method for property PersonId.
     */
    public void setPersonId(Long personId) {
      getPersonIdProperty().setValue(personId);
    }

    public Active getActive() {
      return getFieldByClass(Active.class);
    }

    public City getCity() {
      return getFieldByClass(City.class);
    }

    public Currency getCurrency() {
      return getFieldByClass(Currency.class);
    }

    public Composer getComposer() {
      return getFieldByClass(Composer.class);
    }

    public AddressTable getAddressTable() {
      return getFieldByClass(AddressTable.class);
    }

    public class PersonIdProperty extends AbstractPropertyData<Long> {
      private static final long serialVersionUID = 1L;

      public PersonIdProperty() {
      }
    }

    public class Active extends AbstractValueFieldData<Boolean> {
      private static final long serialVersionUID = 1L;

      public Active() {
      }
    }

    public class City extends AbstractValueFieldData<String> {
      private static final long serialVersionUID = 1L;

      public City() {
      }

    }

    public class Currency extends AbstractValueFieldData<Long> {
      private static final long serialVersionUID = 1L;

      public Currency() {
      }
    }

    public class Composer extends AbstractComposerData {
      private static final long serialVersionUID = 1L;

      public Composer() {
      }
    }

    public class AddressTable extends AbstractTableFieldData {
      private static final long serialVersionUID = 1L;

      public AddressTable() {
      }

      public void setStreet(int row, String street) {
        setValueInternal(row, 0, street);
      }

      public String getStreet(int row) {
        return (String) getValueInternal(row, 0);
      }

      public void setPoBox(int row, String poBox) {
        setValueInternal(row, 1, poBox);
      }

      public String getPoBox(int row) {
        return (String) getValueInternal(row, 1);
      }

      public void setCity(int row, Long city) {
        setValueInternal(row, 2, city);
      }

      public Long getCity(int row) {
        return (Long) getValueInternal(row, 2);
      }

      @Override
      public int getColumnCount() {
        return 3;
      }

      @Override
      public Object getValueAt(int row, int column) {
        switch (column) {
          case 0:
            return getStreet(row);
          case 1:
            return getPoBox(row);
          case 2:
            return getCity(row);
          default:
            return null;
        }
      }

      @Override
      public void setValueAt(int row, int column, Object value) {
        switch (column) {
          case 0:
            setStreet(row, (String) value);
            break;
          case 1:
            setPoBox(row, (String) value);
            break;
          case 2:
            setCity(row, (Long) value);
            break;
        }
      }
    }

  }

}
