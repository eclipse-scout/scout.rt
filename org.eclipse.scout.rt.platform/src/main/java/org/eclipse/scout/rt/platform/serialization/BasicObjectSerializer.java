/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.serialization;

/**
 * This {@link IObjectSerializer} implementation is designed to be used outside an OSGi environment. All classes are
 * expected to be loaded by the application class loader.
 *
 * @since 3.8.2
 */
public class BasicObjectSerializer extends AbstractObjectSerializer {

  public BasicObjectSerializer(IObjectReplacer objectReplacer) {
    super(objectReplacer);
  }
}
