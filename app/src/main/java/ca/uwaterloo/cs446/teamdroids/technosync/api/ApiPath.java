package ca.uwaterloo.cs446.teamdroids.technosync.api;

public enum ApiPath {

    PUBLISH_RECORDING("http://does_not_exist"),
    NEW_SESSION("http://does_not_exist"),
    JOIN_SESSION("http://does_not_exist"),
    GET_RECORDING_LIST("http://does_not_exist");

    private String url;

    public String getUrl()
    {
        return this.url;
    }

    private ApiPath(String url)
    {
        this.url = url;
    }
}
