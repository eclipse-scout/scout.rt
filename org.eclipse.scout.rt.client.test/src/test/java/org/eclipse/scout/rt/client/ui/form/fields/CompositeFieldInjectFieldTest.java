/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
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

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.CompositeFieldInjectFieldTest.TestForm.MainBox.TabBox;
import org.eclipse.scout.rt.client.ui.form.fields.CompositeFieldInjectFieldTest.TestForm.PresentationBox;
import org.eclipse.scout.rt.client.ui.form.fields.CompositeFieldInjectFieldTest.TestForm.PresentationBox2;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class CompositeFieldInjectFieldTest extends AbstractLocalExtensionTestCase {

  public class TestForm extends AbstractForm {
    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class TabBox extends AbstractTabBox {
      }
    }

    @Replace
    public class TabBoxEx extends MainBox.TabBox {
      public TabBoxEx(MainBox container) {
        container.super();
      }
    }

    @Order(30)
    @InjectFieldTo(MainBox.TabBox.class)
    public class PresentationBox extends AbstractGroupBox {
      // box injected to replaced TabBox
    }

    @Order(30)
    @InjectFieldTo(TabBoxEx.class)
    public class PresentationBox2 extends AbstractGroupBox {
      // box injected to TabBoxEx replacing TabBox
    }
  }

  @Test
  public void test() {
    TestForm form = new TestForm();
    assertNotNull(form.getFieldByClass(PresentationBox.class));
    assertNotNull(form.getFieldByClass(PresentationBox2.class));
    assertEquals(2, form.getFieldByClass(TabBox.class).getFieldCount());
  }
}
