/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoNode;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.IDoCollection;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.IDoEntityContribution;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.util.BooleanUtility;

/**
 * DO entity mappings are usually initialized via a subclass of {@link AbstractDoEntityMapper}.
 */
public class DoEntityMappings<DO_ENTITY extends IDoEntity, PEER> {

  // Modifications to this list are not permitted once the instance of this class is created and initialized,
  // otherwise ConcurrentModificationException can occur when to/fromDo is used concurrently with calls to the with methods.
  protected final List<IDoEntityMapping<DO_ENTITY, PEER>> m_mappings = new ArrayList<>();

  /**
   * Applies all mappings from source to DO entity.
   */
  public void toDo(PEER source, DO_ENTITY doEntity) {
    m_mappings.forEach(mapping -> mapping.toDo(source, doEntity));
  }

  /**
   * Applies all mappings from DO entity to target.
   */
  public void fromDo(DO_ENTITY doEntity, PEER target) {
    m_mappings.forEach(n -> n.fromDo(doEntity, target));
  }

  /**
   * Generic mapping, only use if otherwise not possible.
   * <p>
   * for {@link IDoEntityMapping#fromDo(IDoEntity, Object)}, make sure to handle node existence check (i.e.
   * {@link DoNode#exists()}) manually before setting any value in target.
   */
  public <VALUE> DoEntityMappings<DO_ENTITY, PEER> with(IDoEntityMapping<DO_ENTITY, PEER> mapping) {
    m_mappings.add(mapping);
    return this;
  }

  /**
   * Generic mapping, only use if otherwise not possible.
   *
   * @param fromDoConsumer
   *     Make sure to handle node existence check (i.e. {@link DoNode#exists()}) manually before setting any value
   *     in target.
   */
  public <VALUE> DoEntityMappings<DO_ENTITY, PEER> with(BiConsumer<PEER, DO_ENTITY> toDoConsumer, BiConsumer<DO_ENTITY, PEER> fromDoConsumer) {
    m_mappings.add(new IDoEntityMapping<>() {

      @Override
      public void toDo(PEER source, DO_ENTITY dataObject) {
        if (toDoConsumer != null) {
          toDoConsumer.accept(source, dataObject);
        }
      }

      @Override
      public void fromDo(DO_ENTITY dataObject, PEER target) {
        if (fromDoConsumer != null) {
          fromDoConsumer.accept(dataObject, target);
        }
      }
    });
    return this;
  }

  /**
   * Adds a read-only mapping for the given node (source -> DO entity, no setter for target).
   * <p>
   * There is no duplicate detection for the node-based mappings, meaning that adding a DO node multiple times with
   * different mappings will execute all the mappings.
   */
  public <VALUE> DoEntityMappings<DO_ENTITY, PEER> with(Function<DO_ENTITY, DoNode<VALUE>> doNode, Function<PEER, VALUE> valueGetter) {
    return with(doNode, valueGetter, null);
  }

  /**
   * Adds a mapping for the given collection node (getter/setter for peer).
   * <p>
   * There is no duplicate detection for the node-based mappings, meaning that adding a DO node multiple times with
   * different mappings will execute all the mappings.
   * <p>
   * When working with collection nodes the peer side usually uses a collection (i.e. {@link Collection}, {@link List}
   * or {@link Set}) directly and not a DO collection node (i.e. {@link DoCollection}, {@link DoList} or {@link DoSet}),
   * thus use {@link #with(Function, Function, BiConsumer)}. But there might be special peers working with a DO
   * collection node, thus this method is provided for convenience.
   */
  public <VALUE, COLLECTION extends Collection<VALUE>, DO_COLLECTION extends IDoCollection<VALUE, COLLECTION>> DoEntityMappings<DO_ENTITY, PEER> withDoCollection(
      Function<DO_ENTITY, DO_COLLECTION> doCollectionFunction,
      Function<PEER, DO_COLLECTION> valueGetter,
      BiConsumer<PEER, DO_COLLECTION> valueSetter) {
    m_mappings.add(new IDoEntityMapping<>() {

      @Override
      public void toDo(PEER peer, DO_ENTITY doEntity) {
        if (valueGetter == null) {
          return;
        }

        DO_COLLECTION valueCollectionNode = valueGetter.apply(peer); // might be null from provided getter
        COLLECTION values = valueCollectionNode == null ? null : valueCollectionNode.get();
        DO_COLLECTION collectionNode = doCollectionFunction.apply(doEntity); // never null
        collectionNode.updateAll(values);
      }

      @Override
      public void fromDo(DO_ENTITY dataObject, PEER peer) {
        if (valueSetter == null) {
          return;
        }

        DO_COLLECTION collectionNode = doCollectionFunction.apply(dataObject);
        if (!collectionNode.exists()) {
          return;
        }

        valueSetter.accept(peer, collectionNode);
      }
    });
    return this;
  }

