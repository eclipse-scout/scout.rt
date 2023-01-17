/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.form;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class FormDataTest {

  @Test
  public void testGetNestedFieldAndPropertyPackagePrivateClass() {
    TestFormData formData = new TestFormData();
    // package private class NonPublicBoxData is not a valid field data
    assertNull(formData.getNonPublicBoxData());
  }

  @Test
  public void testGetNestedFieldAndPropertyPublicClass() {
    TestFormData formData = new TestFormData();
    // public class PublicBoxData is a valid field Data
    assertNotNull(formData.getPublicBoxData());
    assertNotNull(formData.getPublicBoxData().getTestProperty());
  }

  public abstract static class AbstractTestBoxData extends AbstractFormFieldData {
    private static final long serialVersionUID = 1L;

    public AbstractTestBoxData() {
    }

    public TestProperty getTestProperty() {
      return getPropertyByClass(TestProperty.class);
    }

    public class TestProperty extends AbstractPropertyData<Long> {
      private static final long serialVersionUID = 1L;

      public TestProperty() {
      }
    }
  }

  public static class TestFormData extends AbstractFormData {
    private static final long serialVersionUID = 1L;

    public TestFormData() {
    }

    public NonPublicBoxData getNonPublicBoxData() {
      return getFieldByClass(NonPublicBoxData.class);
    }

    public PublicBoxData getPublicBoxData() {
      return getFieldByClass(PublicBoxData.class);
    }

    final class NonPublicBoxData extends AbstractTestBoxData {
      private static final long serialVersionUID = 1L;

      public NonPublicBoxData() {
      }
    }

    public class PublicBoxData extends AbstractTestBoxData {
      private static final long serialVersionUID = 1L;

      public PublicBoxData() {
      }
    }
  }
}
