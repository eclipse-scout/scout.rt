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
package org.eclipse.scout.rt.shared.data.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 3.8.2
 */
@RunWith(PlatformTestRunner.class)
public class FormDataInjectionTest {

  @Test
  public void testBaseForm() throws Exception {
    BaseFormData formData = new BaseFormData();

    assertEquals(2, formData.getFields().length);
    assertNotNull(formData.getName());
    assertNotNull(formData.getSecond());
  }

  @Test
  public void testExtendedForm() throws Exception {
    ExtendedFormData formData = new ExtendedFormData();

    assertEquals(4, formData.getFields().length);
    assertNotNull(formData.getName());
    assertNotNull(formData.getSalary());
    assertNotNull(formData.getSecond());
    assertNotNull(formData.getAge());
  }

  @Test
  public void testExtendedInjectedField() throws Exception {
    ExtendedInjectedFieldFormData formData = new ExtendedInjectedFieldFormData();

    assertEquals(5, formData.getFields().length);
    assertNotNull(formData.getName());
    assertNotNull(formData.getSalary());
    assertNotNull(formData.getSecond());
    assertNotNull(formData.getFirstName());
    assertNotNull(formData.getAge());
  }

  @Test
  public void testReplaceField() throws Exception {
    ReplaceFieldFormData formData = new ReplaceFieldFormData();

    assertEquals(2, formData.getFields().length);
    assertNotNull(formData.getName());
    assertNotNull(formData.getSecond());
    // getFieldByClass
    assertSame(formData.getNameEx(), formData.getName());

    // fieldId
    assertSame(formData.getNameEx(), formData.getFieldById("Name"));
    assertEquals("Name", formData.getName().getFieldId());
    assertEquals("Name", formData.getNameEx().getFieldId());
  }

  @Test
  public void testReplace2Field() throws Exception {
    Replace2FieldFormData formData = new Replace2FieldFormData();

    assertEquals(3, formData.getFields().length);
    assertNotNull(formData.getName());
    assertNotNull(formData.getSecond());
    assertNotNull(formData.getInjected());

    // getFieldByClass
    assertSame(formData.getNameEx2(), formData.getName());
    assertSame(formData.getNameEx2(), formData.getNameEx());

    // fieldId
    assertSame(formData.getNameEx2(), formData.getFieldById("Name"));
    assertEquals("Name", formData.getName().getFieldId());
    assertEquals("Name", formData.getNameEx().getFieldId());
    assertEquals("Name", formData.getNameEx2().getFieldId());
  }

  @Test
  public void testReplace3Field() throws Exception {
    Replace3FieldFormData formData = new Replace3FieldFormData();

    assertEquals(3, formData.getFields().length);
    assertNotNull(formData.getName());
    assertNotNull(formData.getSecond());
    assertNotNull(formData.getInjected());

    // getFieldByClass
    assertSame(formData.getNameEx3(), formData.getName());
    assertSame(formData.getNameEx3(), formData.getNameEx());
    assertSame(formData.getNameEx3(), formData.getNameEx2());

    // field Id
    assertSame(formData.getNameEx3(), formData.getFieldById("Name"));
    assertEquals("Name", formData.getName().getFieldId());
    assertEquals("Name", formData.getNameEx().getFieldId());
    assertEquals("Name", formData.getNameEx2().getFieldId());
  }

  @Test
  public void testGroupBoxEx() throws Exception {
    TemplateExFormData formData = new TemplateExFormData();

    assertEquals(1, formData.getFields().length);
    assertNotNull(formData.getBox());
    assertSame(formData.getBoxEx(), formData.getBox());
    assertSame(formData.getBoxEx(), formData.getFieldById("Box"));

    assertEquals(1, formData.getBox().getFields().length);
    assertSame(formData.getBoxEx().getNameEx(), formData.getBox().getName());
    assertSame(formData.getBoxEx().getNameEx(), formData.getBox().getFieldById("Name"));
  }

  public static class BaseFormData extends AbstractFormData {
    private static final long serialVersionUID = 1L;

    public BaseFormData() {
    }

    public Name getName() {
      return getFieldByClass(Name.class);
    }

    public Second getSecond() {
      return getFieldByClass(Second.class);
    }

