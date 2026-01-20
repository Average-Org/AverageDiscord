package github.renderbr.hytale.config.obj;

import com.google.gson.annotations.SerializedName;

/**
 * Represents what kind of outputs a channel should get
 */
public enum ChannelOutputTypes {
    /**
     * Despite being named ALL, this type does not include INTERNAL_LOG
     */
    @SerializedName("all") ALL,
    @SerializedName("join_leave") JOIN_LEAVE,
    @SerializedName("chat") CHAT,
    @SerializedName("server_state") SERVER_STATE,
    @SerializedName("internal_log") INTERNAL_LOG,
    @SerializedName("desc_status") DESC_STATUS
}
