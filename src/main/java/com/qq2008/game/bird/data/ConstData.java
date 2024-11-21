package com.qq2008.game.bird.data;

/***
 * 全局常量配置类
 * 放置一些全局的常量
 */
public class ConstData {

    ////////////////// 开关 //////////////////
    // 是否需要实名认证
    public static boolean IS_CHECK_IDENTITY_CARD = true;
    // 是否允许注册 todo 回写数据库
    public static boolean IS_ALLOW_REGISTER = true;


    ////////////////// 性别 //////////////////
    // 中性
    public static Byte GENDER_UNISEX = 0;
    // 雄性
    public static Byte GENDER_MALE = 1;
    // 雌性
    public static Byte GENDER_FEMALE = 2;


    ////////////////// 游戏内部商店购买类型 //////////////////
    // 金钱
    public static Byte FEE_TYPE_COIN = 1;
    // 钻石
    public static Byte FEE_TYPE_DIAMOND = 2;
    // 爱心
    public static Byte FEE_TYPE_LOVE_POINT = 3;


    ////////////////// 包裹类型 //////////////////
    // 诱饵
    public static Byte PACK_TYPE_BAIT = 1;
    // 陷阱
    public static Byte PACK_TYPE_TRAP = 2;
    // 鸟笼
    public static Byte PACK_TYPE_CAGE = 3;
    // 巢穴
    public static Byte PACK_TYPE_NEST = 4;
    // 训练场
    public static Byte PACK_TYPE_TRAIN = 5;
    // 道具
    public static Byte PACK_TYPE_PROP = 6;


    ////////////////// 婚姻类型 //////////////////
    // 喜鹊之恋
    public static Byte MARRIAGE_TYPE1 = 1;
    // 鸳鸯之恋
    public static Byte MARRIAGE_TYPE2 = 2;
    // 比翼鸟之恋
    public static Byte MARRIAGE_TYPE3 = 3;
    // 天使之恋
    public static Byte MARRIAGE_TYPE4 = 4;

    ////////////////// 游戏参数 //////////////////
    // 默认场景Id
    public static Short DEFAULT_FIELD_ID = 1;
    // 默认鸟笼Id
    public static int DEFAULT_CAGE_ID = 1;
    // 默认巢穴Id
    public static int DEFAULT_NEST_ID = 1;
    // 默认训练场Id
    public static int DEFAULT_TRAIN_ID = 1;
    // 默认陷阱Id
    public static int DEFAULT_TRAP_ID = 1;
    // 聊天信息有效天数
    public static int CHAT_TIME = 3;
}
