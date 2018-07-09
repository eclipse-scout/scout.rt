/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.events.management;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.events.management.IListenerListMonitor.ListenerListInfo;
import org.eclipse.scout.rt.platform.reflect.BasicPropertySupport;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ListenerListMonitorTest {

  @Test
  public void testMBean() {
    BasicPropertySupport prop1 = new BasicPropertySupport(new Object());
    prop1.addPropertyChangeListener(new Listener1());
    BasicPropertySupport prop2 = new BasicPropertySupport(new Object());
    prop2.addPropertyChangeListener("Bar", new Listener2a());
    prop2.addPropertyChangeListener("Foo", new Listener2b());

    IListenerListMonitor mon = BEANS.get(IListenerListMonitor.class);

    assertEquals(2, mon.getListenerListCount());
    ListenerListInfo[] listenerLists = mon.getListenerListInfos();

    assertEquals(1, listenerLists.length);
    assertEquals(BasicPropertySupport.class.getName(), listenerLists[0].getListenerListClassName());

    assertEquals(3, listenerLists[0].getListenerTypes().length);
    assertEquals("*", listenerLists[0].getListenerTypes()[0].getEventType());
    assertEquals("Bar", listenerLists[0].getListenerTypes()[1].getEventType());
    assertEquals("Foo", listenerLists[0].getListenerTypes()[2].getEventType());

    assertEquals(3, listenerLists[0].getListenerCount());
    assertEquals(1, listenerLists[0].getListenerTypes()[0].getListenerCount());
    assertEquals(1, listenerLists[0].getListenerTypes()[1].getListenerCount());
    assertEquals(1, listenerLists[0].getListenerTypes()[2].getListenerCount());

    assertEquals(Listener1.class.getName(), listenerLists[0].getListenerTypes()[0].getListenerInfos()[0].getListenerClassName());
    assertEquals(1, listenerLists[0].getListenerTypes()[0].getListenerInfos()[0].getListenerCount());

    assertEquals(Listener2a.class.getName(), listenerLists[0].getListenerTypes()[1].getListenerInfos()[0].getListenerClassName());
    assertEquals(1, listenerLists[0].getListenerTypes()[1].getListenerInfos()[0].getListenerCount());

    assertEquals(Listener2b.class.getName(), listenerLists[0].getListenerTypes()[2].getListenerInfos()[0].getListenerClassName());
    assertEquals(1, listenerLists[0].getListenerTypes()[2].getListenerInfos()[0].getListenerCount());
  }

  private static class Listener1 implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      //nop
    }
  }

  private static class Listener2a implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      //nop
    }
  }

  private static class Listener2b implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      //nop
    }
  }
}
