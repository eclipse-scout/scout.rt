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
package org.eclipse.scout.rt.platform.util;

import java.util.EventListener;

import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link EventListenerList}
 */
public class EventListenerListTest {

  /**
   * Test for {@link EventListenerList#add(Class, java.util.EventListener)}
   */
  @Test
  public void testRemove() {
    EventListenerList listenerList = new EventListenerList();
    EventListener listener = new EventListener() {
    };
    listenerList.add(EventListener.class, listener);
    listenerList.add(EventListener.class, listener);
    listenerList.remove(EventListener.class, listener);
    Assert.assertEquals(1, listenerList.getListenerCount(EventListener.class));
  }

  /**
   * Test for {@link EventListenerList#removeAll(Class, java.util.EventListener)}
   */
  @Test
  public void testRemoveAll() {
    EventListenerList listenerList = new EventListenerList();
    EventListener listener = new EventListener() {
    };
    listenerList.add(EventListener.class, listener);
    listenerList.add(EventListener.class, listener);
    listenerList.removeAll(EventListener.class, listener);
    Assert.assertEquals(0, listenerList.getListenerCount(EventListener.class));
  }

  /**
   * Test for {@link EventListenerList#add(Class, java.util.EventListener)}
   */
  @Test
  public void testAdd() {
    EventListenerList listenerList = new EventListenerList();
    EventListener listener = new EventListener() {
    };
    listenerList.add(EventListener.class, listener);
    listenerList.add(EventListener.class, listener);
    Assert.assertEquals(2, listenerList.getListenerCount(EventListener.class));
  }

}
