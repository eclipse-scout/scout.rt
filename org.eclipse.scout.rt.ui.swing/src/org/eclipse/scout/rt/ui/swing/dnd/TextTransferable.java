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
package org.eclipse.scout.rt.ui.swing.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Transferable with several text flavors supported
 */
public class TextTransferable implements Transferable {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TextTransferable.class);

  private Map<DataFlavor, Object> m_flavorMap = new HashMap<DataFlavor, Object>();

  public TextTransferable(String plainText, String htmlText) {
    registerIfUndefined(DataFlavor.stringFlavor, plainText);

    // charset
    String charset = DataFlavor.getTextPlainUnicodeFlavor().getParameter("charset");
    if (charset == null) {
      charset = "unicode";
    }

    // flavor text/plain
    registerIfUndefined(createFlavor("text/plain;charset=" + charset + ";class=java.io.InputStream"), toInputStream(plainText, charset));

    // flavor text/html
    if (StringUtility.hasText(htmlText)) {
      registerIfUndefined(createFlavor("text/html;charset=" + charset + ";class=java.io.InputStream"), toInputStream(htmlText, charset));
    }
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return m_flavorMap.keySet().toArray(new DataFlavor[m_flavorMap.size()]);
  }

  @Override
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return m_flavorMap.containsKey(flavor);
  }

  @Override
  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (!isDataFlavorSupported(flavor)) {
      throw new UnsupportedFlavorException(flavor);
    }
    return m_flavorMap.get(flavor);
  }

  private InputStream toInputStream(String value, String encoding) {
    byte[] content = StringUtility.emptyIfNull(value).getBytes(Charset.forName(encoding));
    return new ByteArrayInputStream(content);
  }

  private void registerIfUndefined(DataFlavor flavor, Object value) {
    if (flavor == null || value == null || m_flavorMap.containsKey(flavor)) {
      return;
    }
    m_flavorMap.put(flavor, value);
  }

  private DataFlavor createFlavor(String mimeType) {
    try {
      return new DataFlavor(mimeType);
    }
    catch (Throwable e) {
      LOG.error("failed to create data flavor", e);
    }
    return null;
  }
}
