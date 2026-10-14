package com.qq2008.game.bird.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link PathNormalizer#matchesGlob(String, String)} 与
 * {@link PathOps} 的单元测试。
 *
 * <p>★ glob 部分的核心防线是<b>「全匹配」而非子串匹配</b>：
 * 用 {@code find()} 会让 {@code "ab"} 匹配到 {@code "a/b"}，把「以 .txt 结尾」
 * 这种明确意图悄悄放宽成「任意位置含 .txt」。反例已在
 * {@link #星号不跨分隔符()} 与 {@link #必须全匹配而非子串匹配()} 中钉死。
 *
 * <p>★ {@link PathOps} 部分验证「每个方法都先走规范化」这个收口设计：
 * 传入 Windows 风格路径也能在 Linux/macOS 上正确落盘。
 */
class PathNormalizerGlobTest {

    // ==================== glob 基础语义 ====================

    /** {@code *} 匹配单层内任意字符。 */
    @Test
    void 星号匹配单层() {
        assertTrue(PathNormalizer.matchesGlob("*.txt", "a.txt"));
        assertTrue(PathNormalizer.matchesGlob("*.txt", "long-name.txt"));
        assertFalse(PathNormalizer.matchesGlob("*.txt", "a.txt.bak"));
    }

    /** ★ {@code *} 不跨分隔符——否则「一层」的语义就没了。 */
    @Test
    void 星号不跨分隔符() {
        assertFalse(PathNormalizer.matchesGlob("*.txt", "a/b.txt"),
                "* 不得跨 / 匹配");
        assertFalse(PathNormalizer.matchesGlob("/*.txt", "a/b.txt"));
    }

    /** {@code **} 跨层，可匹配多层。 */
    @Test
    void 双星号跨层() {
        assertTrue(PathNormalizer.matchesGlob("**/*.txt", "a.txt"),
                "**/ 需匹配零层");
        assertTrue(PathNormalizer.matchesGlob("**/*.txt", "a/b.txt"), "一层");
        assertTrue(PathNormalizer.matchesGlob("**/*.txt", "a/b/c/d.txt"), "多层");
    }

    /** {@code **} 在末尾时匹配该层及以下全部内容。 */
    @Test
    void 双星号在末尾() {
        assertTrue(PathNormalizer.matchesGlob("a/**", "a/b"));
        assertTrue(PathNormalizer.matchesGlob("a/**", "a/b/c"));
    }

    /** {@code ?} 匹配单个非分隔符字符。 */
    @Test
    void 问号匹配单字符() {
        assertTrue(PathNormalizer.matchesGlob("a?c", "abc"));
        assertFalse(PathNormalizer.matchesGlob("a?c", "ac"), "? 必须恰好一个字符");
        assertFalse(PathNormalizer.matchesGlob("a?c", "abbc"));
    }

    /** {@code ?} 不匹配分隔符。 */
    @Test
    void 问号不匹配分隔符() {
        assertFalse(PathNormalizer.matchesGlob("a?c", "a/c"));
    }

    /** 花括号枚举匹配其一。 */
    @Test
    void 花括号枚举() {
        assertTrue(PathNormalizer.matchesGlob("*.{txt,md}", "a.txt"));
        assertTrue(PathNormalizer.matchesGlob("*.{txt,md}", "a.md"));
        assertFalse(PathNormalizer.matchesGlob("*.{txt,md}", "a.html"));
    }

    /** 花括号可嵌在路径中。 */
    @Test
    void 花括号在路径中() {
        assertTrue(PathNormalizer.matchesGlob("a/{b,c}/d", "a/b/d"));
        assertTrue(PathNormalizer.matchesGlob("a/{b,c}/d", "a/c/d"));
        assertFalse(PathNormalizer.matchesGlob("a/{b,c}/d", "a/x/d"));
    }

    /** 字面量字符按字面匹配，正则元字符不被当作元字符。 */
    @Test
    void 字面量与元字符转义() {
        assertTrue(PathNormalizer.matchesGlob("a.b", "a.b"));
        assertFalse(PathNormalizer.matchesGlob("a.b", "axb"),
                "字面量 . 不应被当作正则通配");
        assertTrue(PathNormalizer.matchesGlob("a+b", "a+b"), "字面量 + 不应被当作量词");
    }

    /**
     * ★ 必须全匹配，不能子串匹配。
     * 这两个反例是本测试存在的核心理由。
     */
    @Test
    void 必须全匹配而非子串匹配() {
        assertFalse(PathNormalizer.matchesGlob("ab", "a/b"),
                "ab 不得匹配 a/b（分隔符也算字符）");
        assertFalse(PathNormalizer.matchesGlob("b", "a/b"), "不得匹配子串");
        assertFalse(PathNormalizer.matchesGlob("txt", "a.txt"), "不得省略前部");
        assertFalse(PathNormalizer.matchesGlob("a", "ab"), "不得匹配前缀");
    }

    /** glob 输出是锚定正则，只能配合 matches() 使用。 */
    @Test
    void 输出锚定正则() {
        String regex = PathNormalizer.globToRegex("*.txt");
        assertTrue(regex.startsWith("^"), "应以 ^ 开头");
        assertTrue(regex.endsWith("$"), "应以 $ 结尾");
        assertTrue(regex.contains("[^/]*"), "* 应转成不跨分隔符的字符类");
    }

    /** 大括号不配对视为非法 glob。 */
    @Test
    void 大括号不配对抛错() {
        PathNormalizationException e = assertThrows(PathNormalizationException.class,
                () -> PathNormalizer.globToRegex("a/{b,c"));
        assertEquals(PathNormalizationException.Reason.NOT_A_PATH, e.getReason());
    }

    /** 空 glob 抛 {@code EMPTY_INPUT}。 */
    @Test
    void 空glob抛错() {
        PathNormalizationException e = assertThrows(PathNormalizationException.class,
                () -> PathNormalizer.globToRegex(""));
        assertEquals(PathNormalizationException.Reason.EMPTY_INPUT, e.getReason());
    }

    /** 多种 glob 与路径的组合。 */
    @ParameterizedTest
    @CsvSource({
            "'*.txt',        'a.txt',       true",
            "'*.txt',        'a/b.txt',     false",
            "'a/*/c',        'a/b/c',       true",
            "'a/*/c',        'a/b/x/c',     false",
            "'**',           'a/b/c',       true",
            "'a/**/b',       'a/b',         true",
            "'a/**/b',       'a/x/y/b',     true",
            "'?.txt',        'a.txt',       true",
            "'?.txt',        'ab.txt',      false",
    })
    void glob组合用例(String glob, String path, boolean expected) {
        assertEquals(expected, PathNormalizer.matchesGlob(glob, path),
                "glob=[" + glob + "] path=[" + path + "]");
    }

    // ==================== glob 确定性与幂等 ====================

    /** glob 匹配是纯函数：重复调用结果一致。 */
    @Test
    void glob匹配是确定性的() {
        String glob = "**/*.txt";
        String path = "a/b/c.txt";
        boolean first = PathNormalizer.matchesGlob(glob, path);
        for (int i = 0; i < 100; i++) {
            assertEquals(first, PathNormalizer.matchesGlob(glob, path));
        }
    }

    /** glob 结果不受平台默认匹配器影响——同一表达式重复编译结果相同。 */
    @Test
    void glob编译结果稳定() {
        String glob = "a/{b,c}/*.txt";
        String first = PathNormalizer.globToRegex(glob);
        for (int i = 0; i < 100; i++) {
            assertEquals(first, PathNormalizer.globToRegex(glob));
        }
    }

    // ==================== 真实文件系统遍历 ==================

    /**
     * 在真实目录上验证 {@link PathNormalizer#listFiles(String, String)}。
     *
     * <p>这里刻意用<b>大写目录名 + 大写文件名</b>建目录，再以小写 glob 查找：
     * 在大小写敏感的文件系统上，{@code DIRECTORY_LOWERED} 策略会把目录段小写化，
     * 所以必须用 {@code PRESERVE} 策略做 glob 遍历，否则 Linux 上会找不到文件。
     * 这一点正是「大小写策略必须显式选择」的现实理由。
     */
    @Test
    void 列出匹配文件(@TempDir Path tempDir) throws IOException {
        Path base = tempDir.resolve("Data");
        Files.createDirectories(base.resolve("Sub"));
        Files.writeString(base.resolve("a.txt"), "1", StandardCharsets.UTF_8);
        Files.writeString(base.resolve("b.md"), "2", StandardCharsets.UTF_8);
        Files.writeString(base.resolve("Sub/c.txt"), "3", StandardCharsets.UTF_8);

        List<String> all = PathNormalizer.listFiles(base.toString(), null);
        assertEquals(3, all.size(), "glob 为 null 时应返回全部文件");
        assertTrue(all.contains("a.txt"));
        assertTrue(all.contains("Sub/c.txt"), "跨层文件应被枚举到");

        List<String> txt = PathNormalizer.listFiles(base.toString(), "**/*.txt");
        assertEquals(2, txt.size(), "应匹配到a.txt 与 Sub/c.txt");
        assertTrue(txt.contains("a.txt"));
        assertTrue(txt.contains("Sub/c.txt"));

        List<String> none = PathNormalizer.listFiles(base.toString(), "*.json");
        assertTrue(none.isEmpty(), "无匹配应返回空列表而非 null");
    }

    /** 目录不存在时返回空列表，不抛错——遍历是可选项，不该让主流程失败。 */
    @Test
    void 目录不存在返回空列表(@TempDir Path tempDir) throws IOException {
        List<String> result = PathNormalizer.listFiles(
                tempDir.resolve("no-such-dir").toString(), null);
        assertTrue(result.isEmpty());
    }

    // ==================== PathOps 收口验证 ==================

    /**
     * ★ 收口设计的核心验证：{@link PathOps} 接受 Windows 风格路径，
     * 且非法字符被自动替换，因此业务侧不必自己记规则。
     *
     * <p>★ 注意这里用<b>字符串拼接</b>而非 {@code Path.resolve}：
     * JDK 在 Windows 上解析含 {@code <} 的路径会先抛
     * {@link java.nio.file.InvalidPathException}，根本到不了我们的规范化逻辑。
     * 这恰恰说明「业务侧直接用 {@code Path.of(userInput)}」是不可行的。
     */
    @Test
    void 写入读取往返一致(@TempDir Path tempDir) throws IOException {
        String root = tempDir.toString().replace('\\', '/');
        // 故意用反斜杠 + 大写目录 + 非法字符，全部应由 PathOps 内部规范化
        String path = root + "\\GameData\\a<b.txt";

        PathOps.writeString(path, "hello");

        assertEquals("hello", PathOps.readString(path));
        assertTrue(PathOps.exists(path));
        // 规范化后非法字符应已变为下划线，目录应已小写
        assertTrue(Files.exists(tempDir.resolve("gamedata").resolve("a_b.txt")),
                "非法字符应被替换、目录应被小写");
    }

    /** 写入时自动创建父目录。 */
    @Test
    void 写入自动创建父目录(@TempDir Path tempDir) throws IOException {
        String path = tempDir.resolve("a/b/c.txt").toString();
        assertFalse(PathOps.exists(path));

        PathOps.writeString(path, "x");

        assertTrue(PathOps.exists(path), "父目录应被自动创建");
    }

    /** 删除文件后可确认不再存在。 */
    @Test
    void 删除文件(@TempDir Path tempDir) throws IOException {
        String path = tempDir.resolve("tmp.txt").toString();
        PathOps.writeString(path, "x");
        assertTrue(PathOps.exists(path));

        PathOps.delete(path);

        assertFalse(PathOps.exists(path));
    }

    /** 创建多级目录。 */
    @Test
    void 创建多级目录(@TempDir Path tempDir) throws IOException {
        String dir = tempDir.resolve("x/y/z").toString();
        PathOps.mkdirs(dir);
        assertTrue(Files.isDirectory(Path.of(dir)));
    }

    /** 重复创建目录静默成功（幂等）。 */
    @Test
    void 重复创建目录幂等(@TempDir Path tempDir) throws IOException {
        String dir = tempDir.resolve("x/y").toString();
        PathOps.mkdirs(dir);
        PathOps.mkdirs(dir);
        assertTrue(Files.isDirectory(Path.of(dir)));
    }

    /** 目录列举通过 PathOps 走规范化。 */
    @Test
    void 目录列举经由规范化(@TempDir Path tempDir) throws IOException {
        Path base = tempDir.resolve("Cfg");
        Files.createDirectories(base);
        Files.writeString(base.resolve("one.txt"), "1", StandardCharsets.UTF_8);
        Files.writeString(base.resolve("two.log"), "2", StandardCharsets.UTF_8);

        List<String> txt = PathOps.list(base.toString(), "*.txt");

        assertEquals(1, txt.size());
        assertEquals("one.txt", txt.get(0));
    }

    /** ★ 不吞异常：读不存在的文件必须抛 IOException，不能返回 null。 */
    @Test
    void 读不存在文件抛异常而非返回空(@TempDir Path tempDir) {
        String missing = tempDir.resolve("missing.txt").toString();
        assertThrows(IOException.class, () -> PathOps.readString(missing));
    }

    /** ★ 不吞异常：删除不存在的路径必须抛 IOException。 */
    @Test
    void 删除不存在路径抛异常(@TempDir Path tempDir) {
        String missing = tempDir.resolve("missing.txt").toString();
        assertThrows(IOException.class, () -> PathOps.delete(missing));
    }

    /**
     * 非法路径经由 PathOps 时抛 {@link PathNormalizationException}，而非 I/O 异常。
     *
     * <p>★ 越界用例必须用<b>相对路径</b>：绝对路径上的 {@code ..} 触到根就停，
     * 是文件系统语义而非越权（见 {@code resolveSegments} 的注释），
     * 因此 {@code /a/../../b} 不会抛错。真正需要拒绝的是相对路径的越界。
     */
    @Test
    void 非法路径抛规范化异常(@TempDir Path tempDir) {
        assertThrows(PathNormalizationException.class,
                () -> PathOps.exists("a/../../b"), "相对路径越界应被拒绝");
        assertThrows(PathNormalizationException.class,
                () -> PathOps.readString("CON"), "保留名应被拒绝");
        assertThrows(PathNormalizationException.class,
                () -> PathOps.mkdirs(""), "空路径应被拒绝");
    }
}
