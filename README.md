GTA Module
==============

GTA Module is suitable for finding network modules related to a complex disease such as cancer, based on gene expression profile of cancerous and non-cancerous samples. 

Tutorial

1) Load network
In Cytoscape go to menu File -> Import -> Network -> File. Choose "swiss-cancer-network.sif" file. The network then is created. You may not crate view for this network, it may take time.

2) Load Gene Expression data
In Cytoscape go to menu File -> Import -> Table -> File. Then choose "swiss-cancer-expression.expr" file. Then, the to each network node a set of expression value is assigned.

3) Construct modules

3-1) Go to GTA Panel.

3-2) Choose your main network.

3-3) Choose normal and cancerous columns. In our sample expression data, NORM1 to NORM35 are normal expressions. Choose all of them in the "Normal Cases" List. CAN1 to CAN124 are cancerous samples expression data, choose all of then in the "Cancer Cases" List. 

3-4) Hit the "Find Modules" button and wait.

If everything goes well, 10 subnetworks sould be created as subnetworks to your selected network. You can browse them.
