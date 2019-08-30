package org.eclipse.scout.migration.ecma6.model.old;

import org.eclipse.scout.rt.platform.exception.VetoException;

public class JsSuperCall extends AbstractJsElement {

  private final String m_fullyQualifiedName;
  private final String m_namespace;
  private final String m_name;


  public JsSuperCall(String fqn){
    m_fullyQualifiedName = fqn;
    String[] split = m_fullyQualifiedName.split("\\.");
    if(split.length != 2){
      throw new VetoException("Could not separate fqn('" + m_fullyQualifiedName + "') in namespace and name!");
    }
    m_namespace= split[0];
    m_name = split[1];
  }

  public String getFullyQualifiedName() {
    return m_fullyQualifiedName;
  }

  public String getNamespace() {
    return m_namespace;
  }

  public String getName() {
    return m_name;
  }


}
