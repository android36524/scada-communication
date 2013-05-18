package com.ht.scada.communication;

import com.ht.scada.communication.dao.EndTagDao;
import com.ht.scada.communication.dao.TagVarTplDao;
import com.ht.scada.communication.dao.VarGroupDao;
import com.ht.scada.communication.dao.VarIOInfoDao;
import com.ht.scada.communication.dao.impl.EndTagDaoImpl;
import com.ht.scada.communication.dao.impl.TagVarTplDaoImpl;
import com.ht.scada.communication.dao.impl.VarGroupDaoImpl;
import com.ht.scada.communication.dao.impl.VarIOInfoDaoImpl;
import com.ht.scada.communication.entity.TagVarTpl;
import com.ht.scada.communication.entity.VarGroupInfo;
import com.ht.scada.communication.entity.VarIOInfo;
import com.ht.scada.communication.entity.EndTag;
import com.ht.scada.communication.model.EndTagWrapper;
import com.ht.scada.communication.model.TagVarTplWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class TagCfgManager {

	private static final Logger log = LoggerFactory.getLogger(TagCfgManager.class);

    private static TagCfgManager instance = new TagCfgManager();
    public static TagCfgManager getInstance() {
        return instance;
    }

    private EndTagDao endTagDao;
    private VarGroupDao varGroupDao;
    private VarIOInfoDao varIOInfoDao;
    private TagVarTplDao tagVarTplDao;

	private final Map<String, List<TagVarTplWrapper>> tagTplMap = new HashMap<>();
	private Map<Integer, List<EndTagWrapper>> endTagMap = new ConcurrentHashMap<>(1000);// 采集设备对应的末端列表, key格式为【通道序号】
    private List<EndTag> endTagList;

    private TagCfgManager() {
        endTagDao = new EndTagDaoImpl();
        varGroupDao = new VarGroupDaoImpl();
        varIOInfoDao = new VarIOInfoDaoImpl();
        tagVarTplDao = new TagVarTplDaoImpl();
    }

    /**
	 * 初始化变量标签
	 */
	//@PostConstruct
	public void init() {
		initTpl();
		initEndTag();
	}

	/**
	 * 初始化末端
	 */
	private void initEndTag() {
		// 加载变量组配置
		List<VarGroupInfo> varGroupInfos = varGroupDao.getAll();
		// 解析末端配置
        endTagList = endTagDao.getAll();
		
		Map<Integer, List<VarIOInfo>> ioInfoListMap = new HashMap<>();
		// TODO: 如果数据量比较大的话此处可能会产生性能问题，可以考虑把IO信息同步存储到实时数据库中
		List<VarIOInfo> ioInfoList = varIOInfoDao.getAll();
		for (VarIOInfo varIOInfo : ioInfoList) {
			List<VarIOInfo> list = ioInfoListMap.get(varIOInfo.getEndTagId());
			if (list == null) {
				list = new LinkedList<>();
				ioInfoListMap.put(varIOInfo.getEndTagId(), list);
			}
			list.add(varIOInfo);
		}
		
		for (EndTag endTag : endTagList) {
			
			List<TagVarTplWrapper> tplList = tagTplMap.get(endTag.getTplName());
			if (tplList != null && !tplList.isEmpty()) {// 变量模板不为空
				EndTagWrapper wrapper = new EndTagWrapper(endTag, varGroupInfos, tplList, ioInfoListMap.get(endTag.getId()));
				
				List<EndTagWrapper> endTagWrapperList = endTagMap.get(endTag.getChannelIdx());
				if (endTagWrapperList == null) {
					endTagWrapperList = new LinkedList<>();
					endTagMap.put(endTag.getChannelIdx(), endTagWrapperList);
				}
				endTagWrapperList.add(wrapper);
			}
		}
	}
	
	/**
	 * 初始化变量模板
	 */
	private void initTpl() {
		List<TagVarTpl> tpls = tagVarTplDao.getAll();
		for (TagVarTpl tpl : tpls) {
			List<TagVarTplWrapper> tplWrapperList = tagTplMap.get(tpl.getTplName());
			if (tplWrapperList == null) {
				tplWrapperList = new ArrayList<TagVarTplWrapper>();
				tagTplMap.put(tpl.getTplName(), tplWrapperList);
			}
			
            TagVarTplWrapper wrapper = new TagVarTplWrapper(tpl);
            tplWrapperList.add(wrapper);
		}
	}
	
	/**
	 * 销毁所有内容
	 */
	//@PostConstruct
	public void destroy() {
		tagTplMap.clear();
		endTagMap.clear();
	}

    /**
     * 根据采集通道序号获取关联的监控对象
     * @param idx
     * @return 如果没有关联监控对象，则返回结果为null
     */
	public List<EndTagWrapper> getEndTagWrapperByChannelIdx(Integer idx) {
		return endTagMap.get(idx);
	}

    public EndTag getByEndTagCode(String endTagCode) {
        for (EndTag endTag : endTagList) {
            if (endTag.getCode().equals(endTagCode)) {
                return endTag;
            }
        }
        return null;
    }

    public Integer getTagVarDataID(String tplName, String varName) {
        List<TagVarTplWrapper> list = tagTplMap.get(tplName);
        if (list != null) {
            for (TagVarTplWrapper varTpl : list) {
                if (varTpl.getTagVarTpl().getVarName().equals(varName)) {
                    return varTpl.getTagVarTpl().getDataId();
                }
            }
        }
        return null;
    }
}
