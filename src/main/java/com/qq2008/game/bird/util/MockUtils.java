package com.qq2008.game.bird.util;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.qq2008.common.util.CommonUtils;

import com.qq2008.game.bird.data.RandomTextData;
import com.qq2008.game.bird.model.dbo.Role;
import com.qq2008.game.bird.model.vo.ChatMessageVO;
import com.qq2008.game.bird.model.vo.ChatRoleVO;

/**
 * 模拟数据工具集
 * 用于获取一些模拟数据
 */
public class MockUtils {
    /**
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

    /**
     * 模拟世界动态列表。
     *
     * <p><b>为什么要有这一个</b>：改造前 {@code GameController} 的世界动态与树林记录
     * 调的是同一个 {@link #mockTreeMessageList}，两个栏目的数据完全相同。
     * 但实测发现 {@code game.html} 的「世界动态」区块是 {@code <div hidden="hidden">}
     * 且内容是<b>写死的静态 HTML</b>（「暂未开放」+ 两条硬编码示例），
     * <b>根本没有读取 {@code globalMessagePage}</b>。
     *
     * <p>也就是说：改造前那份 mock 数据算了但永远不会被展示，属于无效计算。
     * 本方法给出与树林记录<b>可区分</b>的数据源（时间分布跨度更大、文本取自
     * 另一组语料），使两个栏目各有所本；但请注意
     * <b>模板侧仍未消费它</b>——要真正显示世界动态，需先改模板去掉 hidden
     * 并把硬编码示例换成 {@code globalMessagePage.getRecords()} 遍历。
     *
     * <p>★ 世界动态的真实实现（查询全服聊天/事件流）待做，当前为 mock。
     *
     * @param size 动态数量
     * @param role 当前玩家信息（用于构造展示用的角色摘要）
     * @return 世界动态分页数据
     */
    public static Page<ChatMessageVO> mockGlobalMessageList(int size, Role role) {
        //
        int nowTime = CommonUtils.nowTime();
        String[] messageList = RandomTextData.getRandChatMessageArray();
        ChatRoleVO chatRoleVO = GameUtils.makeChatRole(role);
        // 随机世界动态列表
        List<ChatMessageVO> messageVOList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ChatMessageVO messageVO = new ChatMessageVO();
            messageVO.setRole(chatRoleVO);
            messageVO.setMessage(ArrayUtils.randElement(messageList));
            // 与树林记录区分：世界动态的时间跨度按「天」计而非「秒」，
            // 使两个栏目即便同屏展示也不会呈现同样的时间聚集形态。
            messageVO.setTime((int) (nowTime - Math.random() * 86400));
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

    /**
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
