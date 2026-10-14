package com.gamebird.game.bird.util;

import java.util.Set;

/**
 * 路径命名规范的项目级定义。
 *
 * <p><b>为什么把规则常量单独成类</b>：这些常量是「项目约定」而非「实现细节」。
 * {@code PathNormalizer} 引用它们来执行规范化，业务侧与测试也需要直接断言它们，
 * 若散落在 {@code PathNormalizer} 的私有常量里，外部就无法引用同一份事实。
 *
 * <p>本类只做<b>判定与查询</b>，不做任何状态变更，可安全并发调用。
 */
public final class PathNamingRules {

    private PathNamingRules() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /** 规范化后的唯一分隔符：正斜杠 {@code /}。 */
    public static final char SEPARATOR = '/';

    /**
     * 分隔符的字符串形式。
     *
     * <p><b>为什么需要它</b>：Java 中 {@code SEPARATOR + "/"} 是 <b>char 算术</b>而非
     * 字符串拼接——{@code '/' + '/'} 得到 int 94，不是 {@code "//"}。
     * 凡是需要拼接分隔符字符串处一律用本常量，避免这个经典陷阱。
     */
    public static final String SEPARATOR_STRING = "/";

    /**
     * 非法字符集合：{@code < > : " | ? *} 与 ASCII 控制字符。
     *
     * <p><b>为什么冒号也在内</b>：Windows 用冒号表示「驱动器:流」（如
     * {@code C:data}），Linux/macOS 允许冒号出现在文件名中。若不剔除，
     * 在 Windows 上创建的文件到 Linux 上就变成了另一个名字。
     */
    public static final String ILLEGAL_CHARACTERS = "<>:\"|?*";

    /**
     * 非法字符与控制字符的默认替换目标。
     *
     * <p><b>为什么替换而非删除</b>：删除会让两个不同输入塌缩成同一个路径
     * （{@code a<b} 与 {@code ab} 都变成 {@code ab}），从而产生意料之外的路径冲突。
     * 替换保留了「原输入不同」这一信息。
     */
    public static final char REPLACEMENT_CHARACTER = '_';

    /** 单个路径片段的最大长度：255 字符（多数文件系统 ext4/APFS/NTFS 的实际上限）。 */
    public static final int MAX_SEGMENT_LENGTH = 255;

    /** 完整路径的最大长度：4096 字符（Linux 的 {@code PATH_MAX}）。 */
    public static final int MAX_TOTAL_LENGTH = 4096;

    /**
     * Windows 保留设备名集合。
     *
     * <p>这些名称<b>不区分大小写</b>：{@code con}、{@code CON}、{@code Con} 在
     * Windows 上都会指向控制台设备，无法作为普通文件名创建。
     */
    public static final Set<String> WINDOWS_RESERVED_NAMES = Set.of(
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9");

    /**
     * 判断某字符是否为路径中的非法字符。
     *
     * @param ch 待判定字符
     * @return 若为 {@code <>:"|?*} 之一或 ASCII 控制字符（含 {@code DEL}）则返回 {@code true}
     */
    public static boolean isIllegalCharacter(char ch) {
        return ILLEGAL_CHARACTERS.indexOf(ch) >= 0 || ch < 0x20 || ch == 0x7F;
    }

    /**
     * 判断某个<b>片段</b>是否是 Windows 保留设备名。
     *
     * <p><b>★ 只看整段，不看扩展名</b>：{@code CON} 命中，但 {@code CON.txt} <b>不</b>命中。
     * 这与真实文件系统行为一致——Windows 的保留名规则针对的是<b>文件名的基名部分</b>，
     * {@code CON.txt} 的基名是 {@code CON} 会被拒绝，但我们的规范化入口拿到的是
     * 已含扩展名的整段，判断时以整段字面量比对。
     *
     * <p>★ 这条边界与操作系统并不完全一致，是<b>有意选择的保守策略</b>：
     * 我们宁可放过 {@code CON.txt}（在 Linux 上完全合法），也不误杀合法输入。
     * 若将来需要完全对齐 Windows，把扩展名剥离后再比对即可。
     *
     * @param segment 待判定的完整片段；可为 {@code null}
     * @return 若整段（忽略大小写）等于某个保留设备名则返回 {@code true}
     */
    public static boolean isReservedName(String segment) {
        if (segment == null || segment.isEmpty()) {
            return false;
        }
        return WINDOWS_RESERVED_NAMES.contains(segment.toUpperCase(java.util.Locale.ROOT));
    }

    /**
     * 判断某个名称在文件系统上是否被保留。
     *
     * <p><b>为什么 Windows 保留名必须在「命名阶段」就拒绝</b>：这些名称对应的是
     * 操作系统设备而非普通文件——{@code CON} 指向控制台输入，{@code NUL} 丢弃所有写入，
     * {@code PRN} 指向打印机。尝试创建会失败，且失败点通常在<b>最远的 I/O 时刻</b>
     * （可能是一段业务逻辑跑了几分钟后）。静默通过等于把错误推迟到难以定位的位置，
     * 所以必须当场抛 {@link PathNormalizationException}。
     *
     * <p><b>本方法与 {@link #isReservedName(String)} 的区别</b>：本方法回答的是
     * 「这个名称在目标文件系统上能不能用」，会连同扩展名一并剥离基名后比对，
     * 因此 {@code CON.txt} 在本方法下<b>会</b>被判为保留名；而
     * {@code isReservedName} 按整段字面量比对，{@code CON.txt} <b>不</b>命中。
     *
     * <p>两者的差异是有意的：{@link #isReservedName} 服务于「输入是否可被本工具规范化」，
     * {@code isReservedOnFileSystem} 服务于「这个名字能不能落盘」。前者保守放行，
     * 后者如实报错。
     *
     * @param name 文件名（可含扩展名）；可为 {@code null}
     * @return 若基名部分是 Windows 保留设备名则返回 {@code true}
     */
    public static boolean isReservedOnFileSystem(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        String base = name;
        int dot = base.lastIndexOf('.');
        // 点在下标 0 处说明是 .gitignore 这类隐藏文件，没有可剥离的扩展名
        if (dot > 0) {
            base = base.substring(0, dot);
        }
        return WINDOWS_RESERVED_NAMES.contains(base.toUpperCase(java.util.Locale.ROOT));
    }

    /**
     * 判断某个片段是否是合法的路径片段（不含分隔符、无非法字符、非保留名）。
     *
     * <p><b>注意</b>：本方法只做「能否通过校验」的判断，<b>不做大小写转换、
     * 不做替换</b>。校验与转换分离，是为了让调用方能先问「能不能」再决定「怎么改」。
     *
     * @param segment 待判定片段；可为 {@code null}
     * @return 合法返回 {@code true}
     */
    public static boolean isValidSegment(String segment) {
        if (segment == null || segment.isEmpty()) {
            return false;
        }
        if (segment.indexOf(SEPARATOR) >= 0) {
            return false;
        }
        if (isReservedName(segment) || isReservedOnFileSystem(segment)) {
            return false;
        }
        if (segment.length() > MAX_SEGMENT_LENGTH) {
            return false;
        }
        for (int i = 0; i < segment.length(); i++) {
            if (isIllegalCharacter(segment.charAt(i))) {
                return false;
            }
        }
        // Windows 禁止文件名以点或空格结尾；前导点是允许的（.gitignore）
        char last = segment.charAt(segment.length() - 1);
        return last != '.' && last != ' ';
    }
}
