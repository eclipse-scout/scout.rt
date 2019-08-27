package org.eclipse.scout.migration.ecma6.model.old;

public class JsConstant extends AbstractJsElement {

  private final JsClass m_jsClass;
  private final String m_name;
  private String m_body;

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
    return getJsClass().getFullyQuallifiedName()+"."+getName();
  }

  public String getBody() {
    return m_body;
  }

  public void setBody(String body) {
    m_body = body;
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
