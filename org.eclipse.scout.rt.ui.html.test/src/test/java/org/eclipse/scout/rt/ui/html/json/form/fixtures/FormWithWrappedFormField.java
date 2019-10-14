/*
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.form.fixtures;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormWithWrappedFormField.MainBox.WrappedFormField;

@ClassId("c6bb0928-db05-4a08-8c37-bc3fac723192")
public class FormWithWrappedFormField extends AbstractForm {

  public FormWithWrappedFormField() {
    super();
  }

  @Order(10)
  @ClassId("09a58fc6-78c2-4ced-ac69-6d431e1c4a1c")
  public class MainBox extends AbstractGroupBox {

    @Order(10)
    @ClassId("86975773-2ca7-4550-92bb-cb07cc6081c9")
    public class WrappedFormField extends AbstractWrappedFormField<IForm> {
    }
  }

  @Override
  public void start() {
    startInternal(new FormHandler());
  }

  public class FormHandler extends AbstractFormHandler {
    @Override
    protected void execLoad() {
    }

    @Override
    protected void execPostLoad() {
      FormWithOneField f = new FormWithOneField();
      getFieldByClass(WrappedFormField.class).setInnerForm(f, true);
      getFieldByClass(WrappedFormField.class).setInnerForm(null, true);
    }
  }
}
