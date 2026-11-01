package com.gamebird.game.bird.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.game.bird.model.dbo.StorageCollect;
import com.gamebird.game.bird.service.StorageCollectService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 图鉴收集（storage_collect）。
 *
 * <p>收网捕获 / 孵化产出 / 试管孵化首次遇到某个小鸟配置时，往 storage_collect
 * 写一条「曾拥有」记录——出售、放生不会删除图鉴，形成永久的收集进度。
 *
 * <p><b>id 分配</b>：storage_collect 的主键不是自增列（红线 schema），因此用
 * {@code MAX(id)+1} 手工分配；游戏并发量低，事务内取号足够安全。
 */
@Service
public class CollectionService {

    @Resource
    public StorageCollectService collectService;

    /**
     * 记录一次物种收集（已收集过则幂等跳过）。
     *
     * @return true 表示这是首次收集（可用于统计与成就）
     */
    public boolean record(String roleId, Integer birdBaseId) {
        if (roleId == null || birdBaseId == null || birdBaseId <= 0) {
            return false;
        }
        LambdaQueryWrapper<StorageCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StorageCollect::getRoleId, roleId);
        wrapper.eq(StorageCollect::getBirdId, birdBaseId);
        if (collectService.count(wrapper) > 0) {
            return false;
        }
        StorageCollect row = new StorageCollect();
        row.setId(nextId());
        row.setRoleId(roleId);
        row.setBirdId(birdBaseId);
        row.setIsLock((byte) 0);
        row.setTime((int) (System.currentTimeMillis() / 1000));
        collectService.save(row);
        return true;
    }

    /** 已收集的物种配置 id 集合。 */
    public List<Integer> collectedBirdIds(String roleId) {
        LambdaQueryWrapper<StorageCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StorageCollect::getRoleId, roleId);
        return collectService.list(wrapper).stream().map(StorageCollect::getBirdId).toList();
    }

    /** 已收集物种数。 */
    public long countCollected(String roleId) {
        LambdaQueryWrapper<StorageCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StorageCollect::getRoleId, roleId);
        return collectService.count(wrapper);
    }

    /** 手工分配主键（表非自增）。 */
    private int nextId() {
        StorageCollect max = collectService.getOne(
                new LambdaQueryWrapper<StorageCollect>().orderByDesc(StorageCollect::getId).last("LIMIT 1"));
        return max == null || max.getId() == null ? 1 : max.getId() + 1;
    }
}
