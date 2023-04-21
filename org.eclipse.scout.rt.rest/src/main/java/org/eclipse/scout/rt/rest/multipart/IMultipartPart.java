/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.multipart;

import java.io.InputStream;
import java.util.Iterator;

import org.eclipse.scout.rt.platform.util.IOUtility;

/**
 * A part as returned by the {@link Iterator} of {@link IMultipartMessage}.
 * <p>
 * <b>Note</b>: try-finally is required, {@link AutoCloseable} is used to close the {@link #getInputStream()}.
 */
public interface IMultipartPart extends AutoCloseable {

  /**
   * @return Name of the part (always available).
   */
  String getPartName();

  /**
   * @return Filename (optional), only available for a file field part.
   */
  String getFilename();

  /**
   * @return Content type (optional), only available for a file field part.
   */
  String getContentType();

  /**
   * To read a text field part value, {@link IOUtility#readString(InputStream, String)} or
   * {@link IOUtility#readStringUTF8(InputStream)} may be used.
   *
   * @return The stream to consume the content from.
   */
  InputStream getInputStream();
}
