package org.eclipse.scout.rt.client.deeplink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.BrowserHistoryEntry;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class OutlineDeepLinkHandlerTest {

  // used to generate a fletcher16 checksum from the class-name
  private static class P_OutlineFoo extends AbstractOutline {

    @Override
    public String getTitle() {
      return "Foo";
    }
  }

  private static class P_OutlineBar extends AbstractOutline {

    @Override
    public String getTitle() {
      return "Bar";
    }
  }

  @Test
  public void testCreateBrowserHistory() {
    OutlineDeepLinkHandler handler = new OutlineDeepLinkHandler();
    IOutline outline = new P_OutlineFoo();
    BrowserHistoryEntry entry = handler.createBrowserHistoryEntry(outline);
    assertEquals("outline-04446", entry.getDeepLinkPath());
    // title of outline is added to title of desktop (this string is used for the title in the browser-window)
    assertEquals("Test Environment Application - Foo", entry.getTitle());
    // title of outline is used to create the (i)nfo URL parameter
    assertEquals("./?dl=outline-04446&i=foo", entry.getPath());
  }

  /**
   * Checks if the OutlineHandler activates the correct outline for a given deep-link path.
   */
  @Test
  public void testHandleImpl() throws Exception {
    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
    IOutline outlineFoo = new P_OutlineFoo();
    IOutline outlineBar = new P_OutlineBar();

    addOutlineToDesktop(desktop, outlineFoo);
    addOutlineToDesktop(desktop, outlineBar);
    desktop.activateOutline(outlineBar);
    assertSame(outlineBar, desktop.getOutline());

    OutlineDeepLinkHandler handler = new OutlineDeepLinkHandler();
    handler.handle("outline-04446");
    assertSame(outlineFoo, desktop.getOutline());
  }

  /**
   * Adds an outline to the desktop by reflection.
   */
  private void addOutlineToDesktop(IDesktop desktop, IOutline outline) throws ReflectiveOperationException {
    AbstractDesktop ad = (AbstractDesktop) desktop;
    Field field = AbstractDesktop.class.getDeclaredField("m_availableOutlines");
    field.setAccessible(true);
    @SuppressWarnings("unchecked")
    List<IOutline> outlines = (List<IOutline>) field.get(ad);
    outlines.add(outline);
  }

}
