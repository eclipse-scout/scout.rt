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
package org.eclipse.scout.rt.server.jdbc.fixture;

import java.util.Set;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

public class FormDataWithSet extends AbstractFormData {

  private static final long serialVersionUID = 1L;

  public FormDataWithSet() {
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

  public static class Roles extends AbstractValueFieldData<Set<Long>> {

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
