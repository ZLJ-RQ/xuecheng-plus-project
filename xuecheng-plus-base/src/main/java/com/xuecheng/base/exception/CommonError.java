package com.xuecheng.base.exception;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/1/27 12:21
 */
public enum CommonError {
    UNKOWN_ERROR("执行过程异常，请重试。"),
    PARAMS_ERROR("非法参数"),
    OBJECT_NULL("对象为空"),
    QUERY_NULL("查询结果为空"),
    REQUEST_NULL("请求参数为空");

    private final String errMessage;

    CommonError(String errMessage) {
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }
}
