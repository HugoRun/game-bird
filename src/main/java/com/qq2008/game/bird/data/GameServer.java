package com.qq2008.game.bird.data;

import com.qq2008.game.bird.model.vo.GameStatusVO;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class GameServer {

    // 程序状态
    protected static GameStatusVO status = GameStatusVO.NULL;
    // 程序上下文
    protected static ApplicationContext applicationContext;

    /***
     * 获取GameServer单例对象
     * @return GameConfig
     */
    public static GameServer getInstance() {
        return GameServer.getApplicationContext().getBean(GameServer.class);
    }

    /***
     * 获取Springboot上下文
     * @return ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /***
     * 设置Springboot上下文
     * @param applicationContext Springboot上下文
     */
    public static void setApplicationContext(ApplicationContext applicationContext) {
        GameServer.applicationContext = applicationContext;
    }

    /***
     * 设置游戏状态
     * @param status 游戏状态
     */
    public static void setStatus(GameStatusVO status) {
        GameServer.status = status;
    }

    /***
     * 获取游戏状态
     * @return 游戏状态
     */
    public static GameStatusVO getStatus() {
        return status;
    }

    /***
     * 初始化游戏
     */
    public static void init(){
        // 初始化游戏活动
        getInstance().initActivity();
        // 初始化排行榜 todo
        getInstance().initRank();
    }

    /***
     * 初始化活动 todo
     */
    public void initActivity(){

    }

    /***
     * 初始化排行榜 todo
     */
    public void initRank(){
        // 加载等级排行榜
        // 加载财富榜
    }
}
