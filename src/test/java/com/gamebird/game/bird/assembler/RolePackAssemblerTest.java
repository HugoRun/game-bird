package com.gamebird.game.bird.assembler;

import java.lang.reflect.Method;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;

import com.gamebird.game.bird.config.GameConfigGateway;
import com.gamebird.game.bird.model.dbo.BaseBait;
import com.gamebird.game.bird.model.dbo.BaseCage;
import com.gamebird.game.bird.model.dbo.RolePack;
import com.gamebird.game.bird.model.vo.RolePackVO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link RolePackAssembler} 的纯单元测试。
 *
 * <p><b>要防的回归</b>：6 个包裹列表页面各自往 VO 的不同字段塞配置对象
 * （{@code baseBait} / {@code baseCage} / …）。这类「填错字段」的 bug 在页面上表现为
 * 一片空白，且不会抛异常，因此必须有测试钉住「哪种物品填哪个字段」。
 *
 * <p><b>不加载 Spring 容器</b>：{@code GameConfigGateway} 用 Mockito mock。
 */
class RolePackAssemblerTest {

    /** 被测对象，唯一依赖是配置门面。 */
    private final RolePackAssembler assembler = new RolePackAssembler(mock(GameConfigGateway.class));

    /** 商品 id，统一取值。 */
    private static final int BASE_ID = 42;

    /** VO 上6 个「配置对象」getter 的名字，刻意列全而不是前缀匹配。 */
    private static final java.util.Set<String> CONFIG_GETTERS = java.util.Set.of(
            "getBaseBait", "getBaseCage", "getBaseNest", "getBaseProp", "getBaseTrain", "getBaseTrap");

    /**
     * 造一页含 1 条记录的包裹分页。
     *
     * @param current 当前页
     * @param total   总条数
     * @return 分页对象
     */
    private static Page<RolePack> sourcePage(long current, long total) {
        RolePack pack = new RolePack();
        pack.setId(1);
        pack.setRoleId("r-1");
        pack.setTypeId((byte) 1);
        pack.setBaseId(BASE_ID);
        pack.setNum(9);

        Page<RolePack> page = new Page<>();
        page.setCurrent(current);
        page.setSize(5);
        page.setTotal(total);
        page.setRecords(List.of(pack));
        return page;
    }

    @Test
    @DisplayName("分页元数据原样搬运，模板仍能读到总数与页码")
    void carriesPaginationMetadata() {
        Page<RolePackVO> result = assembler.assemble(sourcePage(3L, 13L),
                RolePackAssembler.PackItemKind.BAIT);

        assertEquals(3L, result.getCurrent());
        assertEquals(5L, result.getSize());
        assertEquals(13L, result.getTotal());
        assertEquals(3L, result.getPages(), "13 条 / 每页 5 条 = 3 页");
    }

    @Test
    @DisplayName("业务字段从 RolePack 拷到 RolePackVO，数量不变")
    void copiesBusinessFields() {
        Page<RolePackVO> result = assembler.assemble(sourcePage(1L, 1L),
                RolePackAssembler.PackItemKind.BAIT);

        assertEquals(1, result.getRecords().size());
        RolePackVO view = result.getRecords().get(0);
        assertEquals(1, view.getId());
        assertEquals("r-1", view.getRoleId());
        assertEquals(BASE_ID, view.getBaseId());
        assertEquals(9, view.getNum());
    }

    @Test
    @DisplayName("6 种物品各自填入对应的配置字段")
    void fillsTheRightConfigFieldPerKind() {
        for (RolePackAssembler.PackItemKind kind : RolePackAssembler.PackItemKind.values()) {
            RolePackVO view = assembleOne(kind);
            String filledField = whichConfigFieldIsFilled(view);
            assertNotNull(filledField, kind + " 未能填入任何配置字段");
            assertEquals(expectedField(kind), filledField,
                    kind + " 填错了配置字段，会导致页面对应列为空");
        }
    }

