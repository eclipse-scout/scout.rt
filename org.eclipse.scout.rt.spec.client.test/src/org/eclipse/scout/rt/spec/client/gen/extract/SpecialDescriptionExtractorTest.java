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
package org.eclipse.scout.rt.spec.client.gen.extract;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link SpecialDescriptionExtractor}
 */
public class SpecialDescriptionExtractorTest {

  @Test
  public void testGetText() {
    SpecialDescriptionExtractor nameExtractor = new SpecialDescriptionExtractor(TEXTS.get("org.eclipse.scout.rt.spec.type"), "_name");
    SpecialDescriptionExtractor descriptionExtractor = new SpecialDescriptionExtractor(TEXTS.get("org.eclipse.scout.rt.spec.doc"), "_description");
    Assert.assertTrue("Name for AbstractStringField could not be extracted!", !StringUtility.isNullOrEmpty(nameExtractor.getText(AbstractStringField.class)));
    Assert.assertTrue("Description for AbstractStringField could not be extracted!", !StringUtility.isNullOrEmpty(descriptionExtractor.getText(AbstractStringField.class)));
  }
}
