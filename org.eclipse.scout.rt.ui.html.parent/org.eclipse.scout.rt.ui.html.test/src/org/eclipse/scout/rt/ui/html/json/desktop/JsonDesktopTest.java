/**
 *
 */
package org.eclipse.scout.rt.ui.html.json.desktop;

import java.lang.ref.WeakReference;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonDesktop;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.DesktopWithOneOutline;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.DesktopWithOutlineForms;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class JsonDesktopTest {

  @Test
  public void testDisposeWithoutForms() {
    IDesktop desktop = new DesktopWithOneOutline();
    JsonDesktop object = createJsonDesktopWithMocks(desktop);
    WeakReference<JsonDesktop> ref = new WeakReference<JsonDesktop>(object);

    object.dispose();
    object = null;
    JsonTestUtility.assertGC(ref);
  }

  @Test
  public void testDisposeWithForms() {
    IDesktop desktop = new DesktopWithOutlineForms();
    JsonDesktop object = createJsonDesktopWithMocks(desktop);
    WeakReference<JsonDesktop> ref = new WeakReference<JsonDesktop>(object);

    object.dispose();
    object = null;
    JsonTestUtility.assertGC(ref);
  }

  public static JsonDesktop createJsonDesktopWithMocks(IDesktop desktop) {
    JsonSessionMock jsonSession = new JsonSessionMock();
    JsonDesktop jsonDesktop = new JsonDesktop(desktop, jsonSession, "1");
    jsonDesktop.init();
    return jsonDesktop;
  }
}
