package be.valuya.cestzam.myminfin.sync.config;

import lombok.Getter;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogHandler extends Handler {

    @Getter
    private Queue<LogRecord> lastRecords = new ArrayDeque<>(16000);

    @Override
    public void publish(LogRecord record) {
        boolean accepted = lastRecords.offer(record);
        if (!accepted) {
            lastRecords.poll();
            lastRecords.offer(record);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {

    }

}
