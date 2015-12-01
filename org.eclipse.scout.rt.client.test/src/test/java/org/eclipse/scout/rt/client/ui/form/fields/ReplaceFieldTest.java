/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.ReplaceFieldTest.BaseForm.MainBox;
import org.eclipse.scout.rt.client.ui.form.fields.ReplaceFieldTest.BaseForm.MainBox.FirstField;
import org.eclipse.scout.rt.client.ui.form.fields.ReplaceFieldTest.BaseForm.MainBox.SecondField;
import org.eclipse.scout.rt.client.ui.form.fields.ReplaceFieldTest.TemplateUsageForm.MainBox.Template1Box;
import org.eclipse.scout.rt.client.ui.form.fields.ReplaceFieldTest.TemplateUsageForm.MainBox.Template2Box;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 4.0.1
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ReplaceFieldTest {

  @Test
  public void testGetFieldId() throws Exception {
    assertEquals("BaseField", new BaseField().getFieldId());
    assertEquals("BaseField", new ExtendedField().getFieldId());
    assertEquals("ExtendedFieldWithoutReplace", new ExtendedFieldWithoutReplace().getFieldId());
    //
    assertEquals("Custom", new TestFieldWithCustomFieldId().getFieldId());
    assertEquals("Custom", new ExtendedTestFieldWithCustomFieldId().getFieldId());
  }

  @Test
  public void testBaseForm() throws Exception {
    BaseForm form = new BaseForm();
    MainBox mainBox = form.getMainBox();
    assertNotNull(mainBox);
    assertNotNull(form.getFirstField());
    assertNotNull(form.getSecondField());
    assertSame(BaseForm.MainBox.FirstField.class, form.getFirstField().getClass());
    assertSame(BaseForm.MainBox.SecondField.class, form.getSecondField().getClass());
  }

  @Test
  public void testExtendedForm() throws Exception {
    ExtendedForm form = new ExtendedForm();
    MainBox mainBox = form.getMainBox();
    assertNotNull(mainBox);
    assertNotNull(form.getFirstField());
    assertNotNull(form.getSecondField());
    assertSame(ExtendedForm.FirstExField.class, form.getFirstField().getClass());
    assertSame(BaseForm.MainBox.SecondField.class, form.getSecondField().getClass());

    assertSame(form.getFirstField(), form.getFirstExField());
    assertEquals("FirstField", form.getFirstExField().getFieldId());
  }

  @Test
  public void testExtendedMainBoxAndFieldTopLevelForm() throws Exception {
    ExtendedMainBoxAndFieldTopLevelForm form = new ExtendedMainBoxAndFieldTopLevelForm();
    MainBox mainBox = form.getMainBox();
    assertNotNull(mainBox);
    assertNotNull(form.getFirstField());
    assertNotNull(form.getSecondField());
    assertSame(ExtendedMainBoxAndFieldTopLevelForm.FirstExField.class, form.getFirstField().getClass());
    assertSame(BaseForm.MainBox.SecondField.class, form.getSecondField().getClass());

    assertSame(form.getFirstField(), form.getFirstExField());
    assertEquals("FirstField", form.getFirstExField().getFieldId());

    assertSame(mainBox, form.getExMainBox());
    assertSame(ExtendedMainBoxAndFieldTopLevelForm.ExMainBox.class, mainBox.getClass());
  }

  @Test
  public void testExtendedMainBoxAndFieldNestedForm() throws Exception {
    ExtendedMainBoxAndFieldNestedForm form = new ExtendedMainBoxAndFieldNestedForm();
    MainBox mainBox = form.getMainBox();
    assertNotNull(mainBox);
    assertNotNull(form.getFirstField());
    assertNotNull(form.getSecondField());
    assertSame(ExtendedMainBoxAndFieldNestedForm.ExMainBox.FirstExField.class, form.getFirstField().getClass());
    assertSame(BaseForm.MainBox.SecondField.class, form.getSecondField().getClass());

    assertSame(form.getFirstField(), form.getFirstExField());
    assertEquals("FirstField", form.getFirstExField().getFieldId());

    assertSame(mainBox, form.getExMainBox());
    assertSame(ExtendedMainBoxAndFieldNestedForm.ExMainBox.class, mainBox.getClass());
  }

  @Test
  public void testExtendTemplateUsageForm() {
    TemplateReplaceForm form = new TemplateReplaceForm();
    TemplateUsageForm.MainBox mainBox = form.getMainBox();

    assertNotNull(mainBox);
    assertEquals(2, mainBox.getFieldCount());
    assertSame(form.getTemplate1Box(), mainBox.getFields().get(0));
    assertSame(form.getTemplate2Box(), mainBox.getFields().get(1));

    // check template 1 box
    assertNotNull(form.getTemplate1Box().getFirstTemplateField());
    assertNotNull(form.getTemplate1Box().getSecondTemplateField());
    assertSame(TemplateReplaceForm.Template1ReplacedBox.FirstTemplateExField.class, form.getTemplate1Box().getFirstTemplateField().getClass());
    assertSame(AbstractTemplateBox.SecondTemplateField.class, form.getTemplate1Box().getSecondTemplateField().getClass());

    // check template 2 box
    assertNotNull(form.getTemplate2Box().getFirstTemplateField());
    assertNotNull(form.getTemplate2Box().getSecondTemplateField());
    assertSame(AbstractTemplateBox.FirstTemplateField.class, form.getTemplate2Box().getFirstTemplateField().getClass());
    assertSame(AbstractTemplateBox.SecondTemplateField.class, form.getTemplate2Box().getSecondTemplateField().getClass());
  }

  @Test
  public void testNestedReplace() throws Exception {
    ExampleExForm form = new ExampleExForm();
    assertNotNull(form.getTextField());
    assertSame(ExampleExForm.DetailExBox.TextExField.class, form.getTextField().getClass());
  }

  public static class BaseField extends AbstractStringField {
  }

  @Replace
  public static class ExtendedField extends BaseField {
  }

  public static class ExtendedFieldWithoutReplace extends BaseField {
  }

  public static class TestFieldWithCustomFieldId extends AbstractStringField {
    @Override
    public String getFieldId() {
      return "Custom";
    }
  }

  @Replace
  public static class ExtendedTestFieldWithCustomFieldId extends TestFieldWithCustomFieldId {
  }

  public static class BaseForm extends AbstractForm {
    public BaseForm() {
    }

    public MainBox getMainBox() {
      return (MainBox) getRootGroupBox();
    }

    public FirstField getFirstField() {
      return getFieldByClass(FirstField.class);
    }

    public SecondField getSecondField() {
      return getFieldByClass(SecondField.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class FirstField extends AbstractStringField {
      }

      @Order(20)
      public class SecondField extends AbstractStringField {
      }
    }
  }

  public static class ExtendedForm extends BaseForm {
    public ExtendedForm() {
    }

    public FirstExField getFirstExField() {
      return getFieldByClass(FirstExField.class);
    }

    @Replace
    public class FirstExField extends MainBox.FirstField {
      public FirstExField(MainBox container) {
        container.super();
      }
    }
  }

  public static class ExtendedMainBoxAndFieldTopLevelForm extends BaseForm {
    public ExtendedMainBoxAndFieldTopLevelForm() {
    }

    public ExMainBox getExMainBox() {
      return (ExMainBox) getRootGroupBox();
    }

    public FirstExField getFirstExField() {
      return getFieldByClass(FirstExField.class);
    }

    public class ExMainBox extends MainBox {
      public ExMainBox() {
      }
    }

    @Replace
    public class FirstExField extends MainBox.FirstField {
      public FirstExField(MainBox container) {
        container.super();
      }
    }
  }

  public static class ExtendedMainBoxAndFieldNestedForm extends BaseForm {
    public ExtendedMainBoxAndFieldNestedForm() {
    }

    public ExMainBox getExMainBox() {
      return (ExMainBox) getRootGroupBox();
    }

    public ExMainBox.FirstExField getFirstExField() {
      return getFieldByClass(ExMainBox.FirstExField.class);
    }

    public class ExMainBox extends MainBox {
      public ExMainBox() {
      }

      @Replace
      public class FirstExField extends MainBox.FirstField {
        public FirstExField(MainBox container) {
          container.super();
        }
      }
    }
  }

  public abstract static class AbstractTemplateBox extends AbstractGroupBox {

    public FirstTemplateField getFirstTemplateField() {
      return getFieldByClass(FirstTemplateField.class);
    }

    public SecondTemplateField getSecondTemplateField() {
      return getFieldByClass(SecondTemplateField.class);
    }

    @Order(10)
    public class FirstTemplateField extends AbstractStringField {
    }

    @Order(20)
    public class SecondTemplateField extends AbstractStringField {
    }
  }

  public static class TemplateUsageForm extends AbstractForm {

    public TemplateUsageForm() {
    }

    public MainBox getMainBox() {
      return (MainBox) getRootGroupBox();
    }

    public Template1Box getTemplate1Box() {
      return getFieldByClass(Template1Box.class);
    }

    public Template2Box getTemplate2Box() {
      return getFieldByClass(Template2Box.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class Template1Box extends AbstractTemplateBox {
      }

      @Order(20)
      public class Template2Box extends AbstractTemplateBox {
      }
    }
  }

  public static class TemplateReplaceForm extends TemplateUsageForm {

    public TemplateReplaceForm() {
    }

    @Replace
    public class Template1ReplacedBox extends MainBox.Template1Box {

      public Template1ReplacedBox(MainBox container) {
        container.super();
      }

      @Replace
      public class FirstTemplateExField extends FirstTemplateField {
      }
    }
  }

  public static class ExampleForm extends AbstractForm {

    public ExampleForm() {
      super();
    }

    public MainBox.DetailBox.TextField getTextField() {
      return getFieldByClass(MainBox.DetailBox.TextField.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {
      @Order(10)
      public class DetailBox extends AbstractGroupBox {
        @Order(10)
        public class TextField extends AbstractStringField {
        }
      }
    }
  }

  public static class ExampleExForm extends ExampleForm {
    public ExampleExForm() {
      super();
    }

    @Replace
    public class DetailExBox extends MainBox.DetailBox {
      public DetailExBox(ExampleForm.MainBox container) {
        container.super();
      }

      @Replace
      public class TextExField extends TextField {
      }
    }
  }
}
