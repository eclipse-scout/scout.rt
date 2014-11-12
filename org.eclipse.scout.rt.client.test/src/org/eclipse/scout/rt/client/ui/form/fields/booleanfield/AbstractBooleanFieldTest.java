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
package org.eclipse.scout.rt.client.ui.form.fields.booleanfield;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class AbstractBooleanFieldTest extends AbstractBooleanField {

  @Test
  public void testExecIsEmpty() throws ProcessingException {

    setValue(null);
    assertEquals("Boolean field with value null is not empty.", true, execIsEmpty());
    setValue(true);
    assertEquals("Boolean field with value true is empty.", false, execIsEmpty());
    setValue(false);
    assertEquals("Boolean field with value false is not empty.", true, execIsEmpty());

  }

}
