/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.mobile.Icons;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.groupbox.GroupBoxPropertyDelegator;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;

public class TabForm extends AbstractForm {
  private IGroupBox m_tabGroupBox;

  public TabForm(IGroupBox tabGroupBox) throws ProcessingException {
    super(false);
    m_tabGroupBox = tabGroupBox;
    callInitializer();
  }

  @Order(10)
  public class Mainbox extends AbstractGroupBox {

    @Order(10)
    public class Groupbox extends AbstractGroupBox {

      @Override
      protected void initConfig() {
        super.initConfig();

        new GroupBoxPropertyDelegator(m_tabGroupBox, this).init();

        setTitle(m_tabGroupBox.getLabel());
        setBorderDecoration(BORDER_DECORATION_EMPTY);
      }

      @Override
      protected void injectFieldsInternal(List<IFormField> fieldList) {
        super.injectFieldsInternal(fieldList);

        fieldList.addAll(Arrays.asList(m_tabGroupBox.getFields()));
      }

      /**
       * Returns the field id of the actual tab groupBox.<br>
       * This is necessary to make the things work properly which rely on the fieldId.<br>
       * One example is the formData import/export: It uses the fieldId to find the matching formField and also has to
       * consider group boxes if template fields are used (see FindFieldByFormDataIdVisitor).
       */
      @Override
      public String getFieldId() {
        return m_tabGroupBox.getFieldId();
      }

      @Override
      public String classId() {
        return m_tabGroupBox.classId();
      }
    }

    @Order(10)
    public class CloseButton extends AbstractCloseButton {
      @Override
      protected String getConfiguredLabel() {
        return null;
      }

      @Override
      protected String getConfiguredIconId() {
        return Icons.BackAction;
      }
    }

  }

  public void start() throws ProcessingException {
    startInternal(new FormHandler());
  }

  @Order(20.0f)
  public class FormHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() throws ProcessingException {

    }

  }
}
