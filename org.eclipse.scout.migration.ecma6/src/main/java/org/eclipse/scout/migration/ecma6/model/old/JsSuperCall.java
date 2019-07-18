package org.eclipse.scout.migration.ecma6.model.old;

import org.eclipse.scout.rt.platform.exception.VetoException;

public class JsSuperCall extends AbstractJsElement {

  private final String m_fullyQuallifiedName;
  private final String m_namespace;
  private final String m_name;


  public JsSuperCall(String fqn){
    m_fullyQuallifiedName = fqn;
    String[] split = m_fullyQuallifiedName.split("\\.");
    if(split.length != 2){
      throw new VetoException("Could not separate fqn('"+m_fullyQuallifiedName+"') in namespace and name!");
    }
    m_namespace= split[0];
    m_name = split[1];
  }

  public String getFullyQuallifiedName() {
    return m_fullyQuallifiedName;
  }

  public String getNamespace() {
    return m_namespace;
  }

  public String getName() {
    return m_name;
  }


}
