package github.renderbr.hytale.models.log;

import java.util.logging.LogRecord;

public interface LogEventListener {
    void onLogReceived(LogRecord record);
}
