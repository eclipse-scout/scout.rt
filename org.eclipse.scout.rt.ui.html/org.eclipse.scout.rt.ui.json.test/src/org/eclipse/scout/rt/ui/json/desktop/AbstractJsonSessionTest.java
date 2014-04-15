/**
 *
 */
package org.eclipse.scout.rt.ui.json.desktop;

import java.lang.ref.WeakReference;

import javax.servlet.http.HttpSessionBindingEvent;

import org.eclipse.scout.rt.ui.json.AbstractJsonSession;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.JsonRendererFactory;
import org.eclipse.scout.rt.ui.json.testing.JsonTestUtility;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(ScoutClientTestRunner.class)
public class AbstractJsonSessionTest {

  @BeforeClass
  public static void beforeClass() {
    JsonRendererFactory.init();
  }

  @Test
  public void testDispose() {
    AbstractJsonSession object = (AbstractJsonSession) JsonTestUtility.createAndInitializeJsonSession();
    WeakReference<IJsonSession> ref = new WeakReference<IJsonSession>(object);

    object.dispose();
    object = null;
    JsonTestUtility.assertGC(ref);
  }

  @Test
  public void testSessionInvalidation() {
    AbstractJsonSession object = (AbstractJsonSession) JsonTestUtility.createAndInitializeJsonSession();
    WeakReference<IJsonSession> ref = new WeakReference<IJsonSession>(object);

    HttpSessionBindingEvent event = Mockito.mock(HttpSessionBindingEvent.class);
    object.valueUnbound(event);
    Assert.assertFalse(object.getClientSession().isActive());

    object = null;
    JsonTestUtility.assertGC(ref);
  }

}
