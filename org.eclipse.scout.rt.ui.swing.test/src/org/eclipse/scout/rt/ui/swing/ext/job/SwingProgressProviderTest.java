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
import static org.junit.Assert.assertNull;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link SwingProgressProvider}.
 *
 * @author awe
 */
public class SwingProgressProviderTest {

  private static final String[] EXPECTED = {"foo", "bar"};

  private int m_count;

  private SwingProgressProvider m_prov = new SwingProgressProvider();

  /**
   * Property change must be fired when properties on the _same_ monitor
   * instance have changed.
   */
  @Test
  public void testSetActiveMonitor() throws Exception {
    m_prov.addPropertyChangeListener(
        SwingProgressProvider.PROP_MONITOR_PROPERTIES,
        new PropertyChangeListener() {
          @Override
          public void propertyChange(PropertyChangeEvent evt) {
            MonitorProperties props = (MonitorProperties) evt.getNewValue();
            Assert.assertEquals(EXPECTED[m_count],
                props.getTaskName());
            m_count++;
          }
        });
    SwingProgressMonitor monitor = new SwingProgressMonitor();
    monitor.setTaskName("foo");
    m_prov.setActiveMonitor(monitor);
    monitor.setTaskName("bar");
    m_prov.setActiveMonitor(monitor);
    Assert.assertEquals(2, m_count);
  }

  /**
   * setActiveMontitor must deal with null monitor.
   */
  @Test
  public void testSetActiveMonitor_Null() throws Exception {
    m_prov.addPropertyChangeListener(
        SwingProgressProvider.PROP_MONITOR_PROPERTIES,
        new PropertyChangeListener() {
          @Override
          public void propertyChange(PropertyChangeEvent evt) {
            if (m_count == 0) {
              assertEquals("bar", ((MonitorProperties) evt.getNewValue()).getSubTaskName());
            }
            else if (m_count == 1) {
              assertNull(evt.getNewValue());
            }
            m_count++;
          }
        });
    SwingProgressMonitor monitor = new SwingProgressMonitor();
    monitor.subTask("bar");
    m_prov.setActiveMonitor(monitor);
    m_prov.setActiveMonitor(null);
    Assert.assertEquals(2, m_count);
  }

}
