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
package org.eclipse.scout.commons.beans;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.beans.fixture.WeakPropertyChangeListener;
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

}
