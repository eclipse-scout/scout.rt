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

/**
 * Factory for creating {@link BasicObjectSerializer} instances.
 * 
 * @since 3.8.2
 */
public class BasicObjectSerializerFactory implements IObjectSerializerFactory {

  @Override
  public IObjectSerializer createObjectSerializer(IObjectReplacer objectReplacer) {
    return new BasicObjectSerializer(objectReplacer);
  }

  @Override
  public ClassLoader getClassLoader() {
    return BasicObjectSerializer.class.getClassLoader();
  }
}
