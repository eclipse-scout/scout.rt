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
package org.eclipse.scout.rt.shared.extension.dto.fixture;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.extension.dto.fixture.AbstractTemplateBox.GroupBoxInTemplateField.SecondStringInTemplateField;
import org.eclipse.scout.rt.shared.extension.dto.fixture.AbstractTemplateBox.GroupBoxInTemplateField.ThirdStringInTemplateField;

@FormData(value = AbstractTemplateBoxData.class, sdkCommand = FormData.SdkCommand.CREATE, defaultSubtypeSdkCommand = FormData.DefaultSubtypeSdkCommand.CREATE)
public abstract class AbstractTemplateBox extends AbstractGroupBox {

  public FirstStringInTemplateField getFirstStringInTemplateField() {
    return getFieldByClass(FirstStringInTemplateField.class);
  }

  public SecondStringInTemplateField getSecondStringInTemplateField() {
    return getFieldByClass(SecondStringInTemplateField.class);
  }

  public ThirdStringInTemplateField getThirdStringInTemplateField() {
    return getFieldByClass(ThirdStringInTemplateField.class);
  }

  public GroupBoxInTemplateField getGroupBoxInTemplateField() {
    return getFieldByClass(GroupBoxInTemplateField.class);
  }

  @Order(1000)
  public class FirstStringInTemplateField extends AbstractStringField {
  }

  @Order(2000)
  public class GroupBoxInTemplateField extends AbstractGroupBox {
    @Order(1000)
    public class SecondStringInTemplateField extends AbstractStringField {
    }

    @Order(2000)
    public class ThirdStringInTemplateField extends AbstractStringField {
    }
  }
}
