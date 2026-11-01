package com.gamebird.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.game.bird.config.GameConfigGateway;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.data.ConstData;
import com.gamebird.game.bird.model.dbo.BaseProp;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.RolePack;
import com.gamebird.game.bird.model.dbo.StorageBird;
import com.gamebird.game.bird.service.GameEventService;
import com.gamebird.game.bird.service.RolePackService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 教堂 / 婚姻（role.marriage_type）。
 *
 * <p>求婚需要消耗一枚对戒（base_prop effect_type=27）：易拉罐环(1)→喜鹊之恋、
 * 白银对戒(3)→鸳鸯之恋、黄金对戒(8)→比翼鸟之恋、钻石对戒(20)→天使之恋，
 * 档位映射取 effect_value 的对数档；玫瑰（effect_type=26）作为示好道具直接转化为爱心。
 * 已婚状态重复求婚视为升级，需要更高档位的对戒。离婚收取 1000 金币手续费。
 *
 * <p>此前「结为伴侣」无需任何信物即可随意设置，婚姻四档加成形同虚设。
 */
@RequestMapping("/marriage")
@Controller
public class MarriageController extends BaseController {

    /** 离婚手续费。 */
    public static final long DIVORCE_FEE = 1000;

    @Resource
    public GameConfigGateway configGateway;

    @Resource
    public RolePackService packService;

    @Resource
    public GameEventService gameEventService;

    /** 婚姻状态展示 + 求婚/离婚入口。 */
    @GetMapping(value = {"", "/info"})
    public String info(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        model.addAttribute("marriageType", role.getMarriageType());
        // 包裹里的玫瑰与对戒（求婚材料）
        model.addAttribute("rings", listMarriageProps(role, ConstData.PROP_EFFECT_RING));
        model.addAttribute("roses", listMarriageProps(role, ConstData.PROP_EFFECT_ROSE));
        return "marriage/info";
    }

    /** 求婚：消耗一枚对戒结为伴侣（已婚时为升级，需更高档位）。 */
    @PostMapping(value = "/propose")
    public String propose(HttpSession session, Model model, @RequestParam Integer packId) {
        Role role = getSessionRole(session);
        RolePack pack = packId == null ? null : packService.getById(packId);
        if (pack == null || !role.getRoleId().equals(pack.getRoleId())
                || pack.getTypeId() == null || pack.getTypeId() != ConstData.PACK_TYPE_PROP
                || pack.getNum() == null || pack.getNum() <= 0) {
            model.addAttribute("message", "请先选择一枚对戒。");
            return info(session, model);
        }
        BaseProp ring = configGateway.getBaseProp(pack.getBaseId());
        if (ring == null || ring.getEffectType() == null || ring.getEffectType() != ConstData.PROP_EFFECT_RING
                || ring.getEffectValue() == null) {
            model.addAttribute("message", "那不是对戒，别拿别的来糊弄神父。");
            return info(session, model);
        }
        byte newType = ringTier(ring.getEffectValue());
        byte current = role.getMarriageType() == null ? 0 : role.getMarriageType();
        if (newType <= current) {
            model.addAttribute("message", "这枚对戒配不上你们现在的感情，请换更高档的对戒来升级。");
            return info(session, model);
        }
        // 扣戒 + 更新婚姻
        roleValueService.costPackNum(pack, 1);
        packService.saveOrUpdate(pack);
        role.setMarriageType(newType);
        roleService.updateById(role);
        saveSessionRole(session, role);
        String blessing = switch (newType) {
            case 1 -> "配对重量+1%，配对时间-1%";
            case 2 -> "配对重量+3%，配对时间-3%";
            case 3 -> "配对重量+8%，配对时间-8%";
            default -> "配对重量+20%，配对时间-20%";
        };
        gameEventService.record(role.getRoleId(), "在教堂许下誓言，" + marriageName(newType) + "！（" + blessing + "）");
        model.addAttribute("message", "神父宣布你们结为伴侣！获得永久" + marriageName(newType)
                + "：" + blessing + "。");
        model.addAttribute("marriageType", role.getMarriageType());
        model.addAttribute("rings", listMarriageProps(role, ConstData.PROP_EFFECT_RING));
        model.addAttribute("roses", listMarriageProps(role, ConstData.PROP_EFFECT_ROSE));
        return "marriage/info";
    }

