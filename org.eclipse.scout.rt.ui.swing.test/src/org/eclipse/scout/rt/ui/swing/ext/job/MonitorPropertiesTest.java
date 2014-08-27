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
package org.eclipse.scout.rt.ui.swing.ext.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * Test class for {@link MonitorProperties}.
 * 
 * @author awe
 */
public class MonitorPropertiesTest {

  MonitorProperties m_props1 = new MonitorProperties(1, "foo", "bar");

  @Test
  public void testEqualsHashcode() {
    MonitorProperties props2 = new MonitorProperties(1, "foo", "bar");
    assertEquals(m_props1, props2);
    assertEquals(props2, m_props1);
    assertEquals(m_props1.hashCode(), props2.hashCode());
    assertEquals(props2.hashCode(), m_props1.hashCode());
  }

  @Test
  public void testNotEqualsHashcode() {
    assertFalse(m_props1.equals(new MonitorProperties(2, "foo", "bar")));
    assertFalse(m_props1.equals(new MonitorProperties(1, "bar", "bar")));
    assertFalse(m_props1.equals(new MonitorProperties(1, "foo", "foo")));
  }

  @Test
  public void testNullInstance() throws Exception {
    assertEquals(0L, MonitorProperties.NULL_INSTANCE.getWorked(), 0);
    assertEquals("", MonitorProperties.NULL_INSTANCE.getTaskName());
    assertEquals("", MonitorProperties.NULL_INSTANCE.getSubTaskName());
  }

}
