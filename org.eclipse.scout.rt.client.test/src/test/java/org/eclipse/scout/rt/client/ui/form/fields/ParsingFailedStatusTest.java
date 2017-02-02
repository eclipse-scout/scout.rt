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
package org.eclipse.scout.rt.client.ui.form.fields;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.junit.Test;

public class ParsingFailedStatusTest {

  @Test
  public void testConstructorCopiesCode() {
    ProcessingException veto = new VetoException("Foo").withCode(123);
    ParsingFailedStatus status = new ParsingFailedStatus(veto, "Bar");
    assertEquals("Foo", status.getMessage());
    assertEquals(123, status.getCode());
  }

  @Test
  public void testConstructorWithoutCode() {
    ProcessingException veto = new VetoException("Foo");
    ParsingFailedStatus status = new ParsingFailedStatus(veto, "Bar");
    assertEquals("Foo", status.getMessage());
    assertEquals(0, status.getCode()); // default value for in members
  }

}
