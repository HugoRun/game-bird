package com.qq2008.common.entiy.vo;

import com.qq2008.common.entiy.ResultCode;

import java.util.ArrayList;
import java.util.List;

/***
 * 消息
 */
public class MessageVO {
    // 消息类型
    MessageType messageType;
    // 消息文本
    String message;
    // 是否需要返回
    boolean isGoBack = false;
    // 跳转链接
    List<MessageLink> linkList;

    public MessageVO() {
        this.messageType = MessageType.INFO;
        this.linkList = new ArrayList<>();
    }

    public MessageVO(MessageType messageType, String message, boolean isGoBack, List<MessageLink> linkList) {
        this.messageType = messageType;
        this.message = message;
        this.isGoBack = isGoBack;
        this.linkList = linkList == null ? new ArrayList<>() : linkList;
    }

    public static MessageVO info(String message) {
        return new MessageVO(MessageType.INFO, message, true, null);
    }
    public static MessageVO info(ResultCode resultCode) {
        return new MessageVO(MessageType.INFO, resultCode.getMessage(), true, null);
    }

    public static MessageVO success(String message) {
        return new MessageVO(MessageType.SUCCESS, message, true, null);
    }

    public static MessageVO warn(String message) {
        return new MessageVO(MessageType.WARN, message, true, null);
    }

    public static MessageVO error(String message) {
        return new MessageVO(MessageType.ERROR, message, true, null);
    }
    public static MessageVO error(ResultCode resultCode) {
        return new MessageVO(MessageType.ERROR, resultCode.getMessage(), true, null);
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public MessageVO setMessageType(MessageType messageType) {
        this.messageType = messageType;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public MessageVO setMessage(String message) {
        this.message = message;
        return this;
    }

    public boolean isGoBack() {
        return isGoBack;
    }

    public void setGoBack(boolean goBack) {
        this.isGoBack = goBack;
    }

    public List<MessageLink> getLinkList() {
        return linkList;
    }

    public MessageVO setLinkList(List<MessageLink> linkList) {
        this.linkList = linkList;
        return this;
    }

    public void addLink(MessageLink link) {
        this.linkList.add(link);
    }
}
