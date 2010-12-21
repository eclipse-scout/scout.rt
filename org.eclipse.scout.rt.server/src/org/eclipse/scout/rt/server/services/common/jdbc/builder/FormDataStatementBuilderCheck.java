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
package org.eclipse.scout.rt.server.services.common.jdbc.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import org.eclipse.scout.commons.ClassIdentifier;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModel;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;

/**
 * @author imo
 */
public class FormDataStatementBuilderCheck {
  protected final FormDataStatementBuilder builder;
  private TreeSet<String> m_imports;
  private ArrayList<String> m_body;
  private HashSet<Class<?>> m_visited;

  public FormDataStatementBuilderCheck(FormDataStatementBuilder builder) {
    this.builder = builder;
    m_imports = new TreeSet<String>();
    m_body = new ArrayList<String>();
    m_visited = new HashSet<Class<?>>();
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

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    if (m_imports.size() > 0) {
      for (String s : m_imports) {
        buf.append(s);
        buf.append("\n");
      }
      buf.append("\n");
    }
    if (m_body.size() > 0) {
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
    checkRec(o);
    return m_body.size() == 0;
  }

  protected void checkRec(Object o) {
    if (m_visited.contains(o.getClass())) {
      return;
    }
    else {
      m_visited.add(o.getClass());
    }
    //
    if (o instanceof AbstractFormData) {
      for (Object f : ((AbstractFormData) o).getFields()) {
        checkRec(f);
      }
    }
    else if (o instanceof AbstractFormFieldData) {
      if (o instanceof AbstractValueFieldData<?>) {
        checkValueField((AbstractValueFieldData<?>) o);
      }
      //children
      for (Object f : ((AbstractFormFieldData) o).getFields()) {
        checkRec(f);
      }
    }
    else if (o instanceof AbstractDataModel) {
      for (Object a : ((AbstractDataModel) o).getAttributes()) {
        checkRec(a);
      }
      for (Object e : ((AbstractDataModel) o).getEntities()) {
        checkRec(e);
      }
    }
    else if (o instanceof IDataModelEntity) {
      checkDataModelEntity((IDataModelEntity) o);
      //only
      for (Object a : ((IDataModelEntity) o).getAttributes()) {
        checkRec(a);
      }
      for (Object e : ((IDataModelEntity) o).getEntities()) {
        checkRec(e);
      }
    }
    else if (o instanceof IDataModelAttribute) {
      checkDataModelAttribute((IDataModelAttribute) o);
    }
  }

  protected void checkValueField(AbstractValueFieldData<?> v) {
    ValuePartDefinition part = null;
    for (ValuePartDefinition f : builder.getValuePartDefinitions()) {
      if (part != null) break;
      for (ClassIdentifier t : f.getValueTypeClassIdentifiers()) {
        if (part != null) break;
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
        op = "ComposerConstants.OPERATOR_CONTAINS";
      }
      else {
        op = "ComposerConstants.OPERATOR_EQ";
      }
      addBodyLine("setFieldDefinition(" + resolveImport(v.getClass()) + ".class," + sql + "," + op + ");");
    }
  }

  protected void checkDataModelEntity(IDataModelEntity e) {
    ComposerEntityPartDefinition part = builder.getComposerEntityPartDefinitions().get(e.getClass());
    if (part == null) {
      String name = entityToName(e);
      String sqlTableName = toSqlTable(name);
      String sqlPKName = toSqlPrimaryKey(name);
      String parentName = name;
      String parentSqlPKName = sqlPKName;
      IDataModelEntity parentE = e.getParentEntity();
      if (parentE != null) {
        parentName = entityToName(parentE);
        parentSqlPKName = toSqlPrimaryKey(parentName);
      }
      String sqlTemplate =
          "\"EXISTS ( SELECT 1 \"+\n" +
              "\"FROM ${sqlTableName} @${name}@ \"+\n" +
              "\"WHERE @${name}@.${parentSqlPKName}=@parent.${parentName}@.${parentSqlPKName} \"+\n" +
              "\"<whereParts/> \"+\n" +
              "\"<groupBy> \"+\n" +
              "\"  GROUP BY @${name}@.${parentSqlPKName} \"+\n" +
              "\"  HAVING 1=1 \"+\n" +
              "\"  <havingParts/> \"+\n" +
              "\"</groupBy> \"+\n" +
              "\")\"";
      String sql = sqlTemplate.replace("${name}", name).replace("${parentName}", parentName).replace("${sqlTableName}", sqlTableName).replace("${parentSqlPKName}", parentSqlPKName);
      addBodyLine("//entity " + e.getClass().getSimpleName());
      addBodyLine("setComposerEntityDefinition(" + resolveImport(e.getClass()) + ".class," + sql + ");");
    }
  }

  protected void checkDataModelAttribute(IDataModelAttribute a) {
    ComposerAttributePartDefinition part = builder.getComposerAttributePartDefinitions().get(a.getClass());
    if (part == null) {
      if (a.getClass().getSimpleName().endsWith("CountAttribute")) {
        //default aggregate count attribute
        return;
      }
      String parentName = null;
      IDataModelEntity parentE = a.getParentEntity();
      if (parentE != null) {
        parentName = entityToName(parentE);
      }
      String name = attributeToName(parentE, a);
      String sqlColumnName = toSqlColumn(name);
      //
      String sqlTemplate;
      if (parentE != null) {
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

  protected String entityToName(Object e) {
    String name = e.getClass().getSimpleName();
    name = name.replaceAll("^Abstract(.*)$", "$1");
    name = name.replaceAll("^(.*)Entity$", "$1");
    String[] array = name.replaceAll("([a-z])([A-Z])", "$1 $2").split(" ");
    return array[array.length - 1];
  }

  protected String attributeToName(Object entity, Object attribute) {
    String ename = entityToName(entity);
    String name = attribute.getClass().getSimpleName();
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
