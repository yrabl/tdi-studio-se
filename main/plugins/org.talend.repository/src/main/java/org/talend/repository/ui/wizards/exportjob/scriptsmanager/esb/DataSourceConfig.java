// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.talend.core.CorePlugin;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.IDesignerCoreService;

/**
 * OSGi DataSource
 */
public class DataSourceConfig {

	private DataSourceConfig() {
	}

	public static Collection<String> getAliases(IProcess processItem) {
		return getAliases(processItem, new HashSet<String>());
	}

	private static Collection<String> getAliases(IProcess processItem,
			Collection<String> jobNameVersionChecked) {
		if(processItem == null){
			return Collections.emptyList();
		}
		String jobName = processItem.getName();
		String jobVersion = processItem.getVersion();
		String id = jobName + '_' + jobVersion;
		if (!jobNameVersionChecked.add(id)) {
			return Collections.emptyList(); // no need to add more to the list,
											// just return the empty list
		}
		Collection<String> aliases = new HashSet<String>();
		
		List<? extends INode> generatingNodes = processItem.getGeneratingNodes();
		for(INode node: generatingNodes){
			String datasourceAlias = getDatasourceAliasFrom(node);
			if(datasourceAlias != null && !datasourceAlias.isEmpty()){
				aliases.add(datasourceAlias);
			}
		}

		return aliases;
	}
	
	private static String getDatasourceAliasFrom(INode node) {
		if(!node.isActivate()){
			return null;
		}
		IElementParameter isSpecifyAlias = node.getElementParameter("SPECIFY_DATASOURCE_ALIAS");
		if(isSpecifyAlias == null || Boolean.FALSE.equals(isSpecifyAlias.getValue())){
			return null;
		}
		IElementParameter aliasParam = node.getElementParameter("DATASOURCE_ALIAS");
		if(aliasParam == null){
			return null;
		}
		IDesignerCoreService designerCoreService = CorePlugin.getDefault().getDesignerCoreService();
		boolean evaluate = designerCoreService.evaluate(isSpecifyAlias.getShowIf(), node.getElementParameters());
		if(!evaluate){
			return null;
		}
		String result = aliasParam.getValue()==null?null:aliasParam.getValue().toString();
		return TalendQuoteUtils.removeQuotes(result.trim());
	}
}
