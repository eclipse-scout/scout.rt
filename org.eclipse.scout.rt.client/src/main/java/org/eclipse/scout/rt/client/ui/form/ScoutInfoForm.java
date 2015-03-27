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

import static org.eclipse.scout.commons.html.HTML.p;
import static org.eclipse.scout.commons.html.HTML.table;
import static org.eclipse.scout.commons.html.HTML.cell;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.html.HTML;
import org.eclipse.scout.commons.html.HtmlBinds;
import org.eclipse.scout.commons.html.IHtmlElement;
import org.eclipse.scout.commons.html.IHtmlTable;
import org.eclipse.scout.commons.html.IHtmlTableRow;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.services.common.icon.IconLocator;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.form.ScoutInfoForm.MainBox.CloseButton;
import org.eclipse.scout.rt.client.ui.form.ScoutInfoForm.MainBox.GroupBox.HtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.eclipse.scout.rt.shared.services.common.shell.IShellService;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.Version;

/**
 * Form for general information about the application
 */
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
      return false;
    }

    @Order(10.0f)
    public class GroupBox extends AbstractGroupBox {

      @Override
      protected boolean getConfiguredGridUseUiWidth() {
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
          return false;
        }

        @Override
        protected boolean getConfiguredGridUseUiHeight() {
          return false;
        }

        @Override
        protected int getConfiguredGridW() {
          return 2;
        }

        @Override
        protected int getConfiguredGridH() {
          return 12;
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
        IconSpec iconSpec = IconLocator.instance().getIconSpec(AbstractIcons.ApplicationLogo);
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

  /**
   * @return text contained in Html Field
   */
  protected String createHtmlBody() {
    final HtmlBinds binds = new HtmlBinds();

    final IHtmlElement html = HTML.div(
        p(getLogoHtml(binds)),
        getTitleHtml(binds),
        getPropertyTable(binds)
        );

    return binds.applyBindParameters(html);
  }

  /**
   * @return Product Logo Html
   */
  protected IHtmlElement getLogoHtml(HtmlBinds binds) {
    RemoteFile f = getLogoImage();
    HTML.img(binds.put(f.getPath()));
    if (f != null && f.getPath() != null) {
      return HTML.img(binds.put(f.getPath()));
    }
    else {
      return HTML.h3(binds.put(getProductName()));
    }
  }

  /**
   * @return Product Name with Version Html
   */
  private IHtmlElement getTitleHtml(HtmlBinds binds) {
    Version v = getVersion();
    final String version = v.getMajor() + "." + v.getMinor() + "." + v.getMicro();
    String title = getProductName() + " " + version;
    return HTML.h2(binds.put(title));
  }

  private String getProductName() {
    IProduct product = Platform.getProduct();
    if (product != null) {
      return product.getName();
    }
    else {
      return "unknown";
    }
  }

  private Version getVersion() {
    Version v = Version.emptyVersion;
    IProduct product = Platform.getProduct();
    if (product != null) {
      v = Version.parseVersion("" + product.getDefiningBundle().getHeaders().get("Bundle-Version"));
    }
    return v;
  }

  protected IHtmlTable getPropertyTable(final HtmlBinds binds) {
    List<IHtmlTableRow> rows = new ArrayList<>();
    final Map<String, Object> props = getProperties();
    for (Entry<String, Object> p : props.entrySet()) {
      rows.add(createHtmlRow(binds, p.getKey(), p.getValue()));
    }
    return table(rows).cellspacing(0).cellpadding(0);
  }

  protected Map<String, Object> getProperties() {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put(ScoutTexts.get("Username"), ClientSessionProvider.currentSession().getUserId());
    props.put(ScoutTexts.get("Language"), NlsLocale.get().getDisplayLanguage());
    props.put(ScoutTexts.get("FormattingLocale"), NlsLocale.get());
    props.put(ScoutTexts.get("DetailedVersion"), getVersion().toString());
    return props;
  }

  protected IHtmlTableRow createHtmlRow(HtmlBinds binds, String property, Object value) {
    return HTML.row(cell(binds.put(property + ":")), cell(binds.put(value)));
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
      getHtmlField().setValue(createHtmlBody());
    }
  }
}
