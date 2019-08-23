/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.dataformat.vcard.builder;

import java.util.Arrays;

import org.eclipse.scout.rt.dataformat.ical.model.ICalVCardHelper;
import org.eclipse.scout.rt.dataformat.ical.model.Property;
import org.eclipse.scout.rt.dataformat.ical.model.PropertyParameter;
import org.eclipse.scout.rt.dataformat.vcard.VCardBean;
import org.eclipse.scout.rt.dataformat.vcard.VCardProperties;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;

@Bean
public class VCardBuilder {
  protected VCardBean m_vcard = BEANS.get(VCardBean.class);
  protected ICalVCardHelper m_helper = BEANS.get(ICalVCardHelper.class);

  public VCardBuilder() {
    begin();
  }

  protected VCardBuilder begin() {
    m_vcard.addProperty(VCardProperties.PROP_BEGIN_VCARD);
    m_vcard.addProperty(VCardProperties.PROP_VERSION_3_0);
    return this;
  }

  public VCardBuilder withProductIdentifier(String prodId) {
    m_vcard.addProperty(new Property(VCardProperties.PROP_NAME_PRODID, prodId));
    return this;
  }

  public VCardBuilder withDisplayName(String displayName) {
    m_vcard.addProperty(new Property(VCardProperties.PROP_NAME_FN, displayName));
    return this;
  }

  public VCardBuilder withName(String firstName, String middleName, String lastName, String title) {
    m_vcard.addProperty(new Property(VCardProperties.PROP_NAME_N, m_helper.composeStructuredValueFromSingleValues(lastName, firstName, middleName, title, null)));
    return this;
  }

  public VCardBuilder withJobTitle(String jobDescription) {
    m_vcard.addProperty(new Property(VCardProperties.PROP_NAME_TITLE, jobDescription));
    return this;
  }

  public VCardBuilder withTel(String type, String kind, String number) {
    m_vcard.addProperty(new Property(VCardProperties.PROP_NAME_TEL, Arrays.asList(new PropertyParameter(VCardProperties.PARAM_NAME_TYPE, type, kind)), number));
    return this;
  }

  public VCardBuilder withEmail(String type, String email) {
    m_vcard.addProperty(new Property(VCardProperties.PROP_NAME_EMAIL, Arrays.asList(new PropertyParameter(VCardProperties.PARAM_NAME_TYPE, type)), email));
    return this;
  }

  public VCardBuilder withOrganizations(String... organizations) {
    m_vcard.addProperty(new Property(VCardProperties.PROP_NAME_ORG, m_helper.composeStructuredValueFromSingleValues(organizations)));
    return this;
  }

  public VCardBuilder withAddress(String type, String street, String zipcode, String city, String country) {
    m_vcard.addProperty(new Property(VCardProperties.PROP_NAME_ADR, Arrays.asList(new PropertyParameter(VCardProperties.PARAM_NAME_TYPE, type)),
        m_helper.composeStructuredValueFromSingleValues(null, null, street, city, null, zipcode, country)));
    return this;
  }

  protected VCardBuilder end() {
    m_vcard.addProperty(VCardProperties.PROP_END_VCARD);
    return this;
  }

  public VCardBean build() {
    end();
    return m_vcard;
  }
}
