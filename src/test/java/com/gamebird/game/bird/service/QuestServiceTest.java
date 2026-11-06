package com.gamebird.game.bird.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.gamebird.game.bird.model.dbo.QuestDef;
import com.gamebird.game.bird.model.dbo.QuestProgress;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.service.impl.CollectionService;
import com.gamebird.game.bird.service.impl.QuestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 奋进目标引擎门禁：进度推进、自动可交付、前置串联与奖励交付。
 */
class QuestServiceTest {

    private QuestService service;
    private QuestDefService questDefService;
    private QuestProgressService questProgressService;

    @BeforeEach
    void setUp() {
        service = new QuestService();
        questDefService = mock(QuestDefService.class);
        questProgressService = mock(QuestProgressService.class);
        service.questDefService = questDefService;
        service.questProgressService = questProgressService;
        service.packService = mock(RolePackService.class);
        service.roleValueService = mock(RoleValueService.class);
        service.configGateway = mock(com.gamebird.game.bird.config.GameConfigGateway.class);
        service.collectionService = mock(CollectionService.class);
    }

    private Role role() {
        Role role = new Role();
        role.setRoleId("r1");
        role.setLevel(1);
        role.setCoin(0L);
        role.setDiamond(0);
        return role;
    }

    private QuestDef def(int id, int chapter, int eventType, int target, int pre, long coin, int diamond) {
        QuestDef def = new QuestDef();
        def.setId(id);
        def.setTitle("任务" + id);
        def.setChapter(chapter);
        def.setEventType(eventType);
        def.setTargetNum(target);
        def.setPreQuestId(pre);
        def.setRewardCoin(coin);
        def.setRewardDiamond(diamond);
        def.setRewardPropId(0);
        def.setRewardPropNum(0);
        return def;
    }

    private QuestProgress progress(int questId, int value, int status) {
        QuestProgress p = new QuestProgress();
        p.setId(7);
        p.setQuestId(questId);
        p.setProgress(value);
        p.setStatus(status);
        p.setDayDate("");
        return p;
    }

    @Test
    @DisplayName("捕捉事件推进进行中的捕捉任务，达标自动可交付")
    void catchEventAdvancesAndCompletes() {
        QuestDef def = def(201, 2, QuestService.EV_CATCH, 3, 0, 600, 0);
        QuestProgress row = progress(201, 2, 0);
        when(questDefService.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(Collections.singletonList(def));
        when(questProgressService.list(any(Wrapper.class))).thenReturn(Collections.singletonList(row));
        when(questDefService.getById(201)).thenReturn(def);
        when(questProgressService.getOne(any())).thenReturn(null); // 每日补建查询

        service.onEvent(role(), QuestService.EV_CATCH, 1);

        assertEquals(3, row.getProgress());
        assertEquals(1, row.getStatus());
        verify(questProgressService).updateById(row);
    }

    @Test
    @DisplayName("未达目标只推进不置可交付")
    void underTargetStaysInProgress() {
        QuestDef def = def(201, 2, QuestService.EV_CATCH, 3, 0, 600, 0);
        QuestProgress row = progress(201, 0, 0);
        when(questDefService.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(Collections.singletonList(def));
        when(questProgressService.list(any(Wrapper.class))).thenReturn(Collections.singletonList(row));
        when(questDefService.getById(201)).thenReturn(def);
        when(questProgressService.getOne(any())).thenReturn(null);

        service.onEvent(role(), QuestService.EV_CATCH, 1);

        assertEquals(1, row.getProgress());
        assertEquals(0, row.getStatus());
    }

    @Test
    @DisplayName("交付：发金币与钻石、置已交付")
    void deliverGrantsRewards() {
        Role role = role();
        QuestDef def = def(101, 1, QuestService.EV_CATCH, 1, 0, 500, 5);
        QuestProgress row = progress(101, 1, 1);
        when(questDefService.getById(101)).thenReturn(def);
        when(questProgressService.getOne(any())).thenReturn(row);

        String text = service.deliver(role, 101);

        assertTrue(text.contains("500"));
        assertTrue(text.contains("钻石+5"));
        assertEquals(2, row.getStatus());
        verify(service.roleValueService).addRoleCoin(role, 500L);
        verify(service.roleValueService).addRoleDiamond(role, 5);
    }

    @Test
    @DisplayName("未达成不可交付")
    void deliverBlockedWhenNotReady() {
        QuestDef def = def(101, 1, QuestService.EV_CATCH, 1, 0, 500, 0);
        when(questDefService.getById(101)).thenReturn(def);
        when(questProgressService.getOne(any())).thenReturn(progress(101, 0, 0));

        assertNull(service.deliver(role(), 101));
        verify(questProgressService, never()).updateById(any());
    }

    @Test
    @DisplayName("引擎吞异常：定义查询失败不影响玩法主流程")
    void engineFailureSwallowed() {
        when(questDefService.list(any(Wrapper.class))).thenThrow(new RuntimeException("db down"));
        when(questProgressService.list(any(Wrapper.class))).thenThrow(new RuntimeException("db down"));
        service.onEvent(role(), QuestService.EV_CATCH, 1);
        // 不抛出即通过
    }
}
