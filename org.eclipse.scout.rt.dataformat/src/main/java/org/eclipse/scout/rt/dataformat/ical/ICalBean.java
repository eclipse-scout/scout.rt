/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataformat.ical;

import java.io.Reader;
import java.io.Writer;

import org.eclipse.scout.rt.dataformat.ical.model.AbstractEntity;
import org.eclipse.scout.rt.dataformat.ical.model.ICalVCardHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;

@Bean
public class ICalBean extends AbstractEntity {
  private static final long serialVersionUID = 1L;

  public static ICalBean parse(Reader r, String characterSet) {
    return BEANS.get(ICalVCardHelper.class).parse(r, characterSet, BEANS.get(ICalBean.class));
  }

  public void write(Writer w, String charset) {
    BEANS.get(ICalVCardHelper.class).write(this, w, charset);
  }

  public byte[] toBytes(String charset) {
    return BEANS.get(ICalVCardHelper.class).toBytes(this, charset);
  }
}
