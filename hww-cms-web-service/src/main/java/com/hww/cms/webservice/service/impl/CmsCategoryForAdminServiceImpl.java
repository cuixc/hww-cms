package com.hww.cms.webservice.service.impl;

import com.hww.cms.common.dto.AllCategoryTreeDto;
import com.hww.cms.common.manager.CmsCategoryMng;
import com.hww.cms.webservice.service.CmsCategoryForAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service("cmsCategoryForAdminService")
@Transactional
public class CmsCategoryForAdminServiceImpl implements CmsCategoryForAdminService {

    @Autowired
    CmsCategoryMng cmsCategoryMng;

    @Override
    public List<AllCategoryTreeDto> getRetrievingFullTree() {
        // TODO Auto-generated method stub
        List<AllCategoryTreeDto> allCategoryTreeDtos = cmsCategoryMng.getRetrievingFullTree(null, null, null);
        return allCategoryTreeDtos;
    }
}
