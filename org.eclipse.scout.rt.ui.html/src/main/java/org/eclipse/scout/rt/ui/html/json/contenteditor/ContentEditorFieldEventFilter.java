package org.eclipse.scout.rt.ui.html.json.contenteditor;

import org.eclipse.scout.rt.client.ui.contenteditor.ContentEditorFieldEvent;
import org.eclipse.scout.rt.ui.html.json.AbstractEventFilter;

public class ContentEditorFieldEventFilter extends AbstractEventFilter<ContentEditorFieldEvent, ContentEditorFieldEventFilterCondition> {

  @Override
  public ContentEditorFieldEvent filter(ContentEditorFieldEvent event) {
    for (ContentEditorFieldEventFilterCondition condition : getConditions()) {
      if (condition.getType() == event.getType()) {
        // Ignore event
        return null;
      }
    }
    return event;
  }
}
