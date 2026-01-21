package com.kh.boot.controller.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kh.boot.common.PageData;
import com.kh.boot.common.Result;
import com.kh.boot.security.domain.LoginUser;
import com.kh.boot.util.SecurityUtils;
import org.springframework.web.bind.annotation.RestController;

/**
 * Common Base Controller
 */
@RestController
public abstract class BaseController {

    /**
     * Get current login user
     */
    protected LoginUser getLoginUser() {
        return SecurityUtils.getLoginUser();
    }

    /**
     * Get current user ID
     */
    protected String getUserId() {
        return SecurityUtils.getUserId();
    }

    /**
     * Get current username
     */
    protected String getUsername() {
        return SecurityUtils.getUsername();
    }

    /**
     * Output success result
     */
    protected <T> Result<T> success(T data) {
        return Result.success(data);
    }

    /**
     * Output success result
     */
    protected Result<Void> success() {
        return Result.success();
    }

    /**
     * Output error result
     */
    protected Result<Void> error(String message) {
        return Result.error(message);
    }

    /**
     * Output paginated success result
     */
    protected <T> Result<PageData<T>> pageSuccess(IPage<T> page) {
        return Result.pageSuccess(page);
    }

    /**
     * /**
     * Error result with code and message
     */
    protected <T> Result<T> error(int code, String msg) {
        return Result.error(code, msg);
    }
}
