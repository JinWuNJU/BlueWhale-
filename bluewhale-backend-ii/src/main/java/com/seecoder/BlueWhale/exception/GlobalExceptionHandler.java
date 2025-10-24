package com.seecoder.BlueWhale.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.seecoder.BlueWhale.vo.ResultVO;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * @Author: DingXiaoyu
 * @Date: 0:26 2023/11/26
 * 这个类能够接住项目中所有抛出的异常，
 * 使用了RestControllerAdvice切面完成，
 * 表示所有异常出现后都会通过这里。
 * 这个类将异常信息封装到ResultVO中进行返回。
*/
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = BlueWhaleException.class)
    public ResultVO<String> handleAIExternalException(BlueWhaleException e) {
        e.printStackTrace();
        return ResultVO.buildFailure(e.getMessage());
    }

    /**
     * 全局捕捉api被访问时参数格式不正确，参数无法正常自动转换的异常
     * 例如：访问api时storeId=abc，无法转换为@RequestParam int storeId
     * 向前端报告BlueWhaleException.illegalParameter
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResultVO<String> MethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        e.printStackTrace();
        String simpleName = "未定义";
        if (e.getRequiredType() != null) {
            simpleName = e.getRequiredType().getSimpleName();
        }
        return ResultVO.buildFailure("参数类型不匹配: " + e.getName() + " 应为 " + simpleName);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResultVO<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        e.printStackTrace();
        String fieldName = "未知字段";
        String reason = "未知原因";
        Throwable cause = e.getCause();
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            if (ife.getPath() != null && !ife.getPath().isEmpty()) {
                JsonMappingException.Reference reference = ife.getPath().get(0);
                fieldName = reference.getFieldName();
            }
            reason = ife.getOriginalMessage();
        }
        String errorMessage = "JSON解析错误: \"" + fieldName + "\"参数格式异常: " + reason;
        return ResultVO.buildFailure(errorMessage);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultVO<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        e.printStackTrace();
        return ResultVO.buildFailure(e.getBindingResult().getFieldError().getDefaultMessage());
    }
}
