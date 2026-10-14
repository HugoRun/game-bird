package com.gamebird.game.bird.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 路径规范化工具：把任意来源的路径字符串收敛为**唯一的、可预测的**形式。
 *
 * <p><b>为什么需要它</b>：同一个资源在项目里会以多种写法出现——
 * {@code a\b}（Windows 习惯）、{@code a//b}（拼接产生）、{@code ./a/../a/b}（解析产生）。
 * 若不统一，业务代码里就会散落着「拼一下再判断」的去重逻辑，而每处去重方式不同，
 * 最终表现为「同一文件被当成两个」。
 *
 * <p><b>四条不变式</b>（本类所有方法共同遵守）：
 * <ol>
 *   <li><b>分隔符恒为 {@code /}</b>。不使用 {@code Path.toString()} 往返——
 *       它在 Windows 上返回反斜杠，会把已规范化的路径再次污染。</li>
 *   <li><b>冗余片段消解</b>：{@code .} 删除、{@code ..} 合并。</li>
 *   <li><b>越界 {@code ..} 抛错</b>，绝不静默钳制到根目录。钳制会把「越权访问」
 *       伪装成「正常访问」，是安全上的坏模式——宁可让调用失败。
 *       ★ 判据是「<b>有无根可停</b>」：绝对路径有根，{@code ..} 触根后继续上溯
 *       语义上是「停在根」，不构成越权（与 {@code /a/b/../../../c -> /c} 一致）；
 *       相对路径没有根，越过调用方给定的 base 即越权，必须抛
 *       {@link PathNormalizationException.Reason#ESCAPES_ROOT}。</li>
 *   <li><b>纯函数</b>：不读系统属性、不读环境变量、不持有可变静态状态。
 *       所有可变输入通过参数传入。</li>
 * </ol>
 *
 * <p><b>★ 为什么必须在 {@code Path.of} 之前规范化</b>：JDK 自己就会拒绝非法字符，
 * 且它拒绝得比任何规则都早。实测在 Windows 上执行
 * {@code Paths.get("C:/data/a<b.txt")} 会直接抛
 * {@link java.nio.file.InvalidPathException}（{@code Illegal char <<> at index 1}），
 * <b>根本走不到我们的规范化逻辑</b>。这意味着：
 * <ul>
 *   <li>若业务侧写 {@code Path.of(userInput)}，含 {@code < > : " | ? *} 的输入会在
 *       到达本类之前就失败，我们准备的「替换为下划线」降级策略形同虚设；</li>
 *   <li>更隐蔽的是大小写与分隔符：{@code Path.of} 既不统一分隔符也不管大小写，
 *       {@code a\b} 与 {@code a/b} 会变成两个不同的 {@code Path} 对象。</li>
 * </ul>
 * 因此正确顺序是<b>先 {@code String} 规范化、再 {@code Path.of}</b>。这就是
 * {@link PathOps} 存在的全部理由——它把「先规范化」固化到每一个方法的
 * 第一行，让业务侧无需记住这条顺序。
 *
 * <p><b>为什么保留名要抛错而非法字符只替换</b>：非法字符通常来自用户输入的路径，
 * 替换是可预期的降级；而 {@code CON}/{@code NUL} 这类保留名在 Windows 上根本无法创建，
 * 静默通过会把失败推迟到最远的 I/O 时刻。详见
 * {@link PathNamingRules#isReservedOnFileSystem(String)}。
 */
public final class PathNormalizer {

    private PathNormalizer() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    // ==================== 主入口 ====================

    /**
     * 用默认策略（{@link PathCasePolicy#DIRECTORY_LOWERED}）规范化路径。
     *
     * <p><b>★ 越界 {@code ..} 的判据是「有无根可停」</b>，不是「是否超出某个前缀」：
     * <ul>
     *   <li><b>相对路径</b>（无根）：{@code ..} 无处可停，越过调用方给定的边界即越权，
     *       抛 {@link PathNormalizationException.Reason#ESCAPES_ROOT}。
     *       例：{@code normalize("a/../../b")} 抛错。</li>
     *   <li><b>绝对路径</b>（有根）：{@code ..} 上溯到根后<b>继续上溯仍停在根</b>，
     *       这是文件系统的标准语义（{@code /a/b/../../../c} 就是 {@code /c}），
     *       不构成越权，故不抛错。</li>
     * </ul>
     * 判据可用 {@link #isAbsolute(String)} 统一表述：<b>为真时有根可停，为假时无根、
     * 越界即抛错</b>。这不是两套规则，而是同一条规则在有无根两种情况下的表现。
     *
     * @param raw 原始路径；不可为 {@code null}
     * @return 规范化后的路径，分隔符恒为 {@code /}
     * @throws PathNormalizationException 输入为空、越界或含保留名
     */
    public static String normalize(String raw) {
        return normalize(raw, PathCasePolicy.DIRECTORY_LOWERED);
    }

    /**
     * 按指定大小写策略规范化路径，输入分隔符按「反斜杠与正斜杠等价」处理。
     *
     * @param raw    原始路径；不可为 {@code null}
     * @param policy 大小写策略
     * @return 规范化后的路径
     * @throws PathNormalizationException 输入为空、越界或含保留名
     */
    public static String normalize(String raw, PathCasePolicy policy) {
        return normalize(raw, policy, '\\');
    }

    /**
     * 按指定策略与输入分隔符风格规范化路径。
     *
     * @param raw             原始路径；不可为 {@code null}
     * @param policy          大小写策略
     * @param inputSeparator  输入中可能出现的额外分隔符字符，例如 {@code '\\'}。
     *                        该字符会被转换为 {@link PathNamingRules#SEPARATOR}。
     * @return 规范化后的路径
     * @throws PathNormalizationException 输入为空、越界或含保留名
     */
    public static String normalize(String raw, PathCasePolicy policy, char inputSeparator) {
        if (policy == null) {
            throw new PathNormalizationException(raw, PathNormalizationException.Reason.NOT_A_PATH);
        }
        String checked = requireNonBlank(raw);

        // 第一步：统一分隔符。全程只用 String 拼接，绝不经过 Path.toString()。
        String unified = checked.replace(inputSeparator, PathNamingRules.SEPARATOR);

        // 第二步：拆出「根前缀」，前缀不参与片段消解，也不做大小写转换。
        Prefix prefix = splitPrefix(unified, checked);
        String remainder = prefix.remainder();

        // 第三步：按 '/' 切分并消解。末尾有分隔符时，最后一个有效段也是目录。
        boolean trailingSeparator = !remainder.isEmpty()
                && remainder.charAt(remainder.length() - 1) == PathNamingRules.SEPARATOR;
        List<String> resolved = resolveSegments(remainder, prefix.absolute, checked);

        if (resolved.isEmpty()) {
            if (prefix.absolute) {
                // 绝对路径消解到根是合法的，如 "/a/.." -> "/"
                return prefix.text();
            }
            throw new PathNormalizationException(checked,
                    PathNormalizationException.Reason.NO_SEGMENTS);
        }

        // 第四步：逐段做字符清洗与大小写转换。末段按「文件名」处理，其余按「目录」处理。
        int lastIndex = resolved.size() - 1;
        StringBuilder sb = new StringBuilder(prefix.text());
        for (int i = 0; i < resolved.size(); i++) {
            if (i > 0 || (!prefix.text().isEmpty() && !endsWithSeparator(prefix.text()))) {
                sb.append(PathNamingRules.SEPARATOR);
            }
            String segment = resolved.get(i);
            boolean isFileSegment = (i == lastIndex) && !trailingSeparator;
            sb.append(isFileSegment
                    ? normalizeFileName(segment, policy)
                    : normalizeSegment(segment, policy));
        }

        String result = sb.toString();
        if (result.length() > PathNamingRules.MAX_TOTAL_LENGTH) {
            throw new PathNormalizationException(checked,
                    PathNormalizationException.Reason.TOO_LONG);
        }
        return result;
    }

    // ==================== 片段与文件名 ====================

    /**
     * 规范化<b>目录</b>片段（不含分隔符）。
     *
     * <p>目录段在 {@link PathCasePolicy#DIRECTORY_LOWERED} 下一律转小写；
     * 不做扩展名特殊处理，因为目录名里的点只是普通字符。
     *
     * @param segment 原始片段；不可为 {@code null} 或空
     * @param policy  大小写策略
     * @return 规范化后的片段
     * @throws PathNormalizationException 片段为空、为保留名或超长
     */
    public static String normalizeSegment(String segment, PathCasePolicy policy) {
        String cleaned = cleanSegment(segment, policy);
        if (PathNamingRules.isReservedName(cleaned)) {
            throw new PathNormalizationException(segment,
                    PathNormalizationException.Reason.RESERVED_NAME);
        }
        return applyDirectoryCase(cleaned, policy);
    }

    /**
     * 规范化<b>文件名</b>片段：基名保留大小写，扩展名转小写。
     *
     * <p>例如 {@code Foo.PNG} 在 {@link PathCasePolicy#DIRECTORY_LOWERED} 下
     * 得到 {@code Foo.png}；在 {@link PathCasePolicy#FULL_LOWERED} 下得到
     * {@code foo.png}；在 {@link PathCasePolicy#PRESERVE} 下保持 {@code Foo.PNG}。
     *
     * <p><b>无扩展名时</b>：整段按基名处理，例如 {@code Makefile} 不被改动。
     * <b>隐藏文件</b>：{@code .gitignore} 的点在下标 0，没有可剥离的扩展名，
     * 整段按基名处理，前导点必须保留。
     *
     * @param fileName 原始文件名；不可为 {@code null} 或空
     * @param policy   大小写策略
     * @return 规范化后的文件名
     * @throws PathNormalizationException 文件名为空、为保留名或超长
     */
    public static String normalizeFileName(String fileName, PathCasePolicy policy) {
        String cleaned = cleanSegment(fileName, policy);
        // ★ 只用 isReservedName（整段字面量比对），不用 isReservedOnFileSystem。
        // 后者会剥离扩展名后比对基名，把 CON.txt 也判为保留名——那超出本方法的职责，
        // 本方法回答的是「这段输入能否被规范化」，不是「这个名字能否落盘」。
        if (PathNamingRules.isReservedName(cleaned)) {
            throw new PathNormalizationException(fileName,
                    PathNormalizationException.Reason.RESERVED_NAME);
        }
        int dot = cleaned.lastIndexOf('.');
        if (dot <= 0) {
            // 无扩展名（含隐藏文件）：整段都是基名
            return applyFileBaseCase(cleaned, policy);
        }
        String base = cleaned.substring(0, dot);
        String ext = cleaned.substring(dot);
        return applyFileBaseCase(base, policy) + applyExtensionCase(ext, policy);
    }

    /**
     * 规范化扩展名。
     *
     * <p>接受带点（{@code .PNG}）与不带点（{@code PNG}）两种输入，输出恒为
     * <b>小写且带前导点</b>的形式：{@code .png}。不带点时自动补点。
     *
     * <p><b>为什么扩展名总是小写</b>：{@code .PNG} 与 {@code .png} 在跨平台下是同一个
     * 问题（同 {@link PathCasePolicy#DIRECTORY_LOWERED} 的理由），且统一扩展名
     * 没有任何兼容性代价。
     *
     * @param ext 原始扩展名；可为 {@code null} 或空
     * @return 小写且带前导点的扩展名；输入为空时返回空字符串
     */
    public static String normalizeExtension(String ext) {
        if (ext == null) {
            return "";
        }
        String trimmed = ext.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        // 剥掉可选的前导点。注意这里必须用 '.'，不能用分隔符 '/'。
        String body = trimmed.startsWith(".") ? trimmed.substring(1) : trimmed;
        String cleaned = cleanCharacters(body);
        return cleaned.isEmpty() ? "" : "." + cleaned.toLowerCase(Locale.ROOT);
    }

    // ==================== 组合与解析 ====================

    /**
     * 拼接路径片段并规范化。
     *
     * <p>片段之间的多余分隔符会被消解，因此 {@code join("a/", "/b", "c")} 得到
     * {@code a/b/c} 而非 {@code a//b/c}。
     *
     * @param base  基准路径
     * @param more  待拼接的片段；可为 {@code null} 数组或含 {@code null} 元素（跳过）
     * @return 拼接并规范化后的路径
     * @throws PathNormalizationException 拼接结果为空、越界或含保留名
     */
    public static String join(String base, String... more) {
        StringBuilder sb = new StringBuilder(base == null ? "" : base);
        if (more != null) {
            for (String piece : more) {
                if (piece == null || piece.isEmpty()) {
                    continue;
                }
                if (sb.length() > 0 && sb.charAt(sb.length() - 1) != PathNamingRules.SEPARATOR
                        && piece.charAt(0) != PathNamingRules.SEPARATOR) {
                    sb.append(PathNamingRules.SEPARATOR);
                }
                sb.append(piece);
            }
        }
        return normalize(sb.toString());
    }

    /**
     * 以 {@code base} 为基准解析相对路径，消解 {@code ..} 并做越界检查。
     *
     * <p>{@code resolve("/a/b", "../c")} 得到 {@code /a/c}；
     * {@code resolve("/a/b", "../../c")} 抛
     * {@link PathNormalizationException.Reason#ESCAPES_ROOT}。
     *
     * @param base     基准路径
     * @param relative 相对路径
     * @return 解析并规范化后的路径
     * @throws PathNormalizationException 越出基准根目录
     */
    public static String resolve(String base, String relative) {
        String normalizedBase = normalize(base);
        // ★ 不可先单独规范化 relative：相对路径 "../c" 自身的视角下是越界的，
        // 但拼到 base 之上完全合法。先规范化会把它误判为越权。
        // 正确做法是原样拼接后整体消解，让 ".." 的深度由 base 决定。
        String raw = requireNonBlank(relative)
                .replace('\\', PathNamingRules.SEPARATOR);
        String combined = normalizedBase + PathNamingRules.SEPARATOR + raw;
        return normalize(combined);
    }

    /**
     * 求 {@code target} 相对 {@code base} 的路径。
     *
     * <p><b>无法相对化时抛错，不返回绝对路径</b>：调用方拿到绝对路径很可能直接拿去做
     * 文件访问，那就绕过了「必须在 base 之下」的约束。抛错让越界可见。
     *
     * @param base   基准目录
     * @param target 目标路径
     * @return 相对路径，形如 {@code a/b}；相同路径返回 {@code "."}
     * @throws PathNormalizationException target 不在 base 之下
     */
    public static String relativize(String base, String target) {
        String normalizedBase = normalize(base);
        String normalizedTarget = normalize(target);

        if (normalizedBase.equals(normalizedTarget)) {
            return ".";
        }
        String boundary = normalizedBase.endsWith(PathNamingRules.SEPARATOR_STRING)
                ? normalizedBase
                : normalizedBase + PathNamingRules.SEPARATOR;
        if (!normalizedTarget.startsWith(boundary)) {
            throw new PathNormalizationException(target,
                    PathNormalizationException.Reason.ESCAPES_ROOT);
        }
        String relative = normalizedTarget.substring(boundary.length());
        return relative.isEmpty() ? "." : relative;
    }

    /**
     * 把路径转为绝对路径，基准目录由<b>调用方显式传入</b>。
     *
     * <p><b>为什么不读 {@code user.dir}</b>：那会让方法不再是纯函数——同一输入在不同
     * 进程目录下得到不同输出，测试无法稳定复现，线上问题也无法离线排查。
     *
     * @param raw     原始路径，可为相对或绝对
     * @param baseDir 基准目录；必须是绝对路径
     * @return 绝对路径
     * @throws PathNormalizationException raw 为空，或 baseDir 不是绝对路径
     */
    public static String toAbsolute(String raw, String baseDir) {
        String normalizedBase = normalize(baseDir);
        if (!isAbsolute(normalizedBase)) {
            throw new PathNormalizationException(baseDir,
                    PathNormalizationException.Reason.NOT_A_PATH);
        }
        if (isAbsolute(raw == null ? "" : raw.replace('\\', PathNamingRules.SEPARATOR))) {
            return normalize(raw);
        }
        return resolve(normalizedBase, raw);
    }

    /**
     * 判断是否为绝对路径，<b>同时识别 Windows 盘符、UNC 与 Unix 绝对路径</b>。
     *
     * <p>识别三类的理由：本项目部署在 Docker/Linux，但开发环境多为 Windows，
     * 配置文件里两种写法都可能出现。
     *
     * @param raw 待判断路径；可为 {@code null}
     * @return 是绝对路径返回 {@code true}
     */
    public static boolean isAbsolute(String raw) {
        if (raw == null || raw.isEmpty()) {
            return false;
        }
        String unified = raw.replace('\\', PathNamingRules.SEPARATOR);
        if (unified.charAt(0) == PathNamingRules.SEPARATOR) {
            return true;                       // Unix 绝对路径 或 UNC
        }
        // Windows 盘符：单个字母 +冒号，且冒号后紧跟分隔符或已是结尾
        if (unified.length() >= 2 && unified.charAt(1) == ':') {
            char drive = unified.charAt(0);
            if (Character.isLetter(drive)) {
                return unified.length() == 2 || unified.charAt(2) == PathNamingRules.SEPARATOR;
            }
        }
        return false;
    }

    /**
     * 判断 {@code candidate} 是否在 {@code base} 之内（含等于 base 自身）。
     *
     * <p><b>★ 必须是「路径分量」级别的比较，不能用字符串前缀</b>：
     * {@code isWithin("/a/b", "/a/bc")} 必须返回 {@code false}。
     * 直接用 {@code startsWith} 会返回 {@code true}，因为 {@code "/a/bc"} 确实以
     * {@code "/a/b"} 开头——这是 containment 判断的经典 bug，会让越权访问通过校验。
     *
     * @param base      基准目录
     * @param candidate 待判断路径
     * @return 在 base 之内返回 {@code true}
     */
    public static boolean isWithin(String base, String candidate) {
        String normalizedBase = normalize(base);
        String normalizedCandidate = normalize(candidate);
        if (normalizedBase.equals(normalizedCandidate)) {
            return true;
        }
        String boundary = normalizedBase.endsWith(PathNamingRules.SEPARATOR_STRING)
                ? normalizedBase
                : normalizedBase + PathNamingRules.SEPARATOR;
        return normalizedCandidate.startsWith(boundary);
    }

    // ==================== glob 与遍历 ====================

    /**
     * 判断路径是否匹配 glob 表达式。
     *
     * <p><b>glob 语义</b>：
     * <ul>
     *   <li>{@code ?} —— 匹配<b>单个非分隔符</b>字符</li>
     *   <li>{@code *} —— 匹配<b>单层内</b>任意字符，<b>不跨 {@code /}</b></li>
     *   <li>{@code **} —— 跨层，可匹配零层或多层（含分隔符）</li>
     *   <li>{@code {a,b}} —— 枚举 alternatives</li>
     * </ul>
     *
     * <p><b>★ 必须全匹配</b>：用 {@link java.util.regex.Matcher#matches()} 而非
     * {@code find()}。否则 {@code "ab"} 会匹配到 {@code "a/b"} 的子串，
     * 「以 .txt 结尾」这种意图会被错误地放宽成「任意位置含 .txt」。
     *
     * <p><b>为什么不用 {@code FileSystems.getDefault().getPathMatcher}</b>：
     * 它依赖平台默认语法，Windows 下 {@code *} 与 {@code ?} 的语义与 Linux 不同，
     * 同一份测试在两个平台会得到不同结果——与「跨平台确定性」的要求直接冲突。
     *
     * <p>比较双方均以 {@link PathCasePolicy#PRESERVE} 规范化后做<b>大小写敏感</b>比较。
     *
     * @param glob glob 表达式；不可为 {@code null}
     * @param path 待匹配路径；不可为 {@code null}
     * @return 匹配返回 {@code true}
     * @throws PathNormalizationException glob 为空或格式非法
     */
    public static boolean matchesGlob(String glob, String path) {
        Pattern pattern = Pattern.compile(globToRegex(glob));
        String normalized = normalize(path, PathCasePolicy.PRESERVE);
        return pattern.matcher(normalized).matches();
    }

    /**
     * 把 glob 表达式转换为锚定正则，供调试与测试使用。
     *
     * <p>输出恒以 {@code ^} 开头、{@code $} 结尾，只能配合
     * {@link java.util.regex.Matcher#matches()} 使用。
     *
     * @param glob glob 表达式；不可为 {@code null}
     * @return 锚定正则字符串
     * @throws PathNormalizationException glob 为空或大括号不配对
     */
    public static String globToRegex(String glob) {
        if (glob == null || glob.isBlank()) {
            throw new PathNormalizationException(glob,
                    PathNormalizationException.Reason.EMPTY_INPUT);
        }
        StringBuilder regex = new StringBuilder("^");
        int i = 0;
        int braceDepth = 0;
        while (i < glob.length()) {
            char ch = glob.charAt(i);
            switch (ch) {
                case '*' -> {
                    boolean doubleStar = (i + 1) < glob.length() && glob.charAt(i + 1) == '*';
                    if (doubleStar) {
                        boolean followedBySlash = (i + 2) < glob.length()
                                && glob.charAt(i + 2) == PathNamingRules.SEPARATOR;
                        if (followedBySlash) {
                            // "**/" 需匹配零层或多层，故用 (?:[^/]+/)* 而非 .*/
                            regex.append("(?:[^/]+/)*");
                            i += 3;
                            continue;
                        }
                        regex.append(".*");
                        i += 2;
                        continue;
                    }
                    regex.append("[^/]*");
                    i++;
                }
                case '?' -> {
                    regex.append("[^/]");
                    i++;
                }
                case '{' -> {
                    braceDepth++;
                    regex.append("(?:");
                    i++;
                }
                case '}' -> {
                    braceDepth--;
                    if (braceDepth < 0) {
                        throw new PathNormalizationException(glob,
                                PathNormalizationException.Reason.NOT_A_PATH);
                    }
                    regex.append(')');
                    i++;
                }
                case ',' -> {
                    regex.append(braceDepth > 0 ? '|' : ",");
                    i++;
                }
                case '\\' -> {
                    // 转义下一个字符，按字面量处理
                    if (i + 1 >= glob.length()) {
                        throw new PathNormalizationException(glob,
                                PathNormalizationException.Reason.NOT_A_PATH);
                    }
                    regex.append(Pattern.quote(String.valueOf(glob.charAt(i + 1))));
                    i += 2;
                }
                default -> {
                    if ("^$.|+()[]".indexOf(ch) >= 0) {
                        regex.append('\\');
                    }
                    regex.append(ch);
                    i++;
                }
            }
        }
        if (braceDepth != 0) {
            throw new PathNormalizationException(glob,
                    PathNormalizationException.Reason.NOT_A_PATH);
        }
        regex.append('$');
        return regex.toString();
    }

    /**
     * 列出 {@code root} 下匹配 glob 的所有路径。
     *
     * <p>返回的是<b>相对 {@code root} 的规范化路径</b>，便于直接与 glob 比较或继续拼接。
     * {@code root} 本身先经规范化再交给文件系统，因此 Windows 上传入 {@code a\b}
     * 也能工作。
     *
     * @param root  根目录
     * @param glob 匹配表达式；{@code null} 视为匹配全部
     * @return 匹配到的相对路径列表，按字典序排列；无匹配返回空列表
     * @throws IOException 遍历目录失败（不吞异常，交由调用方决定）
     * @throws PathNormalizationException 路径非法
     */
    public static List<String> listFiles(String root, String glob) throws IOException {
        String normalizedRoot = normalize(root);
        Path rootPath = Path.of(normalizedRoot);
        if (!Files.isDirectory(rootPath)) {
            return List.of();
        }
        String regex = glob == null ? null : globToRegex(glob);
        List<String> result = new ArrayList<>();
        try (var stream = Files.walk(rootPath)) {
            stream.filter(Files::isRegularFile).forEach(p -> {
                String relative = rootPath.relativize(p).toString()
                        .replace('\\', PathNamingRules.SEPARATOR);
                String normalized = normalize(relative, PathCasePolicy.PRESERVE);
                if (regex == null || Pattern.compile(regex).matcher(normalized).matches()) {
                    result.add(normalized);
                }
            });
        } catch (UncheckedIOException e) {
            // Files.walk 的迭代过程可能抛 UncheckedIOException，这里还原为受检异常
            throw e.getCause();
        }
        result.sort(String::compareTo);
        return List.copyOf(result);
    }

    // ==================== 内部实现 ====================

    /**
     * 路径的根前缀。
     *
     * @param text      前缀文本，含尾随分隔符（根前缀为 {@code "/"} 时即其本身）
     * @param remainder 前缀之后的剩余部分
     * @param absolute  是否为绝对路径
     */
    private record Prefix(String text, String remainder, boolean absolute) {
    }

    /**
     * 拆分根前缀：Unix 根 {@code /}、Windows 盘符 {@code C:/}、UNC {@code //}。
     *
     * <p><b>盘符保持大写</b>：{@code c:/} 与 {@code C:/} 在 Windows 上等价，
     * 但规范化输出应只有一个形态；选大写是因为盘符惯例写成大写。
     * UNC 的服务器名与共享名视为目录段，参与 {@link PathCasePolicy} 处理。
     */
    private static Prefix splitPrefix(String unified, String original) {
        if (unified.startsWith("//")) {
            // UNC：//server/share/... 前两段是服务器名与共享名，不做片段消解
            String body = unified.substring(2);
            int firstSlash = body.indexOf(PathNamingRules.SEPARATOR);
            if (firstSlash <= 0) {
                // 只有服务器名没有共享名，形态非法
                throw new PathNormalizationException(original,
                        PathNormalizationException.Reason.NOT_A_PATH);
            }
            String server = body.substring(0, firstSlash);
            String rest = body.substring(firstSlash + 1);
            int secondSlash = rest.indexOf(PathNamingRules.SEPARATOR);
            String share = secondSlash < 0 ? rest : rest.substring(0, secondSlash);
            String remainder = secondSlash < 0 ? "" : rest.substring(secondSlash + 1);
            String text = PathNamingRules.SEPARATOR_STRING
                    + PathNamingRules.SEPARATOR_STRING
                    + server + PathNamingRules.SEPARATOR_STRING
                    + share + PathNamingRules.SEPARATOR_STRING;
            return new Prefix(text, remainder, true);
        }
        if (unified.startsWith(PathNamingRules.SEPARATOR_STRING)) {
            return new Prefix(PathNamingRules.SEPARATOR_STRING,
                    unified.substring(1), true);
        }
        if (unified.length() >= 2 && unified.charAt(1) == ':'
                && Character.isLetter(unified.charAt(0))) {
            if (unified.length() > 2 && unified.charAt(2) != PathNamingRules.SEPARATOR) {
                // "C:data" 这种驱动器相对路径形态歧义，明确拒绝
                throw new PathNormalizationException(original,
                        PathNormalizationException.Reason.NOT_A_PATH);
            }
            String drive = Character.toUpperCase(unified.charAt(0)) + ":"
                    + PathNamingRules.SEPARATOR;
            return new Prefix(drive, unified.substring(2), true);
        }
        return new Prefix("", unified, false);
    }

    /**
     * 切分并消解片段：删除 {@code .}、合并 {@code ..}、丢弃空片段。
     *
     * @param remainder 剩余路径文本
     * @param absolute  是否为绝对路径（绝对路径的 {@code ..} 在根处不再上溯）
     * @param original  原始输入，仅用于异常信息
     * @return 消解后的片段列表
     */
    private static List<String> resolveSegments(String remainder, boolean absolute, String original) {
        List<String> stack = new ArrayList<>();
        if (remainder.isEmpty()) {
            return stack;
        }
        for (String segment : remainder.split("/")) {
            if (segment.isEmpty() || ".".equals(segment)) {
                continue;                       // a//b 与 a/./b 都在此消解
            }
            if ("..".equals(segment)) {
                if (!stack.isEmpty()) {
                    stack.remove(stack.size() - 1);
                    continue;
                }
                if (absolute) {
                    // 绝对路径在根处继续上溯仍停在根，这是文件系统语义而非越权
                    continue;
                }
                // 相对路径继续上溯即越出根目录 —— 抛错，绝不钳制
                throw new PathNormalizationException(original,
                        PathNormalizationException.Reason.ESCAPES_ROOT);
            }
            stack.add(segment);
        }
        return stack;
    }

    /**
     * 清洗片段字符：非法字符替换、尾部点与空格替换，并校验单段长度。
     *
     * <p><b>前导点必须保留</b>：{@code .gitignore} 是合法文件名，不能被当作冗余片段
     * 消解掉——只把<b>整段等于 {@code "."}</b> 的才当冗余。
     */
    private static String cleanSegment(String segment, PathCasePolicy policy) {
        String value = requireNonBlank(segment);
        String cleaned = cleanCharacters(value);
        char last = cleaned.charAt(cleaned.length() - 1);
        if (last == '.' || last == ' ') {
            cleaned = cleaned.substring(0, cleaned.length() - 1)
                    + PathNamingRules.REPLACEMENT_CHARACTER;
        }
        if (cleaned.length() > PathNamingRules.MAX_SEGMENT_LENGTH) {
            throw new PathNormalizationException(segment,
                    PathNormalizationException.Reason.TOO_LONG);
        }
        return cleaned;
    }

    /**
     * 替换非法字符为下划线。
     *
     * <p><b>为什么替换而非删除</b>：删除会让不同输入塌缩成同一路径，
     * 产生意料之外的冲突；替换保留了「原输入不同」这一信息。
     */
    private static String cleanCharacters(String value) {
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            sb.append(PathNamingRules.isIllegalCharacter(ch)
                    ? PathNamingRules.REPLACEMENT_CHARACTER : ch);
        }
        return sb.toString();
    }

    /**
     * 目录段大小写：{@link PathCasePolicy#PRESERVE} 之外一律转小写。
     */
    private static String applyDirectoryCase(String segment, PathCasePolicy policy) {
        return policy == PathCasePolicy.PRESERVE
                ? segment : segment.toLowerCase(Locale.ROOT);
    }

    /**
     * 文件基名大小写：仅 {@link PathCasePolicy#FULL_LOWERED} 转小写。
     */
    private static String applyFileBaseCase(String base, PathCasePolicy policy) {
        return policy == PathCasePolicy.FULL_LOWERED
                ? base.toLowerCase(Locale.ROOT) : base;
    }

    /**
     * 扩展名大小写：除 {@link PathCasePolicy#PRESERVE} 外一律转小写。
     */
    private static String applyExtensionCase(String ext, PathCasePolicy policy) {
        return policy == PathCasePolicy.PRESERVE ? ext : ext.toLowerCase(Locale.ROOT);
    }

    /**
     * 非空白校验。
     *
     * <p><b>★ 这里刻意不调用 {@code trim()}</b>：{@code "a "}（尾部空格）是需要被
     * <b>替换</b>成 {@code "a_"} 的合法输入，若先 trim 就会变成 {@code "a"}，
     * 尾部空格规则将永远不会生效。trim 只用于「判断是否全空白」，
     * 不用于修改返回值。
     *
     * @param raw 待校验输入
     * @return <b>原样返回</b>输入（未做任何裁剪）
     * @throws PathNormalizationException 输入为 null、空或仅含空白
     */
    private static String requireNonBlank(String raw) {
        if (raw == null) {
            throw new PathNormalizationException(null,
                    PathNormalizationException.Reason.NULL_INPUT);
        }
        if (raw.trim().isEmpty()) {
            throw new PathNormalizationException(raw,
                    raw.isEmpty()
                            ? PathNormalizationException.Reason.EMPTY_INPUT
                            : PathNormalizationException.Reason.BLANK_INPUT);
        }
        return raw;
    }

    /**
     * 判断前缀文本是否已以分隔符结尾。
     */
    private static boolean endsWithSeparator(String text) {
        return !text.isEmpty() && text.charAt(text.length() - 1) == PathNamingRules.SEPARATOR;
    }
}
