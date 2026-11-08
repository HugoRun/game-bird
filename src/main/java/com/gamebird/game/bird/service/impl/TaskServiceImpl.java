package com.gamebird.game.bird.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import com.gamebird.game.bird.mapper.TaskMapper;
import com.gamebird.game.bird.model.dbo.Task;
import com.gamebird.game.bird.service.TaskService;

/**
 * <p>
 * 任务表 服务实现类
 * </p>
 */
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

}
