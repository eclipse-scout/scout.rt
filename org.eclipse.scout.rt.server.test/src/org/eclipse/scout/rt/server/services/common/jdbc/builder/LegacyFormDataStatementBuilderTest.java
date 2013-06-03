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
package org.eclipse.scout.rt.server.services.common.jdbc.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.scout.commons.ClassIdentifier;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.services.common.jdbc.style.OracleSqlStyle;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the {@link FormDataStatementBuilder} with externally defined template data.
 */
@SuppressWarnings("deprecation")
public class LegacyFormDataStatementBuilderTest {

  private static final String TEXT = "text";
  private static final String PROPERTY = "property";
  private static final String TEMPLATE1_TEXT = "template 1 text";
  private static final String TEMPLATE1_PROPERTY = "template 1 property";
  private static final String MASTER_TEMPLATE1_TEXT = "master template 1 text";
  private static final String MASTER_TEMPLATE1_PROPERTY = "master template 1 property";
  private static final String TEMPLATE2_TEXT = "template 2 text";
  private static final String TEMPLATE2_PROPERTY = "template 2 property";
  private static final String MASTER_TEMPLATE2_TEXT = "master template 2 text";
  private static final String MASTER_TEMPLATE2_PROPERTY = "master template 2 property";

  private FormData m_formData;
  private FormDataStatementBuilder m_builder;

  @Before
  public void setup() {
    m_formData = new FormData();
    m_formData.getName().setValue(TEXT);
    m_formData.setDirectPropProperty(PROPERTY);
    m_formData.getTemplate1GroupBox().getText().setValue(TEMPLATE1_TEXT);
    m_formData.getTemplate1GroupBox().setTemplatePropProperty(TEMPLATE1_PROPERTY);
    m_formData.getTemplate1GroupBox().getMasterTemplateGroupBox().getMasterTemplateText().setValue(MASTER_TEMPLATE1_TEXT);
    m_formData.getTemplate1GroupBox().getMasterTemplateGroupBox().setMasterTemplatePropProperty(MASTER_TEMPLATE1_PROPERTY);
    m_formData.getTemplate2GroupBox().getText().setValue(TEMPLATE2_TEXT);
    m_formData.getTemplate2GroupBox().setTemplatePropProperty(TEMPLATE2_PROPERTY);
    m_formData.getTemplate2GroupBox().getMasterTemplateGroupBox().getMasterTemplateText().setValue(MASTER_TEMPLATE2_TEXT);
    m_formData.getTemplate2GroupBox().getMasterTemplateGroupBox().setMasterTemplatePropProperty(MASTER_TEMPLATE2_PROPERTY);
    m_builder = new FormDataStatementBuilder(new OracleSqlStyle());
  }

  @Test
  public void testFieldData() throws ProcessingException {
    m_builder.setValueDefinition(FormData.Text.class, "TEXT", DataModelConstants.OPERATOR_EQ);
    assertEquals("  AND TEXT=:__a1", m_builder.build(m_formData));
    assertNotNull(m_builder.getBindMap());
    assertEquals(1, m_builder.getBindMap().size());
    assertEquals(TEXT, m_builder.getBindMap().get("__a1"));
  }

  @Test
  public void testPropertyData() throws ProcessingException {
    m_builder.setValueDefinition(FormData.Prop.class, "PROP", DataModelConstants.OPERATOR_EQ);
    assertEquals("  AND PROP=:__a1", m_builder.build(m_formData));
    assertNotNull(m_builder.getBindMap());
    assertEquals(1, m_builder.getBindMap().size());
    assertEquals(PROPERTY, m_builder.getBindMap().get("__a1"));
  }

  @Test(expected = ProcessingException.class)
  public void testTemplate1FieldData_valueDefinition() throws ProcessingException {
    m_builder.setValueDefinition(FormData.Template1GroupBox.TemplateText.class, "TEMPLATE1_TEXT", DataModelConstants.OPERATOR_EQ);
    m_builder.build(m_formData);
  }

  @Test
  public void testTemplate1FieldData_temlateValueDefinition() throws ProcessingException {
    m_builder.setValueDefinition(new ClassIdentifier(FormData.Template1GroupBox.class, FormData.Template1GroupBox.TemplateText.class), "TEMPLATE1_TEXT", DataModelConstants.OPERATOR_EQ);
    assertEquals("  AND TEMPLATE1_TEXT=:__a1", m_builder.build(m_formData));
    assertNotNull(m_builder.getBindMap());
    assertEquals(1, m_builder.getBindMap().size());
    assertEquals(TEMPLATE1_TEXT, m_builder.getBindMap().get("__a1"));
  }

