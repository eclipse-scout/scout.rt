package org.eclipse.scout.migration.ecma6.model.old;

public class JsConstant extends AbstractJsElement {

  private final JsClass m_jsClass;
  private final String m_name;

  public JsConstant(JsClass jsClass, String name){
    m_jsClass = jsClass;
    m_name = name;
  }

  public JsClass getJsClass() {
    return m_jsClass;
  }

  public String getName() {
    return m_name;
  }

  public String getFqn(){
    return getJsClass().getFullyQualifiedName() + "." + getName();
  }


  @Override
  public String toString() {
    return toString("");
  }

  public String toString(String indent){
    StringBuilder builder = new StringBuilder();
    builder.append(getName());
    return builder.toString();
  }
}
