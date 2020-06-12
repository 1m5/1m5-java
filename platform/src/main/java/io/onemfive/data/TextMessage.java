package io.onemfive.data;

import java.util.Map;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class TextMessage extends BaseMessage {

    protected DID to;
    protected DID from;
    protected String text;

    public TextMessage() {
    }

    public TextMessage(DID to, DID from, String text) {
        this.to = to;
        this.from = from;
        this.text = text;
    }

    public DID getTo() {
        return to;
    }

    public void setTo(DID to) {
        this.to = to;
    }

    public DID getFrom() {
        return from;
    }

    public void setFrom(DID from) {
        this.from = from;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public Map<String, Object> toMap() {
        return super.toMap();
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        super.fromMap(m);
    }
}
