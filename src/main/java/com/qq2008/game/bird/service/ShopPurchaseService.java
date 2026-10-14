package com.qq2008.game.bird.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qq2008.common.entity.BizException;
import com.qq2008.common.entity.ResultCode;
import com.qq2008.common.entity.vo.MessageLink;
import com.qq2008.game.bird.data.ShopItemAccessors;
import com.qq2008.game.bird.model.dbo.Role;
import com.qq2008.game.bird.model.dbo.RolePack;
import com.qq2008.game.bird.model.shop.FeeKind;
import com.qq2008.game.bird.model.shop.PurchaseResult;
import com.qq2008.game.bird.model.shop.ShopItemView;
import com.qq2008.game.bird.model.shop.ShopKind;
import com.qq2008.game.bird.util.GameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商店购买 —— 收敛原 {@code ShopController} 6 个 {@code buyShopXxx}（共 476 行）的唯一实现。
 *
 * <p><b>事务边界</b>：{@link Transactional} <b>只加在本类的这一个 public 方法上</b>。
 * Controller → Service 是经由 Spring 代理的调用，AOP 生效；而本类的 private 委托方法
 * 若也标注 {@code @Transactional}，Spring AOP 的<b>自调用不经过代理</b>，
 * 事务会<b>静默失效</b> —— 不报错，但回滚无效。因此这里严格只标一处。
 *
 * <p><b>写库顺序</b>：扣费只在内存态改 {@link Role} 对象，末尾统一
 * {@code roleService.saveOrUpdate} 落库，途中不落库。这与原 6 个方法的
 * 「先改内存 → 最后 saveOrUpdate」逐字一致。
 *
 * <p><b>失败路径不产生写操作</b>：商品不存在 / 等级不足 / 余额不足三类失败
 * 全部在任何 {@code saveOrUpdate} 之前返回，因此不写库。
 */
@Service
public class ShopPurchaseService {

    /** 单次购买数量上限。 */
    public static final int MAX_PAY_NUM = 999;

    /** 配置访问器。 */
    private final ShopItemAccessors accessors;

    /** 角色数值增减（扣费、加包裹数量）。 */
    private final RoleValueService roleValueService;

    private final IRoleService roleService;

    private final IRolePackService packService;

    /**
     * 构造注入。
     *
     * @param accessors        配置访问器
     * @param roleValueService 角色数值服务
     * @param roleService      角色服务
     * @param packService      包裹服务
     */
    public ShopPurchaseService(ShopItemAccessors accessors,
                               RoleValueService roleValueService,
                               IRoleService roleService,
                               IRolePackService packService) {
        this.accessors = accessors;
        this.roleValueService = roleValueService;
        this.roleService = roleService;
        this.packService = packService;
    }

