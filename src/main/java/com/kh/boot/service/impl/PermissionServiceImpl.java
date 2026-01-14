package com.kh.boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kh.boot.entity.KhPermission;
import com.kh.boot.mapper.PermissionMapper;
import com.kh.boot.service.PermissionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, KhPermission> implements PermissionService {

    @Override
    public List<KhPermission> getPermissionTree() {
        QueryWrapper<KhPermission> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort");
        List<KhPermission> allPermissions = baseMapper.selectList(wrapper);
        return buildTree(allPermissions, "0");
    }

    private List<KhPermission> buildTree(List<KhPermission> params, String parentId) {
        List<KhPermission> tree = new ArrayList<>();
        for (KhPermission child : params) {
            if (parentId.equals(child.getParentId())) {
                child.setChildren(buildTree(params, child.getId()));
                tree.add(child);
            }
        }
        return tree;
    }
}
