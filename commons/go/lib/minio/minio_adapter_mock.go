// Copyright 2021 Harness Inc.
// 
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

// Code generated by MockGen. DO NOT EDIT.
// Source: minio_adapter.go

// Package minio is a generated GoMock package.
package minio

import (
	context "context"
	gomock "github.com/golang/mock/gomock"
	minio "github.com/minio/minio-go/v6"
	reflect "reflect"
)

// MockStorageClient is a mock of StorageClient interface.
type MockStorageClient struct {
	ctrl     *gomock.Controller
	recorder *MockStorageClientMockRecorder
}

// MockStorageClientMockRecorder is the mock recorder for MockStorageClient.
type MockStorageClientMockRecorder struct {
	mock *MockStorageClient
}

// NewMockStorageClient creates a new mock instance.
func NewMockStorageClient(ctrl *gomock.Controller) *MockStorageClient {
	mock := &MockStorageClient{ctrl: ctrl}
	mock.recorder = &MockStorageClientMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockStorageClient) EXPECT() *MockStorageClientMockRecorder {
	return m.recorder
}

// FPutObjectWithContext mocks base method.
func (m *MockStorageClient) FPutObjectWithContext(ctx context.Context, bucketName, objectName, filePath string, opts minio.PutObjectOptions) (int64, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "FPutObjectWithContext", ctx, bucketName, objectName, filePath, opts)
	ret0, _ := ret[0].(int64)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// FPutObjectWithContext indicates an expected call of FPutObjectWithContext.
func (mr *MockStorageClientMockRecorder) FPutObjectWithContext(ctx, bucketName, objectName, filePath, opts interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "FPutObjectWithContext", reflect.TypeOf((*MockStorageClient)(nil).FPutObjectWithContext), ctx, bucketName, objectName, filePath, opts)
}

// FGetObjectWithContext mocks base method.
func (m *MockStorageClient) FGetObjectWithContext(ctx context.Context, bucketName, objectName, filePath string, opts minio.GetObjectOptions) error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "FGetObjectWithContext", ctx, bucketName, objectName, filePath, opts)
	ret0, _ := ret[0].(error)
	return ret0
}

// FGetObjectWithContext indicates an expected call of FGetObjectWithContext.
func (mr *MockStorageClientMockRecorder) FGetObjectWithContext(ctx, bucketName, objectName, filePath, opts interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "FGetObjectWithContext", reflect.TypeOf((*MockStorageClient)(nil).FGetObjectWithContext), ctx, bucketName, objectName, filePath, opts)
}

// StatObject mocks base method.
func (m *MockStorageClient) StatObject(bucketName, objectName string, opts minio.StatObjectOptions) (minio.ObjectInfo, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "StatObject", bucketName, objectName, opts)
	ret0, _ := ret[0].(minio.ObjectInfo)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// StatObject indicates an expected call of StatObject.
func (mr *MockStorageClientMockRecorder) StatObject(bucketName, objectName, opts interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "StatObject", reflect.TypeOf((*MockStorageClient)(nil).StatObject), bucketName, objectName, opts)
}
