/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.form.fixtures;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormWithOneField.MainBox.StringField;

@ClassId("ac06f4ad-7182-4976-9f75-2be0d3ae7fcf")
public class FormWithOneField extends AbstractForm {

  public FormWithOneField() {
    super();
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public StringField getStringField() {
    return getFieldByClass(StringField.class);
  }

  @Override
  public IDesktop getDesktop() {
    return super.getDesktop();
  }

  @Order(10)
  @ClassId("87629b9c-3418-4fbe-80e6-a6edf93c2bc5")
  public class MainBox extends AbstractGroupBox {

    @Order(10)
    @ClassId("44537cc8-250d-4f81-96c8-bf9aaa96b080")
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
      // nop
    }

    @Override
    protected void execPostLoad() {
      getFieldByClass(StringField.class).requestFocus();
    }
  }
}
