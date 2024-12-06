package com.qq2008.game.bird.service;

import com.qq2008.game.bird.model.dbo.StorageBird;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 小鸟数据表 服务类
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
public interface IStorageBirdService extends IService<StorageBird> {

    public int insertStorageBird(StorageBird storageBird);

}
