package by.afinny.userservice.util.constant;

public enum JWT {

    KEY("jxgEQeXHuPq8VdbyYFNkANdudQ53YUn4"),
    HEADER("Authorization"),
    ACCESS_TOKEN_EXPIRATION("600000"),
    REFRESH_TOKEN_EXPIRATION("1800000");

    private final String value;

    JWT(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}