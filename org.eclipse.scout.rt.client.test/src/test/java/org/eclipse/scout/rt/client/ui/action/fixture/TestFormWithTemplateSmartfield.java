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
package org.eclipse.scout.rt.client.ui.action.fixture;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * A form containing two smartfields using a template with menus.
 */
public class TestFormWithTemplateSmartfield extends AbstractForm {
  public static final String TEST_SMARTFIELD_ID_1 = "SMARTFIELD_ID1";
  public static final String TEST_SMARTFIELD_ID_2 = "SMARTFIELD_ID2";

  public TestFormWithTemplateSmartfield() {
    super();
  }

  public class MainBox extends AbstractGroupBox {

    @Order(10)
    @ClassId(TEST_SMARTFIELD_ID_1)
    public class SmartField1 extends SmartfieldTestTemplate {
      @Override
      protected boolean getConfiguredAutoAddDefaultMenus() {
        return false;
      }
    }

    @Order(20)
    @ClassId(TEST_SMARTFIELD_ID_2)
    public class SmartField2 extends SmartfieldTestTemplate {
      @Override
      protected boolean getConfiguredAutoAddDefaultMenus() {
        return false;
      }
    }
  }
}
