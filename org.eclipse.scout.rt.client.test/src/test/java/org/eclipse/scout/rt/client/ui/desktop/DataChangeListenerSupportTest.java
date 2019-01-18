package org.eclipse.scout.rt.client.ui.desktop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.eclipse.scout.rt.client.ui.DataChangeListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DataChangeListenerSupportTest {

  private DataChangeListenerSupport m_support;
  private DataChangeListener m_listener;

  @Before
  public void before() {
    m_support = new DataChangeListenerSupport();
    m_listener = Mockito.mock(DataChangeListener.class);
  }

  @Test
  public void testRegisterNullListener() {
    m_support.addDataChangeListener(null);
    assertEquals(1, m_support.getListenersByDataType().size());
    assertEquals(0, m_support.getListenersByDataType().get(null).getListenerCount(DataChangeListener.class));

    // unregister
    m_support.removeDataChangeListener(null);
    assertEquals(Collections.emptyMap(), m_support.getListenersByDataType());
  }

  @Test
  public void testRegisterListener() {
    m_support.addDataChangeListener(m_listener);
    assertEquals(1, m_support.getListenersByDataType().size());
    assertEquals(1, m_support.getListenersByDataType().get(null).getListenerCount(DataChangeListener.class));

    // unregister
    m_support.removeDataChangeListener(m_listener);
    assertEquals(Collections.emptyMap(), m_support.getListenersByDataType());
  }

  @Test
  public void testRegisterTypedListener() {
    m_support.addDataChangeListener(m_listener, String.class, null, Long.class);
    assertEquals(2, m_support.getListenersByDataType().size());
    assertEquals(1, m_support.getListenersByDataType().get(String.class).getListenerCount(DataChangeListener.class));
    assertEquals(1, m_support.getListenersByDataType().get(Long.class).getListenerCount(DataChangeListener.class));

    // unregister with type
    m_support.removeDataChangeListener(m_listener, String.class);
    assertEquals(1, m_support.getListenersByDataType().size());
    assertEquals(1, m_support.getListenersByDataType().get(Long.class).getListenerCount(DataChangeListener.class));

    // unregister without type
    m_support.removeDataChangeListener(m_listener);
    assertEquals(Collections.emptyMap(), m_support.getListenersByDataType());
  }

  @Test
  public void testSetBuffering() {
    // initial false
    assertFalse(m_support.isBuffering());

    // set to true
    m_support.setBuffering(true);
    assertTrue(m_support.isBuffering());

    // again set to true, should not have any side effects
    m_support.setBuffering(true);
    assertTrue(m_support.isBuffering());

    // set to false is expected to work directly (i.e. not the same number of false needed as true has been issued)
    m_support.setBuffering(false);
    assertFalse(m_support.isBuffering());
  }

  @Test
  public void testEventBuffering() {
    // add mock listener
    m_support.addDataChangeListener(m_listener, String.class);

    // issue data changed, listener is expected to be notified
    m_support.dataChanged(String.class);
    Mockito.verify(m_listener).dataChanged(String.class);
    Mockito.verifyNoMoreInteractions(m_listener);
    Mockito.reset(m_listener);

    // buffer events, issue data change, listener is not expected to be notified
    m_support.setBuffering(true);
    m_support.dataChanged(String.class);
    Mockito.verifyNoMoreInteractions(m_listener);

    // issue same event twice (only one notification is expected in the next part)
    m_support.dataChanged(String.class);
    Mockito.verifyNoMoreInteractions(m_listener);

    // disable buffering, only one event is expected
    m_support.setBuffering(false);
    Mockito.verify(m_listener).dataChanged(String.class);
    Mockito.verifyNoMoreInteractions(m_listener);
  }
}
