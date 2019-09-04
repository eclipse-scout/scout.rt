package org.eclipse.scout.migration.ecma6.model.old;

//TODO imo: remove?
public class AliasedJsClass {

  private final JsClass m_jsClass;
  private String m_alias;

  public AliasedJsClass(JsClass jsClass){
    this(jsClass,null);
  }

  public AliasedJsClass(JsClass jsClass, String alias){
    m_jsClass = jsClass;
    m_alias = alias;
  }

  public JsClass getJsClass() {
    return m_jsClass;
  }

  public String getAlias() {
    return m_alias;
  }

  public void setAlias(String alias) {
    m_alias = alias;
  }

  public String getReferenceName(){
    if(getAlias() != null){
      return getAlias();
    }
    return getJsClass().getName();
  }
}
