/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
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
