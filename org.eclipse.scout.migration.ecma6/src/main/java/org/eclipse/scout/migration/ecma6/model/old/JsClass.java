package org.eclipse.scout.migration.ecma6.model.old;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.exception.VetoException;

public class JsClass extends AbstractJsElement{

  private final String m_fullyQualifiedName;
  private final JsFile m_jsFile;
  private boolean m_default = false;
  private final String m_namespace;
  private final String m_name;
  private JsSuperCall m_superCall;
  private final List<JsFunction> m_functions = new ArrayList<>();
  private final List<JsEnum> m_enums = new ArrayList<>();
  private final List<JsConstant> m_constants = new ArrayList<>();

  public JsClass(String fqn, JsFile jsFile) {
    m_fullyQualifiedName = fqn;
    m_jsFile = jsFile;
    String[] split = m_fullyQualifiedName.split("\\.");
    if (split.length != 2) {
      throw new VetoException("Could not separate fqn('" + m_fullyQualifiedName + "') in namespace and name!");
    }
    m_namespace = split[0];
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

  public void setDefault(boolean aDefault) {
    m_default = aDefault;
  }

  public boolean isDefault() {
    return m_default;
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

  public void addEnum(JsEnum jsEnum) {
    m_enums.add(jsEnum);
  }

  public List<JsEnum> getEnums() {
    return Collections.unmodifiableList(m_enums);
  }

  public JsEnum getEnum(String name) {
    return m_enums.stream()
        .filter(c -> name.equals(c.getName()))
        .findFirst()
        .orElse(null);
  }

  public void addConstant(JsConstant constant){
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
    builder.append(getFullyQualifiedName()).append(" [hasConstructor:").append(getConstructor() != null).append(", #functions:").append(m_functions.size()).append("]");
    if (m_functions.size() > 0) {
      builder.append(System.lineSeparator()).append(indent).append("FUNCTIONS:").append(System.lineSeparator())
          .append(m_functions.stream().filter(f -> !f.isConstructor()).map(f -> indent + "- " + f.toString(indent + "  ")).collect(Collectors.joining(System.lineSeparator())));
    }
    if (m_enums.size() > 0) {
      builder.append(System.lineSeparator()).append(indent).append("ENUMS:").append(System.lineSeparator())
          .append(m_enums.stream()
              .map(c -> indent + "- " + c.toString(indent + "  "))
              .collect(Collectors.joining(System.lineSeparator())));
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
