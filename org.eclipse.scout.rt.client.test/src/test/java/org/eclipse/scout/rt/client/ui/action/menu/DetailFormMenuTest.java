package org.eclipse.scout.rt.client.ui.action.menu;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <h3>{@link DetailFormMenuTest}</h3>
 * <p>
 * Tests that resetting the detail form correctly connects the menus of the corresponding page instead of the menus
 * currently active on the outline.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class DetailFormMenuTest {
  @Test
  public void testMenuPropagationToDetailForm() {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    OutlineWithTwoDetailForms outline = new OutlineWithTwoDetailForms();
    desktop.setAvailableOutlines(Collections.singletonList(outline));
    desktop.activateOutline(outline);
    outline.getRootNode().ensureChildrenLoaded();

    PageWithDetailForm firstPage = (PageWithDetailForm) outline.getRootPage().getChildPage(0);
    PageWithDetailForm secondPage = (PageWithDetailForm) outline.getRootPage().getChildPage(1);

    outline.selectNode(firstPage);
    outline.selectNode(secondPage);

    firstPage.invokeDetailFormMenu(); // count 1 up for first page
    secondPage.invokeDetailFormMenu(); // count 1 up for second page

    // enforce re-connect of menus to new detail form
    firstPage.setDetailForm(new DetailForm());
    secondPage.setDetailForm(new DetailForm());

    firstPage.invokeDetailFormMenu(); // count 1 up for first page
    secondPage.invokeDetailFormMenu(); // count 1 up for second page

    // if the new detail forms have been correctly connected to their corresponding page, the second click on the menu changes the own page only
    assertEquals(2, secondPage.m_counter);
    assertEquals(2, firstPage.m_counter);
  }

  public static class OutlineWithTwoDetailForms extends AbstractOutline {
    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
      pageList.add(new PageWithDetailForm());
      pageList.add(new PageWithDetailForm());
    }
  }

  public static class PageWithDetailForm extends AbstractPageWithNodes {

    private int m_counter = 0;

    @Override
    protected Class<? extends IForm> getConfiguredDetailForm() {
      return DetailFormMenuTest.DetailForm.class;
    }

    /**
     * simulate click on the injected TestMenu within the detail form.
     */
    public void invokeDetailFormMenu() {
      getDetailForm().getRootGroupBox().getContextMenu().getChildActions().get(0).doAction();
    }

    @Order(1000)
    public class TestMenu extends AbstractMenu {
      @Override
      protected void execAction() {
        m_counter++;
      }
    }
  }

  public static class DetailForm extends AbstractForm {
    public class MainBox extends AbstractGroupBox {

    }
  }
}
