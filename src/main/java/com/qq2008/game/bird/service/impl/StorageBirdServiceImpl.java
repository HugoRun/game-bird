package com.qq2008.game.bird.service.impl;

import com.qq2008.game.bird.model.dbo.StorageBird;
import com.qq2008.game.bird.mapper.StorageBirdMapper;
import com.qq2008.game.bird.service.IStorageBirdService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 小鸟数据表 服务实现类
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@Service
public class StorageBirdServiceImpl extends ServiceImpl<StorageBirdMapper, StorageBird> implements IStorageBirdService {

    @Override
    public int insertStorageBird(StorageBird storageBird) {
        // 插入用户数据
        boolean saved = this.save(storageBird);
        if (saved) {
            // 插入成功后，user 对象中的 id 会被自动填充为自增主键值
            return storageBird.getId();
        }
        return 0; // 或者抛出异常
    }
}
