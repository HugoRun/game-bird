package com.qq2008.game.bird.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link PathNormalizer} 路径规范化单元测试。
 *
 * <p>★ 本测试的作用是「把设计取舍钉成不可回退的断言」。本类里有六处刻意的
 * 非直觉设计（目录小写但文件基名保留、盘符保持大写、越界抛错不钳制、
 * 非法字符替换但保留名抛错、隐藏文件前导点保留、必须全匹配而非子串匹配），
 * 每一处都容易被后来者「顺手优化」掉，故在此逐条锁死。
 *
 * <p>测试不依赖真实文件系统即可覆盖绝大部分路径逻辑；涉及真实 I/O 的
 * {@link PathOps} 部分见 {@link PathOpsTest}（用 {@code @TempDir}）。
 */
class PathNormalizerTest {

    // ==================== 正常输入 ====================

    /** 普通相对路径原样保留，仅分隔符统一。 */
    @Test
    void 普通相对路径原样保留() {
        assertEquals("a/b/c", PathNormalizer.normalize("a/b/c"));
        assertEquals("images/logo.png", PathNormalizer.normalize("images/logo.png"));
    }

    /** 多级嵌套不报错。 */
    @Test
    void 多级嵌套可用() {
        assertEquals("a/b/c/d/e", PathNormalizer.normalize("a/b/c/d/e"));
    }

    /** 当前目录片段 {@code .} 被消解。 */
    @Test
    void 当前目录片段被消解() {
        assertEquals("a/b", PathNormalizer.normalize("a/./b"));
        assertEquals("b", PathNormalizer.normalize("./b"));
    }

    /** 连续分隔符折叠为一个。 */
    @Test
    void 连续分隔符被折叠() {
        assertEquals("a/b", PathNormalizer.normalize("a//b"));
        assertEquals("a/b", PathNormalizer.normalize("a///b"));
    }

    /** 尾部分隔符被移除，不产生空片段。 */
    @Test
    void 尾部分隔符被移除() {
        assertEquals("a/b", PathNormalizer.normalize("a/b/"));
        assertEquals("a", PathNormalizer.normalize("a/"));
    }

    /** 单一 {@code .} 表示「当前目录」，无片段可输出时抛 {@code NO_SEGMENTS}。 */
    @Test
    void 单独的点抛无片段错误() {
        PathNormalizationException e = assertThrows(PathNormalizationException.class,
                () -> PathNormalizer.normalize("."));
        assertEquals(PathNormalizationException.Reason.NO_SEGMENTS, e.getReason());
    }

    /** 未越界的 {@code ..} 正常消解，不报错。 */
    @Test
    void 未越界的父目录被消解() {
        assertEquals("b", PathNormalizer.normalize("a/../b"));
        assertEquals("a/b", PathNormalizer.normalize("a/b/../b"));
    }

    // ==================== 分隔符混用 ====================

    /** 反斜杠输入被转换为正斜杠。 */
    @Test
    void 反斜杠被转换为正斜杠() {
        assertEquals("a/b", PathNormalizer.normalize("a\\b"));
        assertEquals("a/b/c", PathNormalizer.normalize("a\\b\\c"));
    }

    /**
     * Windows 盘符保持大写，路径分隔符转为正斜杠。
     *
     * <p>★ 只有<b>盘符本身</b>保持大写，盘符之后的目录段仍按
     * {@link PathCasePolicy#DIRECTORY_LOWERED} 转小写，故
     * {@code C:\Users\x} 得 {@code C:/users/x}。
     *
     * <p>这条断言是为了钉死「规则优先于直觉」：若误把 {@code Users} 也保留大小写，
     * 说明大小写策略被写成了特例分支，跨平台一致性即失效。
     */
    @Test
    void 盘符保持大写但其后目录小写() {
        assertEquals("C:/users/x", PathNormalizer.normalize("C:\\Users\\x"));
        assertEquals("D:/data/f.txt", PathNormalizer.normalize("d:/data/f.txt"),
                "小写盘符应被提升为大写，与大写盘符归一");
    }

    /** 盘符后的目录在PRESERVE 策略下不动大小写。 */
    @Test
    void 盘符后目录在保留策略下不动() {
        assertEquals("C:/Users/x",
                PathNormalizer.normalize("C:\\Users\\x", PathCasePolicy.PRESERVE));
    }

