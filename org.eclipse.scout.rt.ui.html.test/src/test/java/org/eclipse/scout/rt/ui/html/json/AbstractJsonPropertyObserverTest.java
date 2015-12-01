/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractJsonPropertyObserverTest {

  private static final String PROP_FOOBAR = "fooBar";
  private static final String ID = "fooId";

  private IUiSession m_session = new UiSessionMock();
  private IPropertyObserver m_model = Mockito.mock(IPropertyObserver.class);

  class P_Observer extends AbstractJsonPropertyObserver<IPropertyObserver> {

    private boolean m_handled;

    public P_Observer() {
      super(m_model, m_session, ID, null);
    }

    @Override
    public void initJsonProperties(IPropertyObserver model) {
      putJsonProperty(new JsonProperty<IPropertyObserver>(PROP_FOOBAR, model) {
        @Override
        protected Object modelValue() {
          return "baz";
        }

        @Override
        public void handlePropertyChange(Object oldValue, Object newValue) {
          Assert.assertEquals("baz", newValue);
          m_handled = true;
        }
      });
    }

    @Override
    public String getObjectType() {
      return "FooType";
    }

    @Override
    protected void handleModelPropertyChange(String propertyName, Object oldValue, Object newValue) {
      Assert.fail("Must not be called, since a JsonProperty is registered");
    }
  }

  private P_Observer m_observer = new P_Observer();

  @Before
  public void setUp() {
    m_observer.init();
  }

  /**
   * Test property change without any filters (normal case).
   */
  @Test
  public void testPropertyChange() throws Exception {
    PropertyChangeEvent pce = new PropertyChangeEvent(m_model, PROP_FOOBAR, null, "baz");
    m_observer.handleModelPropertyChange(pce);
    Assert.assertTrue("handlePropertyChange must be called", m_observer.m_handled);
    String foobar = JsonTestUtility.extractProperty(m_session.currentJsonResponse(), ID, PROP_FOOBAR);
    Assert.assertEquals("baz", foobar);
  }

  /**
   * onPropertyChange must be called even though event is filtered.
   */
  @Test
  public void testPropertyChange_WithFilter() throws Exception {
    PropertyChangeEvent pce = new PropertyChangeEvent(m_model, PROP_FOOBAR, null, "baz");
    m_observer.addPropertyEventFilterCondition(PROP_FOOBAR, "baz");
    m_observer.handleModelPropertyChange(pce);
    Assert.assertTrue("handlePropertyChange must be called", m_observer.m_handled);
    String foobar = JsonTestUtility.extractProperty(m_session.currentJsonResponse(), ID, PROP_FOOBAR);
    Assert.assertNull(foobar);
  }

}
