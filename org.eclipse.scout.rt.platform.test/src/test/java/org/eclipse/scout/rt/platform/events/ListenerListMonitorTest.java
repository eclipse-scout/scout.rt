/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.events.management.IListenerListMonitorMBean;
import org.eclipse.scout.rt.platform.events.management.IListenerListMonitorMBean.EventType;
import org.eclipse.scout.rt.platform.events.management.IListenerListMonitorMBean.ListenerInfo;
import org.eclipse.scout.rt.platform.events.management.IListenerListMonitorMBean.ListenerListInfo;
import org.eclipse.scout.rt.platform.events.management.ListenerListMonitorMBean;
import org.eclipse.scout.rt.platform.reflect.BasicPropertySupport;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ListenerListMonitorTest {

  private ListenerListRegistry m_originalListenerListRegistry;

  @Before
  public void runWithPrivateListenerListRegistry() {
    m_originalListenerListRegistry = ListenerListRegistry.globalInstance();
    ListenerListRegistry.setGlobalInstance(new ListenerListRegistry());
  }

  @After
  public void restoreOriginalListenerListRegistry() {
    ListenerListRegistry.setGlobalInstance(m_originalListenerListRegistry);
  }

  @Test
  public void testMBean() {
    BasicPropertySupport prop1 = new BasicPropertySupport(new Object());
    prop1.addPropertyChangeListener(new Listener1());
    prop1.addPropertyChangeListener(new Listener1());

    BasicPropertySupport prop2 = new BasicPropertySupport(new Object());
    prop2.addPropertyChangeListener("Bar", new Listener2a());
    prop2.addPropertyChangeListener("Foo", new Listener2b());

    IListenerListMonitorMBean mon = BEANS.get(ListenerListMonitorMBean.class);

    ListenerListInfo[] listenerLists = mon.getListenerListInfos();
    assertEquals("[" + ListenerListInfo.class.getSimpleName() + " [className=" + BasicPropertySupport.class.getName() + ", numInstances=2, listenerTypes=\n" +
        "  " + EventType.class.getSimpleName() + "[type=*, listeners=[" + ListenerInfo.class.getSimpleName() + "[className=" + Listener1.class.getName() + ", count=2]]]\n" +
        "  " + EventType.class.getSimpleName() + "[type=Bar, listeners=[" + ListenerInfo.class.getSimpleName() + "[className=" + Listener2a.class.getName() + ", count=1]]]\n" +
        "  " + EventType.class.getSimpleName() + "[type=Foo, listeners=[" + ListenerInfo.class.getSimpleName() + "[className=" + Listener2b.class.getName() + ", count=1]]]\n" +
        "]]", Arrays.toString(listenerLists));
    assertListenerListRegistryContainsExactly(BasicPropertySupport.class, BasicPropertySupport.class);
  }

  private static void assertListenerListRegistryContainsExactly(Class<?>... expectedClasses) {
    ListenerListSnapshot snapshot = ListenerListRegistry.globalInstance().createSnapshot();
    Set<IListenerListWithManagement> listenerLists = snapshot.getData().keySet();
    List<Class<?>> expected = CollectionUtility.arrayList(expectedClasses);
    for (IListenerListWithManagement list : listenerLists) {
      Class<? extends IListenerListWithManagement> actual = list.getClass();
      boolean removed = expected.remove(actual);
      if (!removed) {
        fail(actual + " found in list but should not be present.");
      }
    }
    if (expected.isEmpty()) {
      return;
    }
    fail("The following items are expected but could not be found in the list: " + StringUtility.join(", ", expected.toArray()));
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
