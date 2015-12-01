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
package org.eclipse.scout.rt.platform.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.scout.rt.platform.beans.fixture.WeakPropertyChangeListener;
import org.eclipse.scout.rt.platform.reflect.BasicPropertySupport;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for {@link BasicPropertySupport}
 */
public class BasicPropertySupportTest {
  private static final String TEST_PROPERTY = "myProperty";

  @Test
  public void testAddListener() {
    final BasicPropertySupport propertySupport = new BasicPropertySupport(null);
    WeakPropertyChangeListener weakEventListener = mock(WeakPropertyChangeListener.class);
    propertySupport.addPropertyChangeListener(weakEventListener);
    assertTrue(propertySupport.hasListeners(null));
  }

  @Test
  public void testRemoveListener() {
    final BasicPropertySupport propertySupport = new BasicPropertySupport(null);
    WeakPropertyChangeListener weakEventListener = mock(WeakPropertyChangeListener.class);
    propertySupport.addPropertyChangeListener(weakEventListener);
    propertySupport.removePropertyChangeListener(weakEventListener);
    assertFalse(propertySupport.hasListeners(null));
  }

  @Test
  public void testAddChildListener() {
    final BasicPropertySupport propertySupport = new BasicPropertySupport(null);
    WeakPropertyChangeListener weakEventListener = mock(WeakPropertyChangeListener.class);
    propertySupport.addPropertyChangeListener(TEST_PROPERTY, weakEventListener);
    assertTrue(propertySupport.hasListeners(TEST_PROPERTY));
    assertFalse(propertySupport.hasListeners(null));
  }

  @Test
  public void testRemoveChildListener() {
    final BasicPropertySupport propertySupport = new BasicPropertySupport(null);
    PropertyChangeListener listener = mock(PropertyChangeListener.class);
    PropertyChangeListener weakEventListener = mock(WeakPropertyChangeListener.class);
    propertySupport.addPropertyChangeListener(TEST_PROPERTY, weakEventListener);
    propertySupport.addPropertyChangeListener(TEST_PROPERTY, listener);
    propertySupport.removePropertyChangeListener(TEST_PROPERTY, weakEventListener);
    propertySupport.removePropertyChangeListener(TEST_PROPERTY, listener);
    assertFalse(propertySupport.hasListeners(TEST_PROPERTY));
    assertFalse(propertySupport.hasListeners(null));
  }

  @Test
  public void testGetListenersEmptyNotNull() {
    final BasicPropertySupport propertySupport = new BasicPropertySupport(null);
    assertTrue(propertySupport.getPropertyChangeListeners().isEmpty());
    assertTrue(propertySupport.getSpecificPropertyChangeListeners().isEmpty());
  }

  @Test
  public void testGetHasListeners() {
    final BasicPropertySupport propertySupport = new BasicPropertySupport(null);
    PropertyChangeListener eventListener1 = mock(PropertyChangeListener.class);
    PropertyChangeListener eventListener2 = mock(PropertyChangeListener.class);
    WeakPropertyChangeListener weakEventListener1 = mock(WeakPropertyChangeListener.class);
    WeakPropertyChangeListener weakEventListener2 = mock(WeakPropertyChangeListener.class);
    PropertyChangeListener testPropertyListener = mock(PropertyChangeListener.class);
    WeakPropertyChangeListener weakTestPropertyListener = mock(WeakPropertyChangeListener.class);
    ArrayList<PropertyChangeListener> specificListeners = new ArrayList<PropertyChangeListener>();

    //initial empty
    checkExpectedListeners(propertySupport, specificListeners);

    // only general listeners
    propertySupport.addPropertyChangeListener(eventListener1);
    propertySupport.addPropertyChangeListener(eventListener2);
    propertySupport.addPropertyChangeListener(weakEventListener1);
    propertySupport.addPropertyChangeListener(weakEventListener2);
    checkExpectedListeners(propertySupport, specificListeners, eventListener1, eventListener2, weakEventListener1, weakEventListener2);

    // remove
    propertySupport.removePropertyChangeListener(eventListener1);
    propertySupport.removePropertyChangeListener(eventListener2);
    propertySupport.removePropertyChangeListener(weakEventListener1);
    propertySupport.removePropertyChangeListener(weakEventListener2);
    checkExpectedListeners(propertySupport, specificListeners);

    // only specific listeners
    propertySupport.addPropertyChangeListener(TEST_PROPERTY, testPropertyListener);
    specificListeners.add(testPropertyListener);
    propertySupport.addPropertyChangeListener(TEST_PROPERTY, weakTestPropertyListener);
    specificListeners.add(weakTestPropertyListener);
    checkExpectedListeners(propertySupport, specificListeners);

    // both
    propertySupport.addPropertyChangeListener(eventListener1);
    propertySupport.addPropertyChangeListener(eventListener2);
    propertySupport.addPropertyChangeListener(weakEventListener1);
    propertySupport.addPropertyChangeListener(weakEventListener2);
    checkExpectedListeners(propertySupport, specificListeners, eventListener1, eventListener2, weakEventListener1, weakEventListener2);

    // remove everything
    propertySupport.removePropertyChangeListener(eventListener1);
    propertySupport.removePropertyChangeListener(eventListener2);
    propertySupport.removePropertyChangeListener(weakEventListener1);
    propertySupport.removePropertyChangeListener(weakEventListener2);
    propertySupport.removePropertyChangeListener(TEST_PROPERTY, testPropertyListener);
    specificListeners.remove(testPropertyListener);
    propertySupport.removePropertyChangeListener(TEST_PROPERTY, weakTestPropertyListener);
    specificListeners.remove(weakTestPropertyListener);
    checkExpectedListeners(propertySupport, specificListeners);
  }

