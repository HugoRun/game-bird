package com.qq2008.game.bird.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qq2008.common.util.CommonUtils;
import com.qq2008.game.bird.data.RandomTextData;
import com.qq2008.game.bird.model.dbo.Role;
import com.qq2008.game.bird.model.vo.ChatMessageVO;
import com.qq2008.game.bird.model.vo.ChatRoleVO;

import java.util.ArrayList;
import java.util.List;

/***
 * 模拟数据工具集
 * 用于获取一些模拟数据
 */
public class MockUtils {
    /***
     * 模拟聊天信息列表
     * @param size 消息数量
     * @param role 玩家信息
     * @return 聊天记录
     */
    public static Page<ChatMessageVO> mockChatMessageList(int size, Role role) {
        //
        int nowTime = CommonUtils.nowTime();
        String[] messageList = RandomTextData.getRandChatMessageArray();
        ChatRoleVO chatRoleVO = GameUtils.makeChatRole(role);
        // 随机聊天信息列表
        List<ChatMessageVO> messageVOList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ChatMessageVO messageVO = new ChatMessageVO();
            messageVO.setRole(chatRoleVO);
            messageVO.setMessage(ArrayUtils.randElement(messageList));
            messageVO.setTime((int) (nowTime - Math.random() * 1000));
            messageVOList.add(messageVO);
        }
        messageVOList.sort((o1, o2) -> {
            if (o1.getTime() == o2.getTime()) {
                return 0;
            }
            return o1.getTime() > o2.getTime() ? -1 : 1;
        });
        // 构造成page
        Page<ChatMessageVO> messageVOPage = new Page<>();
        messageVOPage.setRecords(messageVOList);
        messageVOPage.setTotal(messageVOList.size());
        messageVOPage.setSize(10);
        return messageVOPage;
    }

    public static void main(String[] args) {

    }

    /***
     * 模拟树林记录列表
     * @param size 消息数量
     * @param role 玩家信息
     * @return 树林记录
     */
    public static Page<ChatMessageVO> mockTreeMessageList(int size, Role role) {
        //
        int nowTime = CommonUtils.nowTime();
        String[] messageList = RandomTextData.getRandTreeMessageArray();
        ChatRoleVO chatRoleVO = GameUtils.makeChatRole(role);
        // 随机聊天信息列表
        List<ChatMessageVO> messageVOList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ChatMessageVO messageVO = new ChatMessageVO();
            messageVO.setRole(chatRoleVO);
            messageVO.setMessage(ArrayUtils.randElement(messageList));
            messageVO.setTime((int) (nowTime - Math.random() * 1000));
            messageVOList.add(messageVO);
        }
        messageVOList.sort((o1, o2) -> {
            if (o1.getTime() == o2.getTime()) {
                return 0;
            }
            return o1.getTime() > o2.getTime() ? -1 : 1;
        });
        // 构造成page
        Page<ChatMessageVO> messageVOPage = new Page<>();
        messageVOPage.setRecords(messageVOList);
        messageVOPage.setTotal(messageVOList.size());
        messageVOPage.setSize(10);
        return messageVOPage;
    }
}
