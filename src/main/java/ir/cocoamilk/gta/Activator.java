package ir.cocoamilk.gta;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskManager;
import org.osgi.framework.BundleContext;

import ir.cocoamilk.gta.alg.GTAParametersPanel;
import ir.cocoamilk.gta.service.ServicesUtil;
import ir.cocoamilk.gta.ui.GTANetworkSelectorPanel;
import ir.cocoamilk.gta.ui.GTAPanelComponent;
import ir.cocoamilk.gta.ui.GTAUI;

public class Activator extends AbstractCyActivator {
	public Activator() {
		super();
	}

	public void start(BundleContext bc) {

		CySwingApplication cySwingApplicationServiceRef = getService(bc, CySwingApplication.class);
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc, CyApplicationManager.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc, CyNetworkViewManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc, CyServiceRegistrar.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc, CyEventHelper.class);
		TaskManager<?, ?> taskManagerServiceRef = getService(bc, TaskManager.class);

		CyProperty<Properties> cytoscapePropertiesServiceRef = getService(bc, CyProperty.class,
				"(cyPropertyName=cytoscape3.props)");
		VisualMappingManager visualMappingManagerRef = getService(bc, VisualMappingManager.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc, CyNetworkFactory.class);

		CyRootNetworkManager cyRootNetworkFactory = getService(bc, CyRootNetworkManager.class);
		CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc, CyNetworkViewFactory.class);
		CyLayoutAlgorithmManager cyLayoutsServiceRef = getService(bc, CyLayoutAlgorithmManager.class);

		LoadVizmapFileTaskFactory loadVizmapFileTaskFactory = getService(bc, LoadVizmapFileTaskFactory.class);
		SynchronousTaskManager<?> synchronousTaskManagerServiceRef = getService(bc, SynchronousTaskManager.class);

		// CyHelpBroker cyHelpBroker = getService(bc, CyHelpBroker.class);

		//
		ServicesUtil.cySwingApplicationServiceRef = cySwingApplicationServiceRef;
		ServicesUtil.cyApplicationManagerServiceRef = cyApplicationManagerServiceRef;
		ServicesUtil.cyNetworkViewManagerServiceRef = cyNetworkViewManagerServiceRef;
		ServicesUtil.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
		ServicesUtil.cyServiceRegistrarServiceRef = cyServiceRegistrarServiceRef;
		ServicesUtil.cyEventHelperServiceRef = cyEventHelperServiceRef;
		ServicesUtil.taskManagerServiceRef = taskManagerServiceRef;
		ServicesUtil.cytoscapePropertiesServiceRef = cytoscapePropertiesServiceRef;
		ServicesUtil.visualMappingManagerRef = visualMappingManagerRef;
		ServicesUtil.cyNetworkFactoryServiceRef = cyNetworkFactoryServiceRef;
		ServicesUtil.cyRootNetworkFactory = cyRootNetworkFactory;
		ServicesUtil.cyNetworkViewFactoryServiceRef = cyNetworkViewFactoryServiceRef;
		ServicesUtil.cyLayoutsServiceRef = cyLayoutsServiceRef;
		ServicesUtil.loadVizmapFileTaskFactory = loadVizmapFileTaskFactory;
		ServicesUtil.synchronousTaskManagerServiceRef = synchronousTaskManagerServiceRef;
		// ServicesUtil.cyHelpBrokerServiceRef = cyHelpBroker;

		//

//		ActivePathFinderParameters apfParams = new ActivePathFinderParameters();
//
//		NetworkSelectorPanel networkSelectorPanel = new NetworkSelectorPanel(cyApplicationManagerServiceRef,
//				cyNetworkManagerServiceRef);


//		ActivePathsParameterPanel mainPanel = new ActivePathsParameterPanel(apfParams, networkSelectorPanel);

		GTANetworkSelectorPanel gtaNetworkSelectorPanel = new GTANetworkSelectorPanel(cyApplicationManagerServiceRef,
				cyNetworkManagerServiceRef);

		GTAParametersPanel gtaParametersPannel = new GTAParametersPanel(gtaNetworkSelectorPanel);
		GTAPanelComponent activeModulesCytoPanelComponent = new GTAPanelComponent(gtaParametersPannel);
		GTAUI activeModulesUI = new GTAUI(gtaParametersPannel);

		
		registerAllServices(bc, gtaParametersPannel, new Properties());

//		registerAllServices(bc, networkSelectorPanel, new Properties());
		registerAllServices(bc, gtaNetworkSelectorPanel, new Properties());

//		registerAllServices(bc, mainPanel, new Properties());

		registerService(bc, activeModulesCytoPanelComponent, CytoPanelComponent.class, new Properties());
		registerAllServices(bc, activeModulesUI, new Properties());
	}
}
