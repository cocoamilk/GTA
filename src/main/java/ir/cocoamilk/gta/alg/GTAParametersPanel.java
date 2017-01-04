// ActivePathsParametersPopupDialog
//-----------------------------------------------------------------------------
// $Revision: 14177 $
// $Date: 2008-06-10 15:16:57 -0700 (Tue, 10 Jun 2008) $
// $Author: rmkelley $
//-----------------------------------------------------------------------------
package ir.cocoamilk.gta.alg;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

import org.cytoscape.application.swing.events.CytoPanelComponentSelectedEvent;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ir.cocoamilk.gta.alg.param.GTAAlgParameters;
import ir.cocoamilk.gta.alg.param.GTAAlgParametersAttrSelection;
import ir.cocoamilk.gta.alg.param.NetFilteringMethod;
import ir.cocoamilk.gta.service.ServicesUtil;
import ir.cocoamilk.gta.ui.GTANetworkSelectorPanel;

public class GTAParametersPanel extends JPanel implements ColumnCreatedListener, CytoPanelComponentSelectedListener {

	private static final Logger logger = LoggerFactory.getLogger(GTAParametersPanel.class);

	private boolean isPanelSelected = false;

	private GTANetworkSelectorPanel networkSelectorPanel;
	private AttrSelectionTableModel tableModel;

	private Checkbox generateTScoresCheckBox;
	private Checkbox generateDegreesCheckBox;
	private java.awt.Choice levelOneSubnetMaxSize, levelTwoSubnetMaxSize;
	private java.awt.Choice numberOfTargetNetworks;

	private JComboBox<NetFilteringMethod> netFilteringMethod;

