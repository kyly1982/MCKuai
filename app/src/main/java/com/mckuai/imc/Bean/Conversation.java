package com.mckuai.imc.Bean;


/**
 * Created by kyly on 2016/3/2.
 */
public class Conversation {

    io.rong.imlib.model.Conversation conversation;
    User target;

    public io.rong.imlib.model.Conversation getConversation() {
        return conversation;
    }

    public void setConversation(io.rong.imlib.model.Conversation conversation) {
        this.conversation = conversation;
    }

    public User getTarget() {
        return target;
    }

    public void setTarget(User target) {
        this.target = target;
    }
}
