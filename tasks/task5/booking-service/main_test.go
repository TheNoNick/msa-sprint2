package main

import (
	"net/http"
	"net/http/httptest"
	"sync/atomic"
	"testing"
)

func call(t *testing.T, mux *http.ServeMux, path string) (int, string) {
	t.Helper()
	rec := httptest.NewRecorder()
	mux.ServeHTTP(rec, httptest.NewRequest(http.MethodGet, path, nil))
	return rec.Code, rec.Body.String()
}

func TestPing(t *testing.T) {
	var ready atomic.Bool
	code, body := call(t, newMux("v2", false, &ready), "/ping")
	if code != http.StatusOK || body != "pong v2\n" {
		t.Fatalf("/ping = %d %q, want 200 \"pong v2\"", code, body)
	}
}

func TestReady(t *testing.T) {
	var ready atomic.Bool
	mux := newMux("v1", false, &ready)

	if code, _ := call(t, mux, "/ready"); code != http.StatusServiceUnavailable {
		t.Fatalf("/ready before start = %d, want 503", code)
	}

	ready.Store(true)
	if code, body := call(t, mux, "/ready"); code != http.StatusOK || body != "ready" {
		t.Fatalf("/ready after start = %d %q, want 200 ready", code, body)
	}
}

func TestFeatureFlag(t *testing.T) {
	var ready atomic.Bool

	if code, _ := call(t, newMux("v1", false, &ready), "/feature"); code != http.StatusNotFound {
		t.Fatalf("/feature with flag off = %d, want 404", code)
	}

	code, body := call(t, newMux("v1", true, &ready), "/feature")
	if code != http.StatusOK || body != "Feature X is enabled!" {
		t.Fatalf("/feature with flag on = %d %q, want 200", code, body)
	}
}
