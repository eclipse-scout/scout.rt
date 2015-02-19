/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.pluginxml;

import org.eclipse.scout.rt.platform.Platform;

/**
 * The {@link IPluginXmlParser} will be triggered from {@link Platform#start()}. All plugin.xml and fragment.xml files
 * are visited and passed to all registered visitors.
 *
 * @since 5.1
 */
public interface IPluginXmlParser {

  /**
   * @param visitor
   */
  void visit(IPluginXmlVisitor visitor);

}
