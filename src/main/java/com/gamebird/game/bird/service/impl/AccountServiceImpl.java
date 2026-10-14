package com.gamebird.game.bird.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import com.gamebird.game.bird.mapper.AccountMapper;
import com.gamebird.game.bird.model.dbo.Account;
import com.gamebird.game.bird.service.AccountService;

/**
 * <p>
 * 账号表 服务实现类
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

}
