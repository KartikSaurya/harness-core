/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.delegate.task.citasks.cik8handler.k8java.container;

/**
 * An abstract class to generate K8 container spec based on container parameters provided to it. It builds minimal
 * container spec essential for creating a container on a pod. It is designed to be a superclass for concrete K8
 * container specification builder.
 */

import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.delegate.task.citasks.cik8handler.params.CIConstants.DIND_TAG_REGEX;
import static io.harness.delegate.task.citasks.cik8handler.params.CIConstants.DOCKER_IMAGE_NAME;
import static io.harness.delegate.task.citasks.cik8handler.params.CIConstants.PLUGIN_ACR_IMAGE_NAME;
import static io.harness.delegate.task.citasks.cik8handler.params.CIConstants.PLUGIN_DOCKER_IMAGE_NAME;
import static io.harness.delegate.task.citasks.cik8handler.params.CIConstants.PLUGIN_ECR_IMAGE_NAME;
import static io.harness.delegate.task.citasks.cik8handler.params.CIConstants.PLUGIN_GCR_IMAGE_NAME;
import static io.harness.delegate.task.citasks.cik8handler.params.CIConstants.PLUGIN_HEROKU_IMAGE_NAME;
import static io.harness.validation.Validator.notNullCheck;

import static java.lang.String.format;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.delegate.beans.ci.pod.ContainerParams;
import io.harness.delegate.beans.ci.pod.ContainerResourceParams;
import io.harness.delegate.beans.ci.pod.SecretVarParams;
import io.harness.delegate.task.citasks.cik8handler.params.CIConstants;
import io.harness.k8s.model.ImageDetails;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.V1ContainerBuilder;
import io.kubernetes.client.openapi.models.V1ContainerPort;
import io.kubernetes.client.openapi.models.V1ContainerPortBuilder;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1EnvVarBuilder;
import io.kubernetes.client.openapi.models.V1EnvVarSourceBuilder;
import io.kubernetes.client.openapi.models.V1KeyToPathBuilder;
import io.kubernetes.client.openapi.models.V1LocalObjectReference;
import io.kubernetes.client.openapi.models.V1LocalObjectReferenceBuilder;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import io.kubernetes.client.openapi.models.V1ResourceRequirementsBuilder;
import io.kubernetes.client.openapi.models.V1SecretKeySelectorBuilder;
import io.kubernetes.client.openapi.models.V1SecretVolumeSourceBuilder;
import io.kubernetes.client.openapi.models.V1SecurityContext;
import io.kubernetes.client.openapi.models.V1SecurityContextBuilder;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1VolumeBuilder;
import io.kubernetes.client.openapi.models.V1VolumeMount;
import io.kubernetes.client.openapi.models.V1VolumeMountBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@OwnedBy(HarnessTeam.CI)
public abstract class BaseContainerSpecBuilder {
  public ContainerSpecBuilderResponse createSpec(ContainerParams containerParams) {
    ContainerSpecBuilderResponse containerSpecBuilderResponse = getBaseSpec(containerParams);
    decorateSpec(containerParams, containerSpecBuilderResponse);
    return containerSpecBuilderResponse;
  }

  /**
   * Builds on minimal container spec generated by getBaseSpec method.
   */
  protected abstract void decorateSpec(
      ContainerParams containerParams, ContainerSpecBuilderResponse containerSpecBuilderResponse);

  private ContainerSpecBuilderResponse getBaseSpec(ContainerParams containerParams) {
    notNullCheck("Container parameters should be specified", containerParams);

    List<V1EnvVar> envVars = getContainerEnvVars(containerParams.getEnvVars(), containerParams.getSecretEnvVars());
    List<V1ContainerPort> containerPorts = new ArrayList<>();
    if (containerParams.getPorts() != null) {
      containerParams.getPorts().forEach(
          port -> containerPorts.add(new V1ContainerPortBuilder().withContainerPort(port).build()));
    }

    List<V1VolumeMount> volumeMounts = new ArrayList<>();
    List<V1Volume> volumes = new ArrayList<>();
    if (containerParams.getVolumeToMountPath() != null) {
      containerParams.getVolumeToMountPath().forEach(
          (volumeName, volumeMountPath)
              -> volumeMounts.add(
                  new V1VolumeMountBuilder().withName(volumeName).withMountPath(volumeMountPath).build()));
    }
    if (containerParams.getSecretVolumes() != null) {
      containerParams.getSecretVolumes().forEach((key, secretVolumeParams) -> {
        volumeMounts.add(new V1VolumeMountBuilder()
                             .withName(CIConstants.SECRET_VOLUME_NAME)
                             .withMountPath(secretVolumeParams.getMountPath())
                             .build());
        volumes.add(new V1VolumeBuilder()
                        .withName(CIConstants.SECRET_VOLUME_NAME)
                        .withSecret(new V1SecretVolumeSourceBuilder()
                                        .withSecretName(secretVolumeParams.getSecretName())
                                        .withItems(new V1KeyToPathBuilder()
                                                       .withKey(secretVolumeParams.getSecretKey())
                                                       .withMode(CIConstants.SECRET_FILE_MODE)
                                                       .withPath(secretVolumeParams.getSecretKey())
                                                       .build())
                                        .build())
                        .build());
      });
    }

    V1LocalObjectReference imageSecret = null;
    ImageDetails imageDetails = containerParams.getImageDetailsWithConnector().getImageDetails();
    if (containerParams.getImageSecret() != null) {
      imageSecret = new V1LocalObjectReferenceBuilder().withName(containerParams.getImageSecret()).build();
    }

    V1ResourceRequirements resourceRequirements = getResourceRequirements(containerParams.getContainerResourceParams());

    String imageName = imageDetails.getName();
    if (isNotEmpty(imageDetails.getTag())) {
      imageName = imageName + ":" + imageDetails.getTag();
    }

    V1ContainerBuilder containerBuilder = new V1ContainerBuilder()
                                              .withName(containerParams.getName())
                                              .withImage(imageName)
                                              .withCommand(containerParams.getCommands())
                                              .withArgs(containerParams.getArgs())
                                              .withEnv(envVars)
                                              .withResources(resourceRequirements)
                                              .withPorts(containerPorts)
                                              .withImagePullPolicy(containerParams.getImagePullPolicy())
                                              .withVolumeMounts(volumeMounts);

    boolean isPrivilegedImage = isPrivilegedImage(imageDetails);
    containerBuilder.withSecurityContext(
        getSecurityContext(containerParams.isPrivileged() || isPrivilegedImage, containerParams.getRunAsUser()));

    if (isNotEmpty(containerParams.getWorkingDir())) {
      containerBuilder.withWorkingDir(containerParams.getWorkingDir());
    }

    return ContainerSpecBuilderResponse.builder()
        .containerBuilder(containerBuilder)
        .imageSecret(imageSecret)
        .volumes(volumes)
        .build();
  }

