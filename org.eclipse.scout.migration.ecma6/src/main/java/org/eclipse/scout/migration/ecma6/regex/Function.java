package org.eclipse.scout.migration.ecma6.regex;

public class Function {

  private String m_namespace;
  private String m_classname;
  private String m_functionName;

  public Function(String namespace, String classname, String functionName) {
    m_namespace = namespace;
    m_classname = classname;
    m_functionName = functionName;
  }

  public String getNamespace() {
    return m_namespace;
  }

  public String getClassname() {
    return m_classname;
  }

  public String getFunctionName() {
    return m_functionName;
  }

  public boolean isConstructor() {
    return getFunctionName() == null;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getNamespace())
        .append(".").append(getClassname());
    if (!isConstructor()) {
      builder.append(".").append(getFunctionName());
    }
    return builder.toString();
  }
}
