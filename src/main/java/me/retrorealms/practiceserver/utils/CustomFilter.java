package me.retrorealms.practiceserver.utils;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class CustomFilter implements Filter {

    @Override
    public boolean isLoggable(LogRecord logRecord){
        if(logRecord.getMessage().contains("PlayerTeleportEvent to AAC")) return false;
        if(logRecord.getMessage().contains("moved wrongly")) return false;
        if(logRecord.getMessage().contains("CommandBlock at")) return false;

        return true;
    }
}
