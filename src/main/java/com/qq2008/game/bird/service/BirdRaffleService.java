package com.qq2008.game.bird.service;

import com.qq2008.game.bird.data.GameConfigGateway;
import com.qq2008.game.bird.model.dbo.BaseBird;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 按权重随机抽取一只可捕捉的小鸟。
 *
 * <p>抽离自 {@code TrapController#randBird}，目的是让这段带随机性的算法
 * 能在没有 Spring 容器的情况下被单元测试覆盖。
 */
@Service
public class BirdRaffleService {

    private final GameConfigGateway configGateway;

    /**
     * 构造注入。
     *
     * @param configGateway 配置读取入口
     */
    public BirdRaffleService(GameConfigGateway configGateway) {
        this.configGateway = configGateway;
    }

    /**
     * 按各小鸟配置的概率做加权随机抽取。
     *
     * <p>概率 ≤ 0 的配置不参与抽奖（与原实现一致）。
     *
     * @param birdIdList 小鸟配置 id 列表，逗号分隔的字符串数组；可为 null
     * @return 抽中的小鸟配置；候选为空或全部概率 ≤ 0 时返回 null
     */
    public BaseBird randBird(String[] birdIdList) {
        // 无候选直接返回，避免除以零与空指针
        if (birdIdList == null || birdIdList.length == 0) {
            return null;
        }
        // 收集有效候选并累加总权重
        List<BaseBird> candidates = new ArrayList<>();
        int totalRatio = 0;
        for (String birdId : birdIdList) {
            BaseBird baseBird = configGateway.getBaseBird(Integer.valueOf(birdId.trim()));
            if (baseBird != null && baseBird.getProbability() > 0) {
                candidates.add(baseBird);
                totalRatio += baseBird.getProbability();
            }
        }
        // 全部候选概率为 0 时无票可抽
        if (totalRatio <= 0) {
            return null;
        }
        // 在 [1, totalRatio] 区间取随机数，按累计权重定位
        int randRatio = (int) (Math.random() * totalRatio + 1);
        int accumulated = 0;
        for (BaseBird bird : candidates) {
            accumulated += bird.getProbability();
            if (randRatio <= accumulated) {
                return bird;
            }
        }
        // 理论上不可达（randRatio ≤ totalRatio 恒成立），保留兜底
        return null;
    }
}