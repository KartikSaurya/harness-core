package logs

import (
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
)

// defaultLimit is the default maximum log size in bytes.
const (
	defaultLimit = 5242880 // 5MB
	messageKey   = "msg"
	levelKey     = "level"
	nameKey      = "logger"
	defaultLevel = "info"
)

type RemoteLogger struct {
	BaseLogger *zap.SugaredLogger
	// This Writer is used inside the logger implementation and can be used
	// directly as well for streaming in subprocesses
	Writer StreamWriter
}

// NewRemoteLogger returns an instance of RemoteLogger
func NewRemoteLogger(writer StreamWriter) (*RemoteLogger, error) {
	ws := zapcore.AddSync(writer)
	encoderCfg := zapcore.EncoderConfig{
		MessageKey:     messageKey,
		LevelKey:       levelKey,
		NameKey:        nameKey,
		EncodeLevel:    zapcore.LowercaseLevelEncoder,
		EncodeTime:     zapcore.ISO8601TimeEncoder,
		EncodeDuration: zapcore.StringDurationEncoder,
	}
	core := zapcore.NewCore(zapcore.NewJSONEncoder(encoderCfg), ws, zap.DebugLevel)
	logger := zap.New(core)
	sugar := logger.Sugar()
	rl := &RemoteLogger{sugar, writer}
	// Try to open the stream
	err := rl.Writer.Open()
	if err != nil {
		return nil, err
	}
	return rl, nil
}
