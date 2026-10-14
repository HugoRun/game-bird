package com.gamebird.game.bird.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.gamebird.game.bird.model.dbo.BaseBait;
import com.gamebird.game.bird.model.dbo.BaseBird;
import com.gamebird.game.bird.model.dbo.BaseCage;
import com.gamebird.game.bird.model.dbo.BaseField;
import com.gamebird.game.bird.model.dbo.BaseGene;
import com.gamebird.game.bird.model.dbo.BaseLevel;
import com.gamebird.game.bird.model.dbo.BaseNest;
import com.gamebird.game.bird.model.dbo.BaseProp;
import com.gamebird.game.bird.model.dbo.BaseTitle;
import com.gamebird.game.bird.model.dbo.BaseTrain;
import com.gamebird.game.bird.model.dbo.BaseTrap;
import com.gamebird.game.bird.service.BaseBaitService;
import com.gamebird.game.bird.service.BaseBirdService;
import com.gamebird.game.bird.service.BaseCageService;
import com.gamebird.game.bird.service.BaseFieldService;
import com.gamebird.game.bird.service.BaseGeneService;
import com.gamebird.game.bird.service.BaseLevelService;
import com.gamebird.game.bird.service.BaseNestService;
import com.gamebird.game.bird.service.BasePropService;
import com.gamebird.game.bird.service.BaseTitleService;
import com.gamebird.game.bird.service.BaseTrainService;
import com.gamebird.game.bird.service.BaseTrapService;

/**
 * 游戏配置管理器
 * todo 使用ehcache存储
 */
