package com.ht.scada.communication;

import com.ht.scada.common.tag.entity.EndTag;
import com.ht.scada.common.tag.entity.TagCfgTpl;
import com.ht.scada.common.tag.entity.VarGroupCfg;
import com.ht.scada.common.tag.entity.VarIOInfo;
import com.ht.scada.common.tag.exception.StorageInfoErrorException;
import com.ht.scada.common.tag.service.TagService;
import com.ht.scada.communication.model.EndTagWrapper;
import com.ht.scada.communication.util.VarTplInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Named
public class TagVarController {

	private static final Logger log = LoggerFactory.getLogger(TagVarController.class);
	
	@Inject
	private TagService tagService;
	
	private final Map<String, List<VarTplInfo>> tagTplMap = new HashMap<>();
	private Map<Integer, List<EndTagWrapper>> endTagMap = new ConcurrentHashMap<>(1000);// 采集设备对应的末端列表, key格式为【通道序号】
	
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
		List<VarGroupCfg> varGroupCfgs = tagService.getAllVarGroupCfg();
		// 解析末端配置
		List<EndTag> endTagList = tagService.getEndTag4Comm();
		
		Map<Integer, List<VarIOInfo>> ioInfoListMap = new HashMap<>();
		// TODO: 如果数据量比较大的话此处可能会产生性能问题，可以考虑把IO信息同步存储到实时数据库中
		List<VarIOInfo> ioInfoList = tagService.getAllTagIOInfo();
		for (VarIOInfo varIOInfo : ioInfoList) {
			List<VarIOInfo> list = ioInfoListMap.get(varIOInfo.getEndTag().getId());
			if (list == null) {
				list = new LinkedList<>();
				ioInfoListMap.put(varIOInfo.getEndTag().getId(), list);
			}
			list.add(varIOInfo);
		}
		
		for (EndTag endTag : endTagList) {
			
			List<VarTplInfo> tplList = tagTplMap.get(endTag.getTplName());
			if (tplList != null && !tplList.isEmpty()) {// 变量模板不为空
				//List<VarIOInfo> ioInfoList = tagService.getTagIOInfoByEndTagID(endTag.getId());
				EndTagWrapper wrapper = new EndTagWrapper(endTag, varGroupCfgs, tplList, ioInfoListMap.get(endTag.getId()));
				
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
		List<TagCfgTpl> tpls = tagService.getAllTagTpl();
		for (TagCfgTpl tpl : tpls) {
			List<VarTplInfo> tplWrapperList = tagTplMap.get(tpl.getTplName());
			if (tplWrapperList == null) {
				tplWrapperList = new ArrayList<VarTplInfo>();
				tagTplMap.put(tpl.getTplName(), tplWrapperList);
			}
			
			try {
				VarTplInfo wrapper = new VarTplInfo(tpl);
				tplWrapperList.add(wrapper);
			} catch (StorageInfoErrorException e) {
				e.printStackTrace();
			}
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
	
	public List<EndTagWrapper> getEndTagWrapperByChannelIdx(Integer idx) {
		return endTagMap.get(idx);
	}
}
