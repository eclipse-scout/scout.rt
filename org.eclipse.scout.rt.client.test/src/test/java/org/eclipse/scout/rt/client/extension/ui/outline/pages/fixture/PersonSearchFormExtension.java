/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture;

import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.AbstractGroupBoxExtension;
import org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture.PersonSearchForm.MainBox.TopBox;
import org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture.PersonSearchFormExtension.TopBoxExtension.TopBoxStringField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.BooleanUtility;

/**
 * @since 6.0
 */
public class PersonSearchFormExtension extends AbstractFormExtension<PersonSearchForm> {
  private static String GET_CONFIGURED_LABEL_CALLED = "GET_CONFIGURED_LABEL_CALLED";

  public PersonSearchFormExtension(PersonSearchForm ownerForm) {
    super(ownerForm);
  }

  public TopBoxStringField getTopBoxStringField() {
    return getOwner().getFieldByClass(TopBoxStringField.class);
  }

  public class TopBoxExtension extends AbstractGroupBoxExtension<TopBox> {

    public TopBoxExtension(TopBox owner) {
      super(owner);
    }

    @Order(50)
    @ClassId("ca84fa44-72e0-4bc6-8fd0-541ec6380e2d")
    public class TopBoxStringField extends AbstractStringField {

      @Override
      protected String getConfiguredLabel() {
        setProperty(GET_CONFIGURED_LABEL_CALLED, true);
        return "TopBoxStringField";
      }

      public boolean isGetConfiguredLabelCalled() {
        return BooleanUtility.nvl((Boolean) getProperty(GET_CONFIGURED_LABEL_CALLED));
      }
    }
  }
}
