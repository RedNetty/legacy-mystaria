package me.retrorealms.practiceserver.apis.files;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.utils.AbstractFile;

import java.io.File;

/**
 * Created by Khalid on 8/3/2017.
 */
public class MarketData extends AbstractFile {
    public MarketData(PracticeServer main) {
        super(main, "marketdata.json", false);
    }

    public File getFile() {
        return f;
    }
}
