package ir.cocoamilk.gta.service;

import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.Task;


public class GTATaskFactory extends AbstractTaskFactory {
	
	private Task task;
	public GTATaskFactory(Task task){
		this.task = task;
	}
	
	public TaskIterator createTaskIterator()  {
		return new TaskIterator(task);
	}
}
