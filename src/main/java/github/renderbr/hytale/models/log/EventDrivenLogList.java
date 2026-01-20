package github.renderbr.hytale.models.log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.LogRecord;

public class EventDrivenLogList extends CopyOnWriteArrayList<LogRecord> {
    private final List<LogEventListener> listeners = new ArrayList<>();

    public void addListener(LogEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(LogEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean add(LogRecord record) {
        boolean added = super.add(record);
        if(added){
            for(LogEventListener listener: listeners){
                listener.onLogReceived(record);
            }
        }
        return added;
    }
}

