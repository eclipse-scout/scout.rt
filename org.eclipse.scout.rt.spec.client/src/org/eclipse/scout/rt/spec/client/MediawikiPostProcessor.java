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
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.spec.client.SpecIOUtility.IStringProcessor;
import org.eclipse.scout.rt.spec.client.out.html.HtmlConverter;
import org.eclipse.scout.rt.spec.client.out.html.TemplateUtility;

/**
 * A post processor for mediawiki files which performs the following tasks:
 * <p>
 * <li>Replace link tags to point to the generated files.
 * <li>Convert all mediawiki files to html files
 */
public class MediawikiPostProcessor implements ISpecProcessor {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(MediawikiPostProcessor.class);

  @Override
  public void process() throws ProcessingException {
    if (!SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir().exists()) {
      LOG.warn("MediawikiDir does not exists! (" + SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir().getPath() + ")");
      return;
    }
    for (File wiki : SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir().listFiles()) {
      replaceLinks(wiki);
      File html = convertToHTML(wiki);
      replaceWikiFileLinks(html);
    }
  }

  protected void replaceLinks(File mediawikiFile) throws ProcessingException {
    Properties links = SpecIOUtility.loadLinkPropertiesFile();
    SpecIOUtility.process(mediawikiFile, new P_LinkProcessor(links));
  }

  /**
   * Processor for converting mediawiki links to qualified form (including filename)
   */
  protected static class P_LinkProcessor implements IStringProcessor {
    private Properties m_links;

    /**
     * @param links
     */
    public P_LinkProcessor(Properties links) {
      m_links = links;
    }

    @Override
    public String processLine(String input) {
      // $1=anchorId; $2=displayName
      Pattern pattern = Pattern.compile("\\[\\[([A-Za-z][A-Za-z0-9_\\.-]+)\\|(.*?)]]");
      Matcher matcher = pattern.matcher(input);
      StringBuilder sb = new StringBuilder();
      int index = 0;
      while (matcher.find()) {
        sb.append(input.substring(index, matcher.start(0)));
        String propValue = m_links.getProperty(matcher.group(1));
        if (propValue != null) {
          sb.append("[[").append(propValue).append("|").append(matcher.group(2)).append("]]");
        }
        else {
          sb.append(matcher.group(2));
          LOG.warn(matcher.group(1) + " not found links properties, replacing with plain text: " + matcher.group(2));
        }
        index = matcher.end();
      }
      sb.append(input.substring(index));
      return sb.toString();
    }

    /**
     * @param anchorId
     * @return
     */
    protected String qualifiedAnchorId(String anchorId) {
      return anchorId;
    }
  }

  protected void replaceWikiFileLinks(File htmlFile) throws ProcessingException {
    HashMap<String, String> map = new HashMap<String, String>();
    map.put("/wiki/", "");
    map.put(".mediawiki", ".html");
    SpecIOUtility.replaceAll(htmlFile, map);
  }

  protected File convertToHTML(File mediaWiki) throws ProcessingException {
    File htmlDir = SpecIOUtility.getSpecFileConfigInstance().getHtmlDir();
    File htmlFile = SpecIOUtility.createNewFile(htmlDir, mediaWiki.getName().replace(".mediawiki", ""), ".html");

    // TODO ASA refactor: now css is copied for every file that is converted to html and can not be replaced by a custom css
    // copy css
    File css = new File(htmlDir, "default.css");
    TemplateUtility.copyDefaultCss(css);

    HtmlConverter htmlConverter = new HtmlConverter(css);
    htmlConverter.convertWikiToHtml(mediaWiki, htmlFile);
    return htmlFile;
  }

}
