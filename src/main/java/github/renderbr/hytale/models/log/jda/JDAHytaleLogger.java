package github.renderbr.hytale.models.log.jda;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;
import com.hypixel.hytale.logger.HytaleLogger;
import org.slf4j.helpers.MessageFormatter;

public class JDAHytaleLogger extends AbstractLogger {
    private final HytaleLogger hytaleLogger;

    public JDAHytaleLogger(String name) {
        this.name = name;
        this.hytaleLogger = HytaleLogger.get(name);
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable) {
        String formattedMessage = MessageFormatter.arrayFormat(messagePattern, arguments).getMessage();

        if (throwable != null) {
            formattedMessage += "\n" + throwable;
        }

        var jLevel = switch (level) {
            case TRACE -> java.util.logging.Level.FINEST;
            case INFO -> java.util.logging.Level.INFO;
            case WARN -> java.util.logging.Level.WARNING;
            case ERROR -> java.util.logging.Level.SEVERE;
            case DEBUG -> java.util.logging.Level.FINE;
        };

        hytaleLogger.at(jLevel).log(formattedMessage);
    }

    @Override
    protected String getFullyQualifiedCallerName() {
        return null;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return false;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return false;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return false;
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return false;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;
    }
}