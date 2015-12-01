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
package org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.fixture;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.platform.Order;

/**
 * Group Box with 2 text fields
 */
@FormData(value = AbstractTemplateGroupBoxData.class, sdkCommand = SdkCommand.CREATE, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE)
public abstract class AbstractTemplateGroupBox extends AbstractGroupBox {

  public Text1Field getText1Field() {
    return getFieldByClass(Text1Field.class);
  }

  public Text2Field getText2Field() {
    return getFieldByClass(Text2Field.class);
  }

  @Order(10)
  public class Text1Field extends AbstractStringField {
  }

  @Order(20)
  public class Text2Field extends AbstractStringField {
  }

  @Order(30)
  public class TabGroupBox extends AbstractTabBox {

    @Order(10)
    public class TabTemplateField extends AbstractTemplate3GroupBox {
    }
  }

  @Order(40)
  public class TemplateField extends AbstractTemplate2GroupBox {
  }

}