    @Test
    @DisplayName("配置查询走的是该物品自己的字段名，不会串到别的种类")
    void queriesTheMatchingBaseId() {
        GameConfigGateway gateway = mock(GameConfigGateway.class);
        BaseBait bait = new BaseBait();
        bait.setId(BASE_ID);
        bait.setName("中等面包屑");
        when(gateway.getBaseBait(BASE_ID)).thenReturn(bait);

        RolePackAssembler localAssembler = new RolePackAssembler(gateway);
        Page<RolePackVO> result = localAssembler.assemble(sourcePage(1L, 1L),
                RolePackAssembler.PackItemKind.BAIT);

        assertSame(bait, result.getRecords().get(0).getBaseBait());
    }

    @Test
    @DisplayName("记录为 null 时不抛异常，返回空列表")
    void toleratesNullRecords() {
        Page<RolePack> empty = new Page<>();
        empty.setRecords(null);
        Page<RolePackVO> result = assembler.assemble(empty, RolePackAssembler.PackItemKind.PROP);

        assertNotNull(result.getRecords());
        assertTrue(result.getRecords().isEmpty());
    }

    /**
     * 用一个能真实返回配置对象的 gateway 装配单条记录。
     *
     * @param kind 物品种类
     * @return 装配结果
     */
    private RolePackVO assembleOne(RolePackAssembler.PackItemKind kind) {
        GameConfigGateway gateway = mock(GameConfigGateway.class);
        BaseBait bait = new BaseBait();
        bait.setId(BASE_ID);
        BaseCage cage = new BaseCage();
        cage.setId(BASE_ID);
        when(gateway.getBaseBait(BASE_ID)).thenReturn(bait);
        when(gateway.getBaseCage(BASE_ID)).thenReturn(cage);
        when(gateway.getBaseNest(BASE_ID)).thenReturn(new com.gamebird.game.bird.model.dbo.BaseNest() {{ setId(BASE_ID); }});
        when(gateway.getBaseProp(BASE_ID)).thenReturn(new com.gamebird.game.bird.model.dbo.BaseProp() {{ setId(BASE_ID); }});
        when(gateway.getBaseTrain(BASE_ID)).thenReturn(new com.gamebird.game.bird.model.dbo.BaseTrain() {{ setId(BASE_ID); }});
        when(gateway.getBaseTrap(BASE_ID)).thenReturn(new com.gamebird.game.bird.model.dbo.BaseTrap() {{ setId(BASE_ID); }});

        return new RolePackAssembler(gateway)
                .assemble(sourcePage(1L, 1L), kind)
                .getRecords().get(0);
    }

    /**
     * 用反射找出 VO 上唯一被填入非null 值的配置字段。
     *
     * <p>之所以用反射而不是逐个 {@code assertNotNull}：后者在「填错字段」时只会
     * 报出「baseBait 为 null」，看不出「它其实填进了 baseCage」；而这正是要防的 bug。
     *
     * @param view 装配后的视图对象
     * @return 被填充的字段名；一个都没填时返回 {@code null}
     */
    private static String whichConfigFieldIsFilled(RolePackVO view) {
        String found = null;
        for (Method method : RolePackVO.class.getMethods()) {
            // 只认6 个配置 getter；getBaseId 之类的普通字段不在此列
            if (!CONFIG_GETTERS.contains(method.getName()) || method.getParameterCount() != 0) {
                continue;
            }
            Object value = ReflectionUtils.invokeMethod(method, view);
            if (value != null) {
                if (found != null) {
                    return "多个字段(" + found + " 与 " + method.getName() + ")";
                }
                found = method.getName();
            }
        }
        return found;
    }

    /**
     * 每种物品「应该」被填入的字段。
     *
     * @param kind 物品种类
     * @return 期望的 getter 名
     */
    private static String expectedField(RolePackAssembler.PackItemKind kind) {
        return switch (kind) {
            case BAIT -> "getBaseBait";
            case TRAP -> "getBaseTrap";
            case CAGE -> "getBaseCage";
            case NEST -> "getBaseNest";
            case TRAIN -> "getBaseTrain";
            case PROP -> "getBaseProp";
        };
    }
}