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
package org.eclipse.scout.rt.client.ui.form;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.html.HTML;
import org.eclipse.scout.commons.html.HtmlHelper;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.client.services.common.perf.IPerformanceAnalyzerService;
import org.eclipse.scout.rt.client.ui.form.ScoutInfoForm.MainBox.CloseButton;
import org.eclipse.scout.rt.client.ui.form.ScoutInfoForm.MainBox.GroupBox.HtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.eclipse.scout.rt.shared.services.common.shell.IShellService;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.Version;

public class ScoutInfoForm extends AbstractForm {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ScoutInfoForm.class);

  private RemoteFile m_logoImage;

  public ScoutInfoForm() throws ProcessingException {
    super();
    m_logoImage = new RemoteFile("logo.png", 0);
  }

  public RemoteFile getLogoImage() {
    return m_logoImage;
  }

  public void setLogoImage(RemoteFile f) {
    m_logoImage = f;
  }

  @Override
  protected String getConfiguredTitle() {
    return ScoutTexts.get("Info");
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public HtmlField getHtmlField() {
    return getFieldByClass(HtmlField.class);
  }

  public CloseButton getCloseButton() {
    return getFieldByClass(CloseButton.class);
  }

  public void startModify() throws ProcessingException {
    startInternal(new ModifyHandler());
  }

  @Order(10.0f)
  public class MainBox extends AbstractGroupBox {

    @Override
    protected boolean getConfiguredGridUseUiWidth() {
      if (UserAgentUtility.isSwingUi()) {
        return true;
      }
      return false;
    }

    @Order(10.0f)
    public class GroupBox extends AbstractGroupBox {

      @Override
      protected boolean getConfiguredGridUseUiWidth() {
        if (UserAgentUtility.isSwingUi()) {
          return true;
        }
        return false;
      }

      @Order(10.0f)
      public class HtmlField extends AbstractHtmlField {

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredEnabled() {
          return true;
        }

        @Override
        protected boolean getConfiguredScrollBarEnabled() {
          return false;
        }

        @Override
        protected boolean getConfiguredGridUseUiWidth() {
          if (UserAgentUtility.isSwingUi()) {
            return true;
          }
          return false;
        }

        @Override
        protected boolean getConfiguredGridUseUiHeight() {
          if (UserAgentUtility.isSwingUi()) {
            return true;
          }
          return false;
        }

        @Override
        protected int getConfiguredGridW() {
          if (UserAgentUtility.isSwingUi()) {
            return 1;
          }
          return 2;
        }

        @Override
        protected int getConfiguredGridH() {
          if (UserAgentUtility.isSwingUi()) {
            return 1;
          }

          // If the client is a webclient then some of these informations must be omitted (Security) so that the html
          // field is smaller @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=365761
          else if (UserAgentUtility.isWebClient()) {
            return 12;
          }
          return 20;
        }

        @Override
        protected void execHyperlinkAction(URL url, String path, boolean local) throws ProcessingException {
          if (!local) {
            SERVICES.getService(IShellService.class).shellOpen(url.toExternalForm());
          }
        }
      }
    }

    @Order(20.0f)
    public class CloseButton extends AbstractCloseButton {
      @Override
      protected String getConfiguredTooltipText() {
        return null;
      }
    }
  }

  protected void createHtmlAttachments(Collection<RemoteFile> collection) {
    RemoteFile f = getLogoImage();
    if (f != null && !f.hasContent()) {
      // try to load bundle resource
      try {
        IClientSession clientSession = ClientSyncJob.getCurrentSession();
        IconSpec iconSpec = clientSession.getIconLocator().getIconSpec(AbstractIcons.ApplicationLogo);
        ByteArrayInputStream is = new ByteArrayInputStream(iconSpec.getContent());
        f.readData(is);
        is.close();
      }
      catch (Exception ex2) {
        LOG.info(null, ex2);
        f = null;
      }
    }
    if (f != null && f.hasContent()) {
      collection.add(f);
    }
  }

  protected void createHtmlBody(StringBuffer buf) {
    String title = "unknown";
    Version v = Version.emptyVersion;
    IProduct product = Platform.getProduct();
    if (product != null) {
      title = product.getName();
      v = Version.parseVersion("" + product.getDefiningBundle().getHeaders().get("Bundle-Version"));
    }
    buf.append("<head>\n");
    buf.append("<style type=\"text/css\">\n");
    buf.append("h1 {font-family: sans-serif}\n");
    buf.append("h2 {font-family: sans-serif}\n");
    buf.append("h3 {font-family: sans-serif}\n");
    buf.append("body {font-family: sans-serif}\n");
    buf.append("p {font-family: sans-serif}\n");
    buf.append("</style>\n");
    buf.append("</head>\n");
    buf.append("<p>");
    RemoteFile f = getLogoImage();
    if (f != null) {
      buf.append("<img src=\"" + f.getPath() + "\">");
    }
    else {
      buf.append(HTML.h3(title).toHtml());
    }
    buf.append("<p>");
    buf.append(HTML.h2(title + " " + v.getMajor() + "." + v.getMinor() + "." + v.getMicro()).toHtml());
    buf.append("<table cellspacing=0 cellpadding=0>");
    //
    StringBuffer contentBuf = new StringBuffer();
    createHtmlPropertyTableContent(contentBuf);
    buf.append(contentBuf.toString());
    buf.append("<tr><td>" + ScoutTexts.get("DetailedVersion") + ":</td><td>&nbsp;</td><td>" + HtmlHelper.escape(v.toString()) + "</td></tr>");
    //
    buf.append("</table>");
  }

  protected void createHtmlPropertyTableContent(StringBuffer buf) {
    IClientSession session = ClientSyncJob.getCurrentSession();
    long memUsed = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
    long memTotal = Runtime.getRuntime().totalMemory() / 1024 / 1024;
    long memMax = Runtime.getRuntime().maxMemory() / 1024 / 1024;
    //
    buf.append("<tr><td>" + ScoutTexts.get("Username") + ":</td><td>&nbsp;</td><td>" + HtmlHelper.escape(session.getUserId()) + "</td></tr>");
    buf.append("<tr><td>" + ScoutTexts.get("Language") + ":</td><td>&nbsp;</td><td>" + HtmlHelper.escape(LocaleThreadLocal.get().getDisplayLanguage()) + "</td></tr>");
    buf.append("<tr><td>" + ScoutTexts.get("FormattingLocale") + ":</td><td>&nbsp;</td><td>" + HtmlHelper.escape(LocaleThreadLocal.get().toString()) + "</td></tr>");

    /**
     * These information must only be presented in the case of an richclient. If the client is a webclient (ui.rap) then
     * these informations must be omitted (Security) https://bugs.eclipse.org/bugs/show_bug.cgi?id=365761
     */
    if (UserAgentUtility.isRichClient()) {
      buf.append("<tr><td>" + ScoutTexts.get("JavaVersion") + ":</td><td>&nbsp;</td><td>" + HtmlHelper.escape(System.getProperty("java.version")) + "</td></tr>");
      buf.append("<tr><td>" + ScoutTexts.get("JavaVMVersion") + ":</td><td>&nbsp;</td><td>" + HtmlHelper.escape(System.getProperty("java.vm.version")) + "</td></tr>");
      buf.append("<tr><td>" + ScoutTexts.get("OSVersion") + ":</td><td>&nbsp;</td><td>" + HtmlHelper.escape(System.getProperty("os.name") + " " + System.getProperty("os.version")) + "</td></tr>");
      buf.append("<tr><td>" + ScoutTexts.get("OSUser") + ":</td><td>&nbsp;</td><td>" + HtmlHelper.escape(System.getProperty("user.name")) + "</td></tr>");
      buf.append("<tr><td>" + ScoutTexts.get("MemoryStatus") + ":</td><td>&nbsp;</td><td>" + memUsed + "MB (total " + memTotal + "MB / max " + memMax + "MB)</td></tr>");
      IPerformanceAnalyzerService perf = SERVICES.getService(IPerformanceAnalyzerService.class);
      if (perf != null) {
        buf.append("<tr><td>" + ScoutTexts.get("NetworkLatency") + ":</td><td>&nbsp;</td><td>" + perf.getNetworkLatency() + " ms</td></tr>");
        buf.append("<tr><td>" + ScoutTexts.get("ExecutionTime") + ":</td><td>&nbsp;</td><td>" + perf.getServerExecutionTime() + " ms</td></tr>");
      }
      if (session.getServiceTunnel() != null && session.getServiceTunnel().getServerURL() != null) {
        buf.append("<tr><td>" + ScoutTexts.get("Server") + ":</td><td>&nbsp;</td><td>" + HtmlHelper.escape(session.getServiceTunnel().getServerURL().toString()) + "</td></tr>");
      }
    }
  }

  @Order(20.0f)
  public class ModifyHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() throws ProcessingException {
      ArrayList<RemoteFile> attachments = new ArrayList<RemoteFile>();
      createHtmlAttachments(attachments);
      if (attachments.size() > 0) {
        getHtmlField().setAttachments(attachments);
      }
      StringBuffer buf = new StringBuffer();
      createHtmlBody(buf);
      getHtmlField().setValue(buf.toString());
    }
  }
}
