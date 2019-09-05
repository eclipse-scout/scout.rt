package org.eclipse.scout.migration.ecma6.model.old;

public class JsEnum extends  AbstractJsElement{

  private JsClass m_jsClass;
  private String m_name;

  public JsEnum(JsClass jsClass, String name){
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
