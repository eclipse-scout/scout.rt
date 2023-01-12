/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fixture;

import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.AbstractGroupBoxExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm.MainBox.TopBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * @since 6.0
 */
public class ExtendedFormExtendedGroupWithField extends AbstractFormExtension<OrigForm> {

  public ExtendedFormExtendedGroupWithField(OrigForm ownerForm) {
    super(ownerForm);
  }

  public static class TopBoxExtension extends AbstractGroupBoxExtension<TopBox> {

    public TopBoxExtension(TopBox owner) {
      super(owner);
    }

    @Order(50)
    @ClassId("d8af15be-94c7-475b-a52a-f9d316e8256b")
    public class TopBoxStringField extends AbstractStringField {

      @Override
      protected String getConfiguredLabel() {
        return "TopBoxStringField";
      }
    }
  }
}
