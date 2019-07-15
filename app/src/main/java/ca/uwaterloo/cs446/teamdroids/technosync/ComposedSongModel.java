package ca.uwaterloo.cs446.teamdroids.technosync;

public class ComposedSongModel {
    private String groupId;
    private boolean isDownloaded;
    private boolean isPlaying;

    public ComposedSongModel(String groupId) {
        this.isDownloaded = false;
        this.groupId = groupId;
        this.isPlaying = false;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public boolean isDownloaded() {
        return this.isDownloaded;
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }
}
