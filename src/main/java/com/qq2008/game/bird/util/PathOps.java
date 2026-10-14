package com.qq2008.game.bird.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 路径读 / 写 / 查找的统一入口。
 *
 * <p><b>为什么需要这一层收口</b>：只提供 {@link PathNormalizer} 这样的工具类，
 * 规范很容易被绕过——业务代码随手写 {@code Files.readString(Path.of(userInput))}
 * 就绕过了全部规则，等于没有规范化。<b>每个方法第一行就调用
 * {@link PathNormalizer}</b>，让「走 PathOps」等价于「走了规范化」，规则才真正生效。
 *
 * <p><b>异常策略</b>：I/O 失败一律<b>声明抛出</b>，不吞。不存在「返回 {@code null}
 * 表示读失败」这种设计——那会把错误推迟到调用方解引用时才炸，且丢失原始堆栈。
 * 路径<b>不合法</b>抛 {@link PathNormalizationException}，
 * I/O<b>失败</b>抛 {@link IOException}，两类问题在类型上即可区分。
 *
 * <p><b>★ 本类不做路径拼接</b>：多个片段请先用 {@link PathNormalizer#join(String, String...)}
 * 合并，再交给本类。这样「拼接规则」与「I/O 规则」不会互相掩盖。
 */
public final class PathOps {

    private PathOps() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 读取 UTF-8 文本文件内容。
     *
     * @param path 文件路径（任意分隔符风格均可）
     * @return 文件内容
     * @throws IOException 读取失败，或文件不存在
     */
    public static String readString(String path) throws IOException {
        return Files.readString(toRealPath(path), StandardCharsets.UTF_8);
    }

    /**
     * 以 UTF-8 写入文本文件；父目录不存在时自动创建。
     *
     * @param path    文件路径
     * @param content 待写入内容；不可为 {@code null}
     * @throws IOException 写入失败
     */
    public static void writeString(String path, String content) throws IOException {
        Path real = toRealPath(path);
        Path parent = real.getParent();
        if (parent != null) {
            // 不判断存在性再创建：createDirectories 本身幂等，少一次系统调用
            Files.createDirectories(parent);
        }
        Files.writeString(real, content, StandardCharsets.UTF_8);
    }

    /**
     * 判断路径是否存在（文件或目录均可）。
     *
     * <p>本方法<b>不抛 IOException</b>：{@link Files#exists} 本身对权限问题等异常
     * 返回 {@code false} 而不抛出，这是它的既定契约。这里如实沿用，
     * 但文档必须写明「返回 false 也可能是权限不足，而非真的不存在」。
     *
     * @param path 待检测路径
     * @return 存在返回 {@code true}
     */
    public static boolean exists(String path) {
        return Files.exists(toRealPath(path));
    }

    /**
     * 删除文件或空目录。
     *
     * @param path 待删除路径
     * @throws IOException 删除失败；路径不存在时同样抛 {@link IOException}
     */
    public static void delete(String path) throws IOException {
        Files.delete(toRealPath(path));
    }

    /**
     * 列出目录下匹配 glob 的文件。
     *
     * @param dir  目录路径
     * @param glob 匹配表达式；{@code null} 表示全部
     * @return 相对该目录的路径列表；目录不存在时返回空列表
     * @throws IOException 遍历失败
     */
    public static List<String> list(String dir, String glob) throws IOException {
        return PathNormalizer.listFiles(dir, glob);
    }

    /**
     * 创建目录（含全部父目录），已存在时静默成功。
     *
     * @param path 目录路径
     * @throws IOException 创建失败
     */
    public static void mkdirs(String path) throws IOException {
        Files.createDirectories(toRealPath(path));
    }

    /**
     * 规范化后交给文件系统。
     *
     * <p>这是本类唯一的路径出口——所有公开方法都必须经过它，
     * 这样才能保证「没有一条路径绕过规范化」。
     *
     * @param path 待规范化路径
     * @return {@link Path} 对象
     * @throws PathNormalizationException 路径非法
     */
    private static Path toRealPath(String path) {
        return Path.of(PathNormalizer.normalize(path));
    }
}