    public static class Name extends AbstractValueFieldData<String> {
      private static final long serialVersionUID = 1L;

      public Name() {
      }
    }

    public static class Second extends AbstractValueFieldData<String> {
      private static final long serialVersionUID = 1L;

      public Second() {
      }
    }
  }

  public static class ExtendedFormData extends BaseFormData {
    private static final long serialVersionUID = 1L;

    public ExtendedFormData() {
    }

    public Salary getSalary() {
      return getFieldByClass(Salary.class);
    }

    public Age getAge() {
      return getFieldByClass(Age.class);
    }

    public static class Salary extends AbstractValueFieldData<Double> {
      private static final long serialVersionUID = 1L;

      public Salary() {
      }
    }

    public static class Age extends AbstractValueFieldData<Integer> {
      private static final long serialVersionUID = 1L;

      public Age() {
        super();
      }
    }
  }

  public static class ExtendedInjectedFieldFormData extends ExtendedFormData {
    private static final long serialVersionUID = 1L;

    public ExtendedInjectedFieldFormData() {
    }

    public FirstName getFirstName() {
      return getFieldByClass(FirstName.class);
    }

    public static class FirstName extends AbstractValueFieldData<String> {
      private static final long serialVersionUID = 1L;

      public FirstName() {
        super();
      }
    }
  }

  public static class ReplaceFieldFormData extends BaseFormData {
    private static final long serialVersionUID = 1L;

    public ReplaceFieldFormData() {
    }

    public NameEx getNameEx() {
      return getFieldByClass(NameEx.class);
    }

    @Replace
    public static class NameEx extends BaseFormData.Name {
      private static final long serialVersionUID = 1L;

      public NameEx() {
      }
    }
  }

  public static class Replace2FieldFormData extends ReplaceFieldFormData {
    private static final long serialVersionUID = 1L;

    public Replace2FieldFormData() {
    }

    public NameEx2 getNameEx2() {
      return getFieldByClass(NameEx2.class);
    }

    public Injected getInjected() {
      return getFieldByClass(Injected.class);
    }

    @Replace
    public static class NameEx2 extends NameEx {
      private static final long serialVersionUID = 1L;

      public NameEx2() {
      }
    }

    public static class Injected extends AbstractValueFieldData<String> {
      private static final long serialVersionUID = 1L;

      public Injected() {
      }
    }
  }

  public static class Replace3FieldFormData extends Replace2FieldFormData {
    private static final long serialVersionUID = 1L;

    public Replace3FieldFormData() {
    }

    public NameEx3 getNameEx3() {
      return getFieldByClass(NameEx3.class);
    }

    public InjectedEx getInjectedEx() {
      return getFieldByClass(InjectedEx.class);
    }

    @Replace
    public static class NameEx3 extends NameEx2 {
      private static final long serialVersionUID = 1L;

      public NameEx3() {
      }
    }

    @Replace
    public static class InjectedEx extends Replace2FieldFormData.Injected {
      private static final long serialVersionUID = 1L;

      public InjectedEx() {
        super();
      }
    }
  }

  public static class TemplateFormData extends AbstractFormData {
    private static final long serialVersionUID = 1L;

    public TemplateFormData() {
    }

    public Box getBox() {
      return getFieldByClass(Box.class);
    }

    public static class Box extends AbstractBoxData {
      private static final long serialVersionUID = 1L;

    }
  }

  public static class AbstractBoxData extends AbstractFormFieldData {
    private static final long serialVersionUID = 1L;

    public Name getName() {
      return getFieldByClass(Name.class);
    }

    public static class Name extends AbstractValueFieldData<String> {
      private static final long serialVersionUID = 1L;

    }
  }

  public static class TemplateExFormData extends TemplateFormData {
    private static final long serialVersionUID = 1L;

    public TemplateExFormData() {
    }

    public BoxEx getBoxEx() {
      return getFieldByClass(BoxEx.class);
    }

    @Replace
    public static class BoxEx extends Box {
      private static final long serialVersionUID = 1L;

      public NameEx getNameEx() {
        return getFieldByClass(NameEx.class);
      }

      @Replace
      public static class NameEx extends AbstractBoxData.Name {
        private static final long serialVersionUID = 1L;

      }
    }
  }
}