    /**
     * 购买一件道具。
     *
     * <p>收敛原 79 行 × 6 份的四步流程：
     * <ol>
     *   <li>参数校验（id / payNum 的空值与范围）；</li>
     *   <li>载入配置，不存在或 feeType 非法 → 「没有该商品哦。」；</li>
     *   <li>等级门槛 {@code lu > role.level} → 「您的等级不足。」，再按 {@link FeeKind} 校验并扣费；</li>
     *   <li>取或建 {@link RolePack} → 数量 += payNum → 落库 → 成功消息 + 「去包裹看看」链接。</li>
     * </ol>
     *
     * <p><b>文案红线</b>：所有对玩家可见的文案逐字取自原实现，
     * 且已被购买流程测试逐字断言，一字不许改。
     *
     * @param role   已登录角色（本方法只改其内存态，持久化在此完成）
     * @param kind   6 种道具之一
     * @param id     配置项 id
     * @param payNum 购买数量
     * @return 成功/失败结果
     * @throws BizException 角色或种类为 null（属编程错误，不属用户输入错误）
     */
    @Transactional(rollbackFor = Exception.class)
    public PurchaseResult purchase(Role role, ShopKind kind, Integer id, Integer payNum) {
        if (role == null || kind == null) {
            throw new BizException(ResultCode.PARAM_INVALID, "购买参数不合法。");
        }

        // ---- 步骤 1：参数校验（Q-D 漏洞修复）
        //
        // 【行为变更，刻意为之】重构前 6 个 buyShopXxx 只判断了 payNum == null，
        // 于是 payNum=0 会「白送道具并写库」，payNum<0 会让总价为负、
        // 反而倒扣玩家已有道具。这里补齐范围校验，三条文案为本单元新增。
        if (id == null || id <= 0) {
            return PurchaseResult.fail("商品编号无效。");
        }
        if (payNum == null || payNum <= 0) {
            return PurchaseResult.fail("购买数量必须大于 0。");
        }
        if (payNum > MAX_PAY_NUM) {
            return PurchaseResult.fail("单次购买数量不能超过 " + MAX_PAY_NUM + "。");
        }

        // ---- 步骤 2：载入配置
        ShopItemView item = accessors.load(kind, id);
        if (item == null) {
            return PurchaseResult.fail("没有该商品哦。");
        }
        // feeType 非法时返回「没有该商品哦。」，与重构前 isFeeTypeError() 分支的
        // 用户可见行为**逐字一致**：仍是 common/message 视图 + 红色提示。
        //
        // 【与设计文档的偏离，刻意为之】文档 §2.1.7 建议此处让 FeeKind.of 抛
        // BizException，由 GlobalExceptionHandler 渲染 error.html。但那会把失败页面
        // 从 common/message 换成 error.html —— 对玩家而言是一次可见的行为变更，
        // 且会让 U10 的 24 个门禁用例必须修改断言才能通过（A1 是无行为变更重构，
        // 门禁明确要求「需要改断言就回退，而不是改测试」）。故此处显式转为失败结果，
        // 保持与原实现完全一致。
        FeeKind feeKind = FeeKind.ofOrNull(item.feeType());
        if (feeKind == null) {
            return PurchaseResult.fail("没有该商品哦。");
        }

        // ---- 步骤 3：等级门槛
        if (item.lu() > role.getLevel()) {
            return PurchaseResult.fail("您的等级不足。");
        }
        // 总金额
        int totalFeePrice = item.feePrice() * payNum;
        // 余额校验 + 扣费。校验在前、扣除在后，余额不足时角色不受任何影响。
        if (feeKind.balance(role) < totalFeePrice) {
            return PurchaseResult.fail(feeKind.notEnoughText(role, totalFeePrice));
        }
        roleValueService.cost(feeKind, role, totalFeePrice);

        // ---- 步骤 4：发货
        RolePack pack = loadOrCreatePack(role.getRoleId(), kind, id);
        roleValueService.addPackNum(pack, payNum);

        // 回写数据（顺序与原实现一致：role → pack）
        roleService.saveOrUpdate(role);
        packService.saveOrUpdate(pack);

        // 消息（文案与原实现逐字一致，仅用 record 访问器替代实体 getter）
        String text = "购买成功，您花费" + totalFeePrice
                + GameUtils.getFeeTypeName(item.feeType())
                + "购买了" + item.name() + "×" + payNum;
        return PurchaseResult.success(text,
                new MessageLink("去包裹看看", "/pack/" + kind.getKey() + "/list.do"));
    }

    /**
     * 取指定角色指定物品的包裹记录，没有则新建一条空记录。
     *
     * <p><b>不落库</b>：新建对象带 {@code id = 0}，这是 MyBatis-Plus 的「待插入」标记，
     * 由调用方累加数量后统一 {@code saveOrUpdate}。
     *
     * <p><b>为什么留在本类而不在 {@code RolePackAssembler}</b>：Assembler 的职责是
     * 「把查出来的记录装配成视图对象」，而这里是「按需创建一条待插入的业务记录」，
     * 属于持久化语义，混进去会让它同时承担读写两个方向。
     *
     * @param roleId 角色 id
     * @param kind   道具种类
     * @param id     配置 id
     * @return 已存在的记录，或一条待插入的空记录（{@code num = 0}）
     */
    private RolePack loadOrCreatePack(String roleId, ShopKind kind, Integer id) {
        Byte packType = (byte) kind.getPackType();
        LambdaQueryWrapper<RolePack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePack::getRoleId, roleId);
        queryWrapper.eq(RolePack::getBaseId, id);
        queryWrapper.eq(RolePack::getTypeId, packType);
        RolePack pack = packService.getOne(queryWrapper);
        if (pack != null) {
            return pack;
        }
        RolePack created = new RolePack();
        // id = 0 是 MyBatis-Plus 的「待插入」标记，不能省
        created.setId(0);
        created.setRoleId(roleId);
        created.setTypeId(packType);
        created.setBaseId(id);
        created.setNum(0);
        return created;
    }
}