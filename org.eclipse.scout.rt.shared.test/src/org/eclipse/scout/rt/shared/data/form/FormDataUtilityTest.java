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
package org.eclipse.scout.rt.shared.data.form;

import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link FormDataUtility}
 * 
 * @since 3.8.0
 */
public class FormDataUtilityTest {

  @Test
  public void testGetFieldDataId() {
    Assert.assertNull(FormDataUtility.getFieldDataId(null));
    Assert.assertNull(FormDataUtility.getFieldDataId(""));
    Assert.assertNull(FormDataUtility.getFieldDataId("   "));
    Assert.assertEquals("Test", FormDataUtility.getFieldDataId("Test"));
    Assert.assertEquals("Test", FormDataUtility.getFieldDataId("  Test  "));
    Assert.assertEquals("Name", FormDataUtility.getFieldDataId("NameField"));
    Assert.assertEquals("Name", FormDataUtility.getFieldDataId("NameField  "));
    Assert.assertEquals("Name", FormDataUtility.getFieldDataId("  NameField  "));
    Assert.assertEquals("1234", FormDataUtility.getFieldDataId("1234"));
    Assert.assertEquals("1234", FormDataUtility.getFieldDataId("1234Field"));
    Assert.assertEquals("TopBox", FormDataUtility.getFieldDataId("TopBox"));
    Assert.assertEquals("Ok", FormDataUtility.getFieldDataId("OkButton"));
  }
}
