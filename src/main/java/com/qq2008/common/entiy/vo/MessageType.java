package com.qq2008.common.entiy.vo;

public enum MessageType {

    INFO(1, "普通输出"),
    SUCCESS(2, "成功消息"),
    WARN(3, "警告内容"),
    ERROR(4, "错误消息");

    // 类型Id
    int typeId;
    // 类型描述
    String typeDesc;

    MessageType() {
    }

    MessageType(int typeId, String typeDesc) {
        this.typeId = typeId;
        this.typeDesc = typeDesc;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getTypeDesc() {
        return typeDesc;
    }

    public void setTypeDesc(String typeDesc) {
        this.typeDesc = typeDesc;
    }
}