  /**
   * Adds a mapping for the given node (getter/setter for peer).
   * <p>
   * There is no duplicate detection for the node-based mappings, meaning that adding a DO node multiple times with
   * different mappings will execute all the mappings.
   */
  public <VALUE> DoEntityMappings<DO_ENTITY, PEER> with(Function<DO_ENTITY, DoNode<VALUE>> doNodeFunction, Function<PEER, VALUE> valueGetter, BiConsumer<PEER, VALUE> valueSetter) {
    m_mappings.add(new IDoEntityMapping<>() {

      @Override
      public void toDo(PEER peer, DO_ENTITY doEntity) {
        if (valueGetter == null) {
          return;
        }

        VALUE value = valueGetter.apply(peer);
        DoNode<VALUE> doNode = doNodeFunction.apply(doEntity);
        doNode.set(value);
      }

      @Override
      public void fromDo(DO_ENTITY dataObject, PEER peer) {
        if (valueSetter == null) {
          return;
        }

        DoNode<VALUE> doNode = doNodeFunction.apply(dataObject);
        if (!doNode.exists()) {
          return;
        }

        valueSetter.accept(peer, doNode.get());
      }
    });
    return this;
  }

  /**
   * Adds a mapping for the given node (peer is a {@link IHolder}).
   * <p>
   * There is no duplicate detection for the node-based mappings, meaning that adding a DO node multiple times with
   * different mappings will execute all the mappings.
   */
  public <VALUE> DoEntityMappings<DO_ENTITY, PEER> withHolder(Function<DO_ENTITY, DoNode<VALUE>> doNodeFunction, Function<PEER, IHolder<VALUE>> holderFunction) {
    return with(doNodeFunction, peer -> holderFunction.apply(peer).getValue(), (peer, value) -> holderFunction.apply(peer).setValue(value));
  }

  /**
   * Adds a child mapping for the given node (via own {@link DoEntityMappings}). A child mapping is used if the node
   * represents a {@link IDoEntity} again.
   * <p>
   * There is no duplicate detection for the node-based mappings, meaning that adding a DO node multiple times with
   * different mappings will execute all the mappings.
   */
  public <CHILD_DO_ENTITY extends IDoEntity, CHILD_PEER> DoEntityMappings<DO_ENTITY, PEER> withChildMapping(
      Function<DO_ENTITY, DoNode<CHILD_DO_ENTITY>> childNodeFunction,
      Class<CHILD_DO_ENTITY> childDoEntityClass,
      Function<PEER, CHILD_PEER> childPeerFunction,
      Consumer<DoEntityMappings<CHILD_DO_ENTITY, CHILD_PEER>> initChildMappingsConsumer) {
    DoEntityMappings<CHILD_DO_ENTITY, CHILD_PEER> mappings = new DoEntityMappings<>();
    initChildMappingsConsumer.accept(mappings);
    m_mappings.add(new IDoEntityMapping<>() {

      @SuppressWarnings("unchecked")
      @Override
      public void toDo(PEER peer, DO_ENTITY doEntity) {
        CHILD_PEER childPeer = childPeerFunction.apply(peer);
        DoNode<CHILD_DO_ENTITY> childNode = childNodeFunction.apply(doEntity);
        CHILD_DO_ENTITY childDoEntity = childNode.get();
        if (childDoEntity == null) {
          childDoEntity = BEANS.get(childDoEntityClass);
          childNode.set(childDoEntity);
        }
        mappings.toDo(childPeer, childDoEntity);
      }

      @SuppressWarnings("unchecked")
      @Override
      public void fromDo(DO_ENTITY doEntity, PEER peer) {
        DoNode<CHILD_DO_ENTITY> childNode = childNodeFunction.apply(doEntity);
        if (!childNode.exists()) {
          return;
        }

        CHILD_PEER childPeer = childPeerFunction.apply(peer);
        CHILD_DO_ENTITY childDoEntity = childNode.get();
        if (childDoEntity != null) {
          mappings.fromDo(childDoEntity, childPeer);
        }
      }
    });
    return this;
  }