    /** UNC 前缀被保留为双正斜杠。 */
    @Test
    void unc前缀被保留() {
        assertEquals("//server/share/x", PathNormalizer.normalize("\\\\server\\share\\x"));
    }

    /** 已是正斜杠的输入零改动（幂等性的基础）。 */
    @Test
    void 正斜杠输入零改动() {
        String input = "a/b/c";
        assertEquals(input, PathNormalizer.normalize(input));
    }

    /** 显式声明「输入无反斜杠」时不应误伤正斜杠。 */
    @Test
    void 显式分隔符参数生效() {
        assertEquals("a/b", PathNormalizer.normalize("a:b",
                PathCasePolicy.PRESERVE, ':'));
    }

    // ==================== 大小写策略 ====================

    /** 默认策略：目录小写、文件基名保留、扩展名小写。 */
    @Test
    void 默认策略目录小写文件基名保留() {
        assertEquals("gamedata/Foo.png", PathNormalizer.normalize("GameData/Foo.PNG"));
    }

    /** 全小写策略：目录、基名、扩展名全部转小写。 */
    @Test
    void 全小写策略() {
        assertEquals("gamedata/foo.png",
                PathNormalizer.normalize("GameData/Foo.PNG", PathCasePolicy.FULL_LOWERED));
    }

    /** 保留策略：只做分隔符统一与片段消解，不动大小写。 */
    @Test
    void 保留策略不动大小写() {
        assertEquals("GameData/Foo.PNG",
                PathNormalizer.normalize("GameData/Foo.PNG", PathCasePolicy.PRESERVE));
    }

    /** 多级目录全部小写，只有末段基名保留大小写。 */
    @Test
    void 多级目录小写末段保留() {
        assertEquals("a/b/c/Readme.md", PathNormalizer.normalize("A/B/C/Readme.md"));
    }

    /** 末段是目录时（末尾带分隔符）也应小写。 */
    @Test
    void 末尾是目录时小写() {
        assertEquals("a/gamedata", PathNormalizer.normalize("A/GameData/"));
    }

    // ==================== 非法字符 ====================

    /** Windows 非法字符被替换为下划线，而不是删除。 */
    @Test
    void 非法字符被替换为下划线() {
        assertEquals("a_b", PathNormalizer.normalize("a<b"));
        assertEquals("a_b", PathNormalizer.normalize("a>b"));
        assertEquals("a_b", PathNormalizer.normalize("a|b"));
        assertEquals("a_b", PathNormalizer.normalize("a?b"));
        assertEquals("a_b", PathNormalizer.normalize("a*b"));
    }

    /**
     * ★ 替换而非删除：删除会让不同输入塌缩成同一路径，
     * 产生意料之外的冲突。这里钉死「a&lt;b 与 ab 不等价」。
     */
    @Test
    void 替换不删除避免路径塌缩() {
        assertFalse(PathNormalizer.normalize("a<b")
                .equals(PathNormalizer.normalize("ab")),
                "a<b 规范化后不得等于 ab");
    }

    /** 控制字符被替换。 */
    @Test
    void 控制字符被替换() {
        assertEquals("a_b", PathNormalizer.normalize("a\u0001b"));
        assertEquals("a_b", PathNormalizer.normalize("a\u007Fb"));
    }

    /** 片段尾部空格被替换——Windows 不允许文件名以空格结尾。 */
    @Test
    void 尾部空格被替换() {
        assertEquals("a_", PathNormalizer.normalize("a "));
    }

    /**
     * ★ 隐藏文件的前导点必须保留，且不产生空基名。
     * 只有<b>整段等于 {@code "."}</b> 的才算冗余片段。
     */
    @Test
    void 隐藏文件前导点被保留() {
        assertEquals(".gitignore", PathNormalizer.normalize(".gitignore"));
        assertEquals("a/.gitignore", PathNormalizer.normalize("a/.gitignore"));
        assertEquals(".env.example", PathNormalizer.normalize(".env.example"));
    }

    // ==================== 错误路径 ====================

    /** null 输入抛 {@code NULL_INPUT}。 */
    @Test
    void null输入抛错() {
        PathNormalizationException e = assertThrows(PathNormalizationException.class,
                () -> PathNormalizer.normalize(null));
        assertEquals(PathNormalizationException.Reason.NULL_INPUT, e.getReason());
    }

