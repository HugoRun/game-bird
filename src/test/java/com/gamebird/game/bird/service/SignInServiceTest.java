package com.gamebird.game.bird.service;

import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.SignLog;
import com.gamebird.game.bird.service.impl.SignInService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 每日签到门禁：首签/连签/封顶/断签/重复签与奖励发放。
 */
class SignInServiceTest {

    private SignInService service;
    private SignLogService signLogService;
    private RoleService roleService;

    /** 东八区 yyyyMMdd。 */
    private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    /** 测试桩：固定 getByDay/latest 的返回，绕开真实查询。 */
    private SignInService stubbed(SignLog todayLog, SignLog latestLog) {
        return new SignInService() {
            @Override
            public SignLog getByDay(String roleId, String day) {
                return todayLog;
            }

            @Override
            public SignLog latest(String roleId) {
                return latestLog;
            }
        };
    }

    @BeforeEach
    void setUp() {
        service = new SignInService();
        signLogService = mock(SignLogService.class);
        roleService = mock(RoleService.class);
        service.signLogService = signLogService;
        service.roleService = roleService;
    }

    private Role role() {
        Role role = new Role();
        role.setRoleId("r1");
        role.setCoin(0L);
        role.setDiamond(0);
        return role;
    }

    private SignLog logOf(String day, int continuous) {
        SignLog log = new SignLog();
        log.setSignDate(day);
        log.setContinuous(continuous);
        return log;
    }

    @Test
    @DisplayName("首次签到：连签1天，奖励100金币，无钻石")
    void firstSign() {
        SignInService stub = stubbed(null, null);
        stub.signLogService = signLogService;
        stub.roleService = roleService;
        Role role = role();
        String text = stub.sign(role);
        assertEquals("签到成功！连签1天，获得金币100。", text);
        assertEquals(100L, role.getCoin());
        assertEquals(0, role.getDiamond());
        verify(roleService).updateById(role);
    }

    @Test
    @DisplayName("昨天签过：连签+1；第3天赠5钻石")
    void consecutiveThirdDay() {
        String yesterday = LocalDate.now(ZONE).minusDays(1).format(DAY);
        SignInService stub = stubbed(null, logOf(yesterday, 2));
        stub.signLogService = signLogService;
        stub.roleService = roleService;
        Role role = role();
        String text = stub.sign(role);
        assertEquals("签到成功！连签3天，获得金币300、钻石5。", text);
        assertEquals(5, role.getDiamond());
    }

    @Test
    @DisplayName("连签封顶：超过7天仍只发700金币；第7天赠20钻石")
    void coinCapAndSeventhDayDiamond() {
        String yesterday = LocalDate.now(ZONE).minusDays(1).format(DAY);
        SignInService stub = stubbed(null, logOf(yesterday, 6));
        stub.signLogService = signLogService;
        stub.roleService = roleService;
        Role role = role();
        assertEquals("签到成功！连签7天，获得金币700、钻石20。", stub.sign(role));
        assertEquals(700L, role.getCoin());
        // 更长的连签也不再增加
        Role role2 = role();
        SignInService stub2 = stubbed(null, logOf(yesterday, 20));
        stub2.signLogService = signLogService;
        stub2.roleService = roleService;
        assertEquals("签到成功！连签21天，获得金币700。", stub2.sign(role2));
        assertEquals(700L, role2.getCoin());
        assertEquals(0, role2.getDiamond());
    }

    @Test
    @DisplayName("断签：最近一次签到不是昨天，连签重置为1")
    void brokenStreak() {
        String longAgo = LocalDate.now(ZONE).minusDays(5).format(DAY);
        SignInService stub = stubbed(null, logOf(longAgo, 5));
        stub.signLogService = signLogService;
        stub.roleService = roleService;
        Role role = role();
        assertEquals("签到成功！连签1天，获得金币100。", stub.sign(role));
    }

    @Test
    @DisplayName("重复签到：返回 null，不写库不发奖")
    void twiceADayRejected() {
        String today = LocalDate.now(ZONE).format(DAY);
        SignInService stub = stubbed(logOf(today, 3), logOf(today, 3));
        stub.signLogService = signLogService;
        stub.roleService = roleService;
        Role role = role();
        assertNull(stub.sign(role));
        verify(signLogService, never()).save(any());
        verify(roleService, never()).updateById(any());
    }

    @Test
    @DisplayName("签到记录落库：奖励明细随记录保存")
    void signLogPersisted() {
        SignInService stub = stubbed(null, null);
        stub.signLogService = signLogService;
        stub.roleService = roleService;
        stub.sign(role());
        ArgumentCaptor<SignLog> captor = ArgumentCaptor.forClass(SignLog.class);
        verify(signLogService).save(captor.capture());
        SignLog log = captor.getValue();
        assertEquals(1, log.getContinuous());
        assertEquals(100L, log.getRewardCoin());
        assertEquals(0, log.getRewardDiamond());
    }
}