  /**
   * Adds a child mapping for the given node (via <code>toDoConsumer</code>/<code>fromDoConsumer</code>. A child mapping
   * is used if the node represents a {@link IDoEntity} again.
   * <p>
   * There is no duplicate detection for the node-based mappings, meaning that adding a DO node multiple times with
   * different mappings will execute all the mappings.
   */
  public <CHILD_DO_ENTITY extends IDoEntity, CHILD_PEER> DoEntityMappings<DO_ENTITY, PEER> withChildMapping(
      Function<DO_ENTITY, DoNode<CHILD_DO_ENTITY>> childNodeFunction,
      Class<CHILD_DO_ENTITY> childDoEntityClass,
      Function<PEER, CHILD_PEER> childPeerFunction,
      BiConsumer<CHILD_PEER, CHILD_DO_ENTITY> toDoConsumer, // switched order to allow lambda reference notation for caller
      BiConsumer<CHILD_PEER, CHILD_DO_ENTITY> fromDoConsumer) {
    m_mappings.add(new IDoEntityMapping<>() {

      @Override
      public void toDo(PEER peer, DO_ENTITY dataObject) {
        CHILD_PEER childPeer = childPeerFunction.apply(peer);
        DoNode<CHILD_DO_ENTITY> childDoNode = childNodeFunction.apply(dataObject);
        CHILD_DO_ENTITY childDo = childDoNode.get();
        if (childDo == null) {
          childDo = BEANS.get(childDoEntityClass);
          childDoNode.set(childDo);
        }
        toDoConsumer.accept(childPeer, childDo);
      }

      @Override
      public void fromDo(DO_ENTITY dataObject, PEER peer) {
        DoNode<CHILD_DO_ENTITY> childNode = childNodeFunction.apply(dataObject);
        if (!childNode.exists()) {
          return;
        }

        CHILD_PEER childPeer = childPeerFunction.apply(peer);
        CHILD_DO_ENTITY childDo = childNode.get();
        if (childDo != null) {
          fromDoConsumer.accept(childPeer, childDo);
        }
      }
    });
    return this;
  }

  /**
   * Adds a mapping for a contribution.
   * <p>
   * The contribution is always added, use {@link #withContribution(Class, Predicate, Consumer)} to add contribution
   * depending on a predicate.
   */
  public <CONTRIBUTION_DO_ENTITY extends IDoEntityContribution> DoEntityMappings<DO_ENTITY, PEER> withContribution(
      Class<CONTRIBUTION_DO_ENTITY> contributionClass,
      Consumer<DoEntityMappings<CONTRIBUTION_DO_ENTITY, PEER>> contributionMappingsConsumer) {
    return withContribution(contributionClass, x -> true, contributionMappingsConsumer);
  }

  /**
   * Adds a mapping for a contribution with an add predicate.
   *
   * @param addPredicate
   *     Contribution is only added if predicate returns <code>true</code>. Recommended for optional contributions
   *     which should only be present if e.g. non-empty.
   */
  public <CONTRIBUTION_DO_ENTITY extends IDoEntityContribution> DoEntityMappings<DO_ENTITY, PEER> withContribution(
      Class<CONTRIBUTION_DO_ENTITY> contributionClass,
      Predicate<CONTRIBUTION_DO_ENTITY> addPredicate,
      Consumer<DoEntityMappings<CONTRIBUTION_DO_ENTITY, PEER>> contributionMappingsConsumer) {
    DoEntityMappings<CONTRIBUTION_DO_ENTITY, PEER> mappings = new DoEntityMappings<>();
    contributionMappingsConsumer.accept(mappings);
    m_mappings.add(new IDoEntityMapping<>() {

      @Override
      public void toDo(PEER peer, DO_ENTITY doEntity) {
        CONTRIBUTION_DO_ENTITY contribution = BEANS.get(contributionClass);
        mappings.toDo(peer, contribution);
        if (addPredicate.test(contribution)) {
          // only add contribution if predicate is true
          doEntity.putContribution(contribution);
        }
      }

      @Override
      public void fromDo(DO_ENTITY doEntity, PEER peer) {
        CONTRIBUTION_DO_ENTITY contribution = doEntity.getContribution(contributionClass); // no action if contribution doesn't exist
        if (contribution != null) {
          mappings.fromDo(contribution, peer);
        }
      }
    });
    return this;
  }