  @Test(expected = ProcessingException.class)
  public void testTemplate1PropertyData_valueDefinition() throws ProcessingException {
    m_builder.setValueDefinition(FormData.Template1GroupBox.TemplateProp.class, "TEMPLATE1_PROP", DataModelConstants.OPERATOR_EQ);
    m_builder.build(m_formData);
  }

  @Test
  public void testTemplate1PropertyData_temlateValueDefinition() throws ProcessingException {
    m_builder.setValueDefinition(new ClassIdentifier(FormData.Template1GroupBox.class, FormData.Template1GroupBox.TemplateProp.class), "TEMPLATE1_PROP", DataModelConstants.OPERATOR_EQ);
    assertEquals("  AND TEMPLATE1_PROP=:__a1", m_builder.build(m_formData));
    assertNotNull(m_builder.getBindMap());
    assertEquals(1, m_builder.getBindMap().size());
    assertEquals(TEMPLATE1_PROPERTY, m_builder.getBindMap().get("__a1"));
  }

  @Test(expected = ProcessingException.class)
  public void testMasterTemplate1FieldData_valueDefinition() throws ProcessingException {
    m_builder.setValueDefinition(FormData.Template1GroupBox.MasterTemplateGroupBox.MasterTemplateText.class, "MASTER_TEMPLATE1_TEXT", DataModelConstants.OPERATOR_EQ);
    m_builder.build(m_formData);
  }

  @Test
  public void testMasterTemplate1FieldData_temlateValueDefinition() throws ProcessingException {
    m_builder.setValueDefinition(
        new ClassIdentifier(FormData.Template1GroupBox.class, AbstractTemplateFieldData.MasterTemplateGroupBox.class, AbstractMasterTemplateFieldData.MasterTemplateText.class), "MASTER_TEMPLATE1_TEXT", DataModelConstants.OPERATOR_EQ);
    assertEquals("  AND MASTER_TEMPLATE1_TEXT=:__a1", m_builder.build(m_formData));
    assertNotNull(m_builder.getBindMap());
    assertEquals(1, m_builder.getBindMap().size());
    assertEquals(MASTER_TEMPLATE1_TEXT, m_builder.getBindMap().get("__a1"));
  }

  @Test(expected = ProcessingException.class)
  public void testMasterTemplate1PropertyData_valueDefinition() throws ProcessingException {
    m_builder.setValueDefinition(FormData.Template1GroupBox.MasterTemplateGroupBox.MasterTemplateProp.class, "MASTER_TEMPLATE1_PROP", DataModelConstants.OPERATOR_EQ);
    m_builder.build(m_formData);
  }

  @Test
  public void testMasterTemplate1PropertyData_temlateValueDefinition() throws ProcessingException {
    m_builder.setValueDefinition(
        new ClassIdentifier(
            FormData.Template1GroupBox.class,
            FormData.Template1GroupBox.MasterTemplateGroupBox.class,
            FormData.Template1GroupBox.MasterTemplateGroupBox.MasterTemplateProp.class), "MASTER_TEMPLATE1_PROP", DataModelConstants.OPERATOR_EQ);
    assertEquals("  AND MASTER_TEMPLATE1_PROP=:__a1", m_builder.build(m_formData));
    assertNotNull(m_builder.getBindMap());
    assertEquals(1, m_builder.getBindMap().size());
    assertEquals(MASTER_TEMPLATE1_PROPERTY, m_builder.getBindMap().get("__a1"));
  }

  @Test(expected = ProcessingException.class)
  public void testTemplate1andTemplate2FieldData_valueDefinition() throws ProcessingException {
    m_builder.setValueDefinition(FormData.Template1GroupBox.TemplateText.class, "TEMPLATE1_TEXT", DataModelConstants.OPERATOR_EQ);
    m_builder.setValueDefinition(FormData.Template2GroupBox.TemplateText.class, "TEMPLATE2_TEXT", DataModelConstants.OPERATOR_LE);
    m_builder.build(m_formData);
  }

