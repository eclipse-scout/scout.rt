/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jdbc.fixture;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

public class FormDataWithArray extends AbstractFormData {

  private static final long serialVersionUID = 1L;

  public FormDataWithArray() {
  }

  public PersonNr getPersonNr() {
    return getFieldByClass(PersonNr.class);
  }

  public Roles getRoles() {
    return getFieldByClass(Roles.class);
  }

  public Value getValue() {
    return getFieldByClass(Value.class);
  }

  public static class PersonNr extends AbstractValueFieldData<Long> {

    private static final long serialVersionUID = 1L;

    public PersonNr() {
    }
  }

  public static class Roles extends AbstractValueFieldData<Long[]> {

    private static final long serialVersionUID = 1L;

    public Roles() {
    }
  }

  public static class Value extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;

    public Value() {
    }
  }
}