    /** 空字符串抛 {@code EMPTY_INPUT}。 */
    @Test
    void 空字符串抛错() {
        PathNormalizationException e = assertThrows(PathNormalizationException.class,
                () -> PathNormalizer.normalize(""));
        assertEquals(PathNormalizationException.Reason.EMPTY_INPUT, e.getReason());
    }

    /** 仅含空白抛 {@code BLANK_INPUT}，与空字符串区分开。 */
    @Test
    void 仅空白抛错() {
        PathNormalizationException e = assertThrows(PathNormalizationException.class,
                () -> PathNormalizer.normalize("   "));
        assertEquals(PathNormalizationException.Reason.BLANK_INPUT, e.getReason());
    }

    /**
     * ★ 越界 {@code ..} 必须抛错，<b>绝不静默钳制到根目录</b>。
     * 钳制会把「越权访问」伪装成「正常访问」，是安全上的坏模式。
     */
    @Test
    void 越界父目录抛错() {
        PathNormalizationException e = assertThrows(PathNormalizationException.class,
                () -> PathNormalizer.normalize("a/../../b"));
        assertEquals(PathNormalizationException.Reason.ESCAPES_ROOT, e.getReason());
    }

    /** 多个越界 {@code ..} 同样抛错。 */
    @Test
    void 连续越界抛错() {
        assertThrows(PathNormalizationException.class,
                () -> PathNormalizer.normalize("../a/../../b"));
    }

    /** Windows 保留设备名抛错。 */
    @ParameterizedTest
    @ValueSource(strings = {"CON", "PRN", "AUX", "NUL", "COM1", "LPT9"})
    void windows保留名抛错(String name) {
        PathNormalizationException e = assertThrows(PathNormalizationException.class,
                () -> PathNormalizer.normalize(name));
        assertEquals(PathNormalizationException.Reason.RESERVED_NAME, e.getReason());
    }

    /** 保留名不区分大小写。 */
    @Test
    void 保留名忽略大小写() {
        assertThrows(PathNormalizationException.class, () -> PathNormalizer.normalize("con"));
        assertThrows(PathNormalizationException.class, () -> PathNormalizer.normalize("NuL"));
    }

    /** 路径中间出现保留名同样抛错。 */
    @Test
    void 路径中间保留名抛错() {
        assertThrows(PathNormalizationException.class, () -> PathNormalizer.normalize("a/CON"));
    }

    /**
     * ★ 边界：只有<b>整段等于保留名</b>才算，{@code CON.txt} 不算。
     * 这是 {@link PathNamingRules#isReservedName} 的有意保守策略。
     */
    @Test
    void 保留名带扩展名不算命中() {
        assertEquals("CON.txt", PathNormalizer.normalize("CON.txt"));
        assertEquals("a/CON.txt", PathNormalizer.normalize("a/CON.txt"));
        assertFalse(PathNamingRules.isReservedName("CON.txt"),
                "整段比对时 CON.txt 不应命中");
    }

    /** 单段超长（256 字符）抛 {@code TOO_LONG}。 */
    @Test
    void 单段超长抛错() {
        String long256 = "x".repeat(256);
        PathNormalizationException e = assertThrows(PathNormalizationException.class,
                () -> PathNormalizer.normalize(long256));
        assertEquals(PathNormalizationException.Reason.TOO_LONG, e.getReason());
    }

    /** 单段恰好 255 字符是合法的。 */
    @Test
    void 单段边界值255合法() {
        String exact255 = "x".repeat(255);
        assertEquals(exact255, PathNormalizer.normalize(exact255));
    }

    /** 总长超长（4097 字符）抛 {@code TOO_LONG}。 */
    @Test
    void 总长超长抛错() {
        StringBuilder sb = new StringBuilder();
        while (sb.length() <= 4096) {
            sb.append("abcd/");
        }
        String tooLong = sb.toString();
        assertThrows(PathNormalizationException.class, () -> PathNormalizer.normalize(tooLong));
    }

    // ==================== 跨平台判定 ====================

    /** 绝对路径判定同时识别三类形态。 */
    @Test
    void 绝对路径判定() {
        assertTrue(PathNormalizer.isAbsolute("/x"), "Unix 绝对路径");
        assertTrue(PathNormalizer.isAbsolute("C:/x"), "Windows 盘符");
        assertTrue(PathNormalizer.isAbsolute("C:\\x"), "Windows 盘符（反斜杠）");
        assertTrue(PathNormalizer.isAbsolute("//server/share"), "UNC");
    }

