package com.qq2008.game.bird.data;

/***
 * 全局常量配置类
 *
 * <p><b>所有字段都是 final 且有私有构造器</b>：常量不应在运行期被改写。
 * 加 final 是纯编译期加固 —— 任何试图赋值的地方都会编译失败，
 * 而不是等到运行时静默生效。迁移前已确认全项目零处运行时赋值。
 *
 * <p><b>类型说明</b>：{@code Byte} / {@code Short} 类型刻意保持不变。
 * 改为 int 会让依赖 {@code Objects.equals} 的 7 处比较静默失效
 * （Integer.equals(Byte) 恒为 false），是极隐蔽的回归。
 */
public final class ConstData {

    private ConstData() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }

    ////////////////// 性别 //////////////////
    // 中性
    public static final Byte GENDER_UNISEX = 0;
    // 雄性
    public static final Byte GENDER_MALE = 1;
    // 雌性
    public static final Byte GENDER_FEMALE = 2;


    ////////////////// 游戏内部商店购买类型 //////////////////
    // 金钱
    public static final Byte FEE_TYPE_COIN = 1;
    // 钻石
    public static final Byte FEE_TYPE_DIAMOND = 2;
    // 爱心
    public static final Byte FEE_TYPE_LOVE_POINT = 3;


    ////////////////// 包裹类型 //////////////////
    // 诱饵
    public static final Byte PACK_TYPE_BAIT = 1;
    // 陷阱
    public static final Byte PACK_TYPE_TRAP = 2;
    // 鸟笼
    public static final Byte PACK_TYPE_CAGE = 3;
    // 巢穴
    public static final Byte PACK_TYPE_NEST = 4;
    // 训练场
    public static final Byte PACK_TYPE_TRAIN = 5;
    // 道具
    public static final Byte PACK_TYPE_PROP = 6;


    ////////////////// 婚姻类型 //////////////////
    // 喜鹊之恋
    public static final Byte MARRIAGE_TYPE1 = 1;
    // 鸳鸯之恋
    public static final Byte MARRIAGE_TYPE2 = 2;
    // 比翼鸟之恋
    public static final Byte MARRIAGE_TYPE3 = 3;
    // 天使之恋
    public static final Byte MARRIAGE_TYPE4 = 4;


    ////////////////// 路由路径常量 //////////////////
    // 维护提示页路径（GameStatusHandlerInterceptor 转发至此）
    public static final String MESSAGE_PATH = "/message";
    // 登录路由
    public static final String PATH_LOGIN = "/login.do";
    // 游戏主界面路由
    public static final String PATH_GAME = "/game.do";
    // 角色选择/登录路由
    public static final String PATH_ROLE_LOGIN = "/role/login.do";
    // 角色创建路由
    public static final String PATH_ROLE_CREATE = "/role/create.do";
    // 退出登录路由
    public static final String PATH_ROLE_QUIT = "/role/quit.do";


    ////////////////// 分页 //////////////////
    // 商店列表每页条数
    public static final int SHOP_PAGE_SIZE = 5;
    // 包裹列表每页条数
    public static final int PACK_PAGE_SIZE = 5;
    // 聊天列表每页条数
    public static final int CHAT_PAGE_SIZE = 20;


    ////////////////// 游戏参数 //////////////////
    // 默认场景Id
    public static final Short DEFAULT_FIELD_ID = 1;
    // 默认鸟笼Id
    public static final int DEFAULT_CAGE_ID = 1;
    // 默认巢穴Id
    public static final int DEFAULT_NEST_ID = 1;
    // 默认训练场Id
    public static final int DEFAULT_TRAIN_ID = 1;
    // 默认陷阱Id
    public static final int DEFAULT_TRAP_ID = 1;
    // 聊天信息有效天数
    public static final int CHAT_TIME = 3;
}