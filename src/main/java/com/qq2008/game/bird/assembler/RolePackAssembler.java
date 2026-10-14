package com.qq2008.game.bird.assembler;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.qq2008.game.bird.config.GameConfigGateway;
import com.qq2008.game.bird.data.ConstData;
import com.qq2008.game.bird.model.dbo.RolePack;
import com.qq2008.game.bird.model.vo.RolePackVO;

/**
 * 包裹页面的视图装配器。
 *
 * <p><b>为什么抽出来</b>：{@code PackController} 的 6 个列表方法原本是逐字复制的
 * 30 行模板 —— 建wrapper、翻页查库、拷分页元数据、逐行 {@code BeanUtils.copyProperties}、
 * 再按类型塞入对应的配置对象。6 份之间唯一的差别只有两点：
 * <ol>
 *   <li>用哪个 {@code PACK_TYPE_*} 过滤；</li>
 *   <li>调用 {@code RolePackVO} 的哪个 setter（{@code setBaseBait} / {@code setBaseCage} / …）。</li>
 * </ol>
 * 这两点都只是<b>数据形态</b>的差异，不该让Controller 关心。
 *
 * <p><b>为什么用 {@link BiConsumer} 而不是 switch</b>：6 种类型与 6 个不同类型签名的
 * setter 无法收敛到同一个函数式接口（它们的入参类型分别是 {@code BaseBait}、
 * {@code BaseCage} …）。用 {@code BiConsumer<RolePackVO, Void>} 显式承接后，
 * 新增一种物品时只需在 {@link PackItemKind} 里加一行，Controller 与本类都不用改。
 *
 * <p><b>分页元数据</b>用 {@code BeanUtils.copyProperties(Page, Page)} 原样搬运，
 * 与改造前完全一致 —— {@code total / current / size / pages} 等字段对模板可见。
 *
 * @see PackItemKind 物品种类与「填哪个配置字段」的映射
 */
@Component
public class RolePackAssembler {

    /** 配置读取入口，构造器注入，便于脱离 Spring 容器测试。 */
    private final GameConfigGateway configGateway;

    /**
     * 构造注入。
     *
     * @param configGateway 配置门面
     */
    public RolePackAssembler(GameConfigGateway configGateway) {
        this.configGateway = configGateway;
    }

    /**
     * 把一页包裹记录装配成带配置对象的视图对象。
     *
     * @param source 已查出的分页结果
     * @param kind   物品种类，决定按哪个配置字段填充
     * @return 记录已替换为 {@link RolePackVO}、分页元数据与入参一致的新分页对象
     */
    public Page<RolePackVO> assemble(Page<RolePack> source, PackItemKind kind) {
        Page<RolePackVO> target = new Page<>();
        BeanUtils.copyProperties(source, target);
        List<RolePackVO> viewList = new ArrayList<>();
        if (null != source.getRecords()) {
            for (RolePack pack : source.getRecords()) {
                RolePackVO view = new RolePackVO();
                BeanUtils.copyProperties(pack, view);
                kind.fill(view, configGateway);
                viewList.add(view);
            }
        }
        target.setRecords(viewList);
        return target;
    }

    /**
     * 包裹支持的 6 种物品。
     *
     * <p>每个枚举自带「用哪个 {@code PACK_TYPE_*} 查询」与「把哪个配置对象塞进 VO」，
     * 这两项正是 6 个 Controller 方法之间唯一的差异。
     */
    public enum PackItemKind {

        /** 诱饵。 */
        BAIT(ConstData.PACK_TYPE_BAIT,
                (view, gateway) -> view.setBaseBait(gateway.getBaseBait(view.getBaseId()))),

        /** 陷阱。 */
        TRAP(ConstData.PACK_TYPE_TRAP,
                (view, gateway) -> view.setBaseTrap(gateway.getBaseTrap(view.getBaseId()))),

        /** 鸟笼。 */
        CAGE(ConstData.PACK_TYPE_CAGE,
                (view, gateway) -> view.setBaseCage(gateway.getBaseCage(view.getBaseId()))),

        /** 巢。 */
        NEST(ConstData.PACK_TYPE_NEST,
                (view, gateway) -> view.setBaseNest(gateway.getBaseNest(view.getBaseId()))),

        /** 训练场。 */
        TRAIN(ConstData.PACK_TYPE_TRAIN,
                (view, gateway) -> view.setBaseTrain(gateway.getBaseTrain(view.getBaseId()))),

        /** 道具。 */
        PROP(ConstData.PACK_TYPE_PROP,
                (view, gateway) -> view.setBaseProp(gateway.getBaseProp(view.getBaseId())));

        /** role_pack.type_id，用于列表查询条件。 */
        private final Byte packTypeId;

        /** 把配置对象填进 VO 的动作。 */
        private final BiConsumer<RolePackVO, GameConfigGateway> fill;

        PackItemKind(Byte packTypeId, BiConsumer<RolePackVO, GameConfigGateway> fill) {
            this.packTypeId = packTypeId;
            this.fill = fill;
        }

        /**
         * @return role_pack.type_id
         */
        public Byte packTypeId() {
            return packTypeId;
        }

        /**
         * 按 baseId 查出配置对象并写入 VO 的对应字段。
         *
         * @param view    待填充的视图对象
         * @param gateway 配置门面
         */
        void fill(RolePackVO view, GameConfigGateway gateway) {
            fill.accept(view, gateway);
        }
    }
}