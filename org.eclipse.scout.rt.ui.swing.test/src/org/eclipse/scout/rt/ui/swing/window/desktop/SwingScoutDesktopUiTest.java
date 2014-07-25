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
package org.eclipse.scout.rt.ui.swing.window.desktop;

import static org.junit.Assert.assertEquals;

import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.swing.AbstractSwingEnvironment;
import org.eclipse.scout.rt.ui.swing.SingleLayout;
import org.eclipse.scout.rt.ui.swing.ext.JInternalFrameEx;
import org.eclipse.scout.rt.ui.swing.window.desktop.layout.DefaultColumnSplitStrategy;
import org.eclipse.scout.rt.ui.swing.window.desktop.layout.IMultiSplitStrategy;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link SwingScoutDesktop}
 */
@RunWith(ScoutClientTestRunner.class)
public class SwingScoutDesktopUiTest {

  private SwingScoutDesktop m_desktop;

  @Before
  public void setup() throws Exception {
    m_desktop = setupDesktop();
  }

  /**
   * tests that the view sizes are correct when initially adding views to the desktop
   * <p>
   * <table border="1">
   * <tr>
   * <td>-</td>
   * <td>-</td>
   * <td>-</td>
   * </tr>
   * <tr>
   * <td>v10</td>
   * <td>v11</td>
   * <td>-</td>
   * </tr>
   * <tr>
   * <td>-</td>
   * <td>v21</td>
   * <td>-</td>
   * </tr>
   * </table>
   * </p>
   */
  @Test
  public void testSizeCorrectAfterInitialize() throws Exception {
    JInternalFrameEx v10 = createView("10");
    JInternalFrameEx v11 = createView("11");
    JInternalFrameEx v21 = createView("21");
    m_desktop.addView(v10, createEnvironment().getViewLayoutConstraintsFor(IForm.VIEW_ID_W));
    m_desktop.addView(v11, createEnvironment().getViewLayoutConstraintsFor(IForm.VIEW_ID_CENTER));
    m_desktop.addView(v21, createEnvironment().getViewLayoutConstraintsFor(IForm.VIEW_ID_S));

    assertEquals(new Dimension(250, 600), v10.getSize());
    assertEquals(new Dimension(550, 500), v11.getSize());
    assertEquals(new Dimension(550, 100), v21.getSize());
  }

  /**
   * Tests that the view sizes are correct when adding views and removing a view.
   * <p>
   * <table border="1">
   * <tr>
   * <td>-</td>
   * <td>-</td>
   * <td>-</td>
   * </tr>
   * <tr>
   * <td>v10</td>
   * <td>v11</td>
   * <td>v12</td>
   * </tr>
   * <tr>
   * <td>-</td>
   * <td>v21</td>
   * <td>-</td>
   * </tr>
   * </table>
   * </p>
   */
  @Test
  public void testSizeCorrectAfterViewRemove() throws Exception {
    JInternalFrameEx v10 = createView("10");
    JInternalFrameEx v11 = createView("11");
    JInternalFrameEx v12 = createView("12");
    JInternalFrameEx v21 = createView("21");
    m_desktop.addView(v10, createEnvironment().getViewLayoutConstraintsFor(IForm.VIEW_ID_W));
    m_desktop.addView(v11, createEnvironment().getViewLayoutConstraintsFor(IForm.VIEW_ID_CENTER));
    m_desktop.addView(v12, createEnvironment().getViewLayoutConstraintsFor(IForm.VIEW_ID_E));
    m_desktop.addView(v21, createEnvironment().getViewLayoutConstraintsFor(IForm.VIEW_ID_S));
    m_desktop.removeView(v12);

    assertEquals(new Dimension(250, 600), v10.getSize());
    assertEquals(new Dimension(550, 500), v11.getSize());
    assertEquals(new Dimension(550, 100), v21.getSize());
  }

  private SwingScoutDesktop setupDesktop() throws Exception {
    SwingScoutDesktop d = new SwingScoutDesktop();
    d.createField(new AbstractDesktop() {
    }, createEnvironment());
    showInWindow(d);
    return d;
  }

  private void showInWindow(SwingScoutDesktop desktop) {
    JFrame root = new JFrame();
    root.setVisible(true);
    root.add(desktop.getSwingDesktopPane());
    root.pack();
  }

  /**
   * @return a test view
   */
  private JInternalFrameEx createView(String name) {
    JInternalFrameEx view = new JInternalFrameEx(name, name, true, true, true);
    view.setLayout(new SingleLayout());
    view.getContentPane().add(new JScrollPane(new JTable(new DefaultTableModel(1000, 10)), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
    view.setVisible(true);
    view.setMinimumSize(new Dimension(40, 20));
    view.setPreferredSize(new Dimension(100, 100));
    view.setMaximizable(false);
    return view;
  }

  private AbstractSwingEnvironment createEnvironment() throws InterruptedException, InvocationTargetException, ExecutionException {
    Callable<AbstractSwingEnvironment> c = new Callable<AbstractSwingEnvironment>() {

      @Override
      public AbstractSwingEnvironment call() throws Exception {
        return new AbstractSwingEnvironment() {
        };
      }
    };
    FutureTask<AbstractSwingEnvironment> t = new FutureTask<AbstractSwingEnvironment>(c);
    SwingUtilities.invokeAndWait(t);
    return t.get();
  }

  class TestSwingScoutDesktop extends SwingScoutDesktop {

    @Override
    protected IMultiSplitStrategy createMultiSplitStrategy() {
      return new DefaultColumnSplitStrategy();
    }
  }

}
