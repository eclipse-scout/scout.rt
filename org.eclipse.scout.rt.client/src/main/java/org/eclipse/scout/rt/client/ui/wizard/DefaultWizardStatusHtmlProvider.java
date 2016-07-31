/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.wizard;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.services.common.icon.IconLocator;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultWizardStatusHtmlProvider implements IWizardStatusHtmlProvider {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultWizardStatusHtmlProvider.class);

  private String m_htmlTemplate;

  /**
   * initialize, load html template and inline images
   */
  @Override
  public void initialize(AbstractWizardStatusField htmlField) {
    m_htmlTemplate = initHtmlTemplate();

    // collect attachments for HTML field
    List<BinaryResource> attachments = collectAttachments();
    if (attachments != null && attachments.size() > 0) {
      htmlField.setAttachments(attachments);
    }
  }

  @Override
  public String initHtmlTemplate() {
    try (InputStream in = org.eclipse.scout.rt.client.ResourceBase.class.getResource("html/defaultWizardStatus.html").openStream()) {
      return IOUtility.readString(in, "iso-8859-1");
    }
    catch (Exception t) {
      throw new ProcessingException("Unexpected", t);
    }
  }

  protected String getHtmlTemplate() {
    return m_htmlTemplate;
  }

  @Override
  public String createHtml(IWizard w) {
    String html = m_htmlTemplate;
    String topPart = "";
    String bottomPart = "";
    StringBuilder listPart = new StringBuilder();
    if (w != null) {
      if (w.getSubTitle() != null) {
        topPart = "<div class=\"infoBox\">" + w.getSubTitle() + "</div>";
      }
      if (w.getActiveStep() != null) {
        if (w.getActiveStep().getTooltipText() != null || w.getActiveStep().getSubTitle() != null) {
          bottomPart = "<div class=\"infoBox\">" + StringUtility.nvl(w.getActiveStep().getSubTitle(), w.getActiveStep().getTooltipText()) + "</div>";
        }
      }
      int index = 1;
      for (IWizardStep<?> step : w.getSteps()) {
        String s = createHtmlForStep(step, index, (step == w.getActiveStep()));
        if (StringUtility.hasText(s)) {
          listPart.append(s);
          index++;
        }
      }
    }
    html = html.replace("#FONT_SIZE_UNIT#", "px");
    html = html.replace("#TOP#", topPart);
    html = html.replace("#LIST#", listPart.toString());
    html = html.replace("#BOTTOM#", bottomPart);
    return html;
  }

  /**
   * Adds a step to the HTML document. Uses old school HTML 3.2 with transparent graphics to enforce heights and widths
   * background colors since HTMLEditorToolkit of swing does not support CSS level 2.
   */
  protected String createHtmlForStep(IWizardStep<?> step, int index, boolean selected) {
    String cssClass;
    if (selected) {
      cssClass = "selected";
    }
    else if (step.isEnabled()) {
      cssClass = "default";
    }
    else {
      cssClass = "disabled";
    }
    StringBuilder buf = new StringBuilder();
    String spacerCssClass = "selected".equals(cssClass) ? "spacerselected" : "spacer";
    appendHtmlForSpacerLine(buf, spacerCssClass, 7, AbstractWizardStatusField.STEP_ANCHOR_IDENTIFIER + index);
    buf.append("<tr class=\"" + cssClass + "\">\n");
    buf.append(" <td width=\"15\"><img src=\"binaryResource:empty.png\" width=\"1\" height=\"30\"></td>\n");
    buf.append(" <td width=\"24\" valign=\"top\" class=\"bullet\" style=\"padding:0px;padding-top:5px;\">" + index + "</td>\n");
    buf.append(" <td width=\"17\"></td>\n");
    buf.append(" <td style=\"padding-top:2px;\">" + step.getTitle() + "</td>\n");
    buf.append(" <td width=\"15\"></td>\n");
    buf.append("</tr>\n");
    appendHtmlForSpacerLine(buf, spacerCssClass, 11, null);
    appendHtmlForSpacerLine(buf, "line", 1, null);
    return buf.toString();
  }

  protected void appendHtmlForSpacerLine(StringBuilder buf, String cssClass, int height, String anchor) {
    buf.append("<tr class=\"" + cssClass + "\"><td colspan=\"5\">");
    if (!StringUtility.isNullOrEmpty(anchor)) {
      buf.append("<a name=\"" + anchor + "\"/>");
    }
    buf.append("<img src=\"binaryResource:empty.png\" width=\"1\" height=\"" + height + "\"></td></tr>\n");
  }

  /**
   * To be overwritten in order to provide custom attachments. <br/>
   */
  protected List<BinaryResource> collectAttachments() {
    List<BinaryResource> attachments = new LinkedList<BinaryResource>();
    return attachments;
  }

  /**
   * To load an icon into the given attachments live list
   */
  protected void loadIcon(List<BinaryResource> attachments, String iconName) {
    if (attachments == null || iconName == null) {
      return;
    }
    String tempIconName = iconName;
    try {
      int index;
      // determine file format
      index = tempIconName.lastIndexOf('.');
      if (index > 0) {
        tempIconName = tempIconName.substring(0, index);
      }
      // determine icon base name
      String baseIconName = tempIconName;
      index = tempIconName.lastIndexOf('_');
      if (index > 0) {
        baseIconName = tempIconName.substring(0, index);
      }

      // load icon
      IconSpec iconSpec = IconLocator.instance().getIconSpec(tempIconName);
      if (iconSpec == null && !tempIconName.equals(baseIconName)) {
        iconSpec = IconLocator.instance().getIconSpec(baseIconName);
      }

      if (iconSpec != null) {
        attachments.add(new BinaryResource(iconSpec.getName(), iconSpec.getContent()));
      }
    }
    catch (Exception t) {
      LOG.warn("Failed to load icon '{}'", tempIconName, t);
    }
  }
}
