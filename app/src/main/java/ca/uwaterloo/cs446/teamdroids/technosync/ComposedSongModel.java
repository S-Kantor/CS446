package ca.uwaterloo.cs446.teamdroids.technosync;

public class ComposedSongModel {
    private String groupId;
    private String filePath;
    private boolean isDownloaded;
    private boolean isPlaying;

    public ComposedSongModel(String groupId, String filePath) {
        this.groupId = groupId;
        this.isPlaying = false;
        this.filePath = filePath;

        if (filePath != null) {
            this.isDownloaded = true;
        } else {
            this.isDownloaded = false;
        }
    }

    public String getGroupId() {
        return this.groupId;
    }

    public boolean isDownloaded() {
        return this.isDownloaded;
    }

    public String getFilePath() { return this.filePath; }

    public void setIsDownloaded(boolean isDownloaded) {
        this.isDownloaded = isDownloaded;
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    public void  setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }
}
