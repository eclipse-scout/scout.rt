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
package org.eclipse.scout.rt.spec.client.out.mediawiki;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.spec.client.SpecIOUtility;
import org.eclipse.scout.rt.spec.client.out.ILinkTarget;

/**
 * Responsible for persisting links to a property file
 */
public class MediawikiLinkTargetManager {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MediawikiLinkTargetManager.class);

  private final File m_propertyFile;

  public MediawikiLinkTargetManager(File propertyFile) {
    m_propertyFile = propertyFile;
  }

  /**
   * @param links
   * @throws ProcessingException
   */
  public void writeLinks(Collection<? extends ILinkTarget> links) throws ProcessingException {
    //TODO check already defined links
    Properties p = new Properties();
    for (ILinkTarget t : links) {
      p.put(t.getTargetId(), t.getDisplayName());
    }
    storeProperties(p);
  }

  public Properties readLinks() throws ProcessingException {
    return SpecIOUtility.loadProperties(m_propertyFile);
  }

  private void storeProperties(Properties p) throws ProcessingException {
    FileWriter writer = null;
    try {
      writer = new FileWriter(m_propertyFile, true);
      p.store(writer, "");
    }
    catch (IOException e) {
      throw new ProcessingException("Error storing properties", e);
    }
    finally {
      if (writer != null) {
        try {
          writer.close();
        }
        catch (IOException e) {
          //nop
        }
      }
    }
  }

}
