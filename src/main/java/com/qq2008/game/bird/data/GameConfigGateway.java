package com.qq2008.game.bird.data;

import com.qq2008.game.bird.model.dbo.BaseBait;
import com.qq2008.game.bird.model.dbo.BaseBird;
import com.qq2008.game.bird.model.dbo.BaseCage;
import com.qq2008.game.bird.model.dbo.BaseField;
import com.qq2008.game.bird.model.dbo.BaseGene;
import com.qq2008.game.bird.model.dbo.BaseLevel;
import com.qq2008.game.bird.model.dbo.BaseNest;
import com.qq2008.game.bird.model.dbo.BaseProp;
import com.qq2008.game.bird.model.dbo.BaseTitle;
import com.qq2008.game.bird.model.dbo.BaseTrain;
import com.qq2008.game.bird.model.dbo.BaseTrap;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 游戏配置读取的唯一对外入口。
 *
 * <p><b>为什么需要这一层</b>：改造前所有代码都通过
 * {@code GameConfigManager.getInstance()} 静态取配置，而该方法内部依赖
 * {@code GameServer.applicationContext} 这个静态字段。于是任何单元测试都必须先伪造一个
 * Spring 容器才能跑通一行「查道具名」的代码。本类把配置读取收拢到一处并支持构造器注入，
 * 使得 {@code new GameConfigGateway(mockManager)} 即可在无容器环境下工作。
 *
 * <p><b>为什么不是接口</b>：11 种配置各自返回不同的实体类型，没有共同父类型
 * （且不允许给 dbo 加父类，见N14 约束），抽接口只会得到 11 个单方法接口。保持具体类注入即可。
 *
 * <p><b>用法</b>：Controller / Service 构造器注入本类，业务代码不再出现
 * {@code GameConfigManager.getInstance()}。
 */
@Component
public class GameConfigGateway {

    private final GameConfigManager configManager;

    public GameConfigGateway(GameConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * 取诱饵配置。
     *
     * @param id 诱饵配置 id；可为 null
     * @return 诱饵配置，不存在返回 null
     */
    public BaseBait getBaseBait(Integer id) {
        return configManager.getBaseBait(id);
    }

    /**
     * 取小鸟配置。
     *
     * @param id 小鸟配置 id；可为 null
     * @return 小鸟配置，不存在返回 null
     */
    public BaseBird getBaseBird(Integer id) {
        return configManager.getBaseBird(id);
    }

    /**
     * 取鸟笼配置。
     *
     * @param id 鸟笼配置 id；可为 null
     * @return 鸟笼配置，不存在返回 null
     */
    public BaseCage getBaseCage(Integer id) {
        return configManager.getBaseCage(id);
    }

    /**
     * 取场景配置。
     *
     * @param id 场景配置 id；可为 null
     * @return 场景配置，不存在返回 null
     */
    public BaseField getBaseField(Integer id) {
        return configManager.getBaseField(id);
    }

    /**
     * 取基因配置。
     *
     * @param id 基因配置 id；可为 null
     * @return 基因配置，不存在返回 null
     */
    public BaseGene getBaseGene(Integer id) {
        return configManager.getBaseGene(id);
    }

    /**
     * 取等级配置。
     *
     * @param level 等级；可为 null
     * @return 等级配置，不存在返回 null
     */
    public BaseLevel getBaseLevel(Integer level) {
        return configManager.getBaseLevel(level);
    }

    /**
     * 取鸟巢配置。
     *
     * @param id 鸟巢配置 id；可为 null
     * @return 鸟巢配置，不存在返回 null
     */
    public BaseNest getBaseNest(Integer id) {
        return configManager.getBaseNest(id);
    }

    /**
     * 取道具配置。
     *
     * @param id 道具配置 id；可为 null
     * @return 道具配置，不存在返回 null
     */
    public BaseProp getBaseProp(Integer id) {
        return configManager.getBaseProp(id);
    }

    /**
     * 取称号配置。
     *
     * @param id 称号配置 id；可为 null
     * @return 称号配置，不存在返回 null
     */
    public BaseTitle getBaseTitle(Integer id) {
        return configManager.getBaseTitle(id);
    }

    /**
     * 取训练场配置。
     *
     * @param id 训练场配置 id；可为 null
     * @return 训练场配置，不存在返回 null
     */
    public BaseTrain getBaseTrain(Integer id) {
        return configManager.getBaseTrain(id);
    }

    /**
     * 取陷阱配置。
     *
     * @param id 陷阱配置 id；可为 null
     * @return 陷阱配置，不存在返回 null
     */
    public BaseTrap getBaseTrap(Integer id) {
        return configManager.getBaseTrap(id);
    }

    /**
     * 取称号配置列表。
     *
     * @return 称号配置列表；配置未加载时返回空列表而非 null
     */
    public List<BaseTitle> getBaseTitleList() {
        return configManager.getBaseTitleList();
    }
}