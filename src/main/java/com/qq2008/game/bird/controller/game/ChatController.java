package com.qq2008.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qq2008.common.entiy.ResultCode;
import com.qq2008.common.entiy.vo.MessageLink;
import com.qq2008.common.entiy.vo.MessageVO;
import com.qq2008.common.util.CommonUtils;
import com.qq2008.game.bird.controller.common.BaseController;
import com.qq2008.game.bird.data.ConstData;
import com.qq2008.game.bird.data.RandomTextData;
import com.qq2008.game.bird.model.dbo.LogChat;
import com.qq2008.game.bird.model.dbo.Role;
import com.qq2008.game.bird.util.ArrayUtils;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.logging.Logger;

/***
 * 聊天相关路由
 */
@Controller
@RequestMapping("/chat")
public class ChatController extends BaseController {
    // 日志句柄
    Logger logger = Logger.getLogger(ChatController.class.getName());

    /***
     * 聊天主界面
     * @param session session
     * @param model model
     * @return view
     */
    @GetMapping("/list.do")
    public String pageChatList(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "1") int chatType) {
        // 参数兼容
        if (pageNo < 1) {
            pageNo = 1;
        }
        // 参数回写
        model.addAttribute("chatType", chatType);
        // 查询数据
        LambdaQueryWrapper<LogChat> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.gt(LogChat::getTime, CommonUtils.nowDate() - ConstData.CHAT_TIME * 86400);
        queryWrapper.orderByDesc(LogChat::getTime);
        Page<LogChat> pageInfo = new Page<>();
        pageInfo.setCurrent(pageNo);
        pageInfo.setSize(10);
        chatService.page(pageInfo, queryWrapper);
        model.addAttribute("page", pageInfo);
        return "chat/list";
    }

    /***
     * 发送世界聊天消息
     * @param session session
     * @param model model
     * @return view
     */
    @PostMapping("/chatWorld.do")
    public String doChatWorld(HttpSession session, RedirectAttributes model, String message) {
        //
        Role role = getSessionRole(session);
        // 长度检测 todo
        // 铭感词检测 todo
        // 聊天CD todo
        // 检测和扣除聊天道具 todo
//        if(){
//            model.addFlashAttribute("message", MessageVO.error("发言过频，请稍后再进行发言。"));
//            return "common/message";
//        }
        // 玩家等级不足
        if (role.getLevel() < 10) {
            model.addFlashAttribute("message", MessageVO.error("您的等级不足10级。"));
            return redirectTo("/chat/list.do");
        }
        // 聊天消息为空
        if (message == null || message.trim().isEmpty()){
            model.addFlashAttribute("message", MessageVO.error("聊天消息不能为空哦。"));
            return redirectTo("/chat/list.do");
        }
        // 插入聊天数据
        LogChat chat = new LogChat();
        chat.setId(0L);
        chat.setRoleId(role.getRoleId());
        chat.setRoleName(role.getRoleName());
        chat.setTitleName("小菜鸟");
        chat.setMessage(message);
        chat.setTime(CommonUtils.nowTime());
        chatService.save(chat);
        // 构造
        model.addFlashAttribute("message", MessageVO.error("发送聊天消息成功！"));
        return redirectTo("/chat/list.do");
    }

    /***
     * 随机聊天玩家名称
     * @param session session
     * @param model model
     * @return view
     */
    @GetMapping("/r.do")
    public String pageTest(HttpSession session, Model model) {
        String[] aList = RandomTextData.getRandRoleNameArray();
        // 查询数据
        List<LogChat> chatList = chatService.list();
        for (LogChat logChat : chatList) {
            String roleName = ArrayUtils.randElement(aList);
            logChat.setRoleName(roleName);
            chatService.saveOrUpdate(logChat);
        }
        //
        model.addAttribute("message", MessageVO.success("操作成功。"));
        return "common/message";
    }


    // 孵化场 birthBirth
    // 孵化场 birthWait
    // 孵化结束 birthFinish
    // 孵化场 birthFriend
    // 孵化场 birthPublic
    // 孵化场 birthUser
    // 孵化场 birthMake
    // 孵化场 birthPending
}
