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
import java.util.Properties;

import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;
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

  private final MediaWikiLanguage m_lang;
  private final Properties m_props;
  private final String[] m_images;
  private final IDocSection m_section;

  private final Writer m_indexFileWriter;
  private final Writer m_wikiWriter;

  public MediawikiWriter(Writer indexFileWriter, Writer wikiWriter, IDocSection section, String[] images) {
    m_tableWriter = new MediawikiTableWriter(wikiWriter);
    m_imageWriter = new MediawikiImageWriter(wikiWriter);
    m_indexFileWriter = indexFileWriter;
    m_wikiWriter = wikiWriter;
    m_section = section;
    m_images = images.clone();
    m_props = new Properties();
    m_lang = new MediaWikiLanguage();
  }

  /**
   * Writes the section to a file
   * 
   * @throws ProcessingException
   */
  public void write() throws ProcessingException {
    try {
      LOG.info("writing section " + m_section.getId());
      appendSection(m_section, 2, true);
      LOG.info("writing link properties for " + m_section.getId());
      m_props.store(m_indexFileWriter, "link IDs");
      m_wikiWriter.flush();
    }
    catch (IOException e) {
      String errorText = "Unable to write wikimedia file for " + m_section.getId();
      LOG.error(errorText);
      throw new ProcessingException(errorText, e);
    }
    finally {
      try {
        m_wikiWriter.close();
      }
      catch (IOException e) {
        //
      }
    }
  }

  public void appendSection(IDocSection section, int headingLevel, boolean appendImages) throws IOException {
    if (section != null && (hasSubsections(section) || hasTableCellTexts(section))) {
      LOG.info("writing section " + section.getId());
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

  /**
   * @param fieldWriter
   * @param section
   * @throws IOException
   */
  private void appendSection(IDocSection section, int headingLevel) throws IOException {
    appendSection(section, headingLevel, false);
  }

  /**
   * @throws IOException
   */
  private void appendImages() throws IOException {
    m_imageWriter.appendImages(m_images);
  }

  /**
   * @param section
   * @param headingLevel
   * @throws IOException
   */
  private void appendHeading(IDocSection section, int headingLevel) throws IOException {
    if (section.getTitle() != null) {
      writeHeading(section.getTitle(), section.getId(), headingLevel);
    }
  }

  private void appendTable(IDocSection section) throws IOException {
    if (hasTableCellTexts(section)) {
      if (hasSubsections(section)) {
        m_tableWriter.appendTableTransposed(section.getTable());
      }
      else {
        m_tableWriter.appendTable(section.getTable());
      }
    }
  }

  private boolean hasSubsections(IDocSection section) {
    return section.getSubSections() != null && section.getSubSections().length > 0;
  }

  private boolean hasTableCellTexts(IDocSection section) {
    return section.getTable() != null && section.getTable().getCellTexts() != null && section.getTable().getCellTexts().length > 0;
  }

  private void writeHeading(String heading, String headingId, int level) throws IOException {
    m_tableWriter.appendHeading(heading, level);
    if (headingId != null) {
      String link = m_lang.getIdGenerationStrategy().generateId(heading);
      m_props.put(headingId, link);
    }
  }

}
