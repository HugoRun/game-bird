package com.qq2008.common.entiy.vo;

/***
 * 消息链接
 */
public class MessageLink {
    // 链接文本
    String text;
    // 链接地址
    String href;

    public MessageLink(String text, String href) {
        this.text = text;
        this.href = href;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
