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
package org.eclipse.scout.rt.client.ui.form.fields.booleanfield;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractBooleanFieldWithTriStateTest extends AbstractBooleanField {

  @Test
  public void testExecIsEmpty() {
    setTriStateEnabled(true);

    setValue(null);
    assertEquals("Boolean field with value null is not empty.", true, execIsEmpty());
    setValue(true);
    assertEquals("Boolean field with value true is empty.", false, execIsEmpty());
    setValue(false);
    assertEquals("Boolean field with value false is not empty.", true, execIsEmpty());

    setValue(null);
    assertEquals("Boolean field with value null is not empty.", true, execIsEmpty());
    setValue(Boolean.TRUE);
    assertEquals("Boolean field with value true is empty.", false, execIsEmpty());
    setValue(Boolean.FALSE);
    assertEquals("Boolean field with value false is not empty.", true, execIsEmpty());
  }

  @Test
  public void testChangeUndefinedToFalse() {
    setValue(null);
    assertEquals(false, getValue());

    setValue(true);
    assertEquals(true, getValue());

    setTriStateEnabled(true);
    assertEquals(true, getValue());

    setValue(null);
    assertEquals(null, getValue());

    setTriStateEnabled(false);
    assertEquals(false, getValue());
  }

  @Test
  public void testIsChecked() {
    setValue(null);
    assertEquals(false, getValue());
    assertEquals(false, isChecked());

    toggleValue();
    assertEquals(true, getValue());
    assertEquals(true, isChecked());

    setTriStateEnabled(true);
    setValue(null);
    assertEquals(null, getValue());
    assertEquals(false, isChecked());

    toggleValue();
    assertEquals(false, getValue());
    toggleValue();
    assertEquals(true, getValue());
    toggleValue();
    assertEquals(null, getValue());
  }
}
