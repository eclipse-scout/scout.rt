/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.form.fixtures;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormWithOneField.MainBox.StringField;

public class FormWithOneField extends AbstractForm {

  public FormWithOneField() {
    super();
  }

  @Order(10)
  public class MainBox extends AbstractGroupBox {
    @Order(10)
    public class StringField extends AbstractStringField {

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
      super.execPostLoad();
      getFieldByClass(StringField.class).requestFocus();
    }
  }

}
