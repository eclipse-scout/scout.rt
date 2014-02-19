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
package org.eclipse.scout.rt.spec.client;

import java.io.File;
import java.io.Writer;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.spec.client.config.DefaultDocConfig;
import org.eclipse.scout.rt.spec.client.config.IDocConfig;
import org.eclipse.scout.rt.spec.client.config.SpecFileConfig;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiWriter;

/**
 *
 */
public class AbstractSpecGen {

  protected SpecFileConfig getFileConfig() {
    return SpecIOUtility.getSpecFileConfigInstance();
  }

  protected IDocConfig getConfiguration() {
    return new DefaultDocConfig();
  }

  protected void write(IDocSection section, String id, String[] imagePaths, String simpleId) throws ProcessingException {
    File out = getFileConfig().getSpecDir();
    out.mkdirs();

    File wiki = SpecIOUtility.createNewFile(getFileConfig().getMediawikiDir(), id, ".mediawiki");
    Writer fileWriter = SpecIOUtility.createWriter(wiki);
    MediawikiWriter w = new MediawikiWriter(fileWriter, section, imagePaths);
    w.write();

  }

}
