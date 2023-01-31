package com.xuecheng.base.exception;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/1/27 12:22
 */
public class XueChengPlusException extends RuntimeException{

    private String errMessage;

    public XueChengPlusException(String errMessage) {
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public static void cast(String errMessage){
        throw new XueChengPlusException(errMessage);
    }

    public static void cast(CommonError commonError){
        throw new XueChengPlusException(commonError.getErrMessage());
    }
}
