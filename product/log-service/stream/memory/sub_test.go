// Copyright 2021 Harness Inc.
// 
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package memory

import (
	"testing"

	"github.com/wings-software/portal/product/log-service/stream"
)

func TestSubscription_publish(t *testing.T) {
	s := &subscriber{
		handler: make(chan *stream.Line, 5),
		closec:  make(chan struct{}),
	}

	e := new(stream.Line)
	s.publish(e)

	if got, want := len(s.handler), 1; got != want {
		t.Errorf("Want buffered channel size %d, got %d", want, got)
	}
	if got, want := <-s.handler, e; got != want {
		t.Errorf("Want log entry received from channel")
	}
	if got, want := len(s.handler), 0; got != want {
		t.Errorf("Want buffered channel size %d, got %d", want, got)
	}
}

func TestSubscription_buffer(t *testing.T) {
	s := &subscriber{
		handler: make(chan *stream.Line, 1),
		closec:  make(chan struct{}),
	}

	// the buffer size is 1 to simulate what happens
	// if the subscriber cannot keep up with processing
	// and the buffer fills up. In this case, lines
	// should be ignored until pending lines are
	// processed.

	e := new(stream.Line)
	s.publish(e)
	s.publish(e)
	s.publish(e)
	s.publish(e)
	s.publish(e)

	if got, want := len(s.handler), 1; got != want {
		t.Errorf("Want buffered channel size %d, got %d", want, got)
	}
}

func TestSubscription_stop(t *testing.T) {
	s := &subscriber{
		handler: make(chan *stream.Line, 1),
		closec:  make(chan struct{}),
	}

	if got, want := s.closed, false; got != want {
		t.Errorf("Want subscription open")
	}

	s.close()
	if got, want := s.closed, true; got != want {
		t.Errorf("Want subscription closed")
	}

	// if the subscription is closed we should
	// ignore any new events being published.

	e := new(stream.Line)
	s.publish(e)
	s.publish(e)
	s.publish(e)
	s.publish(e)
	s.publish(e)
}
