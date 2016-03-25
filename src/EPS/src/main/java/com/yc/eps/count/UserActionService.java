package com.yc.eps.count;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.yc.commons.Utility;
import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.count.IUserActionService;
import com.yc.edsi.count.UserActionCountPO;

/**
 * 用户行为统计
 * 
 * @author <a href="mailto:wangji@163.com">wangji</a>
 * @version 1.0
 * @since 2014年4月29日
 */
@Service
public class UserActionService implements IUserActionService {
    @Resource
    private IUserAtionCountDao userActionDao;

    public List<UserActionCountPO> getUserActionList(UserActionCountPO userActionCountPO,
    		String rangePropertyIds,int startIndex, int pageSize)
            throws EdsiException {
        userActionCountPO.setStartIndex(startIndex);
        userActionCountPO.setPageSize(pageSize);
        List<Long> propertyIds = Utility.getSplitList(rangePropertyIds, ",");
        return userActionDao.getUserActionList(userActionCountPO,rangePropertyIds,propertyIds);
    }

    public List<UserActionCountPO> getUserInfoList(UserActionCountPO userActionCountPO,String rangePropertyIds,
    		int startIndex, int pageSize) throws EdsiException {
    	List<Long> propertyIds = Utility.getSplitList(rangePropertyIds, ",");
        return userActionDao.getUserInfoList(userActionCountPO,rangePropertyIds,propertyIds);
    }

}
