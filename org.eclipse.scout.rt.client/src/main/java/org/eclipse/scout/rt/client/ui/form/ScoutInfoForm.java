/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form;

import java.security.AccessController;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.client.services.common.icon.IconLocator;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.form.ScoutInfoForm.MainBox.CloseButton;
import org.eclipse.scout.rt.client.ui.form.ScoutInfoForm.MainBox.GroupBox.HtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.ApplicationNameProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.ApplicationVersionProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.PlatformVersionProperty;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.html.IHtmlContent;
import org.eclipse.scout.rt.platform.html.IHtmlElement;
import org.eclipse.scout.rt.platform.html.IHtmlTable;
import org.eclipse.scout.rt.platform.html.IHtmlTableRow;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

@ClassId("dee01442-979d-4231-a3f9-bd2a163e752a")
public class ScoutInfoForm extends AbstractForm {

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("Info");
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

  /**
   * Call-back method for sub classes allowing them to add additional binary resources (e.g. images). This default
   * implementation does not add any resources.
   */
  protected void contributeHtmlAttachments(Collection<BinaryResource> collection) {
    // subclasses may add additional resources
  }

  protected String createHtmlBody() {
    final IHtmlContent html = HTML.div(
            createLogoHtml(),
            HTML.div(createTitleHtml(), createHtmlTable(getProperties())).cssClass("scout-info-form-text-container"))
        .cssClass("scout-info-form-container");
    return html.toHtml();
  }

  protected IHtmlElement createLogoHtml() {
    IconSpec logo = IconLocator.instance().getIconSpec(getProductLogoId());
    if (logo != null) {
      return HTML.div(HTML.imgByIconId(getProductLogoId()).cssClass("scout-info-form-logo")).cssClass("scout-info-form-logo-container");
    }
    return null;
  }

  protected IHtmlElement createTitleHtml() {
    String title = StringUtility.join(" ", getProductName(), getProductVersion());
    if (StringUtility.hasText(title)) {
      // Set as form title instead of including in the info HTML
      setTitle(title);
    }
    return null;
  }

  protected String getProductName() {
    return CONFIG.getPropertyValue(ApplicationNameProperty.class);
  }

  protected String getProductVersion() {
    return CONFIG.getPropertyValue(ApplicationVersionProperty.class);
  }

  protected String getProductLogoId() {
    return getDesktop().getLogoId();
  }

  protected Map<String, Object> getProperties() {
    Map<String, Object> props = new LinkedHashMap<>();
    String userId = ClientSessionProvider.currentSession().getUserId();
    if (!StringUtility.hasText(userId)) {
      try {
        Subject subject = Subject.getSubject(AccessController.getContext());
        Principal firstPrincipal = CollectionUtility.firstElement(subject.getPrincipals());
        userId = firstPrincipal.getName();
      }
      catch (Exception e) { // NOSONAR (ignore exception)
        userId = "-";
      }
    }
    props.put(TEXTS.get("Username"), userId);

    Locale locale = NlsLocale.get();
    props.put(TEXTS.get("Language"), locale.getDisplayLanguage(locale));
    props.put(TEXTS.get("FormattingLocale"), locale);
    props.put(TEXTS.get("ScoutVersion"), CONFIG.getPropertyValue(PlatformVersionProperty.class));
    return props;
  }

  protected IHtmlTable createHtmlTable(Map<String, ?> properties) {
    List<IHtmlTableRow> rows = new ArrayList<>();
    for (Entry<String, ?> p : properties.entrySet()) {
      rows.add(createHtmlRow(p.getKey(), p.getValue()));
    }
    return HTML.table(rows).cssClass("scout-info-form-table");
  }

  protected IHtmlTableRow createHtmlRow(String property, Object value) {
    return HTML.tr(
        HTML.td(StringUtility.emptyIfNull(StringUtility.box("", property, ":"))).cssClass("scout-info-form-table-cell-description"),
        HTML.td(StringUtility.emptyIfNull(value)).cssClass("scout-info-form-table-cell-value"));
  }

  @Order(10)
  @ClassId("794bf4a4-727b-44f3-a2eb-2d2187110036")
  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredWidthInPixel() {
      return 450;
    }

    @Order(10)
    @ClassId("e5b5d699-9e8a-49c7-84ea-128289f1e616")
    public class GroupBox extends AbstractGroupBox {

      @Order(20)
      @ClassId("cd526428-76ca-4e8f-8b8e-4a4f0964c518")
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
    @ClassId("8c451981-7963-49aa-80ca-ed13469267d8")
    public class CloseButton extends AbstractCloseButton {

    }
  }

  public class ModifyHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() {
      List<BinaryResource> attachments = new ArrayList<>();
      contributeHtmlAttachments(attachments);
      if (!attachments.isEmpty()) {
        getHtmlField().setAttachments(attachments);
      }
      getHtmlField().setValue(createHtmlBody());
    }
  }
}
