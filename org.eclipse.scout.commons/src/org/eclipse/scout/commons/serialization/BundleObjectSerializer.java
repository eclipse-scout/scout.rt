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
package org.eclipse.scout.commons.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.eclipse.scout.commons.osgi.BundleListClassLoader;
import org.eclipse.scout.commons.osgi.BundleObjectInputStream;

/**
 * {@link IObjectSerializer} that uses a {@link BundleObjectInputStream} for reading serialized data.
 * 
 * @since 3.8.2
 */
public class BundleObjectSerializer extends AbstractObjectSerializer {

  private final BundleListClassLoader m_classLoader;

  public BundleObjectSerializer(IObjectReplacer objectReplacer, BundleListClassLoader classLoader) {
    super(objectReplacer);
    m_classLoader = classLoader;
  }

  @Override
  protected ObjectInputStream createObjectInputStream(InputStream in, IObjectReplacer objectReplacer) throws IOException {
    return new BundleObjectInputStream(in, m_classLoader, objectReplacer);
  }
}