    /** 玫瑰示好：转化为爱心（花瓣越贵转化越多）。 */
    @PostMapping(value = "/rose")
    public String rose(HttpSession session, Model model, @RequestParam Integer packId) {
        Role role = getSessionRole(session);
        RolePack pack = packId == null ? null : packService.getById(packId);
        if (pack == null || !role.getRoleId().equals(pack.getRoleId())) {
            model.addAttribute("message", "请选择一束玫瑰。");
            return info(session, model);
        }
        BaseProp prop = configGateway.getBaseProp(pack.getBaseId());
        if (prop == null || prop.getEffectType() == null || prop.getEffectType() != ConstData.PROP_EFFECT_ROSE
                || prop.getEffectValue() == null) {
            model.addAttribute("message", "那不是玫瑰。");
            return info(session, model);
        }
        int love = prop.getEffectValue() * 2;
        roleValueService.addRoleLovePoint(role, love);
        roleValueService.costPackNum(pack, 1);
        packService.saveOrUpdate(pack);
        roleService.updateById(role);
        saveSessionRole(session, role);
        model.addAttribute("message", "「" + prop.getName() + "」的芬芳让爱心 +" + love + "。");
        model.addAttribute("marriageType", role.getMarriageType());
        model.addAttribute("rings", listMarriageProps(role, ConstData.PROP_EFFECT_RING));
        model.addAttribute("roses", listMarriageProps(role, ConstData.PROP_EFFECT_ROSE));
        return "marriage/info";
    }

    /** 离婚：收取手续费，婚姻清零。 */
    @PostMapping(value = "/divorce")
    public String divorce(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        if (role.getMarriageType() == null || role.getMarriageType() == 0) {
            model.addAttribute("message", "你都还没结婚，离什么婚。");
            return info(session, model);
        }
        if (role.getCoin() == null || role.getCoin() < DIVORCE_FEE) {
            model.addAttribute("message", "离婚需要支付 " + DIVORCE_FEE + " 金币手续费，您的现金不足。");
            return info(session, model);
        }
        roleValueService.costRoleCoin(role, DIVORCE_FEE);
        role.setMarriageType((byte) 0);
        roleService.updateById(role);
        saveSessionRole(session, role);
        gameEventService.record(role.getRoleId(), "支付了" + DIVORCE_FEE + "金币手续费，平静地结束了这段感情。");
        model.addAttribute("message", "手续办完了。往后山高水长，各自珍重。");
        model.addAttribute("marriageType", 0);
        model.addAttribute("rings", listMarriageProps(role, ConstData.PROP_EFFECT_RING));
        model.addAttribute("roses", listMarriageProps(role, ConstData.PROP_EFFECT_ROSE));
        return "marriage/info";
    }

    // ==================== 私有工具 ====================

    /** 对戒档位 → 婚姻类型：effect_value 1/3/8/20 → 1/2/3/4。 */
    private byte ringTier(int effectValue) {
        if (effectValue >= 20) {
            return 4;
        }
        if (effectValue >= 8) {
            return 3;
        }
        if (effectValue >= 3) {
            return 2;
        }
        return 1;
    }

    private String marriageName(byte type) {
        return switch (type) {
            case 1 -> "喜鹊之恋";
            case 2 -> "鸳鸯之恋";
            case 3 -> "比翼鸟之恋";
            case 4 -> "天使之恋";
            default -> "未婚";
        };
    }

    /** 角色包裹中指定婚恋效果的道具列表。 */
    private List<RolePack> listMarriageProps(Role role, int effectType) {
        LambdaQueryWrapper<RolePack> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePack::getRoleId, role.getRoleId());
        wrapper.eq(RolePack::getTypeId, ConstData.PACK_TYPE_PROP);
        wrapper.gt(RolePack::getNum, 0);
        List<RolePack> packs = packService.list(wrapper);
        return packs.stream().filter(p -> {
            BaseProp prop = configGateway.getBaseProp(p.getBaseId());
            return prop != null && prop.getEffectType() != null && prop.getEffectType() == effectType;
        }).toList();
    }
}
