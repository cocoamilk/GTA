package ir.cocoamilk.gta.alg;

import javax.swing.JOptionPane;

import ir.cocoamilk.gta.alg.param.GTAAlgParameters;
import ir.cocoamilk.gta.service.GTATaskFactory;
import ir.cocoamilk.gta.service.ServicesUtil;

public class GTAAlgRunner {

	private GTAAlgParameters params;

	public GTAAlgRunner(GTAAlgParameters params) {
		this.params = params;
	}

	public void run() {
		try {
			GTAAlg alg = new GTAAlg(params);
			GTATaskFactory factory = new GTATaskFactory(alg);
			ServicesUtil.taskManagerServiceRef.execute(factory.createTaskIterator());
		} catch (final Exception e) {
			e.printStackTrace(System.err);
			JOptionPane.showMessageDialog(ServicesUtil.cySwingApplicationServiceRef.getJFrame(),
					"Error running GTA (1)!  " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

	}

}