  private V1SecurityContext getSecurityContext(boolean privileged, Integer runAsUser) {
    V1SecurityContextBuilder builder = new V1SecurityContextBuilder().withPrivileged(privileged);
    if (runAsUser != null) {
      builder.withRunAsUser((long) runAsUser);
    }
    return builder.build();
  }

  private boolean isPrivilegedImage(ImageDetails imageDetails) {
    String imageName = imageDetails.getName();
    String tag = imageDetails.getTag();
    if (imageName.equals(PLUGIN_DOCKER_IMAGE_NAME) || imageName.equals(PLUGIN_ECR_IMAGE_NAME)
        || imageName.equals(PLUGIN_ACR_IMAGE_NAME) || imageName.equals(PLUGIN_GCR_IMAGE_NAME)
        || imageName.equals(PLUGIN_HEROKU_IMAGE_NAME)) {
      return true;
    }

    if (imageName.equals(DOCKER_IMAGE_NAME) && isNotEmpty(tag) && tag.matches(DIND_TAG_REGEX)) {
      return true;
    }
    return false;
  }

  private V1ResourceRequirements getResourceRequirements(ContainerResourceParams containerResourceParams) {
    if (containerResourceParams == null) {
      return null;
    }

    Integer resourceRequestMemoryMiB = containerResourceParams.getResourceRequestMemoryMiB();
    Integer resourceLimitMemoryMiB = containerResourceParams.getResourceLimitMemoryMiB();
    Integer resourceRequestMilliCpu = containerResourceParams.getResourceRequestMilliCpu();
    Integer resourceLimitMilliCpu = containerResourceParams.getResourceLimitMilliCpu();
    V1ResourceRequirementsBuilder builder = new V1ResourceRequirementsBuilder();
    if (resourceRequestMemoryMiB != null && resourceRequestMemoryMiB != 0) {
      builder.addToRequests(
          CIConstants.MEMORY, new Quantity(format("%d%s", resourceRequestMemoryMiB, CIConstants.MEMORY_FORMAT)));
    }
    if (resourceLimitMemoryMiB != null && resourceLimitMemoryMiB != 0) {
      builder.addToLimits(
          CIConstants.MEMORY, new Quantity(format("%d%s", resourceLimitMemoryMiB, CIConstants.MEMORY_FORMAT)));
    }

    if (resourceRequestMilliCpu != null && resourceRequestMilliCpu != 0) {
      builder.addToRequests(
          CIConstants.CPU, new Quantity(format("%d%s", resourceRequestMilliCpu, CIConstants.CPU_FORMAT)));
    }
    if (resourceLimitMilliCpu != null && resourceLimitMilliCpu != 0) {
      builder.addToLimits(CIConstants.CPU, new Quantity(format("%d%s", resourceLimitMilliCpu, CIConstants.CPU_FORMAT)));
    }

    return builder.build();
  }

  private List<V1EnvVar> getContainerEnvVars(Map<String, String> envVars, Map<String, SecretVarParams> secretEnvVars) {
    List<V1EnvVar> ctrEnvVars = new ArrayList<>();
    if (envVars != null) {
      envVars.forEach((name, val) -> ctrEnvVars.add(new V1EnvVarBuilder().withName(name).withValue(val).build()));
    }
    if (secretEnvVars != null) {
      secretEnvVars.forEach(
          (name, secretKeyParam)
              -> ctrEnvVars.add(new V1EnvVarBuilder()
                                    .withName(name)
                                    .withValueFrom(new V1EnvVarSourceBuilder()
                                                       .withSecretKeyRef(new V1SecretKeySelectorBuilder()
                                                                             .withKey(secretKeyParam.getSecretKey())
                                                                             .withName(secretKeyParam.getSecretName())
                                                                             .build())
                                                       .build())
                                    .build()));
    }
    return ctrEnvVars;
  }
}
