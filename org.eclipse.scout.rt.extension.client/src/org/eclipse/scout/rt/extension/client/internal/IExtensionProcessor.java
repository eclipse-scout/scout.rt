/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.osgi.framework.Bundle;

/**
 * Processor for scout extensions. This class is used by {@link AbstractExtensionManager} for parsing configuration
 * elements.
 * 
 * @see AbstractExtensionManager#addExtensionProcessor(String, IExtensionProcessor)
 * @since 3.9.0
 */
public interface IExtensionProcessor<T> {

  /**
   * Parses the given configuration element and returns its object representation that can be used for caching. The
   * configuration element is neglected if this method throws an Exception.
   * 
   * @param contributor
   *          the contributing bundle
   * @param element
   *          the configuration element to parse
   * @return an object representation of the given configuration element.
   * @throws Exception
   */
  T processConfigurationElement(Bundle contributor, IConfigurationElement element) throws Exception;
}
