package com.gamebird.game.bird.service;

import com.gamebird.common.entity.BizException;
import com.gamebird.game.bird.config.GameConfigGateway;
import com.gamebird.game.bird.config.GameProperties;
import com.gamebird.game.bird.model.dbo.BaseBird;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.RoleTrap;
import com.gamebird.game.bird.model.vo.TrapResultVO;
import com.gamebird.game.bird.service.impl.TrapServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 收网结算门禁：逃脱分支、仓库上限拒绝、正常捕获与陷阱耐久。
 */
class TrapServiceImplTest {

    private TrapServiceImpl service;
    private GameConfigGateway configGateway;
    private GameProperties gameProperties;
    private RoleTrapService trapService;
    private StorageBirdService storageBirdService;
    private RoleService roleService;
    private RoleValueService roleValueService;
    private RoleCageService cageService;
    private GameEventService gameEventService;
    private TaskTriggerService taskTriggerService;

    private static final String ROLE_ID = "r1";

    @BeforeEach
    void setUp() {
        service = new TrapServiceImpl();
        configGateway = mock(GameConfigGateway.class);
        gameProperties = new GameProperties();
        gameProperties.setBirdStorageCapacity(50);
        trapService = mock(RoleTrapService.class);
        storageBirdService = mock(StorageBirdService.class);
        roleService = mock(RoleService.class);
        roleValueService = mock(RoleValueService.class);
        cageService = mock(RoleCageService.class);
        gameEventService = mock(GameEventService.class);
        taskTriggerService = mock(TaskTriggerService.class);

        service.configGateway = configGateway;
        service.gameProperties = gameProperties;
        service.trapService = trapService;
        service.storageBirdService = storageBirdService;
        service.roleService = roleService;
        service.roleValueService = roleValueService;
        service.cageService = cageService;
        service.gameEventService = gameEventService;
        service.taskTriggerService = taskTriggerService;
        service.birdRaffleService = mock(BirdRaffleService.class);
        service.packService = mock(RolePackService.class);
    }

    private Role role() {
        Role role = new Role();
        role.setRoleId(ROLE_ID);
        role.setLevel(1);
        role.setBirdNum((short) 0);
        return role;
    }

    private BaseBird baseBird() {
        BaseBird bird = new BaseBird();
        bird.setId(1);
        bird.setName("麻雀");
        bird.setCatchExp(100);
        bird.setMinWeight(100);
        bird.setMaxWeight(500);
        return bird;
    }

    private RoleTrap slot(byte star) {
        RoleTrap slot = new RoleTrap();
        slot.setId(9);
        slot.setRoleId(ROLE_ID);
        slot.setBaitId(34);
        slot.setCatchBirdId(1);
        slot.setCatchBirdSex((byte) 1);
        slot.setCatchBirdWeight(300L);
        slot.setCatchBirdStar(star);
        slot.setUseTrapId(2);
        slot.setUseTrapNum(5);
        return slot;
    }

    @Test
    @DisplayName("逃脱结算：负星标记 → 无鸟入仓、一半经验、槽位清空、耐久照扣")
    void escapedSettle() {
        when(trapService.getById(9)).thenReturn(slot((byte) -2)); // 将逃脱的 1 星鸟
        when(configGateway.getBaseBird(1)).thenReturn(baseBird());
        when(cageService.getOne(any())).thenReturn(null);

        TrapResultVO result = service.over(role(), 9);

        assertTrue(result.isEscaped());
        assertEquals(0, result.getBirdId());
        assertEquals(50L, result.getAddExp());
        ArgumentCaptor<RoleTrap> captor = ArgumentCaptor.forClass(RoleTrap.class);
        verify(trapService).updateById(captor.capture());
        assertEquals(0, captor.getValue().getBaitId());
        assertEquals(4, captor.getValue().getUseTrapNum());
        verify(storageBirdService, never()).save(any());
    }

    @Test
    @DisplayName("仓库已满：拒绝收网并保留捕捉状态（抛业务异常）")
    void storageFullRejected() {
        when(trapService.getById(9)).thenReturn(slot((byte) 2));
        when(configGateway.getBaseBird(1)).thenReturn(baseBird());
        when(storageBirdService.count(any())).thenReturn(50L);

        try {
            service.over(role(), 9);
            fail("应当抛出仓库已满的业务异常");
        } catch (BizException e) {
            assertTrue(e.getMessage().contains("仓库已满"));
        }
        // 槽位未被清空：玩家出售后还能回来收
        verify(trapService, never()).updateById(any());
    }

    @Test
    @DisplayName("正常捕获：产出入仓、经验发放、鸟数+1、事件与任务推进")
    void normalCatch() {
        when(trapService.getById(9)).thenReturn(slot((byte) 2));
        when(configGateway.getBaseBird(1)).thenReturn(baseBird());
        when(configGateway.getBaseLevel(any())).thenReturn(null);
        when(storageBirdService.count(any())).thenReturn(10L);
        when(cageService.getOne(any())).thenReturn(null);
        // 模拟 MyBatis-Plus 落库回填自增主键
        org.mockito.Mockito.doAnswer(invocation -> {
            com.gamebird.game.bird.model.dbo.StorageBird bird = invocation.getArgument(0);
            bird.setId(77);
            return true;
        }).when(storageBirdService).save(any());

        Role role = role();
        TrapResultVO result = service.over(role, 9);

        assertFalse(result.isEscaped());
        assertTrue(result.getBirdId() > 0);
        assertEquals(100L, result.getAddExp());
        assertEquals((short) 1, role.getBirdNum());
        ArgumentCaptor<com.gamebird.game.bird.model.dbo.StorageBird> birdCaptor =
                ArgumentCaptor.forClass(com.gamebird.game.bird.model.dbo.StorageBird.class);
        verify(storageBirdService).save(birdCaptor.capture());
        // 成长值随机化（500~1500），不再写死 1000
        assertTrue(birdCaptor.getValue().getGrow() >= 500 && birdCaptor.getValue().getGrow() <= 1500);
        verify(gameEventService).record(any(), any());
        verify(taskTriggerService).onCatch(role);
    }

    @Test
    @DisplayName("空闲陷阱不能收网")
    void idleSlotRejected() {
        RoleTrap idle = slot((byte) 2);
        idle.setBaitId(0);
        when(trapService.getById(9)).thenReturn(idle);
        try {
            service.over(role(), 9);
            fail("应当抛出业务异常");
        } catch (BizException e) {
            assertTrue(e.getMessage().contains("空闲"));
        }
    }
}
