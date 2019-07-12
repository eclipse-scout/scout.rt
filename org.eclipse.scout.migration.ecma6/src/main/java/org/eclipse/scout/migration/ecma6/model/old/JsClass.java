package org.eclipse.scout.migration.ecma6.model.old;

import org.eclipse.scout.rt.platform.exception.VetoException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JsClass {

  private final String m_fullyQuallifiedName;
  private final String m_namespace;
  private final String m_name;
  private JsSuperCall m_superCall;
  private final List<JsFunction> m_functions = new ArrayList<>();

  public JsClass(String fqn) {
    m_fullyQuallifiedName = fqn;
    String[] split = m_fullyQuallifiedName.split("\\.");
    if (split.length != 2) {
      throw new VetoException("Could not separate fqn('" + m_fullyQuallifiedName + "') in namespace and name!");
    }
    m_namespace = split[0];
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

  public void setSuperCall(JsSuperCall superCall) {
    m_superCall = superCall;
  }

  public JsSuperCall getSuperCall() {
    return m_superCall;
  }

  public void addFunction(JsFunction fun) {
    m_functions.add(fun);
  }

  public List<JsFunction> getFunctions() {
    return Collections.unmodifiableList(m_functions);
  }

  public JsFunction getFunction(String name) {
    return m_functions.stream().filter(fun -> name.equals(fun.getName())).findFirst().orElse(null);
  }

  public JsFunction getConstructor(){
    return m_functions.stream()
      .filter(f -> f.isConstructor())
      .findFirst().orElse(null);
  }

  @Override
  public String toString() {
    return toString("");
  }

  public String toString(String indent){
    StringBuilder builder = new StringBuilder();
    builder.append(getFullyQuallifiedName()).append(" [hasConstructor:").append(getConstructor() != null).append(", #functions:").append(m_functions.size()).append("]").append(System.lineSeparator())
        .append(m_functions.stream().filter(f -> !f.isConstructor()).map(f -> indent+"- "+f.toString(indent+"  ")).collect(Collectors.joining(System.lineSeparator())));
    return builder.toString();
  }
}
