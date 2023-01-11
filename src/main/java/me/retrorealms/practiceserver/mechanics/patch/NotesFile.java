package me.retrorealms.practiceserver.mechanics.patch;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by Giovanni on 7-7-2017.
 */
class NotesFile {

    private String versionId;
    private List<String> notes = Lists.newArrayList();

    String getVersionId() {
        return versionId;
    }

    List<String> getNotes() {
        return notes;
    }

    NotesFile setNotes(List<String> notes) {
        this.notes = notes;

        return this;
    }

    NotesFile setVersionId(String versionId) {
        this.versionId = versionId;

        return this;
    }
}
