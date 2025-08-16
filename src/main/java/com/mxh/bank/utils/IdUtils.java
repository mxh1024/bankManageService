package com.mxh.bank.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

public final class IdUtils {
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);

    public static Long getNextId() {
        return SNOWFLAKE.nextId();
    }
}
