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

import java.util.EventListener;

import javax.xml.xpath.XPath;

import org.eclipse.scout.rt.platform.pluginxml.internal.IPluginXml;
import org.eclipse.scout.rt.platform.pluginxml.internal.PluginXmlParser;
import org.w3c.dom.Document;

/**
 * A {@link IPluginXmlVisitor} can be added to the {@link PluginXmlParser}.
 * The visitor must be added during module startup.
 */
public interface IPluginXmlVisitor extends EventListener {

  /**
   * To parse some elements out of the xml and prefill registrys. In case of classloading use the
   * {@link IPluginXml#loadClass(String)} method.
   *
   * @param xmlFile
   *          the current plugin.xml file.
   * @param xmlDoc
   *          the xml document to access e.g. with {@link XPath}
   */
  void visit(IPluginXml xmlFile, Document xmlDoc);
}
