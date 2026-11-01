package com.gamebird.game.bird.service;

import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 任务触发器门禁：登录造任务、捕捉/购买推进任务、旁路失败不抛出。
 */
class TaskTriggerServiceTest {

    private TaskTriggerService service;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        service = new TaskTriggerService();
        taskService = mock(TaskService.class);
        service.taskService = taskService;
    }

    private Role role() {
        Role role = new Role();
        role.setRoleId("r1");
        return role;
    }

    @Test
    @DisplayName("当天首次登录：创建已完成未领的每日任务，奖励 500")
    void onLoginCreatesCompletedTask() {
        when(taskService.count(any())).thenReturn(0L);
        service.onLogin(role());
        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskService).save(captor.capture());
        Task task = captor.getValue();
        assertEquals(TaskTriggerService.TASK_LOGIN, task.getTaskType());
        assertEquals(1, task.getStatus());
        assertEquals(500L, task.getRewardCoin());
        assertNotNull(task.getFinishTime());
    }

    @Test
    @DisplayName("当天已有登录任务：不再重复创建")
    void onLoginIsIdempotentPerDay() {
        when(taskService.count(any())).thenReturn(1L);
        service.onLogin(role());
        verify(taskService, never()).save(any());
    }

    @Test
    @DisplayName("捕捉触发：最早一条进行中的捕捉任务被点亮")
    void onCatchCompletesFirstInProgress() {
        Task inProgress = new Task();
        inProgress.setId(7);
        inProgress.setStatus(0);
        inProgress.setRewardCoin(0L);
        when(taskService.getOne(any())).thenReturn(inProgress);
        service.onCatch(role());
        assertEquals(1, inProgress.getStatus());
        assertEquals(TaskTriggerService.REWARD_CATCH, inProgress.getRewardCoin());
        verify(taskService).updateById(inProgress);
    }

    @Test
    @DisplayName("购买触发：没有进行中的任务时静默忽略")
    void onPurchaseWithoutTaskIsSilent() {
        when(taskService.getOne(any())).thenReturn(null);
        service.onPurchase(role());
        verify(taskService, never()).updateById(any());
    }

    @Test
    @DisplayName("任务旁路抛异常不向外传播")
    void triggerFailureIsSwallowed() {
        when(taskService.count(any())).thenThrow(new RuntimeException("db down"));
        service.onLogin(role());
        // 不抛出即通过
    }
}
