package com.ht.scada.communication.model;

import com.ht.scada.communication.entity.TagVarTpl;
import com.ht.scada.communication.util.StorageFactory;
import com.ht.scada.communication.util.StorageFactory.FaultStorage;
import com.ht.scada.communication.util.StorageFactory.OffLimitsStorage;
import com.ht.scada.communication.util.StorageFactory.YXStorage;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TagVarTplWrapper {
	private final TagVarTpl tagVarTpl;
	
	private final FaultStorage faultStorage;
	private final YXStorage yxStorage;
	private final List<OffLimitsStorage> offLimitsStorages;

	public TagVarTplWrapper(TagVarTpl tagVarTpl) {
		this.tagVarTpl = tagVarTpl;
		
		Object storage = StorageFactory.parseStorage(tagVarTpl.getVarStorage());
		if (storage instanceof List) {
			this.offLimitsStorages = (List<OffLimitsStorage>) storage;
			// 按限值升序排序
			Collections.sort(offLimitsStorages, new Comparator<OffLimitsStorage>() {
				@Override
				public int compare(OffLimitsStorage o1, OffLimitsStorage o2) {
					return Double.compare(o1.threshold, o2.threshold);
				}
			});
			this.yxStorage = null;
			this.faultStorage = null;
		} else if (storage instanceof YXStorage) {
			this.yxStorage = (YXStorage) storage;
			this.offLimitsStorages = null;
			this.faultStorage = null;
		} else if (storage instanceof FaultStorage) {
			this.faultStorage = (FaultStorage) storage;
			this.yxStorage = null;
			this.offLimitsStorages = null;
		} else {
			this.yxStorage = null;
			this.faultStorage = null;
			this.offLimitsStorages = null;
		}
	}
	
	public FaultStorage getFaultStorage() {
		return faultStorage;
	}

	public YXStorage getYxStorage() {
		return yxStorage;
	}

	/**
	 * @return 越限存储器，返回的存储器列表已经按限值和升序排序
	 */
	public List<OffLimitsStorage> getOffLimitsStorages() {
		return offLimitsStorages;
	}

	public TagVarTpl getTagVarTpl() {
		return tagVarTpl;
	}
}
