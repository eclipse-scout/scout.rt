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
package org.eclipse.scout.rt.client.ui.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.rt.client.services.common.icon.IconLocator;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.form.ScoutInfoForm.MainBox.CloseButton;
import org.eclipse.scout.rt.client.ui.form.ScoutInfoForm.MainBox.GroupBox.HtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.ApplicationNameProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.ApplicationVersionProperty;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.html.IHtmlElement;
import org.eclipse.scout.rt.platform.html.IHtmlTable;
import org.eclipse.scout.rt.platform.html.IHtmlTableRow;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.OfficialVersion;
import org.eclipse.scout.rt.shared.ScoutTexts;

public class ScoutInfoForm extends AbstractForm {

  public ScoutInfoForm() {
    super();
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

  public void startModify() {
    startInternal(new ModifyHandler());
  }

  protected void contributeHtmlAttachments(Collection<BinaryResource> collection) {
  }

  protected String createHtmlBody() {
    final IHtmlElement html = HTML.div(
        createLogoHtml(),
        createTitleHtml(),
        createHtmlTable(getProperties()));
    return html.toEncodedHtml();
  }

  protected IHtmlElement createLogoHtml() {
    IconSpec logo = IconLocator.instance().getIconSpec(AbstractIcons.ApplicationLogo);
    if (logo != null) {
      return HTML.p(HTML.imgByIconId(AbstractIcons.ApplicationLogo).cssClass("scout-info-form-logo"));
    }
    return null;
  }

  protected IHtmlElement createTitleHtml() {
    String title = StringUtility.join(" ", getProductName(), getProductVersion());
    if (StringUtility.hasText(title)) {
      return HTML.h2(title);
    }
    return null;
  }

  protected String getProductName() {
    return CONFIG.getPropertyValue(ApplicationNameProperty.class);
  }

  protected String getProductVersion() {
    return CONFIG.getPropertyValue(ApplicationVersionProperty.class);
  }

  protected Map<String, Object> getProperties() {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put(ScoutTexts.get("Username"), ClientSessionProvider.currentSession().getUserId());
    props.put(ScoutTexts.get("Language"), NlsLocale.get().getDisplayLanguage());
    props.put(ScoutTexts.get("FormattingLocale"), NlsLocale.get());
    props.put(ScoutTexts.get("ScoutVersion"), OfficialVersion.VERSION);
    return props;
  }

  protected IHtmlTable createHtmlTable(Map<String, ? extends Object> properties) {
    List<IHtmlTableRow> rows = new ArrayList<>();
    for (Entry<String, ? extends Object> p : properties.entrySet()) {
      rows.add(createHtmlRow(p.getKey(), p.getValue()));
    }
    return HTML.table(rows);
  }

  protected IHtmlTableRow createHtmlRow(String property, Object value) {
    return HTML.row(
        HTML.cell(StringUtility.emptyIfNull(StringUtility.box("", property, ":"))),
        HTML.cell(StringUtility.emptyIfNull(value)));
  }

  @Order(10)
  public class MainBox extends AbstractGroupBox {

    @Order(10)
    public class GroupBox extends AbstractGroupBox {

      @Order(20)
      public class HtmlField extends AbstractHtmlField {

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredScrollBarEnabled() {
          return false;
        }

        @Override
        protected int getConfiguredGridW() {
          return 2;
        }

        @Override
        protected boolean getConfiguredGridUseUiHeight() {
          return true;
        }

        @Override
        protected boolean getConfiguredStatusVisible() {
          return false;
        }
      }
    }

    @Order(20)
    public class CloseButton extends AbstractCloseButton {

    }
  }

  public class ModifyHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() {
      List<BinaryResource> attachments = new ArrayList<BinaryResource>();
      contributeHtmlAttachments(attachments);
      if (!attachments.isEmpty()) {
        getHtmlField().setAttachments(attachments);
      }
      getHtmlField().setValue(createHtmlBody());
    }
  }
}
