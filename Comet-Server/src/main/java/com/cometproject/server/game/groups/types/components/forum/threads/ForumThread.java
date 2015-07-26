package com.cometproject.server.game.groups.types.components.forum.threads;

import com.cometproject.api.networking.messages.IComposer;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.players.PlayerManager;
import com.cometproject.server.game.players.data.PlayerAvatar;

import java.util.ArrayList;
import java.util.List;

public class ForumThread {
    private int id;
    private String title;
    private int authorId;
    private int authorTimestamp;
    private int state;
    private boolean isLocked;
    private boolean isHidden;

    private List<ForumThreadReply> replies;

    public ForumThread(int id, String title, String message, int authorId, int authorTimestamp, int state, boolean isLocked, boolean isHidden) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        this.authorTimestamp = authorTimestamp;
        this.state = state;
        this.isLocked = isLocked;
        this.isHidden = isHidden;
        this.replies = new ArrayList<>();

        // Add the OP.
        this.replies.add(new ForumThreadReply(id, 0, message, this.id, authorId, authorTimestamp, isHidden));
    }
    
    public void compose(IComposer msg) {
        msg.writeInt(this.getId());

        final PlayerAvatar authorAvatar = PlayerManager.getInstance().getAvatarByPlayerId(this.getAuthorId(), PlayerAvatar.USERNAME_FIGURE);

        msg.writeInt(authorAvatar == null ? 0 : authorAvatar.getId());
        msg.writeString(authorAvatar == null ? "Unknown Player" : authorAvatar.getUsername());

        msg.writeString(this.getTitle());
        msg.writeBoolean(false); // stickied.
        msg.writeBoolean(this.isLocked());
        msg.writeInt((int) Comet.getTime() - this.getAuthorTimestamp()); // create time
        msg.writeInt(this.getReplies().size()); // messages
        msg.writeInt(0); // unread messages
        msg.writeInt(this.getMostRecentPost().getId()); // last message id

        final PlayerAvatar replyAuthor = PlayerManager.getInstance().getAvatarByPlayerId(this.getMostRecentPost().getAuthorId(), PlayerAvatar.USERNAME_FIGURE);

        msg.writeInt(replyAuthor == null ? 0 : replyAuthor.getId()); // last message userid
        msg.writeString(replyAuthor == null ? "Unknown Player" : replyAuthor.getUsername()); // last message username
        msg.writeInt((int) Comet.getTime() - this.getMostRecentPost().getAuthorTimestamp()); // last message time ago
        msg.writeByte(this.getState()); // state
        msg.writeInt(0); //admin id
        msg.writeString(""); // admin username
        msg.writeInt(0); // admin action time ago.
    }

    public ForumThreadReply getMostRecentPost() {
        return this.replies.get(this.replies.size() - 1);
    }

    public void addReply(ForumThreadReply reply) {
        this.replies.add(reply);
    }

    public void dispose() {
        this.replies.clear();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ForumThreadReply> getReplies() {
        return replies;
    }

    public void setReplies(List<ForumThreadReply> replies) {
        this.replies = replies;
    }

    public int getAuthorId() {
        return authorId;
    }

    public int getAuthorTimestamp() {
        return authorTimestamp;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setIsLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setIsHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
