package com.qq2008.game.bird.service.impl;

import com.qq2008.game.bird.model.dbo.Account;
import com.qq2008.game.bird.mapper.AccountMapper;
import com.qq2008.game.bird.service.IAccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 账号表 服务实现类
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements IAccountService {

}
