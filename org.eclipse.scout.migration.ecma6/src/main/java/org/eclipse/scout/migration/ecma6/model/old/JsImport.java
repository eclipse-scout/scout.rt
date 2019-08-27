package org.eclipse.scout.migration.ecma6.model.old;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Imports are not used before migration and are not created by the parser. Use imports to resolve class references.
 */
public class JsImport extends AbstractJsElement {

  private final JsFile m_fileToImport;
  private final JsFile m_targetFile;
  private final Map<JsClass, AliasedJsClass> m_classes = new HashMap<>();
  private boolean m_allClasses = false;

  public JsImport(JsFile fileToImport, JsFile targetFile) {
    m_fileToImport = fileToImport;
    m_targetFile = targetFile;
  }

  public JsFile getFileToImport() {
    return m_fileToImport;
  }

  public JsFile getTargetFile() {
    return m_targetFile;
  }

  public AliasedJsClass computeAliasIfAbsent(JsClass jsClass) {
    return m_classes.computeIfAbsent(jsClass, jsClass1 -> {
      AliasedJsClass ac = new AliasedJsClass(jsClass);
      if (m_targetFile.getJsClasses().stream().anyMatch(c -> c.getName().equalsIgnoreCase(jsClass.getName()))) {
        ac.setAlias(StringUtility.uppercaseFirst(jsClass.getNamespace()) + jsClass.getName());
      }
      return ac;
    });
  }


  public String toSource(Context context) {
    AliasedJsClass defaultClass = m_classes.entrySet().stream()
      .filter(e -> e.getKey().isDefault())
      .map(e -> e.getValue())
      .findFirst().orElse(null);
    List<AliasedJsClass> classes = m_classes.entrySet().stream()
      .filter(e -> !e.getKey().isDefault())
      .map(e -> e.getValue())
        .collect(Collectors.toList());
    StringBuilder sourceBuilder = new StringBuilder();
    sourceBuilder.append("import");
    if (defaultClass != null) {
      sourceBuilder.append(" ").append(
          Optional.ofNullable(defaultClass.getAlias())
              .orElse(defaultClass.getJsClass().getName()));
    }
    if (classes.size() > 0) {
      sourceBuilder.append(" {");
      sourceBuilder.append(classes.stream().map(c -> {
        StringBuilder b = new StringBuilder();
        b.append(c.getJsClass().getName());
        if(c.getAlias() != null){
          b.append(" as ").append(c.getAlias());
        }
        return b.toString();
      }).collect(Collectors.joining(", ")));
      sourceBuilder.append(" }");
    }
    sourceBuilder.append(" from")
      .append(" '").append(getTargetFile().getPath().getParent().relativize(getFileToImport().getPath())).append("';");
    return sourceBuilder.toString();
  }
}
