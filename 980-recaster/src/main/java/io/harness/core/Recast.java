/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.core;

import io.harness.annotation.RecasterAlias;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.transformers.RecastTransformer;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;

/**
 * The translation layer for conversion from PipelineService json string to sdk objects
 */
@OwnedBy(HarnessTeam.PIPELINE)
public class Recast {
  private final Recaster recaster;
  private final AliasRegistry aliasRegistry;

  public Recast() {
    this(new Recaster(), new HashSet<>());
  }

  public Recast(final Set<Class<?>> classesToMap) {
    this(new Recaster(), classesToMap);
  }

  public Recast(final Recaster recaster, final Set<Class<?>> classesToMap) {
    this.recaster = recaster;
    this.aliasRegistry = AliasRegistry.getInstance();

    for (final Class<?> c : classesToMap) {
      map(c);
    }
  }

  public synchronized Recast map(final Class<?>... entityClasses) {
    if (entityClasses != null && entityClasses.length > 0) {
      for (final Class<?> entityClass : entityClasses) {
        if (!recaster.isCasted(entityClass)) {
          recaster.addCastedClass(entityClass);
        }
      }
    }
    return this;
  }

  public synchronized void addTransformer(RecastTransformer recastTransformer) {
    if (recastTransformer == null) {
      return;
    }
    recaster.getTransformer().addCustomTransformer(recastTransformer);
  }

  public void registerAliases(String... params) {
    Reflections reflections = new Reflections((Object[]) params);
    Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(RecasterAlias.class);
    aliasRegistry.addPackages(params);
    typesAnnotatedWith.forEach(aliasRegistry::register);
  }

  public <T> T fromMap(final Map<String, Object> map, final Class<T> entityClazz) {
    return recaster.fromMap(map, entityClazz);
  }

  public Map<String, Object> toMap(final Object entity) {
    Map<String, Object> map = recaster.toMap(entity);
    return map == null ? null : new LinkedHashMap<>(map);
  }
}