  @Test
  public void testTemplate1andTemplate2FieldData_temlateValueDefinition() throws ProcessingException {
    m_builder.setValueDefinition(new ClassIdentifier(FormData.Template1GroupBox.class, FormData.Template1GroupBox.TemplateText.class), "TEMPLATE1_TEXT", DataModelConstants.OPERATOR_EQ);
    m_builder.setValueDefinition(new ClassIdentifier(FormData.Template2GroupBox.class, FormData.Template2GroupBox.TemplateText.class), "TEMPLATE2_TEXT", DataModelConstants.OPERATOR_LE);
    assertEquals("  AND TEMPLATE1_TEXT=:__a1  AND TEMPLATE2_TEXT<=:__a2", m_builder.build(m_formData));
    assertNotNull(m_builder.getBindMap());
    assertEquals(2, m_builder.getBindMap().size());
    assertEquals(TEMPLATE1_TEXT, m_builder.getBindMap().get("__a1"));
    assertEquals(TEMPLATE2_TEXT, m_builder.getBindMap().get("__a2"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTemplateDefinition() throws ProcessingException {
    m_builder.setValueDefinition(new ClassIdentifier(FormData.Template1GroupBox.class, String.class, FormData.Template1GroupBox.TemplateText.class), "TEMPLATE1_TEXT", DataModelConstants.OPERATOR_EQ);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTemplateDefinition_FormData() throws ProcessingException {
    m_builder.setValueDefinition(new ClassIdentifier(FormData.class, FormData.Template1GroupBox.class, FormData.Template1GroupBox.TemplateText.class), "TEMPLATE1_TEXT", DataModelConstants.OPERATOR_EQ);
  }

  public static class AbstractMasterTemplateFieldData extends AbstractFormFieldData {
    private static final long serialVersionUID = 1L;

    public MasterTemplateText getMasterTemplateText() {
      return getFieldByClass(MasterTemplateText.class);
    }

    public MasterTemplateProp getMasterTemplateProp() {
      return getPropertyByClass(MasterTemplateProp.class);
    }

    public void setMasterTemplatePropProperty(String s) {
      getMasterTemplateProp().setValue(s);
    }

    public String getMasterTemplatePropProperty() {
      return getMasterTemplateProp().getValue();
    }

    public class MasterTemplateText extends AbstractValueFieldData<String> {
      private static final long serialVersionUID = 1L;
    }

    public class MasterTemplateProp extends AbstractPropertyData<String> {
      private static final long serialVersionUID = 1L;
    }
  }

  public static class AbstractTemplateFieldData extends AbstractFormFieldData {
    private static final long serialVersionUID = 1L;

    public TemplateText getText() {
      return getFieldByClass(TemplateText.class);
    }

    public TemplateProp getTemplateProp() {
      return getPropertyByClass(TemplateProp.class);
    }

    public void setTemplatePropProperty(String s) {
      getTemplateProp().setValue(s);
    }

    public String getTemplatePropProperty() {
      return getTemplateProp().getValue();
    }

    public MasterTemplateGroupBox getMasterTemplateGroupBox() {
      return getFieldByClass(MasterTemplateGroupBox.class);
    }

    public class TemplateText extends AbstractValueFieldData<String> {
      private static final long serialVersionUID = 1L;
    }

    public class TemplateProp extends AbstractPropertyData<String> {
      private static final long serialVersionUID = 1L;
    }

    public class MasterTemplateGroupBox extends AbstractMasterTemplateFieldData {
      private static final long serialVersionUID = 1L;
    }
  }

  public static class FormData extends AbstractFormData {
    private static final long serialVersionUID = 1L;

    public Text getName() {
      return getFieldByClass(Text.class);
    }

    public Template1GroupBox getTemplate1GroupBox() {
      return getFieldByClass(Template1GroupBox.class);
    }

    public Template2GroupBox getTemplate2GroupBox() {
      return getFieldByClass(Template2GroupBox.class);
    }

    public Prop getDirectProp() {
      return getPropertyByClass(Prop.class);
    }

    public void setDirectPropProperty(String s) {
      getDirectProp().setValue(s);
    }

    public String getDirectPropProperty() {
      return getDirectProp().getValue();
    }

    public class Text extends AbstractValueFieldData<String> {
      private static final long serialVersionUID = 1L;
    }

    public class Template1GroupBox extends AbstractTemplateFieldData {
      private static final long serialVersionUID = 1L;
    }

    public class Template2GroupBox extends AbstractTemplateFieldData {
      private static final long serialVersionUID = 1L;
    }

    public class Prop extends AbstractPropertyData<String> {
      private static final long serialVersionUID = 1L;
    }
  }
}
