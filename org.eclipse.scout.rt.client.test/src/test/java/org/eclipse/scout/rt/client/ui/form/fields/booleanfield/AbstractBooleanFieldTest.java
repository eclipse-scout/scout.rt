/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
public class AbstractBooleanFieldTest extends AbstractBooleanField {

  @Test
  public void testExecIsEmpty() {
    setValue(null);
    assertEquals("Boolean field with value null is not empty.", true, execIsEmpty());
    setValue(true);
    assertEquals("Boolean field with value true is empty.", false, execIsEmpty());
    setValue(false);
    assertEquals("Boolean field with value false is not empty.", true, execIsEmpty());
  }
}