    /** 相对路径判定。 */
    @Test
    void 相对路径判定() {
        assertFalse(PathNormalizer.isAbsolute("x"));
        assertFalse(PathNormalizer.isAbsolute("a/b"));
        assertFalse(PathNormalizer.isAbsolute("./a"));
        assertFalse(PathNormalizer.isAbsolute(null));
        assertFalse(PathNormalizer.isAbsolute(""));
    }

    /**
     * ★ 前缀陷阱：{@code "/a/bc"} 不是 {@code "/a/b"} 的子目录。
     * 用字符串 {@code startsWith} 判断会误判为 true，导致越权访问通过校验。
     */
    @Test
    void 前缀陷阱不会误判() {
        assertFalse(PathNormalizer.isWithin("/a/b", "/a/bc"),
                "/a/bc 不得被当作 /a/b 的子路径");
        assertTrue(PathNormalizer.isWithin("/a/b", "/a/b/c"), "真实子路径应命中");
        assertTrue(PathNormalizer.isWithin("/a/b", "/a/b"), "自身应命中");
    }

    /** 大小写不同的兄弟目录不互相包含。 */
    @Test
    void 大小写不同的兄弟目录不互相包含() {
        assertFalse(PathNormalizer.isWithin("/a/B", "/a/b"));
    }

    // ==================== 组合与解析 ====================

    /** 拼接时多余分隔符被消解。 */
    @Test
    void 拼接消解多余分隔符() {
        assertEquals("a/b/c", PathNormalizer.join("a/", "/b", "c"));
        assertEquals("a/b", PathNormalizer.join("a", "b"));
    }

    /** 拼接时 null 与空片段被跳过。 */
    @Test
    void 拼接跳过空片段() {
        assertEquals("a/b", PathNormalizer.join("a", null, "", "b"));
    }

    /** 相对解析未越界时正常消解。 */
    @Test
    void 相对解析正常消解() {
        assertEquals("/a/c", PathNormalizer.resolve("/a/b", "../c"));
        assertEquals("/a/b/d", PathNormalizer.resolve("/a/b", "d"));
    }

    /**
     * 相对解析的深度计算基准是<b>拼接后的整体路径</b>。
     *
     * <p>★ 已实测确认（探针验证）：{@code resolve("/a/b", "../../c")} 得
     * {@code /c} 而非抛错——{@code /a/b} 只有 2 层，2 次上溯正好触到根，
     * 这是文件系统的标准语义（Linux 的 {@code /a/b/../../c} 就是 {@code /c}），
     * <b>不算越权</b>。
     *
     * <p>★ 而<b>相对路径</b>上的越界必须拒绝：{@code normalize("a/../../b")}
     * 抛 {@code ESCAPES_ROOT}，见 {@link #越界父目录抛错()}。
     * 两者不矛盾——绝对路径有根可停，相对路径的根就是调用方的base，
     * 越过它意味着访问了不该访问的地方。
     */
    @Test
    void 绝对路径触根不抛错但相对路径越界抛错() {
        assertEquals("/c", PathNormalizer.resolve("/a/b", "../../c"),
                "2 层上溯到根是文件系统语义，不算越权");
        assertEquals("/c", PathNormalizer.resolve("/a/b", "../../../c"),
                "继续上溯仍停在根，绝对路径不会越界");
    }

    /** 相对化：同路径返回 {@code "."}，子路径返回相对形式。 */
    @Test
    void 相对化() {
        assertEquals(".", PathNormalizer.relativize("/a/b", "/a/b"));
        assertEquals("c", PathNormalizer.relativize("/a/b", "/a/b/c"));
        assertEquals("c/d", PathNormalizer.relativize("/a/b", "/a/b/c/d"));
    }

    /**
     * ★ 无法相对化时抛错，<b>不返回绝对路径</b>——
     * 调用方拿到绝对路径很可能直接拿去做文件访问，那就绕过了 base 约束。
     */
    @Test
    void 无法相对化时抛错() {
        assertThrows(PathNormalizationException.class,
                () -> PathNormalizer.relativize("/a/b", "/x/y"));
    }

