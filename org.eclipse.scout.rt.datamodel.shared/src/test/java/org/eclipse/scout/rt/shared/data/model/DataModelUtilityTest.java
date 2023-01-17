/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.model;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.shared.data.model.fixture.CustomDataModel;
import org.eclipse.scout.rt.shared.data.model.fixture.CustomDataModelExtension;
import org.eclipse.scout.rt.shared.test.ResourceBase;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit test for {@link DataModelUtility}
 */
@RunWith(PlatformTestRunner.class)
public class DataModelUtilityTest {

  @Test
  public void testStructure() throws Exception {
    CustomDataModelExtension.ENABLED = false;
    CustomDataModel dataModel = new CustomDataModel();
    dataModel.init();
    //
    IDataModelVisitor v = (m, ePath, o, prefix, buf) -> {
      buf.append(prefix);
      buf.append("[" + o.getClass().getName() + "] ");
      if (o instanceof IDataModel) {
        buf.append("DataModel");
      }
      else if (o instanceof IDataModelAttribute) {
        IDataModelAttribute x = (IDataModelAttribute) o;
        buf.append(x.getText() + " (type-" + x.getType() + ") " + DataModelUtility.attributePathToExternalId(m, ePath.addToEnd(x)));
      }
      else if (o instanceof IDataModelEntity) {
        IDataModelEntity x = (IDataModelEntity) o;
        buf.append(x.getText() + " " + DataModelUtility.entityPathToExternalId(m, ePath));
      }
      buf.append("\n");
    };
    String s;
    //
    s = visit(dataModel, v, 2);
    assertEquals(readTextFile(ResourceBase.class.getResource("data/model/level2-new.txt")), s.trim());
    //
    s = visit(dataModel, v, 3);
    assertEquals(readTextFile(ResourceBase.class.getResource("data/model/level3-new.txt")), s.trim());
    //
    s = visit(dataModel, v, 4);
    assertEquals(readTextFile(ResourceBase.class.getResource("data/model/level4-new.txt")), s.trim());
  }

  @Test
  public void testExternalId() {
    CustomDataModelExtension.ENABLED = true;
    CustomDataModel dataModel = new CustomDataModel();
    dataModel.init();
    final AtomicInteger counter = new AtomicInteger();
    final HashSet<Object> refSet = new HashSet<>();
    final HashSet<String> externaIdSet = new HashSet<>();
    //
    IDataModelVisitor v = (m, ePath, o, prefix, buf) -> {
      if (o instanceof IDataModel) {
      }
      else if (o instanceof IDataModelAttribute) {
        counter.incrementAndGet();
        IDataModelAttribute x = (IDataModelAttribute) o;
        refSet.add(x);
        AttributePath aPath = ePath.addToEnd(x);
        String extId = DataModelUtility.attributePathToExternalId(m, aPath);
        externaIdSet.add(extId);
        AttributePath aPath2 = DataModelUtility.externalIdToAttributePath(m, extId);
        assertEquals(aPath, aPath2);
        assertSame(x, aPath2.getAttribute());
      }
      else if (o instanceof IDataModelEntity) {
        counter.incrementAndGet();
        IDataModelEntity x = (IDataModelEntity) o;
        refSet.add(x);
        String extId = DataModelUtility.entityPathToExternalId(m, ePath);
        externaIdSet.add(extId);
        EntityPath ePath2 = DataModelUtility.externalIdToEntityPath(m, extId);
        assertEquals(ePath, ePath2);
        assertSame(x, ePath2.lastElement());
      }
    };
    visit(dataModel, v, 7);
    assertEquals(3545, counter.get());
    assertEquals(143, refSet.size());
    assertEquals(3545, externaIdSet.size());
  }

  private String readTextFile(URL url) throws IOException {
    try (InputStream in = url.openStream()) {
      return IOUtility.readStringUTF8(in).replaceAll("[\\n\\r]+", "\n").trim();
    }
  }

  private String visit(IDataModel m, IDataModelVisitor v, int maxLevel) {
    StringBuilder buf = new StringBuilder();
    visitRec(maxLevel, m, EntityPath.EMPTY, m, "", buf, v);
    return buf.toString();
  }

  private void visitRec(int reverseLevel, IDataModel m, EntityPath ePath, Object o, String prefix, StringBuilder buf, IDataModelVisitor v) {
    if (reverseLevel < 0) {
      return;
    }
    v.visit(m, ePath, o, prefix, buf);
    if (o instanceof IDataModel) {
      IDataModel x = (IDataModel) o;
      for (IDataModelAttribute child : x.getAttributes()) {
        visitRec(reverseLevel - 1, m, ePath, child, prefix + " ", buf, v);
      }
      for (IDataModelEntity child : x.getEntities()) {
        visitRec(reverseLevel - 1, m, ePath.addToEnd(child), child, prefix + " ", buf, v);
      }
      return;
    }
    if (o instanceof IDataModelEntity) {
      IDataModelEntity x = (IDataModelEntity) o;
      for (IDataModelAttribute child : x.getAttributes()) {
        visitRec(reverseLevel - 1, m, ePath, child, prefix + " ", buf, v);
      }
      for (IDataModelEntity child : x.getEntities()) {
        visitRec(reverseLevel - 1, m, ePath.addToEnd(child), child, prefix + " ", buf, v);
      }
      return;
    }
  }

  private interface IDataModelVisitor {
    void visit(IDataModel m, EntityPath ePath, Object o, String prefix, StringBuilder buf);
  }
}
