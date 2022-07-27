/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Date;

import org.eclipse.scout.rt.dataobject.AttributeName;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IValueFormatConstants;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.ValueFormat;

/**
 * PartnerBasisV1Do
 */
@TypeName("demo.bsl.PartnerBasisV1")
public class PartnerBasisV1Do extends DoEntity {

  @AttributeName("stichdatum")
  @ValueFormat(pattern = IValueFormatConstants.DATE_PATTERN)
  public DoValue<Date> stichdatum() {
    return doValue("stichdatum");
  }

  public PartnerBasisV1Do withStichdatum(Date stichdatum) {
    stichdatum().set(stichdatum);
    return this;
  }

  public Date getStichdatum() {
    return stichdatum().get();
  }

  @AttributeName("aenderungAm")
  @ValueFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  public DoValue<Date> aenderungAm() {
    return doValue("aenderungAm");
  }

  public PartnerBasisV1Do withAenderungAm(Date aenderungAm) {
    aenderungAm().set(aenderungAm);
    return this;
  }

  public Date getAenderungAm() {
    return aenderungAm().get();
  }

  @AttributeName("aenderungDurch")
  public DoValue<String> aenderungDurch() {
    return doValue("aenderungDurch");
  }

  public PartnerBasisV1Do withAenderungDurch(String aenderungDurch) {
    aenderungDurch().set(aenderungDurch);
    return this;
  }

  public String getAenderungDurch() {
    return aenderungDurch().get();
  }

  @AttributeName("name")
  public DoValue<String> name() {
    return doValue("name");
  }

  public PartnerBasisV1Do withName(String name) {
    name().set(name);
    return this;
  }

  public String getName() {
    return name().get();
  }

  @AttributeName("korrespondenzSprache")
  public DoValue<String> korrespondenzSprache() {
    return doValue("korrespondenzSprache");
  }

  public PartnerBasisV1Do withKorrespondenzSprache(String korrespondenzSprache) {
    korrespondenzSprache().set(korrespondenzSprache);
    return this;
  }

  public String getKorrespondenzSprache() {
    return korrespondenzSprache().get();
  }

  @AttributeName("mitarbeiterBenutzerId")
  public DoValue<String> mitarbeiterBenutzerId() {
    return doValue("mitarbeiterBenutzerId");
  }

  public PartnerBasisV1Do withMitarbeiterBenutzerId(String mitarbeiterBenutzerId) {
    mitarbeiterBenutzerId().set(mitarbeiterBenutzerId);
    return this;
  }

  public String getMitarbeiterBenutzerId() {
    return mitarbeiterBenutzerId().get();
  }

  @AttributeName("partnerschutzDefId")
  public DoValue<String> partnerschutzDefId() {
    return doValue("partnerschutzDefId");
  }

  public PartnerBasisV1Do withPartnerschutzDefId(String partnerschutzDefId) {
    partnerschutzDefId().set(partnerschutzDefId);
    return this;
  }

  public String getPartnerschutzDefId() {
    return partnerschutzDefId().get();
  }

  @AttributeName("id")
  public DoValue<String> id() {
    return doValue("id");
  }

  public PartnerBasisV1Do withId(String id) {
    id().set(id);
    return this;
  }

  public String getId() {
    return id().get();
  }
}
