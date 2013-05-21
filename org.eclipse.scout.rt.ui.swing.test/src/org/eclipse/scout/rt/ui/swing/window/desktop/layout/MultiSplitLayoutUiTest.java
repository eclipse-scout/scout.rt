/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.window.desktop.layout;

import java.awt.Dimension;
import java.awt.event.MouseEvent;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.eclipse.scout.rt.ui.swing.SingleLayout;
import org.eclipse.scout.rt.ui.swing.ext.JInternalFrameEx;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test for {@link MultiSplitLayout}
 */
public class MultiSplitLayoutUiTest {
  private JFrame frame;
  private JDesktopPane desktop;
  private JInternalFrame viewN;
  private JInternalFrame viewS;
  private JInternalFrame viewE;
  private JInternalFrame viewW;
  //
  private int size1Start;
  private int size2Start;
  private int size3Start;
  private int size1Mid;
  private int size2Mid;
  private int size3Mid;

  @Before
  public void setUp() throws Exception {
    swing(new Runnable() {
      @Override
      public void run() {
        frame = new JFrame(getClass().getSimpleName());
        desktop = new JDesktopPane();
        desktop.setDesktopManager(new MultiSplitDesktopManager());
        MultiSplitLayout layout = new MultiSplitLayout(new DefaultColumnSplitStrategy());
        desktop.setLayout(layout);
        //add 4 views at N, S, E, W
        viewN = createView("North");
        viewS = createView("South");
        viewE = createView("East");
        viewW = createView("West");
        desktop.add(viewN, new MultiSplitLayoutConstraints(0, 1, 40, new float[]{7, 9, 7, 6, 6, 5, 4, 3, 4}));
        desktop.add(viewS, new MultiSplitLayoutConstraints(2, 1, 60, new float[]{5, 3, 4, 5, 5, 4, 6, 9, 7}));
        desktop.add(viewE, new MultiSplitLayoutConstraints(1, 2, 80, new float[]{0, 0, 8, 0, 0, 9, 0, 0, 8}));
        desktop.add(viewW, new MultiSplitLayoutConstraints(1, 0, 20, new float[]{8, 0, 0, 9, 0, 0, 8, 0, 0}));
        frame.getContentPane().setLayout(new SingleLayout());
        frame.getContentPane().add(desktop);
        frame.pack();
        frame.setBounds(0, 0, 900, 600);
        frame.setVisible(true);
      }
    });
    swing(new Runnable() {
      @Override
      public void run() {
        resizeView(viewW, 0, 0, 300, 600);
        resizeView(viewE, 600, 0, 300, 600);
        resizeView(viewN, 300, 0, 300, 300);
        resizeView(viewS, 300, 300, 300, 300);
      }
    });
    Assert.assertEquals(900, frame.getWidth());
    Assert.assertEquals(600, frame.getHeight());
  }

  @After
  public void tearDown() throws Exception {
    swing(new Runnable() {
      @Override
      public void run() {
        frame.setVisible(false);
      }
    });
  }