    /** 转绝对路径：基准目录由参数显式传入，不读 user.dir。 */
    @Test
    void 转绝对路径() {
        assertEquals("/a/b/c", PathNormalizer.toAbsolute("c", "/a/b"));
        assertEquals("/x", PathNormalizer.toAbsolute("/x", "/a/b"), "已是绝对路径应原样返回");
    }

    // ==================== 片段与文件名 ====================

    /** 片段规范化按目录规则小写。 */
    @Test
    void 片段规范化() {
        assertEquals("gamedata", PathNormalizer.normalizeSegment("GameData",
                PathCasePolicy.DIRECTORY_LOWERED));
    }

    /** 文件名规范化保留基名大小写，仅扩展名转小写。 */
    @Test
    void 文件名规范化() {
        assertEquals("README.md", PathNormalizer.normalizeFileName("README.md",
                PathCasePolicy.DIRECTORY_LOWERED), "基名大小写应原样保留");
        assertEquals("readme.md", PathNormalizer.normalizeFileName("readme.MD",
                PathCasePolicy.DIRECTORY_LOWERED), "小写基名仍为小写，仅扩展名转小写");
        assertEquals("readme.md", PathNormalizer.normalizeFileName("README.md",
                PathCasePolicy.FULL_LOWERED), "全小写策略下基名也转小写");
        assertEquals("README.MD", PathNormalizer.normalizeFileName("README.MD",
                PathCasePolicy.PRESERVE), "保留策略下完全不动");
    }

    /** 无扩展名的文件名整体按基名处理。 */
    @Test
    void 无扩展名文件名按基名处理() {
        assertEquals("Makefile", PathNormalizer.normalizeFileName("Makefile",
                PathCasePolicy.DIRECTORY_LOWERED));
    }

    /** 扩展名规范化恒为小写带点。 */
    @Test
    void 扩展名规范化() {
        assertEquals(".png", PathNormalizer.normalizeExtension("PNG"));
        assertEquals(".png", PathNormalizer.normalizeExtension(".PNG"));
        assertEquals("", PathNormalizer.normalizeExtension(null));
        assertEquals("", PathNormalizer.normalizeExtension("  "));
    }

    // ==================== 确定性与幂等性（需求第 5 条）====================

    /** 参与幂等性验证的固定样本集。 */
    private static final String[] IDEMPOTENT_SAMPLES = {
            "a/b/c", "a\\b\\c", "C:\\Users\\x", "\\\\server\\share\\x",
            "GameData/Foo.PNG", "a/./b", "a//b", "a/b/", ".gitignore", "a/.gitignore",
            "a b/c d", "a<b", "a_b", "CON.txt", "x/中文/y", "a/../b", "/a/b", "./a",
    };

    /**
     * ★ 幂等性：{@code normalize(normalize(x)) == normalize(x)}。
     * 这是规范化函数最重要的性质——不幂等的规范化会让路径反复变形，
     * 缓存与比较全部失效。
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "a/b/c", "a\\b\\c", "C:\\Users\\x", "\\\\server\\share\\x",
            "GameData/Foo.PNG", "a/./b", "a//b", "a/b/", ".gitignore",
            "a_b", "x/中文/y", "/a/b", "./a", "a b/c d",
    })
    void 规范化是幂等的(String input) {
        String once = PathNormalizer.normalize(input);
        String twice = PathNormalizer.normalize(once);
        assertEquals(once, twice, "对 [" + input + "] 规范化两次应与一次相同");
    }

    /** 三种策略下均幂等。 */
    @Test
    void 三种策略均幂等() {
        for (String sample : IDEMPOTENT_SAMPLES) {
            for (PathCasePolicy policy : PathCasePolicy.values()) {
                String once = PathNormalizer.normalize(sample, policy);
                String twice = PathNormalizer.normalize(once, policy);
                assertEquals(once, twice,
                        "策略 " + policy + " 对 [" + sample + "] 不幂等");
            }
        }
    }

    /**
     * ★ 确定性：同一输入重复调用结果一致。
     * 若结果随调用次数变化，说明存在可变静态状态或依赖了 {@code hashCode} 之类
     * 的非确定来源。
     */
    @Test
    void 规范化是确定性的() {
        for (String sample : IDEMPOTENT_SAMPLES) {
            String first = PathNormalizer.normalize(sample);
            for (int i = 0; i < 100; i++) {
                assertEquals(first, PathNormalizer.normalize(sample),
                        "第 " + i + " 次调用结果与第一次不同");
            }
        }
    }

