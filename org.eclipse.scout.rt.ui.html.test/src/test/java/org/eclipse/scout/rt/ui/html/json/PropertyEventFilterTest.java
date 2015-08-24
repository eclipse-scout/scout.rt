/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.junit.Assert;
import org.junit.Test;

public class PropertyEventFilterTest {

  private static final String MY_PROP = "myProp";
  private static final String FILTERED_VALUE = "filteredValue";

  @Test
  public void testValueFiltered() {
    final boolean[] called = {false};
    final PropertyEventFilter filter = new PropertyEventFilter();
    filter.addCondition(new PropertyChangeEventFilterCondition(MY_PROP, FILTERED_VALUE));

    PropertyChangeSupport support = new PropertyChangeSupport(this);
    support.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        PropertyChangeEvent filteredEvt = filter.filter(evt);
        Assert.assertNull(filteredEvt);
        called[0] = true;
      }
    });

    support.firePropertyChange(MY_PROP, null, FILTERED_VALUE);
    Assert.assertTrue("propertyChange() called", called[0]);
  }

  private void assertProperty(PropertyChangeEvent evt, String expectedValue) {
    Assert.assertNotNull(evt);
    Assert.assertEquals(MY_PROP, evt.getPropertyName());
    Assert.assertEquals(expectedValue, evt.getNewValue());
  }

  @Test
  public void testValueNotFiltered() {
    final boolean[] called = {false};
    final PropertyEventFilter filter = new PropertyEventFilter();
    filter.addCondition(new PropertyChangeEventFilterCondition(MY_PROP, FILTERED_VALUE));

    PropertyChangeSupport support = new PropertyChangeSupport(this);
    support.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        PropertyChangeEvent filteredEvt = filter.filter(evt);
        assertProperty(filteredEvt, "fooBar");
        called[0] = true;
      }
    });

    support.firePropertyChange(MY_PROP, null, "fooBar");
    Assert.assertTrue("propertyChange() called", called[0]);
  }

  /**
   * This test reproduces a problem where we filter a value, set another value (fooBar) which is _not_ filtered, and
   * finally change the value back to the original filtered value (theValue). Without the correct impl. The second
   * propertyChange to 'theValue' would be filtered, which would cause errors, since the value has been changed to
   * 'fooBar' in the meantime. So we must handle the value change event for 'theValue' although is is filtered.
   */
  @Test
  public void testChangeBackToFilteredValue() {
    final int[] numCalls = {0};
    final PropertyEventFilter filter = new PropertyEventFilter();
    filter.addCondition(new PropertyChangeEventFilterCondition(MY_PROP, FILTERED_VALUE));

    PropertyChangeSupport support = new PropertyChangeSupport(this);
    support.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        PropertyChangeEvent filteredEvt = filter.filter(evt);
        if (numCalls[0] == 0) {
          Assert.assertNull(filteredEvt);
        }
        else if (numCalls[0] == 1) {
          assertProperty(filteredEvt, "fooBar");
        }
        else if (numCalls[0] == 2) {
          Assert.assertNull(filteredEvt);
        }
        else if (numCalls[0] == 3) {
          assertProperty(filteredEvt, FILTERED_VALUE);
        }
        else if (numCalls[0] == 4) {
          Assert.assertNull(filteredEvt);
        }
        numCalls[0]++;
      }
    });

    support.firePropertyChange(MY_PROP, null, FILTERED_VALUE);
    support.firePropertyChange(MY_PROP, null, "fooBar");
    support.firePropertyChange(MY_PROP, null, "fooBar");
    support.firePropertyChange(MY_PROP, null, FILTERED_VALUE);
    support.firePropertyChange(MY_PROP, null, FILTERED_VALUE);

    Assert.assertEquals("propertyChange() number of calls", numCalls[0], 5);
  }
}
