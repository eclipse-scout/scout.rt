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
package org.eclipse.scout.rt.spec.client.gen;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 */
public class SpecImageFilter implements FilenameFilter {
  private final String m_prefix;
  private static final String IMAGE_EXTENSION = ".jpg";

  /**
   *
   */
  public SpecImageFilter(String prefix) {
    m_prefix = prefix;
  }

  @Override
  public boolean accept(File dir, String name) {
    return name.startsWith(m_prefix) && name.endsWith(IMAGE_EXTENSION);
  }
}
