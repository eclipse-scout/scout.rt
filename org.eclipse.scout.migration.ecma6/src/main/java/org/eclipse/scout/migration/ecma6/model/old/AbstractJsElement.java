package org.eclipse.scout.migration.ecma6.model.old;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.scout.migration.ecma6.Migration;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.MultiStatus;
import org.eclipse.scout.rt.platform.status.Status;

public class AbstractJsElement extends AbstractSourceRange{
  MultiStatus m_parseStatus = new MultiStatus();


  public void addParseStatus(IStatus status){
    m_parseStatus.add(status);
  }
  public void addParseError(String errorText){
    addParseStatus(new Status(errorText, IStatus.ERROR));
  }

  public boolean hasParseErrors(){
    return m_parseStatus.getSeverity() == IStatus.ERROR;
  }

  public boolean isParseOk(){
    return m_parseStatus.isOK();
  }

  public String toTodoText(String lineSeparator){
    List<IStatus> warningAndErrors = m_parseStatus.getChildren().stream()
      .filter(s -> s.getSeverity() >= IStatus.WARNING).collect(Collectors.toList());
    if(warningAndErrors.size() > 0) {
     return  warningAndErrors.stream()
        .map(s -> "// " + Migration.TODO_PREFIX + " " + s.getMessage())
        .collect(Collectors.joining(lineSeparator));
    }
    return null;
  }
}
