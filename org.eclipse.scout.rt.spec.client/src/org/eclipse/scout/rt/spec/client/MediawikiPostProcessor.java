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
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.spec.client.SpecIOUtility.IStringProcessor;
import org.eclipse.scout.rt.spec.client.gen.DocGenUtility;
import org.eclipse.scout.rt.spec.client.out.html.HtmlConverter;

/**
 * A post processor for mediawiki files which performs the following tasks:
 * <p>
 * <li>Replace link tags to point to the generated files.
 * <li>Convert all mediawiki files to html files
 */
public class MediawikiPostProcessor implements ISpecProcessor {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(MediawikiPostProcessor.class);

  protected HashMap<String, String> m_classIdTargets = new HashMap<String, String>();

  public MediawikiPostProcessor() throws ProcessingException {
    for (Class c : DocGenUtility.getAllDocEntityClasses()) {
      m_classIdTargets.put(c.getSimpleName(), SpecUtility.createAnchorId(ConfigurationUtility.getAnnotatedClassIdWithFallback(c)));
      m_classIdTargets.put(c.getName(), SpecUtility.createAnchorId(ConfigurationUtility.getAnnotatedClassIdWithFallback(c)));
    }
  }

  @Override
  public void process() throws ProcessingException {
    if (!SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir().exists()) {
      LOG.warn("MediawikiDir does not exists! (" + SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir().getPath() + ")");
      return;
    }

    SpecIOUtility.copyFilesFromAllSourceBundles(SpecIOUtility.getSpecFileConfigInstance().getHtmlDir(), SpecIOUtility.getSpecFileConfigInstance().getRelativeCssDirPath(), new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return SpecIOUtility.getSpecFileConfigInstance().getDefaultCssFileName().equals(name);
      }
    });
    File cssFile = new File(SpecIOUtility.getSpecFileConfigInstance().getHtmlDir(), SpecIOUtility.getSpecFileConfigInstance().getDefaultCssFileName());

    for (File wiki : SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir().listFiles()) {
      replaceLinks(wiki);
      File html = convertToHTML(wiki, cssFile);
      replaceWikiFileLinks(html);
    }
  }

  protected void replaceLinks(File mediawikiFile) throws ProcessingException {
    Properties links = SpecIOUtility.loadLinkPropertiesFile();
    SpecIOUtility.process(mediawikiFile, new P_LinkProcessor(links, m_classIdTargets, mediawikiFile.getName()));
  }

  /**
   * Processor for converting mediawiki links to qualified form (including filename)
   */
  protected static class P_LinkProcessor implements IStringProcessor {
    protected Properties m_links;
    private Map<String, String> m_classIdTargets;
    private String m_fileName;

    /**
     * @param links
     * @param fileName
     *          only used for logging
     */
    public P_LinkProcessor(Properties links, Map<String, String> classIdTargets, String fileName) {
      m_links = links;
      m_classIdTargets = classIdTargets;
      m_fileName = fileName;
    }

    @Override
    public String processLine(String input) {
      // $1=anchorId; $2=displayName
      Pattern pattern = Pattern.compile("\\[\\[([A-Za-z][A-Za-z0-9_\\$\\.-]+)\\|(.*?)]]");
      Matcher matcher = pattern.matcher(input);
      StringBuilder sb = new StringBuilder();
      int index = 0;
      while (matcher.find()) {
        sb.append(input.substring(index, matcher.start(0)));
        String propValue = resolveTargetInLinksProperties(matcher.group(1));
        if (propValue != null) {
          sb.append("[[").append(propValue).append("|").append(matcher.group(2)).append("]]");
        }
        else {
          sb.append(matcher.group(2));
          LOG.warn(m_fileName + ": Could not resolve link target:" + matcher.group(1) + ". Replacing with plain text: " + matcher.group(2));
        }
        index = matcher.end();
      }
      sb.append(input.substring(index));
      return sb.toString();
    }

    protected String resolveTargetInLinksProperties(String target) {
      // try direct
      String propValue = m_links.getProperty(target);
      if (propValue == null) {
        // maybe target is a classname for a known type: try to find it's classId-targetId
        String classIdTarget = m_classIdTargets.get(target);
        if (classIdTarget != null) {
          propValue = m_links.getProperty(classIdTarget);
        }
        else {
          // maybe target is a prefixed classname for a known type: try to find it's classId-targetId
          if (target.startsWith(LinearOutputPostProcessor.ANCHOR_PREFIX)) {
            String classIdTargetWithoutprefix = m_classIdTargets.get(target.substring(LinearOutputPostProcessor.ANCHOR_PREFIX.length()));
            String prefixedClassIdTarget = LinearOutputPostProcessor.ANCHOR_PREFIX + classIdTargetWithoutprefix;
            propValue = m_links.getProperty(prefixedClassIdTarget);
          }
        }
      }
      return propValue;
    }

    /**
     * @param group
     * @return
     */
    private String searchFormClassId(String group) {
      return null;
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

  protected File convertToHTML(File mediaWiki, File cssFile) throws ProcessingException {
    File htmlDir = SpecIOUtility.getSpecFileConfigInstance().getHtmlDir();
    File htmlFile = SpecIOUtility.createNewFile(htmlDir, mediaWiki.getName().replace(".mediawiki", ""), ".html");
    HtmlConverter htmlConverter = new HtmlConverter(cssFile);
    htmlConverter.convertWikiToHtml(mediaWiki, htmlFile);
    return htmlFile;
  }

}
