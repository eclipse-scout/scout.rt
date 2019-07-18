package org.eclipse.scout.migration.ecma6.model.old;

import org.eclipse.scout.rt.platform.exception.VetoException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JsClass extends AbstractJsElement{

  private final String m_fullyQuallifiedName;
  private final JsFile m_jsFile;
  private final String m_namespace;
  private final String m_name;
  private JsSuperCall m_superCall;
  private final List<JsFunction> m_functions = new ArrayList<>();
  private final List<JsConstant> m_constants = new ArrayList<>();

  public JsClass(String fqn, JsFile jsFile) {
    m_fullyQuallifiedName = fqn;
    m_jsFile = jsFile;
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

  public JsFile getJsFile() {
    return m_jsFile;
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

  public JsFunction getConstructor() {
    return m_functions.stream()
        .filter(f -> f.isConstructor())
        .findFirst().orElse(null);
  }

  public void addConstant(JsConstant constant) {
    m_constants.add(constant);
  }

  public List<JsConstant> getConstants() {
    return Collections.unmodifiableList(m_constants);
  }

  public JsConstant getConstant(String name) {
    return m_constants.stream()
        .filter(c -> name.equals(c.getName()))
        .findFirst()
        .orElse(null);
  }

  @Override
  public String toString() {
    return toString("");
  }

  public String toString(String indent) {
    StringBuilder builder = new StringBuilder();
    builder.append(getFullyQuallifiedName()).append(" [hasConstructor:").append(getConstructor() != null).append(", #functions:").append(m_functions.size()).append("]");
    if (m_functions.size() > 0) {
      builder.append(System.lineSeparator()).append(indent).append("FUNCTIONS:").append(System.lineSeparator())
          .append(m_functions.stream().filter(f -> !f.isConstructor()).map(f -> indent + "- " + f.toString(indent + "  ")).collect(Collectors.joining(System.lineSeparator())));
    }
    if (m_constants.size() > 0) {
      builder.append(System.lineSeparator()).append(indent).append("CONSTANTS:").append(System.lineSeparator())
          .append(m_constants.stream()
              .map(c -> indent + "- " + c.toString(indent + "  "))
              .collect(Collectors.joining(System.lineSeparator())));
    }
    return builder.toString();
  }
}
