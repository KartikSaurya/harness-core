/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.sm.states;

import static io.harness.beans.PageRequest.PageRequestBuilder.aPageRequest;
import static io.harness.beans.SearchFilter.Operator.EQ;
import static io.harness.data.structure.EmptyPredicate.isEmpty;

import static java.util.stream.Collectors.toMap;

import software.wings.beans.EntityType;
import software.wings.beans.Service;
import software.wings.beans.command.ServiceCommand;
import software.wings.service.intfc.ServiceResourceService;
import software.wings.stencils.DataProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Singleton
public class CommandStateEnumDataProvider implements DataProvider {
  @Inject private ServiceResourceService serviceResourceService;

  @Override
  public Map<String, String> getData(String appId, Map<String, String> params) {
    String serviceId = params.get(EntityType.SERVICE.name());
    if (appId != null) {
      List<Service> services;
      if (params.get("NONE") != null) {
        return new HashMap<>();
      }
      if (isEmpty(serviceId)) {
        services =
            serviceResourceService.list(aPageRequest().addFilter("appId", EQ, appId).build(), false, true, false, null)
                .getResponse();
      } else {
        Service service = serviceResourceService.get(appId, serviceId, true);
        services = service == null ? Collections.EMPTY_LIST : Collections.singletonList(service);
      }
      return services.stream()
          .filter(service -> service.getServiceCommands() != null)
          .flatMap(service -> service.getServiceCommands().stream())
          .map(ServiceCommand::getName)
          .distinct()
          .collect(toMap(Function.identity(), Function.identity()));
    }
    return new HashMap<>();
  }
}
