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
package org.eclipse.scout.rt.ui.swt.ext;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

/**
 *
 */
public class ImageViewerUiTest {
  protected static final int IMAGE_FIELD_SIZE = 200;
  protected static final int IMAGE_WIDTH = 100;
  protected static final int IMAGE_HEIGHT = 50;

  protected static final int LEFT = -1;
  protected static final int CENTER = 0;
  protected static final int RIGHT = 1;

  protected static final int TOP = -1;
  protected static final int MIDDLE = 0;
  protected static final int BOTTOM = 1;

  protected static final Rectangle BOUNDS = new Rectangle(0, 0, IMAGE_FIELD_SIZE, IMAGE_FIELD_SIZE);

  @Test
  public void testImageLocationAutoFit() throws Exception {
    ImageViewer imageViewer = new ImageViewer(createComposite());
    imageViewer.setAutoFit(true);
    assertEquals(new Point(50, 0), imageViewer.getImageLocation(BOUNDS, new Rectangle(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)));
  }

  private Composite createComposite() {
    return new Shell(Display.getDefault());
  }

  @Test
  public void testImageLocationTopLeft() throws Exception {
    testImageViewer(LEFT, TOP, true, new Point(ImageViewer.FOCUS_BORDER_OFFSET_PX, ImageViewer.FOCUS_BORDER_OFFSET_PX));
  }

  @Test
  public void testImageLocationTopRight() throws Exception {
    testImageViewer(RIGHT, TOP, true, new Point(IMAGE_FIELD_SIZE - IMAGE_WIDTH - 2 * ImageViewer.FOCUS_BORDER_OFFSET_PX, ImageViewer.FOCUS_BORDER_OFFSET_PX));
  }

  @Test
  public void testImageLocationBottomLeft() throws Exception {
    testImageViewer(LEFT, BOTTOM, true, new Point(ImageViewer.FOCUS_BORDER_OFFSET_PX, IMAGE_FIELD_SIZE - IMAGE_HEIGHT - 2 * ImageViewer.FOCUS_BORDER_OFFSET_PX));
  }

  @Test
  public void testImageLocationBottomRight() throws Exception {
    testImageViewer(RIGHT, BOTTOM, true, new Point(IMAGE_FIELD_SIZE - IMAGE_WIDTH - 2 * ImageViewer.FOCUS_BORDER_OFFSET_PX, IMAGE_FIELD_SIZE - IMAGE_HEIGHT - 2 * ImageViewer.FOCUS_BORDER_OFFSET_PX));
  }

  @Test
  public void testImageLocationCenter() throws Exception {
    testImageViewer(CENTER, MIDDLE, true, new Point((IMAGE_FIELD_SIZE - IMAGE_WIDTH - 2 * ImageViewer.FOCUS_BORDER_OFFSET_PX) / 2, (IMAGE_FIELD_SIZE - IMAGE_HEIGHT - 2 * ImageViewer.FOCUS_BORDER_OFFSET_PX) / 2));
  }

  @Test
  public void testImageLocationNoFocus() throws Exception {
    testImageViewer(LEFT, TOP, false, new Point(0, 0));
  }

  private void testImageViewer(int horizontalAlign, int verticalAlign, boolean focusable, Point expectedPos) {
    ImageViewer imageViewer = new ImageViewer(createComposite());
    imageViewer.setAutoFit(false);
    imageViewer.setAlignmentX(SwtUtility.getHorizontalAlignment(horizontalAlign));
    imageViewer.setAlignmentY(SwtUtility.getVerticalAlignment(verticalAlign));
    imageViewer.setFocusable(focusable);
    imageViewer.setBounds(0, 0, IMAGE_FIELD_SIZE, IMAGE_FIELD_SIZE);
    assertEquals(expectedPos, imageViewer.getImageLocation(BOUNDS, new Rectangle(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)));
  }
}