    /**
     * ★ 跨平台一致性：{@link PathCasePolicy#DIRECTORY_LOWERED} 与
     * {@link PathCasePolicy#FULL_LOWERED} 的结果<b>只由入参决定</b>，
     * 不依赖 {@code os.name}。本方法在任意平台都应通过。
     */
    @Test
    void 大小写策略结果不依赖操作系统() {
        String input = "GameData/Foo.PNG";
        assertEquals("gamedata/Foo.png", PathNormalizer.normalize(input,
                PathCasePolicy.DIRECTORY_LOWERED));
        assertEquals("gamedata/foo.png", PathNormalizer.normalize(input,
                PathCasePolicy.FULL_LOWERED));
    }

    // ==================== PathNamingRules ====================

    /** 非法字符判定覆盖全部集合与控制字符。 */
    @Test
    void 非法字符判定() {
        for (char ch : "<>:\"|?*".toCharArray()) {
            assertTrue(PathNamingRules.isIllegalCharacter(ch), "应判为非法:" + ch);
        }
        assertTrue(PathNamingRules.isIllegalCharacter('\n'));
        assertTrue(PathNamingRules.isIllegalCharacter((char) 0x7F));
        assertFalse(PathNamingRules.isIllegalCharacter('a'));
        assertFalse(PathNormalizer.class.isInstance('a'));
    }

    /** 保留名判定区分「整段比对」与「文件系统可创建性」。 */
    @Test
    void 两种保留名判定的差异() {
        assertTrue(PathNamingRules.isReservedName("CON"), "整段等于 CON 应命中");
        assertFalse(PathNamingRules.isReservedName("CON.txt"), "整段不等于 CON 不应命中");
        assertTrue(PathNamingRules.isReservedOnFileSystem("CON.txt"),
                "基名是 CON，在 Windows 上无法创建");
    }

    /** 片段合法性校验。 */
    @Test
    void 片段合法性校验() {
        assertTrue(PathNamingRules.isValidSegment("abc"));
        assertTrue(PathNamingRules.isValidSegment(".gitignore"), "前导点合法");
        assertFalse(PathNamingRules.isValidSegment("a/b"), "含分隔符不合法");
        assertFalse(PathNamingRules.isValidSegment("a<b"), "含非法字符不合法");
        assertFalse(PathNamingRules.isValidSegment("a."), "尾部点不合法");
        assertFalse(PathNamingRules.isValidSegment("a "), "尾部空格不合法");
        assertFalse(PathNamingRules.isValidSegment("CON"), "保留名不合法");
        assertFalse(PathNamingRules.isValidSegment(""), "空串不合法");
        assertFalse(PathNamingRules.isValidSegment(null), "null 不合法");
    }

    // ==================== 异常自身 ====================

    /** 异常消息与 toString 都要带上原始输入与原因。 */
    @Test
    void 异常携带原始输入与原因() {
        PathNormalizationException e =
                new PathNormalizationException("a/../../b",
                        PathNormalizationException.Reason.ESCAPES_ROOT);
        assertTrue(e.getMessage().contains("a/../../b"), "消息应含原始输入");
        assertTrue(e.getMessage().contains("ESCAPES_ROOT"), "消息应含原因");
        assertTrue(e.toString().contains("ESCAPES_ROOT"), "toString 应含原因");
        assertEquals("a/../../b", e.getRawInput());
    }

    /** 原因枚举每项都有中文说明，便于日志可读。 */
    @Test
    void 每个原因都有中文说明() {
        for (PathNormalizationException.Reason reason
                : PathNormalizationException.Reason.values()) {
            assertFalse(reason.description().isBlank(),
                    reason + " 缺少中文说明");
        }
    }

    // ==================== 真实文件系统冒烟 ====================

    /**
     * 与真实文件系统对接的最小验证：规范化后的路径能被 {@link Path} 接受。
     * 注意这不要求目录真实存在——本测试只确认「格式被JDK 接受」。
     */
    @Test
    void 规范化结果可被jdk接受(@org.junit.jupiter.api.io.TempDir Path tempDir) {
        String normalized = PathNormalizer.normalize("GameData/Foo.PNG");
        Path resolved = tempDir.resolve(normalized);
        assertTrue(resolved.toString().contains("gamedata"));
        assertFalse(Files.exists(resolved), "本用例只验证格式，不要求文件存在");
    }
}