  private JInternalFrame createView(String name) {
    JInternalFrame view = new JInternalFrameEx(name, name, true, true, true);
    view.setLayout(new SingleLayout());
    view.getContentPane().add(new JScrollPane(new JTable(new DefaultTableModel(1000, 10)), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
    view.setVisible(true);
    view.setMinimumSize(new Dimension(40, 10));
    view.setPreferredSize(new Dimension(800, 800));
    return view;
  }

  private void resizeView(JInternalFrame view, int x, int y, int w, int h) {
    //simulate mouse pressed on view
    view.dispatchEvent(new MouseEvent(view, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 20, 20, 1, false));
    desktop.getDesktopManager().resizeFrame(view, x, y, w, h);
    view.dispatchEvent(new MouseEvent(view, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 20, 20, 1, false));
  }

  private static void swing(Runnable r) throws Exception {
    SwingUtilities.invokeAndWait(r);
  }

  private static void assertEqualsLenient(int expected, int actual) {
    int d = Math.abs(expected - actual);
    Assert.assertTrue("difference less than 5", d < 5);
  }

  private int[] samples(int begin, int mid, int end) {
    int[] a = new int[Math.abs(mid - begin) + Math.abs(end - mid) + 1];
    int k = 0;
    if (begin == mid) {
      a[k++] = begin;
    }
    else {
      int d = (mid - begin) / Math.abs(mid - begin);
      int v = begin;
      while (true) {
        a[k++] = v;
        if (v == mid) {
          break;
        }
        v += d;
      }
    }
    if (end == mid) {
      //nop
    }
    else {
      int d = (end - mid) / Math.abs(end - mid);
      int v = mid + d;
      while (true) {
        a[k++] = v;
        if (v == end) {
          break;
        }
        v += d;
      }
    }
    return a;
  }

  @Test
  public void testHorizontalWindowResize() throws Exception {
    Assert.assertEquals(900, frame.getWidth());
    Assert.assertEquals(600, frame.getHeight());

    swing(new Runnable() {
      @Override
      public void run() {
        size1Start = viewW.getWidth();
        size2Start = viewN.getWidth();
        size3Start = viewE.getWidth();
      }
    });

    //expand, shrink
    for (int i : samples(0, 100, -100)) {
      final int delta = i;
      swing(new Runnable() {
        @Override
        public void run() {
          frame.setSize(900 + delta, 600);
        }
      });
      swing(new Runnable() {
        @Override
        public void run() {
          assertEqualsLenient(size1Start, viewW.getWidth());
          assertEqualsLenient(size2Start + delta, viewN.getWidth());
          assertEqualsLenient(size3Start, viewE.getWidth());
        }
      });
    }

    //set small
    swing(new Runnable() {
      @Override
      public void run() {
        frame.setSize(400, 600);
      }
    });

    swing(new Runnable() {
      @Override
      public void run() {
        size1Mid = viewW.getWidth();
        size2Mid = viewN.getWidth();
        size3Mid = viewE.getWidth();
      }
    });

    //shrink, expand
    for (int i : samples(0, -100, 100)) {
      final int delta = i;
      swing(new Runnable() {
        @Override
        public void run() {
          frame.setSize(400 + delta, 600);
        }
      });
      swing(new Runnable() {
        @Override
        public void run() {
          assertEqualsLenient(size1Mid + delta / 2, viewW.getWidth());
          assertEqualsLenient(size2Mid, viewN.getWidth());
          assertEqualsLenient(size3Mid + delta / 2, viewE.getWidth());
        }
      });
    }

    //back to normal
    swing(new Runnable() {
      @Override
      public void run() {
        frame.setSize(900, 600);
      }
    });
    swing(new Runnable() {
      @Override
      public void run() {
        assertEqualsLenient(size1Start, viewW.getWidth());
        assertEqualsLenient(size2Start, viewN.getWidth());
        assertEqualsLenient(size3Start, viewE.getWidth());
      }
    });
  }

  @Test
  public void testVerticalWindowResize() throws Exception {
    Assert.assertEquals(900, frame.getWidth());
    Assert.assertEquals(600, frame.getHeight());

    swing(new Runnable() {
      @Override
      public void run() {
        size1Start = viewN.getHeight();
        size2Start = viewS.getHeight();
      }
    });

    //expand, shrink
    for (int i : samples(0, 100, -100)) {
      final int delta = i;
      swing(new Runnable() {
        @Override
        public void run() {
          frame.setSize(900, 600 + delta);
        }
      });
      swing(new Runnable() {
        @Override
        public void run() {
          assertEqualsLenient(size1Start + delta, viewN.getHeight());
          assertEqualsLenient(size2Start, viewS.getHeight());
        }
      });
    }

    //set small
    swing(new Runnable() {
      @Override
      public void run() {
        frame.setSize(900, 200);
      }
    });

    swing(new Runnable() {
      @Override
      public void run() {
        size1Mid = viewN.getHeight();
        size2Mid = viewS.getHeight();
      }
    });

    //shrink
    for (int i : samples(0, -100, -100)) {
      final int delta = i;
      swing(new Runnable() {
        @Override
        public void run() {
          frame.setSize(900, 200 + delta);
        }
      });
      swing(new Runnable() {
        @Override
        public void run() {
          assertEqualsLenient(size1Mid, viewN.getHeight());
          assertEqualsLenient(size2Mid + delta, viewS.getHeight());
        }
      });
    }

    swing(new Runnable() {
      @Override
      public void run() {
        size1Mid = viewN.getHeight();
        size2Mid = viewS.getHeight();
      }
    });

    //expand
    for (int i : samples(-100, -100, 0)) {
      final int delta = i;
      swing(new Runnable() {
        @Override
        public void run() {
          frame.setSize(900, 200 + delta);
        }
      });
      swing(new Runnable() {
        @Override
        public void run() {
          assertEqualsLenient(size1Mid + delta + 100, viewN.getHeight());
          assertEqualsLenient(size2Mid, viewS.getHeight());
        }
      });
    }
  }

  @Test
  public void testHorizontalSplitDrag() {
    //Assert.fail();
  }

  @Test
  public void testVerticalSplitDrag() {
    //Assert.fail();
  }

}
