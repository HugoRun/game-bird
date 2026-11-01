package com.gamebird.game.bird.service;

import com.gamebird.game.bird.config.GameConfigGateway;
import com.gamebird.game.bird.model.dbo.BaseProp;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.RolePack;
import com.gamebird.game.bird.model.vo.UseResult;
import com.gamebird.game.bird.service.impl.PackUseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 道具使用分派门禁：金条兑金币、爱心树、礼包解析与归属校验。
 */
class PackUseServiceTest {

    private PackUseService service;
    private GameConfigGateway configGateway;
    private RolePackService packService;
    private RoleValueService roleValueService;
    private RoleService roleService;

    private static final String ROLE_ID = "r1";

    @BeforeEach
    void setUp() {
        service = new PackUseService();
        configGateway = mock(GameConfigGateway.class);
        packService = mock(RolePackService.class);
        roleValueService = mock(RoleValueService.class);
        roleService = mock(RoleService.class);
        service.configGateway = configGateway;
        service.packService = packService;
        service.roleValueService = roleValueService;
        service.roleService = roleService;
        service.gameProperties = new com.gamebird.game.bird.config.GameProperties();
        service.trapService = mock(RoleTrapService.class);
        service.cageService = mock(RoleCageService.class);
        service.birthService = mock(RoleBirthService.class);
        service.trainService = mock(RoleTrainService.class);
        service.storageBirdService = mock(StorageBirdService.class);
    }

    private Role role() {
        Role role = new Role();
        role.setRoleId(ROLE_ID);
        role.setCoin(0L);
        role.setLovePoint(0L);
        return role;
    }

    private RolePack pack(int baseId, int num) {
        RolePack pack = new RolePack();
        pack.setId(34);
        pack.setRoleId(ROLE_ID);
        pack.setTypeId((byte) 6);
        pack.setBaseId(baseId);
        pack.setNum(num);
        return pack;
    }

    private BaseProp prop(int id, int effectType, int effectValue, String awardList) {
        BaseProp prop = new BaseProp();
        prop.setId(id);
        prop.setName("测试道具" + id);
        prop.setEffectType((short) effectType);
        prop.setEffectValue(effectValue);
        prop.setAwardList(awardList);
        return prop;
    }

    @Test
    @DisplayName("金条：兑换金币并落库、扣减道具")
    void goldBarConvertsToCoin() {
        when(packService.getById(34)).thenReturn(pack(29, 2));
        when(configGateway.getBaseProp(29)).thenReturn(prop(29, 13, 20000, ""));
        Role role = role();

        UseResult result = service.useProp(role, 34, null, null, null, null);

        assertTrue(result.success());
        assertTrue(result.text().contains("20000"));
        verify(roleValueService).addRoleCoin(role, 20000L);
        verify(roleService).updateById(role);
        // 扣减 1 张（真实实现会改对象数量，这里验证调用契约）
        verify(roleValueService).costPackNum(org.mockito.ArgumentMatchers.argThat(p -> p != null),
                org.mockito.ArgumentMatchers.eq(1));
        verify(packService).saveOrUpdate(any());
    }

    @Test
    @DisplayName("爱心树：按双倍发放爱心")
    void loveTreeGrantsDoubleLove() {
        when(packService.getById(34)).thenReturn(pack(10, 1));
        when(configGateway.getBaseProp(10)).thenReturn(prop(10, 2, 2, ""));
        Role role = role();

        UseResult result = service.useProp(role, 34, null, null, null, null);

        assertTrue(result.success());
        verify(roleValueService).addRoleLovePoint(role, 4);
    }

    @Test
    @DisplayName("礼包：award_list 逐项发放到包裹")
    void giftPackOpens() {
        when(packService.getById(34)).thenReturn(pack(101, 1));
        when(configGateway.getBaseProp(101)).thenReturn(
                prop(101, 0, 0, "[{\"id\":4,\"num\":2,\"type\":1},{\"id\":17,\"num\":3,\"type\":6}]"));
        when(packService.getOne(any())).thenReturn(null);
        Role role = role();

        UseResult result = service.useProp(role, 34, null, null, null, null);

        assertTrue(result.success());
        assertTrue(result.text().contains("×2"));
        assertTrue(result.text().contains("×3"));
        verify(roleValueService, org.mockito.Mockito.times(2)).addPackNum(any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    @DisplayName("别人的道具不能用：归属校验失败且零写库")
    void foreignPackRejected() {
        RolePack foreign = pack(29, 3);
        foreign.setRoleId("someone-else");
        when(packService.getById(34)).thenReturn(foreign);
        Role role = role();

        UseResult result = service.useProp(role, 34, null, null, null, null);

        assertFalse(result.success());
        verify(roleValueService, never()).addRoleCoin(any(), any());
        verify(packService, never()).saveOrUpdate(any());
    }

    @Test
    @DisplayName("没有 award_list 且未知效果的道具提示未开放")
    void unknownEffectRejected() {
        when(packService.getById(34)).thenReturn(pack(999, 1));
        when(configGateway.getBaseProp(999)).thenReturn(prop(999, 42, 0, ""));
        UseResult result = service.useProp(role(), 34, null, null, null, null);
        assertFalse(result.success());
        assertTrue(result.text().contains("未开放") || result.text().contains("解析失败"));
        verify(packService, never()).saveOrUpdate(any());
    }

    @Test
    @DisplayName("玫瑰/对戒跳转教堂页")
    void roseRedirectsToMarriage() {
        when(packService.getById(34)).thenReturn(pack(112, 5));
        when(configGateway.getBaseProp(112)).thenReturn(prop(112, 26, 1, ""));
        UseResult result = service.useProp(role(), 34, null, null, null, null);
        assertTrue(result.success());
        assertEquals("/marriage", result.redirectPath());
    }
}
