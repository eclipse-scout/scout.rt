package org.eclipse.scout.rt.shared.extension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.ClassIdentifier;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.IOrdered;
import org.eclipse.scout.service.SERVICES;

/**
 * The top level list is expected to be sorted by the caller.
 */
public abstract class AbstractMoveModelObjectHandler<T extends IOrdered> {

  private final List<T> m_rootModelObjects;
  private final String m_modelObjectTypeName;
  private final IInternalExtensionRegistry m_extensionRegistry;

  public AbstractMoveModelObjectHandler(String modelObjectTypeName, List<T> rootModelObjects) {
    m_rootModelObjects = rootModelObjects;
    m_modelObjectTypeName = modelObjectTypeName;
    m_extensionRegistry = SERVICES.getService(IInternalExtensionRegistry.class);
  }

  protected abstract T getParent(T child);

  protected abstract void removeChild(T parent, T child);

  protected abstract void addChild(T parent, T child);

  protected abstract void sortChildren(T parent);

  protected abstract List<T> collectAllModelObjects();

  protected List<? extends T> getRootModelObjects() {
    return m_rootModelObjects;
  }

  public void moveModelObjects() {
    // collect all model objects and move descriptors
    List<T> allModelObject = collectAllModelObjects();
    Set<MoveDescriptor<T>> moveDescriptors = collectMoveDescriptors(allModelObject);

    if (CollectionUtility.isEmpty(moveDescriptors)) {
      return;
    }

    StringBuilder sb = new StringBuilder();
    for (MoveDescriptor<T> moveDescriptor : moveDescriptors) {
      T modelObject = moveDescriptor.getModel();
      T oldParent = getParent(modelObject);

      Class<?> newContainer = null;
      ClassIdentifier newContainerIdentifer = moveDescriptor.getNewContainerIdentifer();
      if (newContainerIdentifer != null) {
        if (newContainerIdentifer.size() > 1) {
          throw new IllegalExtensionException("multi-segment move target class identifier are not supported by this handler. " + moveDescriptor);
        }
        newContainer = newContainerIdentifer.getLastSegment();
      }

      if (oldParent == null && (newContainer == null || newContainer == IMoveModelObjectToRootMarker.class)) {
        // 1. model object is in root list and should stay there -> just update order without sorting the list. The method expects, that the top level
        //    list is sorted by the caller.
        applyOrder(moveDescriptor, modelObject);
      }

      else if (oldParent != null && newContainer == IMoveModelObjectToRootMarker.class) {
        // 2. model object is not in root list, but should be moved into it
        removeChild(oldParent, modelObject);
        applyOrder(moveDescriptor, modelObject);
        m_rootModelObjects.add(modelObject);
      }

      else if (newContainer == null) {
        // 3. model object remains in its current container -> just update order and sort items
        applyOrder(moveDescriptor, modelObject);
        sortChildren(oldParent);
      }

      else {
        // 4. model object is moved into another container

        // find new parent
        T newParent = null;
        for (T a : allModelObject) {
          if (newContainer.isInstance(a) && a != modelObject) {
            newParent = a;
            break;
          }
        }

        if (newParent == null) {
          if (sb.length() == 0) {
            sb.append("Invalid ").append(m_modelObjectTypeName).append(" move commands:");
          }
          sb.append("  \n").append(m_modelObjectTypeName).append(" '").append(modelObject).append("' cannot be moved into container '").append(newParent).append("'");
          continue;
        }

        // remove from old parent
        if (oldParent == null) {
          // remove model object from root list
          m_rootModelObjects.remove(modelObject);
        }
        else {
          // remove model object from old parent
          removeChild(oldParent, modelObject);
        }

        // apply order and add to new parent
        applyOrder(moveDescriptor, modelObject);
        addChild(newParent, modelObject);
      }
    }

    if (sb.length() > 0) {
      throw new IllegalArgumentException(sb.toString());
    }
  }

  protected void applyOrder(MoveDescriptor<T> moveItem, T actionNode) {
    Double newOrder = moveItem.getNewOrder();
    if (newOrder != null) {
      actionNode.setOrder(newOrder);
    }
  }

  protected Set<MoveDescriptor<T>> collectMoveDescriptors(List<T> allActionNodes) {
    if (CollectionUtility.isEmpty(allActionNodes)) {
      return null;
    }
    Set<MoveDescriptor<T>> moveDescriptors = new HashSet<MoveDescriptor<T>>();
    for (T actionNode : allActionNodes) {
      MoveDescriptor<T> moveDesc = m_extensionRegistry.createModelMoveDescriptorFor(actionNode, null);
      if (moveDesc != null) {
        moveDescriptors.add(moveDesc);
      }
    }
    return moveDescriptors;
  }
}
