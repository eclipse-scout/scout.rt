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

import java.io.IOException;
import java.io.Writer;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.spec.client.out.IDocSection;

/**
 * Writer for {@link IDocSection} in mediawiki format
 */
public class MediawikiWriter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MediawikiWriter.class);

  private final MediawikiTableWriter m_tableWriter;
  private final MediawikiImageWriter m_imageWriter;

  private final String[] m_images;
  private final IDocSection m_section;

  private final Writer m_wikiWriter;

  public MediawikiWriter(Writer wikiWriter, IDocSection section, String[] images) {
    m_tableWriter = new MediawikiTableWriter(wikiWriter);
    m_imageWriter = new MediawikiImageWriter(wikiWriter);
    m_wikiWriter = wikiWriter;
    m_section = section;
    m_images = images.clone();
  }

  /**
   * Writes the section to a file
   * 
   * @throws ProcessingException
   */
  public void write() throws ProcessingException {
    try {
      LOG.info("writing section " + m_section.getTitle());
      appendSection(m_section, 2, true);
      m_wikiWriter.flush();
    }
    catch (IOException e) {
      String errorText = "Unable to write wikimedia file for " + m_section.getTitle();
      LOG.error(errorText);
      throw new ProcessingException(errorText, e);
    }
    finally {
      try {
        m_wikiWriter.close();
      }
      catch (IOException e) {
        //nop
      }
    }
  }

  private void appendSection(IDocSection section, int headingLevel, boolean appendImages) throws IOException {
    if (section.isDisplayed()) {
      LOG.info("writing section " + section.getTitle());
      appendHeading(section, headingLevel);
      appendTable(section);
      if (appendImages) {
        appendImages();
      }
      if (section.getSubSections() != null) {
        for (IDocSection subSection : section.getSubSections()) {
          appendSection(subSection, headingLevel + 1);
        }
      }
    }
  }

  private void appendSection(IDocSection section, int headingLevel) throws IOException {
    appendSection(section, headingLevel, false);
  }

  private void appendImages() throws IOException {
    m_imageWriter.appendImages(m_images);
  }

  private void appendHeading(IDocSection section, int headingLevel) throws IOException {
    if (section.getTitle() != null) {
      m_tableWriter.appendHeading(section.getTitle(), headingLevel);
    }
  }

  private void appendTable(IDocSection section) throws IOException {
    if (section.hasTableCellTexts()) {
      if (section.getTable().isTransposedLayout()) {
        m_tableWriter.appendTableTransposed(section.getTable());
      }
      else {
        m_tableWriter.appendTable(section.getTable());
      }
    }
  }

}
