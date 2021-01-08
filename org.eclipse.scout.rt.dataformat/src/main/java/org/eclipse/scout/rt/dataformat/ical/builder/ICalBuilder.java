/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataformat.ical.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.dataformat.ical.ICalBean;
import org.eclipse.scout.rt.dataformat.ical.ICalProperties;
import org.eclipse.scout.rt.dataformat.ical.model.Property;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;

@Bean
public class ICalBuilder {
  private ICalBean m_ical = BEANS.get(ICalBean.class);
  private List<ICalBean> m_components = new ArrayList<>();

  public ICalBuilder() {
    begin();
  }

  protected ICalBuilder begin() {
    m_ical.addProperty(ICalProperties.PROP_BEGIN_ICALENDAR);
    m_ical.addProperty(ICalProperties.PROP_VERSION_2_0);
    return this;
  }

  public ICalBuilder withProductIdentifier(String prodid) {
    m_ical.addProperty(new Property(ICalProperties.PROP_NAME_PRODID, prodid));
    return this;
  }

  public ICalBuilder withComponent(ICalVEventBuilder vevent) {
    m_components.add(vevent.build());
    return this;
  }

  public ICalBuilder end() {
    m_ical.addProperty(ICalProperties.PROP_END_ICALENDAR);
    return this;
  }

  protected void buildComponents() {
    for (ICalBean comp : m_components) {
      for (Property prop : comp.getProperties()) {
        m_ical.addProperty(prop);
      }
    }
  }

  public ICalBean build() {
    buildComponents();
    end();
    return m_ical;
  }
}
