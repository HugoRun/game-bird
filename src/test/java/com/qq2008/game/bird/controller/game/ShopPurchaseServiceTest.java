package com.qq2008.game.bird.controller.game;

import com.qq2008.common.entity.vo.MessageVO;
import com.qq2008.game.bird.data.ConstData;
import com.qq2008.game.bird.data.GameConfigManager;
import com.qq2008.game.bird.model.dbo.BaseBait;
import com.qq2008.game.bird.model.dbo.BaseCage;
import com.qq2008.game.bird.model.dbo.BaseNest;
import com.qq2008.game.bird.model.dbo.BaseProp;
import com.qq2008.game.bird.model.dbo.BaseTrain;
import com.qq2008.game.bird.model.dbo.BaseTrap;
import com.qq2008.game.bird.model.dbo.Role;
import com.qq2008.game.bird.model.dbo.RolePack;
import com.qq2008.game.bird.service.IBaseBaitService;
import com.qq2008.game.bird.service.IBaseCageService;
import com.qq2008.game.bird.service.IBaseNestService;
import com.qq2008.game.bird.service.IBasePropService;
import com.qq2008.game.bird.service.IBaseTrainService;
import com.qq2008.game.bird.service.IBaseTrapService;
import com.qq2008.game.bird.service.IRolePackService;
import com.qq2008.game.bird.service.IRoleService;
import com.qq2008.game.bird.service.RoleValueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 商店购买流程的纯单元测试（T-PURCH-TEST）。
 *
 * <p><b>为什么测Controller 而不是 Service</b>：购买流程的真正风险在
 * {@code ShopController#buyShopXxx} 这一层—— 6 个方法各自复制了一整套
 * 「商品存在 → feeType 合法 → 等级足够 → 扣对应货币 → 加包裹数量」的模板，
 * 历史上出现过「钻石分支扣了金币」这类只在特定分支出现的错位。
 * 纯 Service 测试覆盖不到这种复制粘贴分歧，因此这里直接调用公开的 Controller 方法，
 * 用 {@link ReflectionTestUtils} 把 {@code @Resource} 字段替换成 Mockito mock。
 *
 * <p><b>这样做的收益</b>：等U10 把这 6 个方法抽成 {@code ShopPurchaseService} 后，
 * 本测试只需把 {@code controller.buyShopBait(...)} 换成 {@code service.purchase(...)}，
 * 断言部分可以原样保留 —— 它们描述的是业务契约，不是实现细节。
 *
 * <p><b>约束</b>（刻意遵守）：
 * <ul>
 *   <li>不加载 Spring 容器，全类无注解式容器引导，单测毫秒级完成；</li>
 *   <li>不连任何真实数据库，不声明任何数据源；</li>
 *   <li>不 mock 静态方法 —— 涉及的唯一静态方法
 *       {@code GameUtils.getFeeTypeName} 是纯函数，没必要 mock；</li>
 *   <li>断言同时检查<b>失败文案</b>，因为这些文案直接展示给玩家，属于业务契约。</li>
 * </ul>
 */
class ShopPurchaseServiceTest {

    /** 被测对象：真实 Controller 实例，但其20+ 个 {@code @Resource} 字段全部替换为 mock。 */
    private ShopController controller;

    /** 6 个商品类型对应的 mock Service。 */
    private IBaseBaitService baseBaitService;
    private IBaseCageService baseCageService;
    private IBaseNestService baseNestService;
    private IBaseTrainService baseTrainService;
    private IBaseTrapService baseTrapService;
    private IBasePropService basePropService;
    private IRolePackService packService;
    private IRoleService roleService;

    /** 真实实例：扣币 / 加包裹数量是本测试要验证的行为，不能 mock 掉。 */
    private RoleValueService roleValueService;

    /** 商品 id，全用例统一，避免每个测试重复赋值。 */
    private static final int ITEM_ID = 7;

    /** 包裹里该商品的初始数量，统一从 0 开始，便于断言增量。 */
    private static final int PACK_INIT_NUM = 0;

    /**
     * 商店在售的 6 种商品。
     *
     * <p>每个枚举自带「包裹类型」与「购买入口」，测试用例据此参数化，
     * 避免6 份逐字复制的用例代码。
     */
    private enum Item {

        /** 诱饵。 */
        BAIT(ConstData.PACK_TYPE_BAIT),
        /** 陷阱。 */
        TRAP(ConstData.PACK_TYPE_TRAP),
        /** 鸟笼。 */
        CAGE(ConstData.PACK_TYPE_CAGE),
        /** 巢。 */
        NEST(ConstData.PACK_TYPE_NEST),
        /** 训练场。 */
        TRAIN(ConstData.PACK_TYPE_TRAIN),
        /** 道具。 */
        PROP(ConstData.PACK_TYPE_PROP);

        /** 该商品在 role_pack 表中对应的 type_id。 */
        private final Byte packType;

        Item(Byte packType) {
            this.packType = packType;
        }

        Byte packType() {
            return packType;
        }
    }

    /**
     * 构造被测对象并注入 mock。
     *
     * <p>{@code RoleValueService} 用<b>真实实例</b>而非 mock：本测试的核心断言就是
     * 「扣多少、加多少」，把它 mock 掉等于把被测行为也一起 mock 掉了。
     */
    @BeforeEach
    void setUp() {
        controller = new ShopController();
        baseBaitService = mock(IBaseBaitService.class);
        baseCageService = mock(IBaseCageService.class);
        baseNestService = mock(IBaseNestService.class);
        baseTrainService = mock(IBaseTrainService.class);
        baseTrapService = mock(IBaseTrapService.class);
        basePropService = mock(IBasePropService.class);
        packService = mock(IRolePackService.class);
        roleService = mock(IRoleService.class);
        roleValueService = new RoleValueService(mock(GameConfigManager.class));

        ReflectionTestUtils.setField(controller, "baseBaitService", baseBaitService);
        ReflectionTestUtils.setField(controller, "baseCageService", baseCageService);
        ReflectionTestUtils.setField(controller, "baseNestService", baseNestService);
        ReflectionTestUtils.setField(controller, "baseTrainService", baseTrainService);
        ReflectionTestUtils.setField(controller, "baseTrapService", baseTrapService);
        ReflectionTestUtils.setField(controller, "basePropService", basePropService);
        ReflectionTestUtils.setField(controller, "packService", packService);
        ReflectionTestUtils.setField(controller, "roleService", roleService);
        ReflectionTestUtils.setField(controller, "roleValueService", roleValueService);
    }

    /**
     * 占位别名，仅为让上面的 import 列表保持可读；实际类型见 {@link IBaseNestService}。
     */
    private interface INestServiceAlias extends IBaseNestService {
    }

    // ------------------------------------------------------------------
    // 成功路径：6 种商品 x 3种货币 = 18 个用例
    // ------------------------------------------------------------------

    @Test
    @DisplayName("购买诱饵 - 支付金币：扣金币、加诱饵数量")
    void buyBaitWithCoin() {
        SuccessCase result = purchase(Item.BAIT, ConstData.FEE_TYPE_COIN);
        assertEquals(INIT_COIN - TOTAL_PRICE, result.role().getCoin());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买诱饵 - 支付钻石：扣钻石、加诱饵数量")
    void buyBaitWithDiamond() {
        SuccessCase result = purchase(Item.BAIT, ConstData.FEE_TYPE_DIAMOND);
        assertEquals(INIT_DIAMOND - TOTAL_PRICE, result.role().getDiamond().intValue());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买诱饵 - 支付爱心值：扣爱心值、加诱饵数量")
    void buyBaitWithLovePoint() {
        SuccessCase result = purchase(Item.BAIT, ConstData.FEE_TYPE_LOVE_POINT);
        assertEquals(INIT_LOVE_POINT - TOTAL_PRICE, result.role().getLovePoint());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买陷阱 - 支付金币：扣金币、加陷阱数量")
    void buyTrapWithCoin() {
        SuccessCase result = purchase(Item.TRAP, ConstData.FEE_TYPE_COIN);
        assertEquals(INIT_COIN - TOTAL_PRICE, result.role().getCoin());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买陷阱 - 支付钻石：扣钻石、加陷阱数量")
    void buyTrapWithDiamond() {
        SuccessCase result = purchase(Item.TRAP, ConstData.FEE_TYPE_DIAMOND);
        assertEquals(INIT_DIAMOND - TOTAL_PRICE, result.role().getDiamond().intValue());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买陷阱 - 支付爱心值：扣爱心值、加陷阱数量")
    void buyTrapWithLovePoint() {
        SuccessCase result = purchase(Item.TRAP, ConstData.FEE_TYPE_LOVE_POINT);
        assertEquals(INIT_LOVE_POINT - TOTAL_PRICE, result.role().getLovePoint());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买鸟笼 - 支付金币：扣金币、加鸟笼数量")
    void buyCageWithCoin() {
        SuccessCase result = purchase(Item.CAGE, ConstData.FEE_TYPE_COIN);
        assertEquals(INIT_COIN - TOTAL_PRICE, result.role().getCoin());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买鸟笼 - 支付钻石：扣钻石、加鸟笼数量")
    void buyCageWithDiamond() {
        SuccessCase result = purchase(Item.CAGE, ConstData.FEE_TYPE_DIAMOND);
        assertEquals(INIT_DIAMOND - TOTAL_PRICE, result.role().getDiamond().intValue());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买鸟笼 - 支付爱心值：扣爱心值、加鸟笼数量")
    void buyCageWithLovePoint() {
        SuccessCase result = purchase(Item.CAGE, ConstData.FEE_TYPE_LOVE_POINT);
        assertEquals(INIT_LOVE_POINT - TOTAL_PRICE, result.role().getLovePoint());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买巢 - 支付金币：扣金币、加巢数量")
    void buyNestWithCoin() {
        SuccessCase result = purchase(Item.NEST, ConstData.FEE_TYPE_COIN);
        assertEquals(INIT_COIN - TOTAL_PRICE, result.role().getCoin());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买巢 - 支付钻石：扣钻石、加巢数量")
    void buyNestWithDiamond() {
        SuccessCase result = purchase(Item.NEST, ConstData.FEE_TYPE_DIAMOND);
        assertEquals(INIT_DIAMOND - TOTAL_PRICE, result.role().getDiamond().intValue());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买巢 - 支付爱心值：扣爱心值、加巢数量")
    void buyNestWithLovePoint() {
        SuccessCase result = purchase(Item.NEST, ConstData.FEE_TYPE_LOVE_POINT);
        assertEquals(INIT_LOVE_POINT - TOTAL_PRICE, result.role().getLovePoint());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买训练场 - 支付金币：扣金币、加训练场数量")
    void buyTrainWithCoin() {
        SuccessCase result = purchase(Item.TRAIN, ConstData.FEE_TYPE_COIN);
        assertEquals(INIT_COIN - TOTAL_PRICE, result.role().getCoin());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买训练场 - 支付钻石：扣钻石、加训练场数量")
    void buyTrainWithDiamond() {
        SuccessCase result = purchase(Item.TRAIN, ConstData.FEE_TYPE_DIAMOND);
        assertEquals(INIT_DIAMOND - TOTAL_PRICE, result.role().getDiamond().intValue());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买训练场 - 支付爱心值：扣爱心值、加训练场数量")
    void buyTrainWithLovePoint() {
        SuccessCase result = purchase(Item.TRAIN, ConstData.FEE_TYPE_LOVE_POINT);
        assertEquals(INIT_LOVE_POINT - TOTAL_PRICE, result.role().getLovePoint());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买道具 - 支付金币：扣金币、加道具数量")
    void buyPropWithCoin() {
        SuccessCase result = purchase(Item.PROP, ConstData.FEE_TYPE_COIN);
        assertEquals(INIT_COIN - TOTAL_PRICE, result.role().getCoin());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买道具 - 支付钻石：扣钻石、加道具数量")
    void buyPropWithDiamond() {
        SuccessCase result = purchase(Item.PROP, ConstData.FEE_TYPE_DIAMOND);
        assertEquals(INIT_DIAMOND - TOTAL_PRICE, result.role().getDiamond().intValue());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买道具 - 支付爱心值：扣爱心值、加道具数量")
    void buyPropWithLovePoint() {
        SuccessCase result = purchase(Item.PROP, ConstData.FEE_TYPE_LOVE_POINT);
        assertEquals(INIT_LOVE_POINT - TOTAL_PRICE, result.role().getLovePoint());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    // ------------------------------------------------------------------
    // 失败路径：4 类拒绝理由
    // ------------------------------------------------------------------

    @Test
    @DisplayName("商品不存在 - 返回「没有该商品哦。」且不扣任何货币")
    void rejectUnknownItem() {
        Role role = newRole();
        // 刻意不 stub getById，Mockito 默认返回 null，模拟「配置表里没有这一行」
        ExtendedModelMap model = new ExtendedModelMap();
        String view = buy(Item.BAIT, role, model, 3);

        assertEquals("common/message", view);
        assertEquals("没有该商品哦。", errorText(model));
        assertEquals(INIT_COIN, role.getCoin());
        Mockito.verify(packService, Mockito.never()).saveOrUpdate(any());
    }

    @Test
    @DisplayName("支付类型非法 - 视为商品不存在，不进入扣款分支")
    void rejectIllegalFeeType() {
        Role role = newRole();
        stubItem(Item.BAIT, (byte) 9, 12, 1);
        ExtendedModelMap model = new ExtendedModelMap();
        String view = buy(Item.BAIT, role, model, 3);

        assertEquals("common/message", view);
        assertEquals("没有该商品哦。", errorText(model));
        assertEquals(1000L, role.getCoin());
        assertEquals(40, role.getDiamond());
        assertEquals(900L, role.getLovePoint());
    }

    @Test
    @DisplayName("等级不足 - 返回「您的等级不足。」且不扣款")
    void rejectLevelTooLow() {
        Role role = newRole();
        role.setLevel(1);
        // 商品要求 30 级
        stubItem(Item.CAGE, ConstData.FEE_TYPE_COIN, 12, 30);
        ExtendedModelMap model = new ExtendedModelMap();
        String view = buy(Item.CAGE, role, model, 3);

        assertEquals("common/message", view);
        assertEquals("您的等级不足。", errorText(model));
        assertEquals(1000L, role.getCoin());
    }

    @Test
    @DisplayName("金币不足 - 提示带当前值与所需值，且不扣款")
    void rejectCoinNotEnough() {
        Role role = newRole();
        role.setCoin(10L);
        stubItem(Item.BAIT, ConstData.FEE_TYPE_COIN, 12, 1);
        ExtendedModelMap model = new ExtendedModelMap();
        String view = buy(Item.BAIT, role, model, 3);

        assertEquals("common/message", view);
        assertEquals("您的金钱不足哦(10/36)。", errorText(model));
        assertEquals(10L, role.getCoin());
    }

    @Test
    @DisplayName("钻石不足 - 提示带当前值与所需值，且不扣款")
    void rejectDiamondNotEnough() {
        Role role = newRole();
        role.setDiamond(5);
        stubItem(Item.NEST, ConstData.FEE_TYPE_DIAMOND, 12, 1);
        ExtendedModelMap model = new ExtendedModelMap();
        String view = buy(Item.NEST, role, model, 3);

        assertEquals("common/message", view);
        assertEquals("您的钻石不足(5/36)。", errorText(model));
        assertEquals(5, role.getDiamond());
    }

    @Test
    @DisplayName("爱心值不足 - 提示带当前值与所需值，且不扣款")
    void rejectLovePointNotEnough() {
        Role role = newRole();
        role.setLovePoint(7L);
        stubItem(Item.PROP, ConstData.FEE_TYPE_LOVE_POINT, 12, 1);
        ExtendedModelMap model = new ExtendedModelMap();
        String view = buy(Item.PROP, role, model, 3);

        assertEquals("common/message", view);
        assertEquals("您的爱心值不足(7/36)。", errorText(model));
        assertEquals(7L, role.getLovePoint());
    }

    // ------------------------------------------------------------------
    // 夹具与工具
    // ------------------------------------------------------------------

    /** 单件商品价格，18 个成功用例统一使用。 */
    private static final int UNIT_PRICE = 12;

    /** 成功用例统一购买数量。 */
    private static final int PAY_NUM = 3;

    /** 单次成功购买的总价 = {@link #UNIT_PRICE} x {@link #PAY_NUM}。 */
    private static final int TOTAL_PRICE = UNIT_PRICE * PAY_NUM;

    /** 角色初始金币，保证金币用例余额充足。 */
    private static final long INIT_COIN = 1000L;

    /** 角色初始钻石，保证钻石用例余额充足。 */
    private static final int INIT_DIAMOND = 40;

    /** 角色初始爱心值，保证爱心值用例余额充足。 */
    private static final long INIT_LOVE_POINT = 900L;

    /** 角色初始等级，保证等级用例通过。 */
    private static final int INIT_LEVEL = 50;

    /**
     * 一次成功购买的结果快照。
     *
     * @param role    购买后的角色（三种货币余额均已更新）
     * @param packNum 购买后的包裹数量
     */
    private record SuccessCase(Role role, int packNum) {
    }

    /**
     * 执行一次「应当成功」的购买。
     *
     * @param item    商品种类
     * @param feeType 支付类型
     * @return 购买后的角色与包裹数量
     */
    private SuccessCase purchase(Item item, Byte feeType) {
        Role role = newRole();
        stubItem(item, feeType, UNIT_PRICE, null);
        RolePack pack = stubExistingPack(item);
        ExtendedModelMap model = new ExtendedModelMap();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("sRole", role);

        String view = buy(item, role, model, PAY_NUM, session);

        assertEquals("common/message", view);
        MessageVO message = (MessageVO) model.asMap().get("message");
        assertNotNull(message, "成功购买必须写入 message");
        assertEquals(2, message.getMessageType().getTypeId(), "必须是成功类型");
        assertTrue(message.getMessage().contains("购买成功"), message.getMessage());
        // 三种货币中，只有被指定的那一种发生变化
        long expectedCoin = ConstData.FEE_TYPE_COIN.equals(feeType) ? INIT_COIN - UNIT_PRICE * PAY_NUM : INIT_COIN;
        assertEquals(expectedCoin, role.getCoin());
        assertEquals(role.getCoin().longValue(), ((Role) session.getAttribute("sRole")).getCoin().longValue(),
                "会话中的角色必须是同一个已被更新的实例");
        // 包裹数量与持久化
        assertEquals(PACK_INIT_NUM + PAY_NUM, pack.getNum());
        Mockito.verify(packService).saveOrUpdate(pack);
        Mockito.verify(roleService).saveOrUpdate(role);
        return new SuccessCase(role, pack.getNum());
    }

    /**
     * 构造一个余额充足、等级足够的角色。
     *
     * @return 新角色实例
     */
    private static Role newRole() {
        Role role = new Role();
        role.setRoleId("r-0001");
        role.setLevel(INIT_LEVEL);
        role.setCoin(INIT_COIN);
        role.setDiamond(INIT_DIAMOND);
        role.setLovePoint(INIT_LOVE_POINT);
        return role;
    }

    /**
     * 让对应商品的配置查询返回一条合法记录。
     *
     * @param item商品种类
     * @param feeType 支付类型
     * @param price 单价
     * @param lu    等级门槛；为 {@code null} 时使用 1 级（即不构成门槛）
     */
    private void stubItem(Item item, Byte feeType, int price, Integer lu) {
        switch (item) {
            case BAIT -> {
                BaseBait base = new BaseBait();
                base.setId(ITEM_ID);
                base.setName("中等面包屑");
                base.setFeeType(feeType);
                base.setFeePrice(price);
                // BaseBait 的 lu 是 Short，BaseTrain/BaseProp 等是 Integer —— 不可统一
                base.setLu((short) (lu == null ? 1 : lu));
                when(baseBaitService.getById(ITEM_ID)).thenReturn(base);
            }
            case TRAP -> {
                BaseTrap base = new BaseTrap();
                base.setId(ITEM_ID);
                base.setName("捕鸟夹");
                base.setFeeType(feeType);
                base.setFeePrice(price);
                base.setLu(lu == null ? 1 : lu);
                when(baseTrapService.getById(ITEM_ID)).thenReturn(base);
            }
            case CAGE -> {
                BaseCage base = new BaseCage();
                base.setId(ITEM_ID);
                base.setName("竹笼");
                base.setFeeType(feeType);
                base.setFeePrice(price);
                base.setLu(lu == null ? 1 : lu);
                when(baseCageService.getById(ITEM_ID)).thenReturn(base);
            }
            case NEST -> {
                BaseNest base = new BaseNest();
                base.setId(ITEM_ID);
                base.setName("爱的巢穴");
                base.setFeeType(feeType);
                base.setFeePrice(price);
                base.setLu(lu == null ? 1 : lu);
                when(baseNestService.getById(ITEM_ID)).thenReturn(base);
            }
            case TRAIN -> {
                BaseTrain base = new BaseTrain();
                base.setId(ITEM_ID);
                base.setName("宠物训练场");
                base.setFeeType(feeType);
                base.setFeePrice(price);
                base.setLu(lu == null ? 1 : lu);
                when(baseTrainService.getById(ITEM_ID)).thenReturn(base);
            }
            case PROP -> {
                BaseProp base = new BaseProp();
                base.setId(ITEM_ID);
                base.setName("加速卡");
                base.setFeeType(feeType);
                base.setFeePrice(price);
                base.setLu(lu == null ? 1 : lu);
                when(basePropService.getById(ITEM_ID)).thenReturn(base);
            }
            default -> throw new IllegalArgumentException("未覆盖的商品类型: " + item);
        }
    }

    /**
     * 让包裹查询返回一条「已存在但数量为 0」的记录，覆盖累加分支。
     *
     * @param item 商品种类
     * @return 被操作的包裹记录
     */
    private RolePack stubExistingPack(Item item) {
        RolePack pack = new RolePack();
        pack.setId(11);
        pack.setRoleId("r-0001");
        pack.setTypeId(item.packType());
        pack.setBaseId(ITEM_ID);
        pack.setNum(PACK_INIT_NUM);
        when(packService.getOne(any())).thenReturn(pack);
        return pack;
    }

    /**
     * 分派到对应的 {@code buyShopXxx} 方法。
     *
     * @param item 商品种类
     * @param role 已放入 session 的角色
     * @param model 模型
     * @param payNum 购买数量
     * @return 视图名
     */
    private String buy(Item item, Role role, Model model, int payNum) {
        return buy(item, role, model, payNum, newSessionWith(role));
    }

    /**
     * 分派到对应的 {@code buyShopXxx} 方法（显式传入 session）。
     *
     * @param item    商品种类
     * @param role    已放入 session 的角色
     * @param model   模型
     * @param payNum  购买数量
     * @param session 会话
     * @return 视图名
     */
    private String buy(Item item, Role role, Model model, int payNum, MockHttpSession session) {
        return switch (item) {
            case BAIT -> controller.buyShopBait(session, model, ITEM_ID, payNum);
            case TRAP -> controller.buyShopTrap(session, model, ITEM_ID, payNum);
            case CAGE -> controller.buyShopCage(session, model, ITEM_ID, payNum);
            case NEST -> controller.buyShopNest(session, model, ITEM_ID, payNum);
            case TRAIN -> controller.buyShopTrain(session, model, ITEM_ID, payNum);
            case PROP -> controller.buyShopProp(session, model, ITEM_ID, payNum);
        };
    }

    /**
     * 构造一个只含角色的会话。
     *
     * @param role 角色
     * @return 会话
     */
    private static MockHttpSession newSessionWith(Role role) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("sRole", role);
        return session;
    }

    /**
     * 取出模型中的错误文案。
     *
     * @param model 模型
     * @return 文案
     */
    private static String errorText(Model model) {
        Object message = model.asMap().get("message");
        assertNotNull(message, "失败分支必须写入 message");
        assertSame(MessageVO.class, message.getClass());
        return ((MessageVO) message).getMessage();
    }
}