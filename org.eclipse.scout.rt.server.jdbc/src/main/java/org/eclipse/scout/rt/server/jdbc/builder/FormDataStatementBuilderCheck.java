/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jdbc.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.scout.rt.platform.classid.ClassIdentifier;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModel;
import org.eclipse.scout.rt.shared.data.model.AttributePath;
import org.eclipse.scout.rt.shared.data.model.EntityPath;
import org.eclipse.scout.rt.shared.data.model.IDataModel;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;

public class FormDataStatementBuilderCheck {
  @SuppressWarnings("squid:S00116")
  protected final FormDataStatementBuilder builder;
  private final TreeSet<String> m_imports;
  private final ArrayList<String> m_body;
  private final Set<Class<?>> m_visited;

  public FormDataStatementBuilderCheck(FormDataStatementBuilder builder) {
    this.builder = builder;
    m_imports = new TreeSet<>();
    m_body = new ArrayList<>();
    m_visited = new HashSet<>();
  }

  /**
   * @return the imports
   */
  public TreeSet<String> getImports() {
    return m_imports;
  }

  /**
   * @return the body
   */
  public ArrayList<String> getBody() {
    return m_body;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    if (!m_imports.isEmpty()) {
      for (String s : m_imports) {
        buf.append(s);
        buf.append("\n");
      }
      buf.append("\n");
    }
    if (!m_body.isEmpty()) {
      for (String s : m_body) {
        buf.append(s);
        buf.append("\n");
      }
    }
    return buf.toString();
  }

  /**
   * Check for all parts a statement mapping was defined
   */
  public boolean check(Object o) {
    m_imports.clear();
    m_body.clear();
    m_visited.clear();
    checkRec(EntityPath.EMPTY, o);
    return m_body.isEmpty();
  }

  protected void checkRec(EntityPath parentPath, Object o) {
    if (m_visited.contains(o.getClass())) {
      return;
    }
    m_visited.add(o.getClass());
    //
    if (o instanceof AbstractFormData) {
      for (Object f : ((AbstractFormData) o).getFields()) {
        checkRec(parentPath, f);
      }
    }
    else if (o instanceof AbstractFormFieldData) {
      if (o instanceof AbstractValueFieldData<?>) {
        checkValueField((AbstractValueFieldData<?>) o);
      }
      //children
      for (Object f : ((AbstractFormFieldData) o).getFields()) {
        checkRec(parentPath, f);
      }
    }
    else if (o instanceof AbstractDataModel) {
      for (Object a : ((IDataModel) o).getAttributes()) {
        checkRec(parentPath, a);
      }
      for (Object e : ((IDataModel) o).getEntities()) {
        checkRec(parentPath, e);
      }
    }
    else if (o instanceof IDataModelEntity) {
      EntityPath subPath = parentPath.addToEnd((IDataModelEntity) o);
      checkDataModelEntity(subPath);
      //only
      for (Object a : ((IDataModelEntity) o).getAttributes()) {
        checkRec(subPath, a);
      }
      for (Object e : ((IDataModelEntity) o).getEntities()) {
        checkRec(subPath, e);
      }
    }
    else if (o instanceof IDataModelAttribute) {
      checkDataModelAttribute(parentPath.addToEnd((IDataModelAttribute) o));
    }
  }

  protected void checkValueField(AbstractValueFieldData<?> v) {
    BasicPartDefinition part = null;
    for (BasicPartDefinition f : builder.getBasicPartDefinitions()) {
      if (part != null) {
        break;
      }
      for (ClassIdentifier t : f.getValueTypeClassIdentifiers()) {
        if (part != null) {
          break;
        }
        if (t.getLastSegment() == v.getClass()) {
          part = f;
        }
      }
    }
    if (part == null) {
      String name = fieldToName(v);
      String sqlColumnName = toSqlColumn(name);
      String sqlTemplate = "\"${sqlName}\"";
      String sql = sqlTemplate.replace("${sqlColumnName}", sqlColumnName);
      String op;
      Class<?> dataType = v.getHolderType();
      if (String.class.isAssignableFrom(dataType)) {
        op = "DataModelConstants.OPERATOR_CONTAINS";
      }
      else {
        op = "DataModelConstants.OPERATOR_EQ";
      }
      addBodyLine("setBasicDefinition(" + resolveImport(v.getClass()) + ".class," + sql + "," + op + ");");
    }
  }