  /**
   * Adds a mapping for a contribution with an add predicate allowing to use an own sub peer class.
   * <p>
   * The contribution is always added, use {@link #withContribution(Class, Class, Predicate, Consumer)} to add
   * contribution depending on a predicate.
   */
  public <CONTRIBUTION_DO_ENTITY extends IDoEntityContribution, SUB_PEER extends PEER> DoEntityMappings<DO_ENTITY, PEER> withContribution(
      Class<CONTRIBUTION_DO_ENTITY> contributionClass,
      Class<SUB_PEER> subPeerClass,
      Consumer<DoEntityMappings<CONTRIBUTION_DO_ENTITY, SUB_PEER>> contributionMappingsConsumer) {
    return withContribution(contributionClass, subPeerClass, x -> true, contributionMappingsConsumer);
  }

  /**
   * Adds a mapping for a contribution allowing to use an own sub peer class.
   *
   * @param addPredicate
   *     Contribution is only added if predicate returns <code>true</code>. Recommended for optional contributions
   *     which should only be present if e.g. non-empty.
   */
  public <CONTRIBUTION_DO_ENTITY extends IDoEntityContribution, SUB_PEER extends PEER> DoEntityMappings<DO_ENTITY, PEER> withContribution(
      Class<CONTRIBUTION_DO_ENTITY> contributionClass,
      Class<SUB_PEER> subPeerClass,
      Predicate<CONTRIBUTION_DO_ENTITY> addPredicate,
      Consumer<DoEntityMappings<CONTRIBUTION_DO_ENTITY, SUB_PEER>> contributionMappingsConsumer) {
    withContribution(contributionClass, addPredicate, m -> m.withChainedMapping(contributionClass, subPeerClass, contributionMappingsConsumer));
    return this;
  }

  /**
   * Adds a mapping that will apply all the chained mappings.
   */
  public <SUB_DO_ENTITY extends DO_ENTITY, SUB_PEER extends PEER> DoEntityMappings<DO_ENTITY, PEER> withChainedMapping(
      Class<SUB_DO_ENTITY> subDoEntityClass,
      Class<SUB_PEER> subPeerClass,
      Consumer<DoEntityMappings<SUB_DO_ENTITY, SUB_PEER>> mappingsConsumer) {
    DoEntityMappings<SUB_DO_ENTITY, SUB_PEER> mappings = new DoEntityMappings<>();
    mappingsConsumer.accept(mappings);
    m_mappings.add(new IDoEntityMapping<>() {

      @SuppressWarnings("unchecked")
      @Override
      public void toDo(PEER source, DO_ENTITY dataObject) {
        validateType(source, dataObject);
        mappings.toDo((SUB_PEER) source, (SUB_DO_ENTITY) dataObject);
      }

      @SuppressWarnings("unchecked")
      @Override
      public void fromDo(DO_ENTITY dataObject, PEER target) {
        validateType(target, dataObject);
        mappings.fromDo((SUB_DO_ENTITY) dataObject, (SUB_PEER) target);
      }

      private void validateType(PEER peer, DO_ENTITY doEntity) {
        if (!subDoEntityClass.isInstance(doEntity) || !subPeerClass.isInstance(peer)) {
          throw new PlatformException("Peer {} or do entity {} is not is not of expected type: {} or {}", peer, doEntity, subDoEntityClass, subPeerClass);
        }
      }
    });
    return this;
  }

  /**
   * Helper method to be used with e.g. {@link #with(Function, Function, BiConsumer)} that converts the value of a
   * {@link Boolean} node to the value required by a setter accepting primitive <code>boolean</code>.
   */
  public static <E> BiConsumer<E, Boolean> toPrimitiveBoolean(BiConsumer<E, Boolean> consumer) {
    return (target, value) -> consumer.accept(target, BooleanUtility.nvl(value));
  }
}
