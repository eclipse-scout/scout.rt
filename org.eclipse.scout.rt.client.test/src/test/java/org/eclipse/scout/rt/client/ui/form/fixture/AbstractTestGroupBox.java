/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fixture;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.data.form.fixture.AbstractTestGroupBoxData;

/**
 * Group Box with 2 text fields
 */
@FormData(value = AbstractTestGroupBoxData.class, sdkCommand = SdkCommand.CREATE, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE)
public abstract class AbstractTestGroupBox extends AbstractGroupBox {

  public Text1Field getText1Field() {
    return getFieldByClass(Text1Field.class);
  }

  public Text2Field getText2Field() {
    return getFieldByClass(Text2Field.class);
  }

  public TestListBox getTestListBox() {
    return getFieldByClass(TestListBox.class);
  }

  public InnerTestGroupBox getInnerTestGroupBox() {
    return getFieldByClass(InnerTestGroupBox.class);
  }

  @ClassId("1b67f4c4-1579-4875-a2c9-d62c7cd63508")
  @Order(10)
  public class Text1Field extends AbstractStringField {
  }

  @Order(20)
  public class Text2Field extends AbstractStringField {
  }

  @Order(30)
  public class TestListBox extends AbstractListBox<String> {
  }

  @Order(40)
  public class InnerTestGroupBox extends AbstractInnerTestGroupBox {

  }

  protected abstract static class AbstractInnerTestGroupBox extends AbstractGroupBox {

    public InnerText1Field getInnerText1Field() {
      return getFieldByClass(InnerText1Field.class);
    }

    @Order(10)
    public class InnerText1Field extends AbstractStringField {

    }
  }
}
