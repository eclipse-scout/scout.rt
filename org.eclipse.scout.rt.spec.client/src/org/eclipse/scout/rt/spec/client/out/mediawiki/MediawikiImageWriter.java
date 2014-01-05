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
package org.eclipse.scout.rt.spec.client.out.mediawiki;

import java.io.IOException;
import java.io.Writer;

/**
 *
 */
public class MediawikiImageWriter {
  public static final String NEWLINE = System.getProperty("line.separator");
  private static final int IMAGE_SCALE = 500;

  private final Writer m_writer;

  /**
   * @param {@link Writer}
   */
  public MediawikiImageWriter(Writer writer) {
    m_writer = writer;
  }

  /**
   * @param imageName
   * @param scale
   *          in pixel
   * @throws IOException
   */
  public void appendImageLink(String imageName, int scale) throws IOException {
    m_writer.append("[[Image:" + imageName + "|" + scale + "px]]");
    m_writer.append(NEWLINE);
  }

  public void appendImages(String[] images) throws IOException {
    for (String image : images) {
      appendImageLink(image, IMAGE_SCALE);
    }
  }

}
