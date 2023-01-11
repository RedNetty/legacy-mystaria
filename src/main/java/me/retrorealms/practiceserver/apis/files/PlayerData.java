package me.retrorealms.practiceserver.apis.files;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.utils.AbstractFile;

import java.io.File;

/**
 * Created by Khalid on 8/3/2017.
 */
public class PlayerData extends AbstractFile {

    public PlayerData(PracticeServer main) {
        super(main, "playerdata.json", false);
    }

    public File getFile() {
        return f;
    }
}
