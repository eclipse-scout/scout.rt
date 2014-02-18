/*******************************************************************************
 * Copyright (c) 2010,2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.lookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

public class LookupRowTest {

  @Test
  public void testSerializeDeserialize() throws Exception {
    ILookupRow<String> row = new LookupRow<String>("key", "text");

    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    ObjectOutputStream oout = new ObjectOutputStream(bout);
    oout.writeObject(row);
    oout.close();

    byte[] serializedData = bout.toByteArray();

    ByteArrayInputStream bin = new ByteArrayInputStream(serializedData);
    ObjectInputStream oin = new ObjectInputStream(bin);
    Object obj = oin.readObject();
    oin.close();

    assertNotNull(obj);
    assertTrue(obj instanceof LookupRow);
    LookupRow deserializedRow = (LookupRow) obj;
    assertEquals("key", deserializedRow.getKey());
    assertEquals("text", deserializedRow.getText());
  }
}
