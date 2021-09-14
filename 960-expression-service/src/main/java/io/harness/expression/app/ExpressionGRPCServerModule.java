/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.expression.app;

import io.harness.expression.service.ExpressionEvaulatorServiceGrpc;
import io.harness.expression.service.ExpressionServiceImpl;
import io.harness.grpc.auth.ServiceInfo;
import io.harness.grpc.server.Connector;
import io.harness.grpc.server.GrpcServerModule;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import java.util.List;
import java.util.Set;

public class ExpressionGRPCServerModule extends AbstractModule {
  private static final String SERVICE_ID = "expression-service";
  private final List<Connector> connectors;
  private final String serviceSecret;

  @Inject
  public ExpressionGRPCServerModule(List<Connector> connectors, String serviceSecret) {
    this.connectors = connectors;
    this.serviceSecret = serviceSecret;
  }

  @Override
  protected void configure() {
    install(new ExpressionServiceModule());
    Multibinder<BindableService> bindableServiceMultibinder = Multibinder.newSetBinder(binder(), BindableService.class);
    bindableServiceMultibinder.addBinding().to(ExpressionServiceImpl.class);

    MapBinder<String, ServiceInfo> stringServiceInfoMapBinder =
        MapBinder.newMapBinder(binder(), String.class, ServiceInfo.class);
    stringServiceInfoMapBinder.addBinding(ExpressionEvaulatorServiceGrpc.SERVICE_NAME)
        .toInstance(ServiceInfo.builder().id(SERVICE_ID).secret(serviceSecret).build());

    install(new GrpcServerModule(connectors, getProvider(Key.get(new TypeLiteral<Set<BindableService>>() {})),
        getProvider(Key.get(new TypeLiteral<Set<ServerInterceptor>>() {}))));
  }

  @Provides
  @Singleton
  public ServiceManager serviceManager(Set<Service> services) {
    return new ServiceManager(services);
  }
}