	public GTAParametersPanel(GTANetworkSelectorPanel gtaNetworkSelectorPanel) {
		this.networkSelectorPanel = gtaNetworkSelectorPanel;
		this.networkSelectorPanel.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				updateAttributePanel();
			}
		});

		// Set global parameters
		this.setLayout(new BorderLayout());
		// this.setMinimumSize(new Dimension(320, 420));
		// this.setPreferredSize(new Dimension(320, 420));

		this.tableModel = new AttrSelectionTableModel();

		initComponents();
	}

	private void initComponents() {
		setLayout(new java.awt.GridBagLayout());

		java.awt.GridBagConstraints gridBagConstraints;
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		add(networkSelectorPanel, gridBagConstraints);

		initAttrSelectionTable();

		javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
		buttonPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

		javax.swing.JButton runButton = new javax.swing.JButton("Find Modules");
		runButton.addActionListener(new FindModulesAction());
		// aboutButton.setPreferredSize(new java.awt.Dimension(67, 23));
		buttonPanel.add(runButton, gridBagConstraints);

		add(buttonPanel, gridBagConstraints);

	}

	private javax.swing.JTable normalAttributeSelectorTable, cancerAttributeSelectorTable;

	private static GridBagConstraints gridConstraint(int gridx, int gridy, int gridwidth, int gridheight, int anchor,
			int fill) {
		final Insets insets = new Insets(0, 0, 0, 0);

		return new GridBagConstraints(gridx, gridy, gridwidth, gridheight, 1.0, 1.0, anchor, fill, insets, 0, 0);
	}

	private void initAttrSelectionTable() {
		normalAttributeSelectorTable = new javax.swing.JTable();
		cancerAttributeSelectorTable = new javax.swing.JTable();

		normalAttributeSelectorTable.setModel(tableModel);
		cancerAttributeSelectorTable.setModel(tableModel);

		// Add a exclamation point if attribute is not p-value
		// TableColumn nameColumn =
		// normalAttributeSelectorTable.getColumn(AttrSelectionTableModel.COL_NAME);
		// nameColumn.setCellRenderer(new NameColumnCellRenderer());
		// cancerAttributeSelectorTable.getColumn(AttrSelectionTableModel.COL_NAME).setCellRenderer(new
		// NameColumnCellRenderer());

		// // give user the option to switch sig
		// TableColumn switchSigColumn =
		// normalAttributeSelectorTable.getColumn(AttrSelectionTableModel.COL_REVERSE_SIG);
		// // switchSigColumn.setCellEditor(new CheckBoxCellEditor());
		// // switchSigColumn.setCellRenderer(new CheckBoxCellRenderer());
		// switchSigColumn.setCellRenderer(normalAttributeSelectorTable.getDefaultRenderer(Boolean.class));
		// switchSigColumn.setCellEditor(normalAttributeSelectorTable.getDefaultEditor(Boolean.class));
		//
		//
		// CheckBoxCellEditorListener checkBoxCellEditorListener = new
		// CheckBoxCellEditorListener(normalAttributeSelectorTable.getModel());
		// normalAttributeSelectorTable.getDefaultEditor(Boolean.class).addCellEditorListener(checkBoxCellEditorListener);
		//
		// // Let user select normalization method with comboBox
		// TableColumn normColumn =
		// normalAttributeSelectorTable.getColumn(AttrSelectionTableModel.COL_SCALING);
		// NormalizationCellRenderer normCellRender = new
		// NormalizationCellRenderer();
		// normColumn.setCellRenderer(normCellRender);
		// // TableCellEditor editor = new DefaultCellEditor(normCellRender);
		// normColumn.setCellEditor(new NormalizationComboboxEditor());
		//
		// cancerAttributeSelectorTable.getColumn(AttrSelectionTableModel.COL_SCALING).setCellRenderer(new
		// NormalizationCellRenderer());
		// cancerAttributeSelectorTable.getColumn(AttrSelectionTableModel.COL_SCALING).setCellEditor(new
		// NormalizationComboboxEditor());

		java.awt.GridBagConstraints gridBagConstraints;

		javax.swing.JScrollPane normalAttrScroll = new javax.swing.JScrollPane(),
				cancerAttrScroll = new javax.swing.JScrollPane();

		normalAttrScroll.setViewportView(normalAttributeSelectorTable);
		cancerAttrScroll.setViewportView(cancerAttributeSelectorTable);

		javax.swing.JPanel normalAttributeSelectorPanel = new javax.swing.JPanel(),
				cancerAttributeSelectorPanel = new javax.swing.JPanel();

		normalAttributeSelectorPanel.setLayout(new java.awt.GridBagLayout());
		normalAttributeSelectorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Normal Cases"));

		cancerAttributeSelectorPanel.setLayout(new java.awt.GridBagLayout());
		cancerAttributeSelectorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Cancer Cases"));

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;

		normalAttributeSelectorPanel.add(normalAttrScroll, gridBagConstraints);
		cancerAttributeSelectorPanel.add(cancerAttrScroll, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		add(networkSelectorPanel, gridBagConstraints);

		javax.swing.JPanel bothAttributeSelectorPanel = new javax.swing.JPanel(new java.awt.GridBagLayout());

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		bothAttributeSelectorPanel.add(normalAttributeSelectorPanel, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		bothAttributeSelectorPanel.add(cancerAttributeSelectorPanel, gridBagConstraints);

		javax.swing.JPanel configPanel = new javax.swing.JPanel(new java.awt.GridBagLayout());
		configPanel.add(bothAttributeSelectorPanel,
				gridConstraint(0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH));

		javax.swing.JPanel advancedConfigPanel = new javax.swing.JPanel(new java.awt.GridBagLayout());
		advancedConfigPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Advanced Options"));

		generateTScoresCheckBox = new Checkbox("Generate T-Scores");
		generateTScoresCheckBox.setState(true);
		advancedConfigPanel.add(generateTScoresCheckBox,
				gridConstraint(0, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL));

		generateDegreesCheckBox = new Checkbox("Generate degrees");
		generateDegreesCheckBox.setState(true);
		advancedConfigPanel.add(generateDegreesCheckBox,
				gridConstraint(0, 1, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL));

		advancedConfigPanel.add(new java.awt.Label("Level one subnet max size"),
				gridConstraint(0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL));
		levelOneSubnetMaxSize = new java.awt.Choice();
		for (int i = 1; i < GTAAlgParameters.SUBNETWORK_MAX_SIZE_MAX; i++)
			levelOneSubnetMaxSize.add("" + i);
		levelOneSubnetMaxSize.select("" + GTAAlgParameters.SUBNETWORK_MAX_SIZE_DEFAULT);
		advancedConfigPanel.add(levelOneSubnetMaxSize,
				gridConstraint(1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL));

		advancedConfigPanel.add(new java.awt.Label("Level two subnet max size"),
				gridConstraint(0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL));
		levelTwoSubnetMaxSize = new java.awt.Choice();
		for (int i = 1; i < GTAAlgParameters.SUBNETWORK_MAX_SIZE_MAX; i++)
			levelTwoSubnetMaxSize.add("" + i);
		levelTwoSubnetMaxSize.select("" + GTAAlgParameters.SUBNETWORK_MAX_SIZE_DEFAULT);
		advancedConfigPanel.add(levelTwoSubnetMaxSize,
				gridConstraint(1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL));

		java.awt.Label nuumberOfTargetNetworksLabel = new java.awt.Label("No. of results");
		advancedConfigPanel.add(nuumberOfTargetNetworksLabel,
				gridConstraint(0, 4, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL));
		numberOfTargetNetworks = new java.awt.Choice();
		for (int i = 1; i < GTAAlgParameters.NUMBER_OF_GENERATED_NETWORKS_MAX; i++)
			numberOfTargetNetworks.add("" + i);
		numberOfTargetNetworks.select("" + GTAAlgParameters.NUMBER_OF_GENERATED_NETWORKS_DEFAULT);
		advancedConfigPanel.add(numberOfTargetNetworks,
				gridConstraint(1, 4, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL));
		

		advancedConfigPanel.add(new java.awt.Label("Node filtering method"),
				gridConstraint(0, 5, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL));
		netFilteringMethod = new JComboBox<NetFilteringMethod>(NetFilteringMethod.values());
		advancedConfigPanel.add(netFilteringMethod,
				gridConstraint(1, 5, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL));


		javax.swing.JScrollPane advancedConfilScroll = new javax.swing.JScrollPane(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		advancedConfilScroll.setViewportView(advancedConfigPanel);

		configPanel.add(advancedConfilScroll,
				gridConstraint(0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH));

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		add(configPanel, gridBagConstraints);

		// Adjust the table size
		// Dimension tableSize =
		// normalAttributeSelectorTable.getPreferredSize();
		// normalAttributeSelectorPanel.setPreferredSize(new
		// Dimension(normalAttributeSelectorPanel.getWidth(), (tableSize.height
		// + 50)));
		// cancerAttributeSelectorPanel.setPreferredSize(new
		// Dimension(cancerAttributeSelectorPanel.getWidth(), (tableSize.height
		// + 50)));
		//
		// ColumnResizer.adjustColumnPreferredWidths(normalAttributeSelectorTable);
		// ColumnResizer.adjustColumnPreferredWidths(cancerAttributeSelectorTable);

	}

	private void populateAttributeTable(Vector<String[]> dataVect) {
		// AttrSelectionTableModel tableModel = new
		// AttrSelectionTableModel(dataVect);
		tableModel.setData(dataVect);
		tableModel.fireTableDataChanged();
		logger.debug("populateAttributeTable");
		normalAttributeSelectorTable.repaint();
		cancerAttributeSelectorTable.repaint();
	}

	// private class NormalizationCellRenderer extends JComboBox implements
	// TableCellRenderer {
	// public Component getTableCellRendererComponent(JTable table, Object
	// value, boolean isSelected, boolean hasFocus,
	// int row, int column) {
	// DefaultComboBoxModel model = new DefaultComboBoxModel();
	// for (final ScalingMethodX method : ScalingMethodX.values())
	// model.addElement(method.getDisplayString());
	//
	// this.setModel(model);
	//
	// if (value ==null || value.toString().equalsIgnoreCase("")) {
	// this.setSelectedItem(ScalingMethodX.NONE.getDisplayString());
	// } else {
	// this.setSelectedItem(value);
	// }
	//
	// if (isSelected) {
	// this.setBackground(table.getSelectionBackground());
	// this.setForeground(table.getSelectionForeground());
	// } else {
	// this.setBackground(table.getBackground());
	// this.setForeground(table.getForeground());
	// }
	//
	// Double min = (Double) table.getModel().getValueAt(row, 1);
	// Double max = (Double) table.getModel().getValueAt(row, 2);
	//
	// setEnabled(true);
	//
	// return this;
	// }
	// }

	// private class NormalizationComboboxEditor extends AbstractCellEditor
	// implements TableCellEditor {
	//
	// // This is the component that will handle the editing of the cell value
	// private JComboBox component = new JComboBox();
	//
	// public NormalizationComboboxEditor() {
	// super();
	//
	// for (final ScalingMethodX method : ScalingMethodX.values())
	// component.addItem(method.getDisplayString());
	// }
	//
	// // This method is called when a cell value is edited by the user.
	// public Component getTableCellEditorComponent(JTable table, Object value,
	// boolean isSelected, int rowIndex,
	// int vColIndex) {
	// final AttrSelectionTableModel tableModel = (AttrSelectionTableModel)
	// table.getModel();
	// final double val1 = (Double) tableModel.getValueAt(rowIndex, 1);
	// final double val2 = (Double) tableModel.getValueAt(rowIndex, 2);
	// final boolean isPossiblePValue = Math.min(val1, val2) >= 0.0 &&
	// Math.max(val1, val2) <= 1.0;
	//
	// final String firstItem = (String) component.getItemAt(0);
	// final boolean firstItemIsNONE =
	// firstItem.equals(ScalingMethodX.NONE.getDisplayString());
	//
	// if (isPossiblePValue) {
	// if (!firstItemIsNONE)
	// component.insertItemAt(ScalingMethodX.NONE.getDisplayString(), 0);
	// } else {
	// if (firstItemIsNONE)
	// component.removeItemAt(0);
	// }
	//
	// // Configure the component with the specified value
	// component.setSelectedItem((String) value);
	//
	// // Return the configured component
	// return component;
	// }
	//
	// // This method is called when editing is completed.
	// // It must return the new value to be stored in the cell.
	// public Object getCellEditorValue() {
	// return component.getSelectedItem();// ((JTextField)component).getText();
	// }
	// }

	// private class NameColumnCellRenderer extends DefaultTableCellRenderer {
	// private javax.swing.ImageIcon icon = new
	// ImageIcon(getClass().getResource("/images/exclamationpoint.jpg"));
	// private javax.swing.ImageIcon icon1 = new
	// ImageIcon(getClass().getResource("/images/empty.jpg"));
	// private String TOOLTIP_EXCLAIMATION = "<html>GTA requires that any
	// numeric node attribute<br>"
	// + "used as a search parameter have a value between 0 and 1.<br>"
	// + "This attribute will be automatically adjusted using the<br>specified
	// scaling approach</html>";
	// private String TOOLTIP_NONE = "";
	//
	// public Component getTableCellRendererComponent(JTable table, Object
	// value, boolean isSelected, boolean hasFocus,
	// int row, int column) {
	// if (value != null)
	// this.setText(value.toString());
	//
	// double min = Double.valueOf(table.getModel().getValueAt(row,
	// 1).toString());
	// double max = Double.valueOf(table.getModel().getValueAt(row,
	// 2).toString());
	//
	// if (Math.min(min, max) < 0 || Math.max(min, max) > 1) {
	// setIcon(icon);
	// this.setToolTipText(this.TOOLTIP_EXCLAIMATION);
	// } else {
	// setIcon(icon1);
	// this.setToolTipText(this.TOOLTIP_NONE);
	// }
	// if (isSelected) {
	// this.setBackground(table.getSelectionBackground());
	// this.setForeground(table.getSelectionForeground());
	// } else {
	// this.setBackground(table.getBackground());
	// this.setForeground(table.getForeground());
	// }
	// return this;
	// }
	// }

	private class AttrSelectionTableModel extends AbstractTableModel {
		private Vector<String[]> dataVect = null;

		private final static int SIZE = 1;
		public final static int COL_NAME_ID = 0;// , COL_MOST_SIG_ID=1,
												// COL_LEAST_SIG_ID=2,
												// COL_REVERSE_SIG_ID=3,
												// COL_SCALING_ID=4;
		public final static String COL_NAME = "Name";// , COL_MOST_SIG="Most
														// sig",
														// COL_LEAST_SIG="Least
														// sig",
														// COL_REVERSE_SIG="Reverse
														// sig",
														// COL_SCALING="Scaling";
		private final String[] columnNames = { COL_NAME };// , COL_MOST_SIG,
															// COL_LEAST_SIG,
															// COL_REVERSE_SIG,
															// COL_SCALING };
		private final boolean[] editable = { false };// , false, false, true,
														// true};

		// private final static int SIZE = 5;
		// public final static int COL_NAME_ID = 0, COL_MOST_SIG_ID=1,
		// COL_LEAST_SIG_ID=2, COL_REVERSE_SIG_ID=3, COL_SCALING_ID=4;
		// public final static String COL_NAME = "Name", COL_MOST_SIG="Most
		// sig", COL_LEAST_SIG="Least sig", COL_REVERSE_SIG="Reverse sig",
		// COL_SCALING="Scaling";
		// private final String[] columnNames = { COL_NAME, COL_MOST_SIG,
		// COL_LEAST_SIG, COL_REVERSE_SIG, COL_SCALING };
		// private final boolean[] editable = {false, false, false, true, true};

		// public AttrSelectionTableModel(Vector pDataVect) {
		// dataVect = pDataVect;
		// }

		public void setData(Vector<String[]> dataVect) {
			this.dataVect = dataVect;
		}

		public String getColumnName(int columnIndex) {
			return columnNames[columnIndex];
		}

		public int getRowCount() {
			if (dataVect == null) {
				return 0;
			}
			return dataVect.size();
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public Object getValueAt(int row, int col) {
			return dataVect.get(row)[col];
			// Object[] oneRow = (Object[]) dataVect.elementAt(row);
			// return oneRow[col];
		}

		// public void setValueAt(String value, int row, int col) {
		//// Object[] oneRow = (Object[]) dataVect.elementAt(row);
		//// oneRow[col] = value;
		//// fireTableCellUpdated(row, col);
		// dataVect.get(row)[col] = value;
		// }

		@Override
		public Class<?> getColumnClass(int col) {
			// Object[] oneRow = (Object[]) dataVect.elementAt(0);
			// return oneRow[col].getClass();
			// return dataVect.get(0)[col].getClass();
			// TODO:
			return String.class;
		}

		public boolean isCellEditable(int row, int col) {
			return editable[col];
		}

		public String[] getRow(int row) {
			// return (Object[]) dataVect.elementAt(row);
			return dataVect.get(row);
		}
	}

	// private class CheckBoxCellEditorListener implements CellEditorListener {
	// private TableModel model;
	//
	// public CheckBoxCellEditorListener(TableModel model) {
	// this.model = model;
	// }
	//
	// public void editingCanceled(ChangeEvent e) {
	// }
	//
	// public void editingStopped(ChangeEvent e) {
	// int rowCount = model.getRowCount();
	// for (int i = 0; i < rowCount; i++) {
	// boolean switchSig = Boolean.valueOf(
	// model.getValueAt(i,
	// AttrSelectionTableModel.COL_REVERSE_SIG_ID).toString());
	// double min = Double.valueOf(
	// model.getValueAt(i, AttrSelectionTableModel.COL_MOST_SIG_ID).toString());
	// double max = Double.valueOf(
	// model.getValueAt(i,
	// AttrSelectionTableModel.COL_LEAST_SIG_ID).toString());
	// if (switchSig && min < max) {
	// model.setValueAt(max, i, AttrSelectionTableModel.COL_MOST_SIG_ID);
	// model.setValueAt(min, i, AttrSelectionTableModel.COL_LEAST_SIG_ID);
	// }
	// if (!switchSig && min > max) {
	// model.setValueAt(max, i, AttrSelectionTableModel.COL_MOST_SIG_ID);
	// model.setValueAt(min, i, AttrSelectionTableModel.COL_LEAST_SIG_ID);
	// }
	// }
	// }
	// }

	// public void itemStateChanged(ItemEvent e) {
	// updateAttributePanel();
	// }
	//
	private void updateAttributePanel() {
		if (!this.isPanelSelected) {
			return;
		}
		Vector<String[]> data = this.getDataVector();
		this.populateAttributeTable(data);
		// apfParams.setNetwork(networkPanel.getSelectedNetwork());
		// apfParams.reloadExpressionAttributes();
	}

	@Override
	public void handleEvent(ColumnCreatedEvent e) {
		// logger.error("ColumnCreated Event Occured " + e.toString());
		updateAttributePanel();
	}

	public void handleEvent(CytoPanelComponentSelectedEvent e) {
		logger.debug("Event Occured " + e.toString() + " " + (e.getCytoPanel().getSelectedComponent() == this));
		if (e.getCytoPanel().getSelectedComponent() == this) {
			this.isPanelSelected = true;
			updateAttributePanel();
		} else {
			this.isPanelSelected = false;
		}
	}

	private Vector<String[]> getDataVector() {
		Vector<String[]> dataVect = new Vector<String[]>();

		if (networkSelectorPanel.getSelectedNetwork() == null) {
			return dataVect;
		}

		CyTable table = networkSelectorPanel.getSelectedNetwork().getDefaultNodeTable();
		Collection<CyColumn> columns = table.getColumns();

		for (Iterator<CyColumn> i = columns.iterator(); i.hasNext();) {
			CyColumn c = i.next();
			if (c.getType() == Double.class) {
				String[] val = new String[AttrSelectionTableModel.SIZE];
				val[AttrSelectionTableModel.COL_NAME_ID] = c.getName();
				dataVect.add(val);
			}
		}

		return dataVect;
	}

	// private Vector<Object[]> getDataVect() {
	// Vector<Object[]> dataVect = new Vector<Object[]>();
	//
	// if (networkSelectorPanel.getSelectedNetwork() == null) {
	// return dataVect;
	// }
	//
	// CyTable table =
	// networkSelectorPanel.getSelectedNetwork().getDefaultNodeTable();
	//
	// Collection<CyColumn> columns = table.getColumns();
	//
	//// Object[] objs = CyTableUtil.getColumnNames(table).toArray();
	////
	// String[] names = new String[columns.size()];
	//
	// int ii =0;
	// for (Iterator<CyColumn> i = columns.iterator(); i.hasNext(); ii++) {
	// CyColumn c = i.next();
	// names[ii] = c.getName();
	// }
	////
	//// for (int i = 0; i < columns.size(); i++) {
	//// names[i] = columns..toString();
	//// }
	//
	// for (String name : names) {
	// CyColumn col = table.getColumn(name);
	// if (col.getType() == Double.class) {
	// List<Double> vals = (List<Double>) col.getValues(Double.class);
	//
	// if (vals == null)
	// continue; // no values have been defined for the attr yet
	//
	// Object[] row = new Object[AttrSelectionTableModel.SIZE];
	// row[AttrSelectionTableModel.COL_NAME_ID] = name;
	//
	// boolean isPValue = true;
	//
	// for (Iterator<Double> i = vals.iterator(); i.hasNext();) {
	// Double value = i.next();
	// if (value == null) {
	// i.remove();
	// } else if (value < 0 || value > 1) {
	// isPValue = false;
	// }
	// }
	//
	// row[AttrSelectionTableModel.COL_MOST_SIG_ID] = Collections.min(vals);
	// row[AttrSelectionTableModel.COL_LEAST_SIG_ID] = Collections.max(vals);
	// row[AttrSelectionTableModel.COL_REVERSE_SIG_ID] = false;
	//
	// if (!isPValue) {
	//
	// row[AttrSelectionTableModel.COL_SCALING_ID] = isPValue ?
	// ScalingMethodX.NONE.getDisplayString()
	// : ScalingMethodX.RANK_UPPER.getDisplayString();
	// } else
	// row[AttrSelectionTableModel.COL_SCALING_ID] =
	// ScalingMethodX.NONE.getDisplayString();
	//
	// dataVect.add(row);
	// }
	// }
	//
	// // do sorting, put the rows with value range 0-1 ahead to the rest
	//// Collections.sort(dataVect, new CompareTableRow());
	//
	// return dataVect;
	// }

	// // do sorting, put the rows with value range 0-1 ahead to the rest
	// class CompareTableRow implements Comparator<Object[]> {
	// public int compare(Object[] row1, Object[] row2) {
	// if (!(Math.min((Double) row1[AttrSelectionTableModel.COL_MOST_SIG_ID],
	// (Double) row1[AttrSelectionTableModel.COL_LEAST_SIG_ID]) >= 0
	// && Math.max((Double) row1[AttrSelectionTableModel.COL_MOST_SIG_ID],
	// (Double) row1[AttrSelectionTableModel.COL_LEAST_SIG_ID]) <= 1)) {
	// return 1;
	// }
	// return 0;
	// }
	//
	// public boolean equals(Object[] o) {
	// return false;
	// }
	// }

	public class FindModulesAction extends AbstractAction {

		public FindModulesAction() {
			super("Find Modules");
		}

		public void actionPerformed(ActionEvent e) {
			int[] selectedNormalIndices = normalAttributeSelectorTable.getSelectedRows(),
					selectedCancerIndices = cancerAttributeSelectorTable.getSelectedRows();

			final AttrSelectionTableModel normalModel = (AttrSelectionTableModel) normalAttributeSelectorTable
					.getModel(), cancerModel = (AttrSelectionTableModel) cancerAttributeSelectorTable.getModel();

			GTAAlgParametersAttrSelection normalSelParam = new GTAAlgParametersAttrSelection(),
					cancerSelParam = new GTAAlgParametersAttrSelection();

			int[][] selectedIndices = new int[2][];
			selectedIndices[0] = selectedNormalIndices;
			selectedIndices[1] = selectedCancerIndices;
			AttrSelectionTableModel[] models = new AttrSelectionTableModel[2];
			models[0] = normalModel;
			models[1] = cancerModel;
			GTAAlgParametersAttrSelection[] selParams = new GTAAlgParametersAttrSelection[2];
			selParams[0] = normalSelParam;
			selParams[1] = cancerSelParam;

			for (int j = 0; j < selParams.length; j++) {
				for (int i = 0; i < selectedIndices[j].length; i++) {
					selParams[j].getSelectedNames()
							.add((String) models[j].getRow(selectedIndices[j][i])[AttrSelectionTableModel.COL_NAME_ID]);
					// selParams[j].getSwitchSigs().add((Boolean)
					// models[j].getRow(selectedIndices[j][i])[AttrSelectionTableModel.COL_REVERSE_SIG_ID]);
					// selParams[j].getScalingMethods().add((String)
					// models[j].getRow(selectedIndices[j][i])[AttrSelectionTableModel.COL_SCALING_ID]);
				}
			}

			runAlgorithm(normalSelParam, cancerSelParam);

			//
			//
			// ArrayList<String> selectedNames = new ArrayList<String>();
			// ArrayList<Boolean> switchSigs = new ArrayList<Boolean>();
			// ArrayList<String> scalingMethods = new ArrayList<String>();
			//
			// for (int i = 0; i < selectedIndices.length; i++){
			// selectedNames.add((String)model.getRow(selectedIndices[i])[0]);
			// switchSigs.add((Boolean)model.getRow(selectedIndices[i])[3]);
			// scalingMethods.add((String)model.getRow(selectedIndices[i])[4]);
			// }
			//
			// apfParams.setNetwork(networkPanel.getSelectedNetwork());
			// apfParams.setExpressionAttributes(selectedNames);
			// apfParams.setSwitchSigs(switchSigs);
			// apfParams.setScalingMethods(scalingMethods);
			// pluginMainClass.startFindActivePaths(networkPanel.getSelectedNetwork());

		}

		private void runAlgorithm(GTAAlgParametersAttrSelection normalSelParam,
				GTAAlgParametersAttrSelection cancerSelParam) {
			GTAAlgParameters params = new GTAAlgParameters(networkSelectorPanel.getSelectedNetwork(), normalSelParam,
					cancerSelParam, generateTScoresCheckBox.getState(), generateDegreesCheckBox.getState(),
					Integer.parseInt(levelOneSubnetMaxSize.getSelectedItem()),
					Integer.parseInt(levelTwoSubnetMaxSize.getSelectedItem()),
					Integer.parseInt(numberOfTargetNetworks.getSelectedItem()), (NetFilteringMethod) netFilteringMethod.getSelectedItem());
			GTAAlgRunner alg = new GTAAlgRunner(params);
			alg.run();
		}

	}

} // class ActivePathsParametersPopupDialog