  @SuppressWarnings("bsiRulesDefinition:htmlInString")
  protected void checkDataModelEntity(EntityPath ePath) {
    IDataModelEntity e = ePath.lastElement();
    DataModelEntityPartDefinition part = builder.getDataModelEntityPartDefinitions().get(e.getClass());
    if (part == null) {
      String name = entityToName(ePath);
      String sqlTableName = toSqlTable(name);
      String sqlPKName = toSqlPrimaryKey(name);
      String parentName = name;
      String parentSqlPKName = sqlPKName;
      EntityPath parentPath = ePath.parent();
      if (!parentPath.isEmpty()) {
        parentName = entityToName(parentPath);
        parentSqlPKName = toSqlPrimaryKey(parentName);
      }
      String sqlTemplate =
          "\"EXISTS ( SELECT 1 \"+\n"
              + "\"FROM ${sqlTableName} @${name}@ \"+\n"
              + "\"WHERE @${name}@.${parentSqlPKName}=@parent.${parentName}@.${parentSqlPKName} \"+\n"
              + "\"<whereParts/> \"+\n"
              + "\"<groupBy> \"+\n"
              + "\"  GROUP BY @${name}@.${parentSqlPKName} \"+\n"
              + "\"  HAVING 1=1 \"+\n"
              + "\"  <havingParts/> \"+\n"
              + "\"</groupBy> \"+\n"
              + "\")\"";
      String sql = sqlTemplate.replace("${name}", name).replace("${parentName}", parentName).replace("${sqlTableName}", sqlTableName).replace("${parentSqlPKName}", parentSqlPKName);
      addBodyLine("//entity " + e.getClass().getSimpleName());
      addBodyLine("setComposerEntityDefinition(" + resolveImport(e.getClass()) + ".class," + sql + ");");
    }
  }

  protected void checkDataModelAttribute(AttributePath aPath) {
    IDataModelAttribute a = aPath.getAttribute();
    DataModelAttributePartDefinition part = builder.getDataModelAttributePartDefinitions().get(a.getClass());
    if (part == null) {
      if (a.getClass().getSimpleName().endsWith("CountAttribute")) {
        //default aggregate count attribute
        return;
      }
      String parentName = "";
      EntityPath parentPath = aPath.getEntityPath();
      if (!parentPath.isEmpty()) {
        parentName = entityToName(parentPath);
      }
      String name = attributeToName(aPath);
      String sqlColumnName = toSqlColumn(name);
      //
      String sqlTemplate;
      if (!parentPath.isEmpty()) {
        sqlTemplate = "\"@${parentName}@.${sqlColumnName}\"";
      }
      else {
        sqlTemplate = "\"${sqlColumnName}\"";
      }
      String sql = sqlTemplate.replace("${sqlColumnName}", sqlColumnName).replace("${parentName}", parentName);
      addBodyLine("setComposerAttributeDefinition(" + resolveImport(a.getClass()) + ".class," + sql + ");");
    }
  }

  protected String fieldToName(Object f) {
    String name = f.getClass().getSimpleName();
    name = name.replaceAll("^Abstract(.*)$", "$1");
    name = name.replaceAll("^(.*)Data$", "$1");
    name = name.replaceAll("^(.*)Box", "$1");
    return name;
  }

  protected String entityToName(EntityPath ePath) {
    if (ePath.isEmpty()) {
      return null;
    }
    IDataModelEntity e = ePath.lastElement();
    String name = e.getClass().getSimpleName();
    name = name.replaceAll("^Abstract(.*)$", "$1");
    name = name.replaceAll("^(.*)Entity$", "$1");
    String[] array = name.replaceAll("([a-z])([A-Z])", "$1 $2").split(" ");
    return array[array.length - 1];
  }

  protected String attributeToName(AttributePath aPath) {
    String ename = entityToName(aPath.getEntityPath());
    IDataModelAttribute a = aPath.getAttribute();
    String name = a.getClass().getSimpleName();
    name = name.replaceAll("^Abstract(.*)$", "$1");
    name = name.replaceAll("^(.*)Attribute", "$1");
    if (ename != null) {
      String[] array = ename.replaceAll("([a-z])([A-Z])", "$1 $2").split(" ");
      for (int i = array.length - 1; i >= 0; i--) {
        if (name.startsWith(array[i])) {
          name = name.substring(array[i].length());
        }
      }
    }
    return name;
  }

  protected String toSqlTable(String s) {
    s = s.replaceAll("([a-z])([A-Z])", "$1_$2");
    return s.toUpperCase();
  }

  protected String toSqlPrimaryKey(String s) {
    s = s.replaceAll("([a-z])([A-Z])", "$1_$2") + "_NR";
    return s.toUpperCase();
  }

  protected String toSqlColumn(String s) {
    s = s.replaceAll("([a-z])([A-Z])", "$1_$2");
    return s.toUpperCase();
  }

  protected String resolveImport(Class<?> c) {
    String pck = c.getPackage().getName();
    String s = c.getName().substring(pck.length() + 1);
    int i = s.indexOf('$');
    if (i >= 0) {
      s = s.substring(0, i);
    }
    addImportLine("import " + pck + "." + s + ";");
    return c.getName().substring(pck.length() + 1).replace('$', '.');
  }

  protected void addImportLine(String s) {
    m_imports.add(s);
  }

  protected void addBodyLine(String s) {
    m_body.add(s);
  }

}
