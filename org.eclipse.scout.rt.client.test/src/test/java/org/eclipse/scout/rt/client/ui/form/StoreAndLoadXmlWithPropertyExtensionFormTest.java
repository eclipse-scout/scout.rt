package org.eclipse.scout.rt.client.ui.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.StoreAndLoadXmlWithPropertyExtensionFormTest.TestForm.MainBox.GroupBox;
import org.eclipse.scout.rt.client.ui.form.StoreAndLoadXmlWithPropertyExtensionFormTest.TestForm.MainBox.GroupBox.TextField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests loading and storing to xml of forms with extensions.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class StoreAndLoadXmlWithPropertyExtensionFormTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testOneExtensionPropertyOnForm() {
    BEANS.get(IExtensionRegistry.class).register(TestFormExtension.class);

    String xml;
    {
      // create form and test setup
      TestForm form = new TestForm();
      TestFormExtension formExtension = form.getExtension(TestFormExtension.class);
      assertEquals(2, form.getAllExtensions().size());
      assertNotNull(formExtension);

      form.setProperty("staticPropertyValue");
      formExtension.setExtensionProperty("extensionPropertyValue");

      form.getTextField().setValue("staticFieldValue");
      form.getTextField().setTextFieldProperty("staticTextFieldProperty");

      xml = form.storeToXmlString();
    }

    assertTrue(xml.contains("name=\"property\""));
    assertTrue(xml.contains("name=\"extensionProperty\""));
    assertTrue(xml.contains("fieldId=\"MainBox\""));
    assertTrue(xml.contains("fieldId=\"GroupBox\""));
    assertTrue(xml.contains("fieldId=\"TextField\""));
    assertFalse(xml.contains("\"textFieldProperty\""));

    TestForm loadedForm = new TestForm();
    loadedForm.loadFromXmlString(xml);
    TestFormExtension loadedFormExtension = loadedForm.getExtension(TestFormExtension.class);

    assertEquals("staticPropertyValue", loadedForm.getProperty());
    assertEquals("extensionPropertyValue", loadedFormExtension.getExtensionProperty());

    assertEquals("staticFieldValue", loadedForm.getTextField().getValue());
    assertNull(loadedForm.getTextField().getTextFieldProperty()); // field properties are not stored in XML
  }

  @Test
  public void testNonExistingExtensionPropertyOnForm() {
    BEANS.get(IExtensionRegistry.class).register(TestFormExtension.class);

    String xml;
    {
      // create form and test setup
      TestForm form = new TestForm();
      TestFormExtension formExtension = form.getExtension(TestFormExtension.class);
      assertEquals(2, form.getAllExtensions().size());
      assertNotNull(formExtension);

      form.setProperty("staticPropertyValue");
      formExtension.setExtensionProperty("extensionPropertyValue");

      form.getTextField().setValue("staticFieldValue");
      form.getTextField().setTextFieldProperty("staticTextFieldProperty");

      xml = form.storeToXmlString();
    }

    xml = xml.replace(TestFormExtension.class.getSimpleName(), "NonExisting" + TestFormExtension.class.getSimpleName());

    assertTrue(xml.contains("name=\"property\""));
    assertTrue(xml.contains("name=\"extensionProperty\""));
    assertTrue(xml.contains("fieldId=\"MainBox\""));
    assertTrue(xml.contains("fieldId=\"GroupBox\""));
    assertTrue(xml.contains("fieldId=\"TextField\""));
    assertFalse(xml.contains("\"textFieldProperty\""));

    TestForm loadedForm = new TestForm();
    loadedForm.loadFromXmlString(xml);
    TestFormExtension loadedFormExtension = loadedForm.getExtension(TestFormExtension.class);

    assertEquals("staticPropertyValue", loadedForm.getProperty());
    assertNull(loadedFormExtension.getExtensionProperty());

    assertEquals("staticFieldValue", loadedForm.getTextField().getValue());
    assertNull(loadedForm.getTextField().getTextFieldProperty()); // field properties are not stored in XML
  }

  @Test
  public void testTwoExtensionPropertyOnForm() {
    BEANS.get(IExtensionRegistry.class).register(TestFormExtension.class);
    BEANS.get(IExtensionRegistry.class).register(OtherTestFormExtension.class);

    String xml;
    {
      // create form and test setup
      TestForm form = new TestForm();
      TestFormExtension formExtension = form.getExtension(TestFormExtension.class);
      OtherTestFormExtension otherFormExtension = form.getExtension(OtherTestFormExtension.class);
      assertEquals(3, form.getAllExtensions().size());
      assertNotNull(formExtension);
      assertNotNull(otherFormExtension);

      form.setProperty("staticPropertyValue");
      formExtension.setExtensionProperty("extensionPropertyValue");
      otherFormExtension.setExtensionProperty("otherExtensionPropertyValue");

      form.getTextField().setValue("staticFieldValue");
      form.getTextField().setTextFieldProperty("staticTextFieldProperty");

      xml = form.storeToXmlString();
    }

    assertTrue(xml.contains("name=\"property\""));
    assertTrue(xml.contains("name=\"extensionProperty\""));
    assertTrue(xml.contains("fieldId=\"MainBox\""));
    assertTrue(xml.contains("fieldId=\"GroupBox\""));
    assertTrue(xml.contains("fieldId=\"TextField\""));
    assertFalse(xml.contains("\"textFieldProperty\"")); // field properties are not stored in XML

    TestForm loadedForm = new TestForm();
    loadedForm.loadFromXmlString(xml);

    TestFormExtension loadedFormExtension = loadedForm.getExtension(TestFormExtension.class);
    OtherTestFormExtension loadedOtherFormExtension = loadedForm.getExtension(OtherTestFormExtension.class);

    assertEquals("staticPropertyValue", loadedForm.getProperty());
    assertEquals("extensionPropertyValue", loadedFormExtension.getExtensionProperty());
    assertEquals("otherExtensionPropertyValue", loadedOtherFormExtension.getExtensionProperty());

    assertEquals("staticFieldValue", loadedForm.getTextField().getValue());
    assertNull(loadedForm.getTextField().getTextFieldProperty()); // field properties are not stored in XML
  }

  @Test
  public void testExtensionPropertyOnFormField() {
    BEANS.get(IExtensionRegistry.class).register(TestFormFieldExtension.class, TestForm.MainBox.GroupBox.TextField.class);

    String xml;
    {
      // create form and test setup
      TestForm form = new TestForm();
      assertEquals(1, form.getAllExtensions().size());
      assertEquals(1, form.getMainBox().getAllExtensions().size());
      assertEquals(1, form.getGroupBox().getAllExtensions().size());
      assertEquals(2, form.getTextField().getAllExtensions().size());

      TestFormFieldExtension fieldExtension = form.getTextField().getExtension(TestFormFieldExtension.class);
      assertNotNull(fieldExtension);

      form.setProperty("staticPropertyValue");

      form.getTextField().setValue("staticFieldValue");
      fieldExtension.setExtensionProperty("extensionPropertyValue");

      xml = form.storeToXmlString();
    }

    assertTrue(xml.contains("name=\"property\""));
    assertTrue(xml.contains("fieldId=\"MainBox\""));
    assertTrue(xml.contains("fieldId=\"GroupBox\""));
    assertTrue(xml.contains("fieldId=\"TextField\""));
    assertFalse(xml.contains("\"textFieldProperty\"")); // field properties are not stored in XML
    assertFalse(xml.contains("name=\"extensionProperty\"")); // field extension properties are not stored in XML

    TestForm loadedForm = new TestForm();
    loadedForm.loadFromXmlString(xml);

    TestFormFieldExtension loadedFieldExtension = loadedForm.getTextField().getExtension(TestFormFieldExtension.class);

    assertEquals("staticPropertyValue", loadedForm.getProperty());

    assertEquals("staticFieldValue", loadedForm.getTextField().getValue());
    assertNull(loadedForm.getTextField().getTextFieldProperty()); // field properties are not stored in XML
    assertNull(loadedFieldExtension.getExtensionProperty()); // field extension properties are not stored in XML
  }

  public static final class TestForm extends AbstractForm {

    private String m_property;

    public String getProperty() {
      return m_property;
    }

    public void setProperty(String property) {
      m_property = property;
    }

    public TextField getTextField() {
      return getFieldByClass(TextField.class);
    }

    public MainBox getMainBox() {
      return (MainBox) getRootGroupBox();
    }

    public GroupBox getGroupBox() {
      return getFieldByClass(GroupBox.class);
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {

      @Order(50.0)
      public class GroupBox extends AbstractGroupBox {

        @Order(10.0)
        public class TextField extends AbstractStringField {

          private String m_textFieldProperty;

          public String getTextFieldProperty() {
            return m_textFieldProperty;
          }

          public void setTextFieldProperty(String textFieldProperty) {
            m_textFieldProperty = textFieldProperty;
          }
        }
      }
    }
  }

  public abstract static class AbstractTestFormExtension extends AbstractFormExtension<TestForm> {

    public AbstractTestFormExtension(TestForm owner) {
      super(owner);
    }

    private String m_extensionProperty;

    public String getExtensionProperty() {
      return m_extensionProperty;
    }

    public void setExtensionProperty(String extensionProperty) {
      m_extensionProperty = extensionProperty;
    }
  }

  public static class TestFormExtension extends AbstractTestFormExtension {

    public TestFormExtension(TestForm owner) {
      super(owner);
    }
  }

  public static class OtherTestFormExtension extends AbstractTestFormExtension {

    public OtherTestFormExtension(TestForm owner) {
      super(owner);
    }
  }

  public static class TestFormFieldExtension extends AbstractFormFieldExtension<AbstractFormField> {

    public TestFormFieldExtension(AbstractFormField owner) {
      super(owner);
    }

    private String m_extensionProperty;

    public String getExtensionProperty() {
      return m_extensionProperty;
    }

    public void setExtensionProperty(String extensionProperty) {
      m_extensionProperty = extensionProperty;
    }
  }
}
