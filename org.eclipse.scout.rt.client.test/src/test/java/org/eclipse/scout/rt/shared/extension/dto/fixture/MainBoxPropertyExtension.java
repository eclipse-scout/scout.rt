/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.extension.dto.fixture;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.AbstractGroupBoxExtension;
import org.eclipse.scout.rt.platform.extension.Extends;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm.MainBox;

@Extends(MainBox.class)
@Data(PropertyExtensionData.class)
public class MainBoxPropertyExtension extends AbstractGroupBoxExtension<MainBox> {

  private Long m_longValue;

  public MainBoxPropertyExtension(MainBox owner) {
    super(owner);
  }

  @Data
  public Long getLongValue() {
    return m_longValue;
  }

  @Data
  public void setLongValue(Long value) {
    m_longValue = value;
  }

}
