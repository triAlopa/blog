package com.mojian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mojian.entity.SysWebConfig;

public interface SysWebConfigService extends IService<SysWebConfig> {

    int update(SysWebConfig sysWebConfig);
}
