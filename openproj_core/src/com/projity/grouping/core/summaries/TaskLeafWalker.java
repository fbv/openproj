package com.projity.grouping.core.summaries;

import java.util.Collection;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;

import com.projity.grouping.core.Node;

import org.openproj.domain.task.Task;

public class TaskLeafWalker extends LeafWalker {

	public TaskLeafWalker(Closure visitor) {
		super(visitor);
	}
	@Override
	public void execute(Object arg0) {
		Node node = (Node)arg0;
		if (!(node.getValue() instanceof Task))
			return;
		
		Collection nodeList = ((Task)node.getValue()).isWbsParent() ? nodeModel.getChildren(node) : null;
		if (nodeList == null || nodeList.isEmpty()) { // if has no children
			if (visitor != null)	
				visitor.execute(node); // add value
		} else {
			CollectionUtils.forAllDo(nodeList, this); // treat children
		}
	}

}
