// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: io/harness/event/payloads/k8s_utilization_messages.proto

package io.harness.event.payloads;

@javax.annotation.Generated(value = "protoc", comments = "annotations:UsageOrBuilder.java.pb.meta")
public interface UsageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:io.harness.event.payloads.Usage)
    com.google.protobuf.MessageOrBuilder {
  /**
   * <code>string cpu = 1;</code>
   */
  java.lang.String getCpu();
  /**
   * <code>string cpu = 1;</code>
   */
  com.google.protobuf.ByteString getCpuBytes();

  /**
   * <code>string memory = 2;</code>
   */
  java.lang.String getMemory();
  /**
   * <code>string memory = 2;</code>
   */
  com.google.protobuf.ByteString getMemoryBytes();
}
