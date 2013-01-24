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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.services.common.file.IFileService;
import org.eclipse.scout.service.SERVICES;

public class RemoteFileResourceLocator implements IResourceLocator {
  private String m_remoteFolder;

  public RemoteFileResourceLocator(String remoteFolder) {
    m_remoteFolder = remoteFolder;
  }

  @Override
  public void initialize() throws ProcessingException {
    SERVICES.getService(IFileService.class).syncRemoteFiles(m_remoteFolder, null);
  }

  @Override
  public File getFile(String name) throws ProcessingException {
    return SERVICES.getService(IFileService.class).getRemoteFile(m_remoteFolder, name, null, false);
  }

}
