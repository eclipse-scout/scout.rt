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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.html.HTML;
import org.eclipse.scout.commons.html.IHtmlElement;
import org.eclipse.scout.commons.html.IHtmlTable;
import org.eclipse.scout.commons.html.IHtmlTableRow;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.commons.resource.BinaryResource;
import org.eclipse.scout.rt.client.services.common.icon.IconLocator;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.form.ScoutInfoForm.MainBox.CloseButton;
import org.eclipse.scout.rt.client.ui.form.ScoutInfoForm.MainBox.GroupBox.HtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.ApplicationNameProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.ApplicationVersionProperty;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;

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
          // If the client is a webclient then some of these informations must be omitted (Security) so that the html
          // field is smaller @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=365761
          return UserAgentUtility.isWebClient() ? 12 : 20;
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

  protected void createHtmlAttachments(Collection<BinaryResource> collection) {
    RemoteFile f = getLogoImage();
    BinaryResource res = null;
    if (f != null && !f.hasContent()) {
      // try to load bundle resource
      try {
        IconSpec iconSpec = IconLocator.instance().getIconSpec(AbstractIcons.ApplicationLogo);
        if (iconSpec != null) {
          res = new BinaryResource(iconSpec.getName(), iconSpec.getContent());
        }
      }
      catch (Exception ex2) {
        LOG.info(null, ex2);
        res = null;
      }
    }
    else if (f != null && f.hasContent()) {
      try {
        res = new BinaryResource(f.getName(), f.extractData());
      }
      catch (Exception e) {
        LOG.info(null, e);
        res = null;
      }
    }
    if (res != null && res.getContentLength() > 0) {
      collection.add(res);
    }
  }

  /**
   * @return text contained in Html Field
   */
  protected String createHtmlBody() {
    final IHtmlElement html = HTML.div(
        HTML.p(getLogoHtml()),
        getTitleHtml(),
        createHtmlTable(getProperties())
        );
    return html.toEncodedHtml();
  }

  /**
   * @return Product Logo Html
   */
  protected IHtmlElement getLogoHtml() {
    RemoteFile f = getLogoImage();
    HTML.img(f.getPath());
    if (f != null && f.getPath() != null) {
      return HTML.img(f.getPath());
    }
    else {
      return HTML.h3(getProductName());
    }
  }

  /**
   * @return Product Name with Version Html
   */
  private IHtmlElement getTitleHtml() {
    String title = getProductName() + " " + getVersion();
    return HTML.h2(title);
  }

  private String getProductName() {
    return CONFIG.getPropertyValue(ApplicationNameProperty.class);
  }

  private String getVersion() {
    return CONFIG.getPropertyValue(ApplicationVersionProperty.class);
  }

  protected Map<String, Object> getProperties() {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put(ScoutTexts.get("Username"), ClientSessionProvider.currentSession().getUserId());
    props.put(ScoutTexts.get("Language"), NlsLocale.get().getDisplayLanguage());
    props.put(ScoutTexts.get("FormattingLocale"), NlsLocale.get());
    props.put(ScoutTexts.get("DetailedVersion"), getVersion());
    return props;
  }

  public IHtmlTable createHtmlTable(Map<String, ? extends Object> properties) {
    List<IHtmlTableRow> rows = new ArrayList<>();
    for (Entry<String, ? extends Object> p : properties.entrySet()) {
      rows.add(createHtmlRow(p.getKey(), p.getValue()));
    }
    return HTML.table(rows);
  }

  public IHtmlTableRow createHtmlRow(String property, Object value) {
    return HTML.row(HTML.cell(property + ":"), HTML.cell(StringUtility.emptyIfNull(value)));
  }

  @Order(20.0f)
  public class ModifyHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() throws ProcessingException {
      ArrayList<BinaryResource> attachments = new ArrayList<BinaryResource>();
      createHtmlAttachments(attachments);
      if (attachments.size() > 0) {
        getHtmlField().setAttachments(attachments);
      }
      getHtmlField().setValue(createHtmlBody());
    }
  }
}
