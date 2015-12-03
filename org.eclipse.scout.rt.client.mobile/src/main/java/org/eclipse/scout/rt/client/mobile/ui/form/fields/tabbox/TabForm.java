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
package org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox;

import org.eclipse.scout.rt.client.mobile.Icons;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.groupbox.GroupBoxPropertyDelegator;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

public class TabForm extends AbstractForm {
  private IGroupBox m_tabGroupBox;

  public TabForm(IGroupBox tabGroupBox) {
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
      protected void injectFieldsInternal(OrderedCollection<IFormField> fields) {
        super.injectFieldsInternal(fields);

        fields.addAllOrdered(m_tabGroupBox.getFields());
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

  @Override
  public void start() {
    startInternal(new FormHandler());
  }

  public class FormHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() {

    }

  }
}
