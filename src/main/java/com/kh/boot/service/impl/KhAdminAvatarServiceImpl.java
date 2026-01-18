package com.kh.boot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kh.boot.entity.KhAdminAvatar;
import com.kh.boot.mapper.KhAdminAvatarMapper;
import com.kh.boot.service.KhAdminAvatarService;
import org.springframework.stereotype.Service;

@Service
public class KhAdminAvatarServiceImpl extends ServiceImpl<KhAdminAvatarMapper, KhAdminAvatar> implements KhAdminAvatarService {
}
