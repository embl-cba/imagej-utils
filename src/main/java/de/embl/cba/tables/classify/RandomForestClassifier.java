package de.embl.cba.tables.classify;

import de.embl.cba.classifiers.weka.FastRandomForest;

public class RandomForestClassifier
{

	public RandomForestClassifier()
	{

		final FastRandomForest rf = new FastRandomForest();

//		// Initialization of Fast Random Forest classifier
//		rf.setNumTrees( classifierNumTrees );
//		rf.setNumFeatures( classifierNumRandomFeatures );
//		rf.setSeed((new Random()).nextInt());
//		rf.setNumThreads( threadsClassifierTraining );
//		rf.setMaxDepth( classifierMaxDepth );
//		//rf.setBatchSize("50");
//		rf.setComputeImportances(true);
//
//		// Training of the classifier
//		rf.buildClassifier( instancesAndMetadata.getInstances() );
//
//		// Classification of one instance
//		ins.setValues( 1.0, featureValues );
//
//		result = classifier.distributionForInstance( ins, accuracy );
//		classifier.setAttIndicesWindow( attIndicesWindow  );
	}
}
