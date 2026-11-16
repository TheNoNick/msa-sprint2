package main

import (
	"context"
	"errors"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"sync/atomic"
	"syscall"
	"time"
)

// newMux собирает маршруты. Вынесено отдельно, чтобы покрыть тестами без запуска сервера.
//
//	/ping    — liveness: процесс жив; в ответе версия, чтобы было видно, куда Istio отправил запрос
//	/ready   — readiness: сервис готов принимать трафик (503 во время старта и остановки)
//	/feature — есть только при ENABLE_FEATURE_X=true
func newMux(version string, enableFeatureX bool, ready *atomic.Bool) *http.ServeMux {
	mux := http.NewServeMux()

	mux.HandleFunc("/ping", func(w http.ResponseWriter, r *http.Request) {
		fmt.Fprintf(w, "pong %s\n", version)
	})

	mux.HandleFunc("/ready", func(w http.ResponseWriter, r *http.Request) {
		if !ready.Load() {
			http.Error(w, "not ready", http.StatusServiceUnavailable)
			return
		}
		fmt.Fprint(w, "ready")
	})

	if enableFeatureX {
		mux.HandleFunc("/feature", func(w http.ResponseWriter, r *http.Request) {
			fmt.Fprint(w, "Feature X is enabled!")
		})
	}

	return mux
}

func main() {
	enableFeatureX := os.Getenv("ENABLE_FEATURE_X") == "true"
	version := os.Getenv("APP_VERSION")
	if version == "" {
		version = "v1"
	}
	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}

	var ready atomic.Bool
	server := &http.Server{
		Addr:              ":" + port,
		Handler:           newMux(version, enableFeatureX, &ready),
		ReadHeaderTimeout: 5 * time.Second,
	}

	go func() {
		log.Printf("Server %s running on :%s (ENABLE_FEATURE_X=%t)", version, port, enableFeatureX)
		if err := server.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			log.Fatal(err)
		}
	}()
	ready.Store(true)

	// Kubernetes присылает SIGTERM: сначала перестаём быть ready, потом дорабатываем запросы
	stop := make(chan os.Signal, 1)
	signal.Notify(stop, syscall.SIGTERM, syscall.SIGINT)
	<-stop
	ready.Store(false)
	log.Println("Shutting down...")

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	if err := server.Shutdown(ctx); err != nil {
		log.Printf("Shutdown error: %v", err)
	}
}
