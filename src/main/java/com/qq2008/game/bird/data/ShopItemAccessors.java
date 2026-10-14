package com.qq2008.game.bird.data;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qq2008.game.bird.model.shop.ShopItemView;
import com.qq2008.game.bird.model.shop.ShopKind;
import com.qq2008.game.bird.service.IBaseBaitService;
import com.qq2008.game.bird.service.IBaseCageService;
import com.qq2008.game.bird.service.IBaseNestService;
import com.qq2008.game.bird.service.IBasePropService;
import com.qq2008.game.bird.service.IBaseTrainService;
import com.qq2008.game.bird.service.IBaseTrapService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 6 种商店配置项的访问器注册表 —— 消除 476 行购买重复的核心。
 *
 * <p><b>为什么这样设计</b>：6 个 {@code Base*} 实体无公共父类/接口，
 * 且 {@code BaseBait.lu} 是 {@code Short}、其余 5 个是 {@code Integer}（实测），
 * 因此不能用接口统一。改为「枚举持有元数据 + 访问器以方法引用注入」：
 * <ul>
 *   <li>{@link ShopKind}：纯静态元数据，不持有任何 Spring bean；</li>
 *   <li>本类：Spring bean，在初始化期用 6 个 lambda 把各 {@code IService} 绑定到 {@link ShopKind}；</li>
 *   <li>{@link ShopItemView}：record 投影，吸收 {@code Short}/{@code Integer} 差异。</li>
 * </ul>
 * 业务代码只面向 {@code ShopKind + ShopItemView} 编程，完全不知道 6 个实体各自的存在。
 *
 * <p><b>泛型落点</b>：{@link #pageShopItems} 用 {@link QueryWrapper} 的字符串列名，
 * 因为 6 张 base 表的 {@code lu / is_shopping / fee_type} 列名完全同构，
 * 字符串列名是异构实体下唯一能让方法保持泛型可行的写法。
 *
 * <p><b>与 {@link GameConfigGateway} 的边界（两者并存，不重叠）</b>：
 * <ul>
 *   <li>{@link GameConfigGateway}：从 {@code GameConfigManager} 读<b>启动期加载到内存</b>的
 *       配置，返回原始 {@code Base*} 实体，用于渲染与业务判断这类「配置必须全量在场」的场合；</li>
 *   <li>本类：走 {@link IService} <b>按 id / 分页条件查库</b>，返回 {@link ShopItemView} 投影，
 *       用于商店与包裹列表这类「要分页、要筛选、且只关心少数字段」的场合。</li>
 * </ul>
 * 二者数据同源（同一批 base 表）但访问方式与返回类型不同：网关不查库、返回实体；
 * 本类查库、返回投影。架构师曾建议取消本类并把读取并入网关的 {@code findShopItem}，
 * 实测后未采纳 —— 网关的 6 个 {@code getBaseXxx} 方法均无分页与筛选能力，
 * 且返回原始实体会把 {@code Short}/{@code Integer} 差异重新泄漏给调用方，
 * 正是 {@link ShopItemView} 要消除的那一处。
 */
@Component
public class ShopItemAccessors {

    /** 6 个配置 Service，由 {@link #bind()} 在初始化期绑定进 loaders。 */
    private IBaseBaitService baseBaitService;

    private IBaseTrapService baseTrapService;

    private IBaseCageService baseCageService;

    private IBaseNestService baseNestService;

    private IBaseTrainService baseTrainService;

    private IBasePropService basePropService;

    /** {@code ShopKind} → 按 id 载入配置。 */
    private final Map<ShopKind, Function<Integer, ShopItemView>> loaders = new EnumMap<>(ShopKind.class);

    /**
     * 注入 6 个配置 Service 并建立 {@code ShopKind → loader} 映射。
     *
     * <p>用 setter 注入而非构造器注入，是为了与项目既有的 {@code @Resource} 风格一致，
     * 同时保证 {@code loaders} 的填充发生在所有依赖注入完成之后。
     */
    @PostConstruct
    public void bind() {
        loaders.put(ShopKind.BAIT, id -> ShopItemView.of(baseBaitService.getById(id)));
        loaders.put(ShopKind.TRAP, id -> ShopItemView.of(baseTrapService.getById(id)));
        loaders.put(ShopKind.CAGE, id -> ShopItemView.of(baseCageService.getById(id)));
        loaders.put(ShopKind.NEST, id -> ShopItemView.of(baseNestService.getById(id)));
        loaders.put(ShopKind.TRAIN, id -> ShopItemView.of(baseTrainService.getById(id)));
        loaders.put(ShopKind.PROP, id -> ShopItemView.of(basePropService.getById(id)));
    }

    /**
     * setter 注入，供 Spring 调用。
     *
     * @param service 诱饵配置服务
     */
    @jakarta.annotation.Resource
    public void setBaseBaitService(IBaseBaitService service) {
        this.baseBaitService = service;
    }

    /**
     * setter 注入，供 Spring 调用。
     *
     * @param service 陷阱配置服务
     */
    @jakarta.annotation.Resource
    public void setBaseTrapService(IBaseTrapService service) {
        this.baseTrapService = service;
    }

    /**
     * setter 注入，供 Spring 调用。
     *
     * @param service 鸟笼配置服务
     */
    @jakarta.annotation.Resource
    public void setBaseCageService(IBaseCageService service) {
        this.baseCageService = service;
    }

    /**
     * setter 注入，供 Spring 调用。
     *
     * @param service 巢配置服务
     */
    @jakarta.annotation.Resource
    public void setBaseNestService(IBaseNestService service) {
        this.baseNestService = service;
    }

    /**
     * setter 注入，供 Spring 调用。
     *
     * @param service 训练场配置服务
     */
    @jakarta.annotation.Resource
    public void setBaseTrainService(IBaseTrainService service) {
        this.baseTrainService = service;
    }

    /**
     * setter 注入，供 Spring 调用。
     *
     * @param service 道具配置服务
     */
    @jakarta.annotation.Resource
    public void setBasePropService(IBasePropService service) {
        this.basePropService = service;
    }

    /**
     * 按 id 取配置。
     *
     * @param kind 道具种类，可为 null
     * @param id   配置 id，可为 null
     * @return 配置投影；kind/id 为 null、未绑定、或数据库中无此行时返回 {@code null}
     */
    public ShopItemView load(ShopKind kind, Integer id) {
        if (kind == null || id == null) {
            return null;
        }
        Function<Integer, ShopItemView> loader = loaders.get(kind);
        return loader == null ? null : loader.apply(id);
    }

    /**
     * 商店列表分页查询 —— 收敛 6 个 {@code pageShopXxx}。
     *
     * <p>条件逐字保留原实现：{@code lu <= level}、{@code is_shopping = 1}、
     * {@code order by lu desc}；{@code feeType > 0} 时才追加货币过滤。
     *
     * @param page    MyBatis-Plus 分页对象，调用方已设置 current/size
     * @param level   角色等级，对应 {@code lu <= level}
     * @param feeType 货币类型过滤；小于等于 0 表示不过滤
     * @param service 目标表的 Service（6 个之一）
     * @param <T>     实体类型
     */
    public <T> void pageShopItems(Page<T> page, int level, int feeType, IService<T> service) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.le("lu", level)
                .eq("is_shopping", 1)
                .orderByDesc("lu");
        if (feeType > 0) {
            wrapper.eq("fee_type", feeType);
        }
        service.page(page, wrapper);
    }
}