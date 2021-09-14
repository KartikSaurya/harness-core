/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.resources;

import io.harness.beans.FeatureFlag;
import io.harness.datahandler.models.FeatureFlagBO;
import io.harness.datahandler.services.AdminFeatureFlagService;
import io.harness.exception.InvalidRequestException;
import io.harness.rest.RestResponse;

import software.wings.security.annotations.AdminPortalAuth;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import retrofit2.http.Body;

@Path("/admin/feature-flags")
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@AdminPortalAuth
public class AdminFeatureFlagResource {
  private AdminFeatureFlagService adminFeatureFlagService;

  @Inject
  public AdminFeatureFlagResource(AdminFeatureFlagService adminFeatureFlagService) {
    this.adminFeatureFlagService = adminFeatureFlagService;
  }

  @GET
  @Path("/")
  public RestResponse<List<FeatureFlagBO>> getAllFeatureFlags() {
    return new RestResponse<>(adminFeatureFlagService.getAllFeatureFlags()
                                  .stream()
                                  .map(FeatureFlagBO::fromFeatureFlag)
                                  .collect(Collectors.toList()));
  }

  @GET
  @Path("/{featureName}")
  public RestResponse<FeatureFlagBO> getFeatureFlag(@PathParam("featureName") String featureName) {
    Optional<FeatureFlag> featureFlagOptional = adminFeatureFlagService.getFeatureFlag(featureName);
    if (!featureFlagOptional.isPresent()) {
      throw new InvalidRequestException("No such feature flag found.");
    }
    return new RestResponse<>(FeatureFlagBO.fromFeatureFlag(featureFlagOptional.get()));
  }

  @PUT
  @Path("/{featureName}")
  public RestResponse<FeatureFlagBO> updateFeatureFlag(
      @PathParam("featureName") String featureFlagName, @Body FeatureFlag featureFlag) {
    Optional<FeatureFlag> featureFlagOptional = adminFeatureFlagService.updateFeatureFlag(featureFlagName, featureFlag);
    if (!featureFlagOptional.isPresent()) {
      throw new InvalidRequestException("No such feature flag found.");
    }
    return new RestResponse<>(FeatureFlagBO.fromFeatureFlag(featureFlagOptional.get()));
  }
}
