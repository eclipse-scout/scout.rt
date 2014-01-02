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
package org.eclipse.scout.rt.spec.client.gen;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.spec.client.config.DefaultDocConfig;
import org.eclipse.scout.rt.spec.client.fixture.SimplePersonForm;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.junit.Test;

/**
 *
 */
public class TableFieldVisitorTest {

  @Test
  public void testSimpleTableField() throws ProcessingException {
    SimplePersonForm f = new SimplePersonForm();
    TableFieldVisitor visitor = new TableFieldVisitor(new DefaultDocConfig());
    f.visitFields(visitor);
    List<IDocSection> tableFieldSections = visitor.getDocSections();
    assertEquals(1, tableFieldSections.size());
  }
}
