package com.ht.scada.communication;

import com.ht.scada.communication.dao.TagVarTplDao;
import com.ht.scada.communication.dao.impl.TagVarTplDaoImpl;
import com.ht.scada.communication.entity.TagVarTpl;

import java.io.IOException;
import java.util.List;

public class Test {

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
        TagVarTplDao dao = new TagVarTplDaoImpl();
        List<TagVarTpl> list = dao.getAll ();
        System.out.println(list.size());
        for (TagVarTpl tpl :list) {
            System.out.println(tpl.getTagName());
            System.out.println(tpl.getTplName());
            System.out.println(tpl.getVarStorage());
            System.out.println(tpl.getMaxValue());
        }
    }
	

}
