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
package org.eclipse.scout.rt.client.services.common.spellchecker;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.services.common.file.IFileService;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.Bundle;

public class BundleResourceLocator implements IResourceLocator {
  private Bundle m_bundle;
  private String m_folder;

  /**
   * @param folder
   *          for example /resources/spellchecker
   */
  public BundleResourceLocator(Bundle bundle, String folder) {
    m_bundle = bundle;
    m_folder = folder;
  }

  @Override
  public void initialize() throws ProcessingException {
  }

  @Override
  public File getFile(String name) throws ProcessingException {
    File f = SERVICES.getService(IFileService.class).getLocalFileLocation(m_folder, name);
    f.delete();
    URL url = m_bundle.getResource(m_folder + "/" + name);
    if (url != null) {
      try {
        IOUtility.writeContent(f.getAbsolutePath(), IOUtility.getContent(url.openStream()));
      }
      catch (IOException e) {
        throw new ProcessingException("reading bundle resource " + m_bundle.getSymbolicName() + ":" + m_folder + "/" + name);
      }
    }
    return f;
  }

}
