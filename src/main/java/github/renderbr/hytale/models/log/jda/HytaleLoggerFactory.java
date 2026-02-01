package github.renderbr.hytale.models.log.jda;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class HytaleLoggerFactory implements ILoggerFactory {
    @Override
    public Logger getLogger(String name) {
        return new JDAHytaleLogger(name);
    }
}
