package com.qq2008.game.bird.util;

/***
 * 数组工具集
 */
public class ArrayUtils {

    /***
     * 从数组中随机一个元素(Object)
     * @param array Object数组
     * @return 随机道德元素
     */
    public static Object randElement(Object[] array) {
        if(array == null || array.length == 0) {
            return null;
        }
        return array[(int) (Math.random() * array.length)];
    }

    /***
     * 从数组中随机一个元素(String)
     * @param array String数组
     * @return 随机道德元素
     */
    public static String randElement(String[] array) {
        if(array == null || array.length == 0) {
            return null;
        }
        return array[(int) (Math.random() * array.length)];
    }

    /***
     * 从数组中随机一个元素(int)
     * @param array int数组
     * @return 随机道德元素
     */
    public static int randElement(int[] array) {
        if(array == null || array.length == 0) {
            return 0;
        }
        return array[(int) (Math.random() * array.length)];
    }
}
