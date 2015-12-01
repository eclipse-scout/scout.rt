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
package org.eclipse.scout.rt.shared.data.form;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 3.8.2
 */
@RunWith(PlatformTestRunner.class)
public class FormFieldDataTest {

  @Test
  public void testGetFieldId() {
    assertEquals("BaseFormFieldData", new BaseFormFieldData().getFieldId());
    assertEquals("BaseFormFieldData", new ExtendedFormFieldData().getFieldId());
    assertEquals("ExtendedFormFieldDataWithoutReplace", new ExtendedFormFieldDataWithoutReplace().getFieldId());
    //
    assertEquals("Custom", new BaseFormFieldDataWithCustomId().getFieldId());
    assertEquals("Custom", new ExtendedFormFieldDataWithCustomId().getFieldId());
  }

  public static class BaseFormFieldData extends AbstractFormFieldData {
    private static final long serialVersionUID = 1L;
  }

  @Replace
  public static class ExtendedFormFieldData extends BaseFormFieldData {
    private static final long serialVersionUID = 1L;
  }

  public static class ExtendedFormFieldDataWithoutReplace extends BaseFormFieldData {
    private static final long serialVersionUID = 1L;
  }

  public static class BaseFormFieldDataWithCustomId extends AbstractFormFieldData {
    private static final long serialVersionUID = 1L;

    @Override
    public String getFieldId() {
      return "Custom";
    }
  }

  @Replace
  public static class ExtendedFormFieldDataWithCustomId extends BaseFormFieldDataWithCustomId {
    private static final long serialVersionUID = 1L;
  }
}
