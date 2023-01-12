/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.form.fixture;

import java.util.Set;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "org.eclipse.scout.rt.client.ui.form.fixture.AbstractTestGroupBox", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public abstract class AbstractTestGroupBoxData extends AbstractFormFieldData {
  private static final long serialVersionUID = 1L;

  public TestListBox getTestListBox() {
    return getFieldByClass(TestListBox.class);
  }

  public Text1 getText1() {
    return getFieldByClass(Text1.class);
  }

  public Text2 getText2() {
    return getFieldByClass(Text2.class);
  }

  public static class TestListBox extends AbstractValueFieldData<Set<String>> {
    private static final long serialVersionUID = 1L;
  }

  @ClassId("1b67f4c4-1579-4875-a2c9-d62c7cd63508-formdata")
  public static class Text1 extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;
  }

  public static class Text2 extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;
  }
}
