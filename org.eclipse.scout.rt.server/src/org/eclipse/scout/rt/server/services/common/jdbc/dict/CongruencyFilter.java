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
package org.eclipse.scout.rt.server.services.common.jdbc.dict;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public class CongruencyFilter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(CongruencyFilter.class);
  private ArrayList<String> m_forceTriggerChangeTableNames = new ArrayList<String>();

  public boolean isRelevant(Object o) {
    return true; // every object is relevant
  }

  public boolean allowDropTable() {
    return false;
  }

  public boolean allowDropTableColumn() {
    return false;
  }

  public void forceTriggerChange(String fullTableName) {
    m_forceTriggerChangeTableNames.add(fullTableName);
  }

  public boolean isTriggerChanged(String fullTableName) {
    return m_forceTriggerChangeTableNames.contains(fullTableName);
  }

  public boolean isAnyTriggerChanged() {
    return m_forceTriggerChangeTableNames.size() > 0;
  }

  public boolean isCongruent(DataDictionary o1, DataDictionary o2) {
    // don't check schema name
    return isCongruentAndRelevant(o1.getTables(), o2.getTables()) &&
        isCongruentAndRelevant(o1.getViews(), o2.getViews()) &&
        isCongruentAndRelevant(o1.getSequences(), o2.getSequences());
  }

  public boolean isCongruent(TableDesc o1, TableDesc o2) {
    // forced changes
    if (o1 != null && isTriggerChanged(o1.getFullName())) {
      return false;
    }
    if (o2 != null && isTriggerChanged(o2.getFullName())) {
      return false;
    }
    // default
    if (o1 == o2) {
      return true;
    }
    if (o1 == null || o2 == null) {
      return false;
    }
    return o1.getName().equals(o2.getName()) &&
        isCongruent(o1.getPrimaryKey(), o2.getPrimaryKey()) &&
        isCongruentAndRelevant(o1.getColumns(), o2.getColumns()) &&
        isCongruentAndRelevant(o1.getIndices(), o2.getIndices()) &&
        isCongruentAndRelevant(o1.getGrants(), o2.getGrants());
  }

  public boolean isCongruent(ViewDesc o1, ViewDesc o2) {
    if (o1 == o2) {
      return true;
    }
    if (o1 == null || o2 == null) {
      return false;
    }
    return o1.getName().equals(o2.getName()) &&
        o1.getStatement().equals(o2.getStatement()) &&
        isCongruentAndRelevant(o1.getGrants(), o2.getGrants());
  }

  public boolean isCongruent(TableGrantDesc o1, TableGrantDesc o2) {
    if (o1 == o2) {
      return true;
    }
    if (o1 == null || o2 == null) {
      return false;
    }
    return o1.getGrantee().equals(o2.getGrantee()) &&
        o1.getType().equals(o2.getType());
  }

  public boolean isCongruent(ColumnDesc o1, ColumnDesc o2) {
    if (o1 == o2) {
      return true;
    }
    if (o1 == null || o2 == null) {
      return false;
    }
    //
    return o1.getName().equals(o2.getName()) &&
        o1.isNullable() == o2.isNullable() &&
        getCanonicalColumnType(o1).equals(getCanonicalColumnType(o2));
  }

  public boolean isCongruent(IndexDesc o1, IndexDesc o2) {
    if (o1 == o2) {
      return true;
    }
    if (o1 == null || o2 == null) {
      return false;
    }
    return o1.isUnique() == o2.isUnique() &&
        o1.getColumnNames().equals(o2.getColumnNames());// equality of content
    // AND order
  }

  public boolean isCongruent(PrimaryKeyDesc o1, PrimaryKeyDesc o2) {
    if (o1 == o2) {
      return true;
    }
    if (o1 == null || o2 == null) {
      return false;
    }
    return o1.getColumnNames().equals(o2.getColumnNames());// equality of
    // content AND order
  }

  public boolean isCongruent(SequenceDesc o1, SequenceDesc o2) {
    if (o1 == o2) {
      return true;
    }
    if (o1 == null || o2 == null) {
      return false;
    }
    return o1.getName().equals(o2.getName());
  }

  public boolean isCongruentAndRelevant(Collection c1, Collection c2) {
    for (Iterator it = c1.iterator(); it.hasNext();) {
      Object o = it.next();
      if (isRelevant(o)) {
        if (!containsCongruent(c2, o)) {
          return false;
        }
      }
    }
    for (Iterator it = c2.iterator(); it.hasNext();) {
      Object o = it.next();
      if (isRelevant(o)) {
        if (!containsCongruent(c1, o)) {
          return false;
        }
      }
    }
    return true;
  }

  public boolean containsCongruent(Collection c, Object o) {
    if (o == null) {
      return false;
    }
    try {
      Method m = getClass().getMethod("isCongruent", new Class[]{o.getClass(), o.getClass()});
      for (Iterator it = c.iterator(); it.hasNext();) {
        Object o1 = it.next();
        // use reflection
        boolean found = ((Boolean) m.invoke(this, new Object[]{o1, o})).booleanValue();
        if (found) {
          return true;
        }
      }
    }
    catch (Exception e) {
      LOG.error("Error in reflection on Collection " + c + " and Object " + o + ":" + e, e);
    }
    return false;
  }

  public boolean isNumeric(ColumnDesc cd) {
    String type = cd.getTypeName().toUpperCase();
    return (type.equals("INT") ||
        type.equals("INTEGER") ||
        type.equals("DOUBLE") ||
        type.equals("DOUBLE PRECISION") ||
        type.equals("REAL") ||
        type.equals("BIGINT") ||
        type.equals("SMALLINT") ||
        type.equals("TINYINT") ||
        type.equals("NUMBER") || type.equals("DECIMAL"));
  }

  public String getCanonicalColumnType(ColumnDesc cd) {
    String type = cd.getTypeName().toUpperCase();
    // generic, if size <>0 then use size, if also decimaldigits <>0 then also
    // use that parameter
    if (cd.getPrecision() > 0) {
      if (cd.getDecimalDigits() > 0) {
        return type + "(" + cd.getPrecision() + "," + cd.getDecimalDigits() + ")";
      }
      else {
        return type + "(" + cd.getPrecision() + ")";
      }
    }
    if (cd.getSize() > 0) {
      return type + "(" + cd.getSize() + ")";
    }
    // return raw type
    return type;
  }

}
