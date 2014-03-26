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
package org.eclipse.scout.rt.ui.swing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.datatransfer.DataFlavor;

import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.junit.Test;

/**
 * Test for {@link SwingUtility}
 */
public class SwingUtilityTest {
  @Test
  public void testSupportedTransfer() throws ClassNotFoundException {
    DataFlavor text = DataFlavor.stringFlavor;
    DataFlavor java = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
    DataFlavor image = DataFlavor.imageFlavor;
    DataFlavor file = DataFlavor.javaFileListFlavor;

    assertFalse(SwingUtility.isSupportedTransfer(0, new DataFlavor[]{text}));
    assertFalse(SwingUtility.isSupportedTransfer(0, null));
    assertFalse(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_TEXT_TRANSFER, null));
    assertFalse(SwingUtility.isSupportedTransfer(10, new DataFlavor[]{text}));

    assertTrue(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_TEXT_TRANSFER, new DataFlavor[]{text}));
    assertFalse(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_IMAGE_TRANSFER, new DataFlavor[]{text}));
    assertFalse(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_FILE_TRANSFER, new DataFlavor[]{text}));
    assertFalse(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_JAVA_ELEMENT_TRANSFER, new DataFlavor[]{text}));

    assertFalse(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_TEXT_TRANSFER, new DataFlavor[]{java}));
    assertFalse(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_IMAGE_TRANSFER, new DataFlavor[]{java}));
    assertFalse(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_FILE_TRANSFER, new DataFlavor[]{java}));
    assertTrue(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_JAVA_ELEMENT_TRANSFER, new DataFlavor[]{java}));

    assertFalse(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_TEXT_TRANSFER, new DataFlavor[]{image}));
    assertTrue(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_IMAGE_TRANSFER, new DataFlavor[]{image}));
    assertFalse(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_FILE_TRANSFER, new DataFlavor[]{image}));
    assertFalse(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_JAVA_ELEMENT_TRANSFER, new DataFlavor[]{image}));

    assertFalse(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_TEXT_TRANSFER, new DataFlavor[]{file}));
    assertFalse(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_IMAGE_TRANSFER, new DataFlavor[]{file}));
    assertTrue(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_FILE_TRANSFER, new DataFlavor[]{file}));
    assertFalse(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_JAVA_ELEMENT_TRANSFER, new DataFlavor[]{file}));

    assertTrue(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_TEXT_TRANSFER | IDNDSupport.TYPE_FILE_TRANSFER, new DataFlavor[]{text, file}));
    assertFalse(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_JAVA_ELEMENT_TRANSFER | IDNDSupport.TYPE_IMAGE_TRANSFER, new DataFlavor[]{text, file}));

    assertTrue(SwingUtility.isSupportedTransfer(IDNDSupport.TYPE_TEXT_TRANSFER | IDNDSupport.TYPE_FILE_TRANSFER | IDNDSupport.TYPE_IMAGE_TRANSFER | IDNDSupport.TYPE_JAVA_ELEMENT_TRANSFER, new DataFlavor[]{text, file, java, image}));
  }
}
