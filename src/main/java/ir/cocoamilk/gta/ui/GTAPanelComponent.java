package ir.cocoamilk.gta.ui;

import java.awt.Component;

import javax.swing.Icon;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;

import ir.cocoamilk.gta.alg.GTAParametersPanel;

public class GTAPanelComponent implements CytoPanelComponent {

	private GTAParametersPanel panel;
	
	public GTAPanelComponent(GTAParametersPanel gtaParametersPannel) {
		this.panel = gtaParametersPannel; 
	}
	
	@Override
	public Component getComponent() {
		return panel;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public String getTitle() {
		return "GTA";
	}

	@Override
	public Icon getIcon() {
		return null;
	}

}