@Component
public class GameConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(GameConfigManager.class);

    // 诱饵配置
    private static ConcurrentHashMap<Integer, BaseBait> mBaitCache;
    // 小鸟配置
    private static ConcurrentHashMap<Integer, BaseBird> mBirdCache;
    // 鸟笼配置
    private static ConcurrentHashMap<Integer, BaseCage> mCageCache;
    // 场景配置
    private static ConcurrentHashMap<Integer, BaseField> mFieldCache;
    // 基因配置
    private static ConcurrentHashMap<Integer, BaseGene> mGeneCache;
    // 等级配置
    private static ConcurrentHashMap<Integer, BaseLevel> mLevelCache;
    // 鸟巢配置
    private static ConcurrentHashMap<Integer, BaseNest> mNestCache;
    // 道具配置
    private static ConcurrentHashMap<Integer, BaseProp> mPropCache;
    // 训练场配置
    private static ConcurrentHashMap<Integer, BaseTrain> mTrainCache;
    // 陷阱配置
    private static ConcurrentHashMap<Integer, BaseTrap> mTrapCache;
    // 称号配置
    private static ConcurrentHashMap<Integer, BaseTitle> mTitleCache;

    //
    @Resource
    public BaseBaitService baseBaitService;
    @Resource
    public BaseBirdService baseBirdService;
    @Resource
    public BaseCageService baseCageService;
    @Resource
    public BaseFieldService baseFieldService;
    @Resource
    public BaseGeneService baseGeneService;
    @Resource
    public BaseLevelService baseLevelService;
    @Resource
    public BaseNestService baseNestService;
    @Resource
    public BasePropService basePropService;
    @Resource
    public BaseTrainService baseTrainService;
    @Resource
    public BaseTrapService baseTrapService;
    @Resource
    public BaseTitleService baseTitleService;

    /**
     * 计算人物等级
     * @param exp 经验
     * @return 等级
     */
    public static short calcLevel(Integer exp) {
        return (short) (exp / 100);
    }

    /**
     * 加载全部游戏配置。
     *
     * <p>改造前是static 方法，内部靠 {@code getInstance()} 反查 Spring 容器，
     * 这使配置层无法脱离容器测试。现在改为实例方法，由 {@code ServerStartedListener}
     * 直接注入调用。
     */
    public void init() {
        loadBaseBait();
        loadBaseBird();
        loadBaseCage();
        loadBaseField();
        loadBaseGene();
        loadBaseLevel();
        loadBaseNest();
        loadBaseProp();
        loadBaseTrain();
        loadBaseTrap();
        loadBaseTitle();
    }

    /**
     * 加载诱饵配置
     */
    public void loadBaseBait() {
        logger.info("开始加载诱饵配置...");
        mBaitCache = new ConcurrentHashMap<>();
        List<BaseBait> baseBaitList = baseBaitService.list();
        baseBaitList.forEach(baseBait -> mBaitCache.put(baseBait.getId(), baseBait));
        logger.info("诱饵配置加载完成，共{}条。", mBaitCache.size());
    }

    /**
     * 获取诱饵配置列表
     */
    public List<BaseBait> getBaseBaitList() {
        return mBaitCache.values().stream().toList();
    }

    /**
     * 获取诱饵配置
     * @param id 诱饵配置Id
     * @return 诱饵配置
     */
    public BaseBait getBaseBait(Integer id) {
        return mBaitCache.get(id);
    }

    /**
     * 加载小鸟配置
     */
    public void loadBaseBird() {
        logger.info("开始加载小鸟配置...");
        mBirdCache = new ConcurrentHashMap<>();
        List<BaseBird> baseBirdList = baseBirdService.list();
        baseBirdList.forEach(baseBird -> mBirdCache.put(baseBird.getId(), baseBird));
        logger.info("小鸟配置加载完成，共{}条。", mBirdCache.size());
    }

    /**
     * 获取小鸟配置列表
     */
    public List<BaseBird> getBaseBirdList() {
        return mBirdCache.values().stream().toList();
    }

    /**
     * 获取小鸟配置
     * @param id 小鸟配置Id
     * @return 小鸟配置
     */
    public BaseBird getBaseBird(Integer id) {
        return mBirdCache.get(id);
    }

    /**
     * 加载鸟笼配置
     */
    public void loadBaseCage() {
        logger.info("开始加载鸟笼配置...");
        mCageCache = new ConcurrentHashMap<>();
        List<BaseCage> baseCageList = baseCageService.list();
        baseCageList.forEach(baseCage -> mCageCache.put(baseCage.getId(), baseCage));
        logger.info("鸟笼配置加载完成，共{}条。", mCageCache.size());
    }

    /**
     * 获取鸟笼配置列表
     */
    public List<BaseCage> getBaseCageList() {
        return mCageCache.values().stream().toList();
    }

    /**
     * 获取鸟笼配置
     * @param id 鸟笼配置Id
     * @return 鸟笼配置
     */
    public BaseCage getBaseCage(Integer id) {
        return mCageCache.get(id);
    }

    /**
     * 加载场景配置
     */
    public void loadBaseField() {
        logger.info("开始加载场景配置...");
        mFieldCache = new ConcurrentHashMap<>();
        List<BaseField> baseFieldList = baseFieldService.list();
        baseFieldList.forEach(baseField -> mFieldCache.put(baseField.getId().intValue(), baseField));
        logger.info("场景配置加载完成，共{}条。", mFieldCache.size());
    }

    /**
     * 获取场景配置列表
     */
    public List<BaseField> getBaseFieldList() {
        return mFieldCache.values().stream().toList();
    }

    /**
     * 获取场景配置
     * @param id 场景配置Id
     * @return 场景配置
     */
    public BaseField getBaseField(Integer id) {
        return mFieldCache.get(id);
    }

    /**
     * 加载基因配置
     */
    public void loadBaseGene() {
        logger.info("开始加载基因配置...");
        mGeneCache = new ConcurrentHashMap<>();
        List<BaseGene> baseGeneList = baseGeneService.list();
        baseGeneList.forEach(baseGene -> mGeneCache.put(baseGene.getId(), baseGene));
        logger.info("基因配置加载完成，共{}条。", mGeneCache.size());
    }

    /**
     * 获取基因配置列表
     */
    public List<BaseGene> getBaseGeneList() {
        return mGeneCache.values().stream().toList();
    }

    /**
     * 获取基因配置
     * @param id 基因配置Id
     * @return 基因配置
     */
    public BaseGene getBaseGene(Integer id) {
        return mGeneCache.get(id);
    }

    /**
     * 加载等级配置
     */
    public void loadBaseLevel() {
        logger.info("开始加载等级配置...");
        mLevelCache = new ConcurrentHashMap<>();
        List<BaseLevel> baseLevelList = baseLevelService.list();
        baseLevelList.forEach(baseLevel -> mLevelCache.put(baseLevel.getLevel().intValue(), baseLevel));
        logger.info("等级配置加载完成，共{}条。", mLevelCache.size());
    }

    /**
     * 获取等级配置列表
     */
    public List<BaseLevel> getBaseLevelList() {
        return mLevelCache.values().stream().toList();
    }

    /**
     * 获取等级配置
     * @param level 等级
     * @return 等级配置
     */
    public BaseLevel getBaseLevel(Integer level) {
        return mLevelCache.get(level);
    }

    /**
     * 加载鸟巢配置
     */
    public void loadBaseNest() {
        logger.info("开始加载鸟巢配置...");
        mNestCache = new ConcurrentHashMap<>();
        List<BaseNest> baseNestList = baseNestService.list();
        baseNestList.forEach(baseNest -> mNestCache.put(baseNest.getId(), baseNest));
        logger.info("鸟巢配置加载完成，共{}条。", mNestCache.size());
    }

    /**
     * 获取鸟巢配置列表
     */
    public List<BaseNest> getBaseNestList() {
        return mNestCache.values().stream().toList();
    }

    /**
     * 获取鸟巢配置
     * @param id 鸟巢配置Id
     * @return 鸟巢配置
     */
    public BaseNest getBaseNest(Integer id) {
        return mNestCache.get(id);
    }

    /**
     * 加载道具配置
     */
    public void loadBaseProp() {
        logger.info("开始加载道具配置...");
        mPropCache = new ConcurrentHashMap<>();
        List<BaseProp> basePropList = basePropService.list();
        basePropList.forEach(baseProp -> mPropCache.put(baseProp.getId(), baseProp));
        logger.info("道具配置加载完成，共{}条。", mPropCache.size());
    }

    /**
     * 获取道具配置列表
     */
    public List<BaseProp> getBasePropList() {
        return mPropCache.values().stream().toList();
    }

    /**
     * 获取道具配置
     * @param id 道具配置Id
     * @return 道具配置
     */
    public BaseProp getBaseProp(Integer id) {
        return mPropCache.get(id);
    }

    /**
     * 加载称号配置
     */
    public void loadBaseTitle() {
        logger.info("开始加载称号配置...");
        mTitleCache = new ConcurrentHashMap<>();
        List<BaseTitle> baseTitleList = baseTitleService.list();
        baseTitleList.forEach(baseTitle -> mTitleCache.put(baseTitle.getId(), baseTitle));
        logger.info("称号配置加载完成，共{}条。", mTitleCache.size());
    }

    /**
     * 获取称号配置列表
     */
    public List<BaseTitle> getBaseTitleList() {
        return mTitleCache.values().stream().toList();
    }

    /**
     * 获取陷阱配置
     * @param id 陷阱配置Id
     * @return 陷阱配置
     */
    public BaseTitle getBaseTitle(Integer id) {
        return mTitleCache.get(id);
    }

    /**
     * 加载训练场配置
     */
    public void loadBaseTrain() {
        logger.info("开始加载训练场配置...");
        mTrainCache = new ConcurrentHashMap<>();
        List<BaseTrain> baseTrainList = baseTrainService.list();
        baseTrainList.forEach(baseTrain -> mTrainCache.put(baseTrain.getId(), baseTrain));
        logger.info("训练场配置加载完成，共{}条。", mTrainCache.size());
    }

    /**
     * 获取训练场配置列表
     */
    public List<BaseTrain> getBaseTrainList() {
        return mTrainCache.values().stream().toList();
    }

    /**
     * 获取训练场配置
     * @param id 训练场配置Id
     * @return 训练场配置
     */
    public BaseTrain getBaseTrain(Integer id) {
        return mTrainCache.get(id);
    }

    /**
     * 加载陷阱配置
     */
    public void loadBaseTrap() {
        logger.info("开始加载陷阱配置...");
        mTrapCache = new ConcurrentHashMap<>();
        List<BaseTrap> baseTrapList = baseTrapService.list();
        baseTrapList.forEach(baseTrap -> mTrapCache.put(baseTrap.getId(), baseTrap));
        logger.info("陷阱配置加载完成，共{}条。", mTrapCache.size());
    }

    /**
     * 获取陷阱配置列表
     */
    public List<BaseTrap> getBaseTrapList() {
        return mTrapCache.values().stream().toList();
    }

    /**
     * 获取陷阱配置
     * @param id 陷阱配置Id
     * @return 陷阱配置
     */
    public BaseTrap getBaseTrap(Integer id) {
        return mTrapCache.get(id);
    }

}
