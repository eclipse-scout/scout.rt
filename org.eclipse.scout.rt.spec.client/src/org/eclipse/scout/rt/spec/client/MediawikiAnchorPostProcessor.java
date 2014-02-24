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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.spec.client.SpecIOUtility.IStringProcessor;
import org.eclipse.scout.rt.spec.client.link.LinkTarget;
import org.eclipse.scout.rt.spec.client.out.ILinkTarget;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiLinkTargetManager;

// TODO ASA javadoc MediawikiAnchorPostProcessor
public class MediawikiAnchorPostProcessor implements ISpecProcessor {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(MediawikiAnchorPostProcessor.class);

  @Override
  public void process() throws ProcessingException {
    if (!SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir().exists()) {
      LOG.warn("MediawikiDir does not exists! (" + SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir().getPath() + ")");
      return;
    }
    for (File wiki : SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir().listFiles()) {
      replaceScoutAnchors(wiki);
    }
  }

  // TODO ASA unittest
  private void replaceScoutAnchors(File wiki) throws ProcessingException {
    P_ScoutAnchorProcessor anchorProcessor = new P_ScoutAnchorProcessor();
    SpecIOUtility.process(wiki, anchorProcessor);
    MediawikiLinkTargetManager w = new MediawikiLinkTargetManager(SpecIOUtility.getSpecFileConfigInstance().getLinksFile());
    ArrayList<ILinkTarget> targets = new ArrayList<ILinkTarget>();
    for (String id : anchorProcessor.getAnchorsIds()) {
      targets.add(new LinkTarget(id, wiki.getName()));
    }
    w.writeLinks(targets);
  }

  protected static class P_ScoutAnchorProcessor implements IStringProcessor {
    private List<String> m_anchorIds = new ArrayList<String>();

    @Override
    public String processLine(String input) {
      Pattern pattern = Pattern.compile("(\\{\\{a:)([^}]+)(}})");
      Matcher matcher = pattern.matcher(input);
      StringBuilder sb = new StringBuilder();
      int index = 0;
      while (matcher.find()) {
        sb.append(input.substring(index, matcher.start(0)));
        sb.append("<span id=\"").append(matcher.group(2)).append("\"/>");
        m_anchorIds.add(matcher.group(2));
        index = matcher.end();
      }
      sb.append(input.substring(index));
      return sb.toString();
    }

    public List<String> getAnchorsIds() {
      return m_anchorIds;
    }
  }

}
