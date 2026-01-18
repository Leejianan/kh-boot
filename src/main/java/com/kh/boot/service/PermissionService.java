package com.kh.boot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kh.boot.entity.KhPermission;

import java.util.List;

public interface PermissionService extends IService<KhPermission> {
    List<KhPermission> getPermissionTree();

    List<KhPermission> getMenuTree();
}
