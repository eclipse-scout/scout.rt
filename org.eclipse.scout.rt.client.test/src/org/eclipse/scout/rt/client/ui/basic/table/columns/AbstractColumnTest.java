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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.junit.Test;

/**
 *
 */
public class AbstractColumnTest extends AbstractColumn {

  @Test
  public void testMapEditorFieldProperties() {
    String bColor = "469406";
    setBackgroundColor(bColor);
    String fColor = "FAAAF1";
    setForegroundColor(fColor);
    FontSpec fontSpec = new FontSpec("Arial", FontSpec.STYLE_ITALIC | FontSpec.STYLE_BOLD, 0);
    setFont(fontSpec);
    setMandatory(true);

    AbstractValueField<String> field = new AbstractValueField<String>() {
    };
    mapEditorFieldProperties(field);
    assertEquals("expected backgroundColor property to be progagated to field", bColor, field.getBackgroundColor());
    assertEquals("expected foregroundColor property to be progagated to field", fColor, field.getForegroundColor());
    assertEquals("expected font property to be progagated to field", fontSpec, field.getFont());
    assertTrue("expected mandatory property to be progagated to field", field.isMandatory());
  }

}