  /**
   * This test is ignored because nothing is guaranteed with System.gc(). It can be included when run manually.
   */
  @Ignore
  @Test
  public void testGetHasListenersResetWeakRefs() {
    final BasicPropertySupport propertySupport = new BasicPropertySupport(null);
    for (int i = 0; i < 100; i++) {
      PropertyChangeListener eventListener1 = mock(PropertyChangeListener.class);
      PropertyChangeListener eventListener2 = mock(PropertyChangeListener.class);
      WeakPropertyChangeListener weakEventListener1 = mock(WeakPropertyChangeListener.class);
      WeakPropertyChangeListener weakEventListener2 = mock(WeakPropertyChangeListener.class);
      PropertyChangeListener testPropertyListener = mock(PropertyChangeListener.class);
      WeakPropertyChangeListener weakTestPropertyListener = mock(WeakPropertyChangeListener.class);
      ArrayList<PropertyChangeListener> specificListeners = new ArrayList<PropertyChangeListener>();

      //initial empty
      checkExpectedListeners(propertySupport, specificListeners);

      // general listeners
      propertySupport.addPropertyChangeListener(eventListener1);
      propertySupport.addPropertyChangeListener(eventListener2);
      propertySupport.addPropertyChangeListener(weakEventListener1);
      propertySupport.addPropertyChangeListener(weakEventListener2);
      checkExpectedListeners(propertySupport, specificListeners, eventListener1, eventListener2, weakEventListener1, weakEventListener2);
      // specific listeners
      propertySupport.addPropertyChangeListener(TEST_PROPERTY, testPropertyListener);
      specificListeners.add(testPropertyListener);
      propertySupport.addPropertyChangeListener(TEST_PROPERTY, weakTestPropertyListener);
      specificListeners.add(weakTestPropertyListener);
      checkExpectedListeners(propertySupport, specificListeners, eventListener1, eventListener2, weakEventListener1, weakEventListener2);

      // set references to 2 of 3 weakListeners to null and request the garbage collector to run
      weakEventListener1 = null;
      specificListeners.remove(weakTestPropertyListener);
      weakTestPropertyListener = null;
      System.gc();
      try {
        Thread.sleep(10);
      }
      catch (InterruptedException e) {
      }
      checkExpectedListeners(propertySupport, specificListeners, eventListener1, eventListener2, weakEventListener2);

      // remove everything
      propertySupport.removePropertyChangeListener(eventListener1);
      propertySupport.removePropertyChangeListener(eventListener2);
      propertySupport.removePropertyChangeListener(weakEventListener2);
      propertySupport.removePropertyChangeListener(TEST_PROPERTY, testPropertyListener);
      specificListeners.remove(testPropertyListener);
      checkExpectedListeners(propertySupport, specificListeners);
    }
  }

  private void checkExpectedListeners(BasicPropertySupport propertySupport, ArrayList<PropertyChangeListener> specificListeners, PropertyChangeListener... pcl) {
    assertEquals(pcl.length > 0, propertySupport.hasListeners(null));
    assertEquals(pcl.length, propertySupport.getPropertyChangeListeners().size());
    assertTrue(propertySupport.getPropertyChangeListeners().containsAll(Arrays.asList(pcl)));

    assertEquals(pcl.length > 0 || specificListeners.size() > 0, propertySupport.hasListeners(TEST_PROPERTY));
    if (specificListeners.size() > 0) {
      assertEquals(specificListeners.size(), propertySupport.getSpecificPropertyChangeListeners().get(TEST_PROPERTY).size());
      assertTrue(propertySupport.getSpecificPropertyChangeListeners().get(TEST_PROPERTY).containsAll(specificListeners));
    }
    else {
      assertTrue(propertySupport.getSpecificPropertyChangeListeners().isEmpty());
    }

  }
}
