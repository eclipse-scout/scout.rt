/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.listbox;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.ListBoxTestForm.MainBox.FirstListBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.ListBoxTestForm.MainBox.SecondListBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * @since 5.2
 */
@ClassId("39a01dec-9a6b-420f-ae6c-eacd216a8c53")
public class ListBoxTestForm extends AbstractForm {

  public static final String FIRST_LIST_BOX_CLASS_ID = "5ce66cc6-c89e-4ec2-a177-2ab4324e0f95";
  public static final String SECOND_LIST_BOX_CLASS_ID = "5bf22280-6796-4947-8505-49d490b48f20";

  public FirstListBox getFirstListBox() {
    return getFieldByClass(FirstListBox.class);
  }

  public SecondListBox getSecondListBox() {
    return getFieldByClass(SecondListBox.class);
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  @ClassId("ba015995-f2b0-46c9-8c82-672fcddb24af")
  public class MainBox extends AbstractGroupBox {

    @Order(1000)
    @ClassId(FIRST_LIST_BOX_CLASS_ID)
    public class FirstListBox extends AbstractListBox<Long> {
    }

    @Order(2000)
    @ClassId(SECOND_LIST_BOX_CLASS_ID)
    public class SecondListBox extends AbstractListBox<Long> {
    }
  }
}
